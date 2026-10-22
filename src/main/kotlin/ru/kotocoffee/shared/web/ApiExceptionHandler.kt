package ru.kotocoffee.shared.web

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException
import ru.kotocoffee.shared.error.BusinessException
import ru.kotocoffee.shared.error.BusinessRuleViolationException
import ru.kotocoffee.shared.error.ConflictException
import ru.kotocoffee.shared.error.InvalidRequestException
import ru.kotocoffee.shared.error.NotFoundException
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(exception: InvalidRequestException): ResponseEntity<ProblemDetail> =
        validationProblem(listOf(FieldViolation(exception.field, exception.reason)))

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ProblemDetail> {
        val status = statusFor(exception)
        val problem = ProblemDetails.create(status, exception.code, exception.message ?: status.reasonPhrase)
        for ((key, value) in exception.details) {
            problem.setProperty(key, value)
        }
        log.debug("Business error {}: {}", exception.code, exception.message)
        return ResponseEntity.status(status).body(problem)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(exception: ConstraintViolationException): ResponseEntity<ProblemDetail> {
        val errors = mutableListOf<FieldViolation>()
        for (violation in exception.constraintViolations) {
            errors.add(FieldViolation(lastPathSegment(violation.propertyPath.toString()), violation.message))
        }
        return validationProblem(errors)
    }

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(exception: OptimisticLockingFailureException): ResponseEntity<ProblemDetail> {
        log.info("Concurrent modification detected: {}", exception.javaClass.simpleName)
        val problem = ProblemDetails.create(
            HttpStatus.CONFLICT,
            "concurrent-modification",
            "Объект был изменён другим запросом. Обновите данные и повторите попытку.",
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(exception: DataIntegrityViolationException): ResponseEntity<ProblemDetail> {
        log.warn("Data integrity violation: {}", exception.javaClass.simpleName)
        val problem = ProblemDetails.create(
            HttpStatus.CONFLICT,
            "data-integrity-violation",
            "Операция нарушает целостность данных.",
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception): ResponseEntity<ProblemDetail> {
        log.error("Unexpected error", exception)
        val problem = ProblemDetails.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "internal-error",
            "Внутренняя ошибка сервера.",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem)
    }

    override fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val errors = mutableListOf<FieldViolation>()
        for (fieldError in exception.bindingResult.fieldErrors) {
            errors.add(FieldViolation(fieldError.field, fieldError.defaultMessage ?: "некорректное значение"))
        }
        for (globalError in exception.bindingResult.globalErrors) {
            errors.add(FieldViolation(globalError.objectName, globalError.defaultMessage ?: "некорректное значение"))
        }
        return ResponseEntity.badRequest().body(validationProblemBody(errors))
    }

    override fun handleHandlerMethodValidationException(
        exception: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val errors = mutableListOf<FieldViolation>()
        for (result in exception.parameterValidationResults) {
            val parameterName = result.methodParameter.parameterName ?: "parameter"
            for (error in result.resolvableErrors) {
                errors.add(FieldViolation(parameterName, error.defaultMessage ?: "некорректное значение"))
            }
        }
        return ResponseEntity.badRequest().body(validationProblemBody(errors))
    }

    override fun handleExceptionInternal(
        exception: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val response = super.handleExceptionInternal(exception, body, headers, statusCode, request)
        val problem = response?.body
        if (problem is ProblemDetail) {
            val properties = problem.properties
            if (properties == null || !properties.containsKey("code")) {
                val code = codeForSpringException(exception)
                problem.type = URI.create(ProblemDetails.TYPE_PREFIX + code)
                problem.setProperty("code", code)
            }
        }
        return response
    }

    private fun codeForSpringException(exception: Exception): String =
        when (exception) {
            is HttpMessageNotReadableException -> "malformed-request"
            is TypeMismatchException -> "type-mismatch"
            is MissingServletRequestParameterException -> "missing-parameter"
            is HttpRequestMethodNotSupportedException -> "method-not-allowed"
            is HttpMediaTypeNotSupportedException -> "unsupported-media-type"
            is HttpMediaTypeNotAcceptableException -> "not-acceptable"
            is NoResourceFoundException -> "not-found"
            else -> "bad-request"
        }

    private fun validationProblem(errors: List<FieldViolation>): ResponseEntity<ProblemDetail> =
        ResponseEntity.badRequest().body(validationProblemBody(errors))

    private fun validationProblemBody(errors: List<FieldViolation>): ProblemDetail {
        val problem = ProblemDetails.create(HttpStatus.BAD_REQUEST, "validation-failed", "Запрос не прошёл валидацию.")
        problem.setProperty("errors", errors)
        return problem
    }

    private fun lastPathSegment(path: String): String {
        val dotIndex = path.lastIndexOf('.')
        if (dotIndex < 0) {
            return path
        }
        return path.substring(dotIndex + 1)
    }

    private fun statusFor(exception: BusinessException): HttpStatus =
        when (exception) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is ConflictException -> HttpStatus.CONFLICT
            is BusinessRuleViolationException -> HttpStatus.UNPROCESSABLE_ENTITY
            is InvalidRequestException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
}

data class FieldViolation(val field: String, val message: String)
