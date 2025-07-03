package com.group.library.infrastructure.driven.postgres

import com.group.library.domain.model.Book
import com.group.library.domain.port.BookRepositoryPort
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Repository
class BookDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate): BookRepositoryPort {
    override fun getBooks(): List<Book> {
        return namedParameterJdbcTemplate
            .query("SELECT * FROM BOOK", MapSqlParameterSource()) { rs, _ ->
                Book(
                    title = rs.getString("title"),
                    author = rs.getString("author"),
                    reserved = rs.getBoolean("reserved")
                )
            }
    }

    override fun addBook(book: Book) {
        namedParameterJdbcTemplate
            .update("INSERT INTO BOOK (title, author, reserved) values (:title, :author, :reserved)", mapOf(
                "title" to book.title,
                "author" to book.author,
                "reserved" to book.reserved
            ))
    }

    override fun updateReservation(title: String, author: String, reserved: Boolean) {
        val params = mapOf(
            "title" to title,
            "author" to author,
            "reserved" to reserved
        )

        namedParameterJdbcTemplate.update(
            """
        UPDATE BOOK 
        SET reserved = :reserved 
        WHERE title = :title AND author = :author
        """.trimIndent(),
            params
        )
    }
}