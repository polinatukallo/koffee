package ru.kotocoffee.shared.error

abstract class BusinessException(
    message: String,
    val code: String,
    val details: Map<String, Any> = emptyMap(),
) : RuntimeException(message)

open class NotFoundException(message: String, code: String = "not-found") : BusinessException(message, code)

open class ConflictException(
    message: String,
    code: String = "conflict",
    details: Map<String, Any> = emptyMap(),
) : BusinessException(message, code, details)

open class BusinessRuleViolationException(
    message: String,
    code: String = "business-rule-violation",
    details: Map<String, Any> = emptyMap(),
) : BusinessException(message, code, details)

class InvalidRequestException(
    val field: String,
    val reason: String,
) : BusinessException("Поле $field: $reason", "validation-failed")

class ConcurrentUpdateException(message: String) : ConflictException(message, "concurrent-modification")
