package com.group.library.infrastructure.driven.postgres

import com.group.library.domain.model.Book
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.ResultSet

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookDAOIT(
    private val bookDAO: BookDAO
) : StringSpec() {
    init {
        extension(SpringExtension)

        beforeTest {
            performQuery(
                // language=sql
                "DELETE FROM book"
            )
        }

        "get all books from db" {
            // GIVEN
            performQuery(
                // language=sql
                """
               insert into book (title, author)
               values 
                   ('Khapta Max', 'NAPS'),
                   ('Le Hobbit', 'Tolkien'),
                   ('Minou Minou', 'Le Chat');
            """.trimIndent()
            )

            // WHEN
            val res = bookDAO.getBooks()

            // THEN
            res.shouldContainExactlyInAnyOrder(
                Book("Khapta Max", "NAPS"), Book("Le Hobbit", "Tolkien"), Book("Minou Minou", "Le Chat")
            )
        }

        "create book in db" {
            // GIVEN
            val book = Book("Viv le Zanimo", "Asterion")

            // WHEN
            bookDAO.addBook(book)

            // THEN
            val res = performQuery(
                // language=sql
                "SELECT * from book"
            )

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["id"].shouldNotBeNull().shouldBeInstanceOf<Int>()
                this["title"].shouldBe("Viv le Zanimo")
                this["author"].shouldBe("Asterion")
            }
        }

        "updateReservation updates reserved status in db" {
            // GIVEN
            performQuery("""
        INSERT INTO book (title, author, reserved) VALUES ('Test Book', 'Test Author', false)
    """)

            // WHEN
            bookDAO.updateReservation("Test Book", "Test Author", true)

            // THEN
            val res = performQuery("""
        SELECT reserved FROM book WHERE title = 'Test Book' AND author = 'Test Author'
    """)

            res.size shouldBe 1
            res[0]["reserved"] shouldBe true
        }


        afterSpec {
            container.stop()
        }
    }

    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:13-alpine")

        init {
            container.start()
            System.setProperty("spring.datasource.url", container.jdbcUrl)
            System.setProperty("spring.datasource.username", container.username)
            System.setProperty("spring.datasource.password", container.password)
        }

        private fun ResultSet.toList(): List<Map<String, Any>> {
            val md = this.metaData
            val columns = md.columnCount
            val rows: MutableList<Map<String, Any>> = ArrayList()
            while (this.next()) {
                val row: MutableMap<String, Any> = HashMap(columns)
                for (i in 1..columns) {
                    row[md.getColumnName(i)] = this.getObject(i)
                }
                rows.add(row)
            }
            return rows
        }

        fun performQuery(sql: String): List<Map<String, Any>> {
            val hikariConfig = HikariConfig()
            hikariConfig.setJdbcUrl(container.jdbcUrl)
            hikariConfig.username = container.username
            hikariConfig.password = container.password
            hikariConfig.setDriverClassName(container.driverClassName)

            val ds = HikariDataSource(hikariConfig)

            val statement = ds.connection.createStatement()
            statement.execute(sql)
            val resultSet = statement.resultSet
            return resultSet?.toList() ?: listOf()
        }
    }
}