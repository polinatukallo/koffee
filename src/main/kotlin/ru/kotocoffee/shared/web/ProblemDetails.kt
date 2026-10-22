package ru.kotocoffee.shared.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI

object ProblemDetails {

    const val TYPE_PREFIX = "https://kotocoffee.ru/problems/"

    fun create(status: HttpStatus, code: String, detail: String): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.type = URI.create(TYPE_PREFIX + code)
        problem.title = status.reasonPhrase
        problem.setProperty("code", code)
        return problem
    }
}
