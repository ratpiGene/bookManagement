package com.group.library.domain.model

data class Book(
    val title: String,
    val author: String,
    val reserved: Boolean = false
) {
    init {
        require(title.isNotBlank()) { "Title must not be empty" }
        require(author.isNotBlank()) { "Author must not be empty" }
    }
}