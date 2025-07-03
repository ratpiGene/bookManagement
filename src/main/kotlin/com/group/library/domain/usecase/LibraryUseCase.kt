package com.group.library.domain.usecase

import com.group.library.domain.model.Book
import com.group.library.domain.port.BookRepositoryPort

class LibraryUseCase(val bookRepository: BookRepositoryPort) {
    fun getBooks() : List<Book> = bookRepository.getBooks().sortedBy { it.title }
    fun addBook(book: Book) {
        bookRepository.addBook(book)
    }
}