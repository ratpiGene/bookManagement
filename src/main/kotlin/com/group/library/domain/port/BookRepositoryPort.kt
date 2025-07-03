package com.group.library.domain.port

import com.group.library.domain.model.Book

interface BookRepositoryPort {
    fun addBook(book: Book)
    fun getBooks(): List<Book>
    fun updateReservation(title: String, author: String, reserved: Boolean)
}