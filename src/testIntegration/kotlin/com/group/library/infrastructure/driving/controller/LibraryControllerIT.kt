package com.group.library.infrastructure.driving.controller

import com.group.library.domain.model.Book
import com.group.library.domain.usecase.LibraryUseCase
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest
class LibraryControllerIT(
    @MockkBean private val libraryUseCase: LibraryUseCase,
    private val mockMvc: MockMvc
) : StringSpec({
    extension(SpringExtension)

    "rest route for get books" {
        // GIVEN
        every { libraryUseCase.getBooks() } returns listOf(Book("A", "B"))

        // WHEN
        mockMvc.get("/books")
            //THEN
            .andExpect {
                status { isOk() }
                content { content { APPLICATION_JSON } }
                content {
                    json(
                        // language=json
                        """
                        [
                          {
                            "title": "A",
                            "author": "B"
                          }
                        ]
                        """.trimIndent()
                    )
                }
            }
    }

    "rest route for post book" {
        justRun { libraryUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "title": "Le Hobbit",
                  "author": "Tolkien"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val expected = Book(
            title = "Le Hobbit",
            author = "Tolkien"
        )

        verify(exactly = 1) { libraryUseCase.addBook(expected) }
    }

    "rest route post book error should return 400" {
        justRun { libraryUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "name": "Le Hobbit",
                  "author": "Tolkien"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { libraryUseCase.addBook(any()) }
    }

    "rest route to reserve a book successfully" {
        // Arrange
        val title = "Le Petit Prince"
        val author = "Saint-Exupéry"
        justRun { libraryUseCase.reserveBook(title, author) }

        // Act & Assert
        mockMvc.post("/books/reserve") {
            content = """
            {
              "title": "$title",
              "author": "$author"
            }
        """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }  // ou isNoContent() selon ton choix
        }

        verify(exactly = 1) { libraryUseCase.reserveBook(title, author) }
    }

    "rest route to reserve a book that is already reserved should return 409" {
        // Arrange
        val title = "1984"
        val author = "George Orwell"
        every { libraryUseCase.reserveBook(title, author) } throws IllegalStateException("Book already reserved")

        // Act & Assert
        mockMvc.post("/books/reserve") {
            content = """
            {
              "title": "$title",
              "author": "$author"
            }
        """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isConflict() }
            content { string("Book already reserved") }
        }
    }

    "rest route to reserve a book that does not exist should return 404" {
        // Arrange
        val title = "Unknown Book"
        val author = "Unknown Author"
        every { libraryUseCase.reserveBook(title, author) } throws IllegalArgumentException("Book not found")

        // Act & Assert
        mockMvc.post("/books/reserve") {
            content = """
            {
              "title": "$title",
              "author": "$author"
            }
        """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { string("Book not found") }
        }
    }
})