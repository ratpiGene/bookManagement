package com.group.library.domain.usecase

import com.group.library.domain.model.Book
import com.group.library.domain.port.BookRepositoryPort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class LibraryUseCaseTest : StringSpec({

    "getBooks returns sorted list of books" {
        // Arrange
        val bookRepository = mockk<BookRepositoryPort>()
        val libraryUseCase = LibraryUseCase(bookRepository)
        val books = listOf(
            Book("Les robots", "Isaac Asimov"),
            Book("Hypérion", "Dan Simons"),
            Book("Axiomatique", "Greg Egan")
        )
        every { bookRepository.getBooks() } returns books

        // Act
        val result = libraryUseCase.getBooks()

        // Assert
        result shouldContainExactly listOf(
            Book("Axiomatique", "Greg Egan"),
            Book("Hypérion", "Dan Simons"),
            Book("Les robots", "Isaac Asimov")
        )
    }

    "addBook adds a book to the repository" {
        // Arrange
        val bookRepository = mockk<BookRepositoryPort>()
        val libraryUseCase = LibraryUseCase(bookRepository)
        val book = Book("Les robots", "Isaac Asimov")
        justRun { bookRepository.addBook(any()) }

        // Act
        libraryUseCase.addBook(book)

        // Assert
        verify(exactly = 1) { bookRepository.addBook(book) }
    }

    "reserveBook reserves a book if it's available" {
        // Arrange
        val bookRepository = mockk<BookRepositoryPort>()
        val libraryUseCase = LibraryUseCase(bookRepository)
        val title = "Le Petit Prince"
        val author = "Saint-Exupéry"
        val books = listOf(Book(title, author, reserved = false))

        every { bookRepository.getBooks() } returns books
        justRun { bookRepository.updateReservation(title, author, true) }

        // Act
        libraryUseCase.reserveBook(title, author)

        // Assert
        verify(exactly = 1) {
            bookRepository.updateReservation(title, author, true)
        }
    }

    "reserveBook throws if the book is already reserved" {
        // Arrange
        val bookRepository = mockk<BookRepositoryPort>()
        val libraryUseCase = LibraryUseCase(bookRepository)
        val title = "1984"
        val author = "George Orwell"
        val books = listOf(Book(title, author, reserved = true))

        justRun { bookRepository.updateReservation(any(), any(), any()) }
        every { bookRepository.getBooks() } returns books

        // Act
        val exception = shouldThrow<IllegalStateException> {
            libraryUseCase.reserveBook(title, author)
        }

        // Assert
        exception.message shouldBe "Book already reserved"

        verify(exactly = 0) {
            bookRepository.updateReservation(any(), any(), any())
        }
    }

    "reserveBook throws if the book is not found" {
        // Arrange
        val bookRepository = mockk<BookRepositoryPort>()
        val libraryUseCase = LibraryUseCase(bookRepository)
        val title = "Inconnu"
        val author = "Inconnu"
        val books = listOf(
            Book("Autre Titre", "Autre Auteur", reserved = false)
        )

        justRun { bookRepository.updateReservation(any(), any(), any()) }
        every { bookRepository.getBooks() } returns books

        // Act
        val exception = shouldThrow<IllegalArgumentException> {
            libraryUseCase.reserveBook(title, author)
        }

        // Assert
        exception.message shouldBe "Book not found"

        verify(exactly = 0) {
            bookRepository.updateReservation(any(), any(), any())
        }
    }



})
