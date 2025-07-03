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
})