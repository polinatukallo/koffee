package ru.kotocoffee.shared.kernel

data class PageQuery(val page: Int, val size: Int) {
    init {
        require(page >= 0) { "Номер страницы не может быть отрицательным" }
        require(size in 1..MAX_SIZE) { "Размер страницы должен быть от 1 до $MAX_SIZE" }
    }

    companion object {
        const val MAX_SIZE = 100
    }
}

data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
)
