package com.group.library.infrastructure.driving.controller

import com.group.library.domain.usecase.LibraryUseCase
import com.group.library.infrastructure.driving.controller.dto.BookDTO
import com.group.library.infrastructure.driving.controller.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping("/books")
class LibraryController(
    private val libraryUseCase: LibraryUseCase
) {
    @CrossOrigin
    @GetMapping
    fun getAllBooks(): List<BookDTO> {
        return libraryUseCase.getBooks()
            .map { it.toDto() }
    }

    @CrossOrigin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBook(@RequestBody bookDTO: BookDTO) {
        libraryUseCase.addBook(bookDTO.toDomain())
    }

    @CrossOrigin
    @PostMapping("/reserve")
    fun reserveBook(@RequestBody bookDTO: BookDTO): String {
        return try {
            libraryUseCase.reserveBook(bookDTO.title, bookDTO.author)
            "Book reserved successfully."
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
        }
    }
}