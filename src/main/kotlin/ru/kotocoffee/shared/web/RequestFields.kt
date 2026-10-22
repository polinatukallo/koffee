package ru.kotocoffee.shared.web

import ru.kotocoffee.shared.error.InvalidRequestException

object RequestFields {

    fun <T : Any> required(value: T?, field: String): T =
        value ?: throw InvalidRequestException(field, "обязательное поле")

    fun <T : Any> requiredElements(values: List<T?>?, field: String): List<T> {
        val source = values ?: throw InvalidRequestException(field, "обязательное поле")
        val result = mutableListOf<T>()
        for ((index, value) in source.withIndex()) {
            val element = value ?: throw InvalidRequestException("$field[$index]", "элемент не может быть null")
            result.add(element)
        }
        return result
    }
}
