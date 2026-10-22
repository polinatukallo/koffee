package ru.kotocoffee.shared.web

import io.swagger.v3.oas.annotations.media.Schema
import ru.kotocoffee.shared.kernel.PageResult

@Schema(description = "Страница результатов")
data class PageResponse<T>(
    @field:Schema(description = "Элементы страницы")
    val items: List<T>,
    @field:Schema(description = "Номер страницы (с нуля)", example = "0")
    val page: Int,
    @field:Schema(description = "Размер страницы", example = "20")
    val size: Int,
    @field:Schema(description = "Общее количество элементов", example = "42")
    val totalElements: Long,
) {
    companion object {
        fun <S, T> from(result: PageResult<S>, transform: (S) -> T): PageResponse<T> =
            PageResponse(result.items.map(transform), result.page, result.size, result.totalElements)
    }
}
