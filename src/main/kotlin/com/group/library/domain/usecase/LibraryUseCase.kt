package com.group.library.domain.usecase

import com.group.library.domain.model.Book
import com.group.library.domain.port.BookRepositoryPort

class LibraryUseCase(val bookRepository: BookRepositoryPort) {
    fun getBooks() : List<Book> = bookRepository.getBooks().sortedBy { it.title }
    fun addBook(book: Book) {
        bookRepository.addBook(book)
    }
    fun reserveBook(title: String, author: String) {
        val bookList = bookRepository.getBooks()

        val bookToReserve = bookList.find { it.title == title && it.author == author }
            ?: throw IllegalArgumentException("Book not found")

        println("🎯 Trouvé : $bookToReserve")

        if (bookToReserve.reserved) {
            println("❌ Réservation déjà faite")
            throw IllegalStateException("Book already reserved")
        }

        println("✅ Réservation en cours pour : $title, $author")
        bookRepository.updateReservation(title, author, true)
    }

}