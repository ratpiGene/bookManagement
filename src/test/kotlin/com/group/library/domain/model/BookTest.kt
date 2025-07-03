package com.group.library.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec

class BookTest : StringSpec({
    "name is mandatory" {
        shouldThrow<IllegalArgumentException> { Book("", "Tolkien") }

    }

    "author is mandatory" {
        shouldThrow<IllegalArgumentException> { Book("Minou Minou", "") }
    }

    "valid title and author does not throw" {
        shouldNotThrowAny { Book("PatPatrouille", "Ouaf Ouaf") }
    }
})