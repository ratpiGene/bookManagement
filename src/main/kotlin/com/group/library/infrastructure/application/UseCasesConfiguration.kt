package com.group.library.infrastructure.application

import com.group.library.domain.port.BookRepositoryPort
import com.group.library.domain.usecase.LibraryUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCasesConfiguration {
    @Bean
    fun libraryUseCase(bookDAO: BookRepositoryPort): LibraryUseCase {
        return LibraryUseCase(bookDAO)
    }
}