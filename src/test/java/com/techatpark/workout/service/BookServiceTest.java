package com.techatpark.workout.service;

import com.techatpark.workout.model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
public class BookServiceTest {

    public static final String STATE_BOARD_IN_ENGLISH = "State Book";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Book Description";
    public static final String STATE_BOARD_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_BOARD_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";
    @Autowired
    private BookService bookService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        bookService.deleteAll();
    }

    @Test
    void create() {
        final Book book = bookService.create("mani", null,
                anBook());
        Assertions.assertTrue(bookService.read("mani", null, book.id()).isPresent(),
                "Created Book");
    }

    @Test
    void read() {
        final Book book = bookService.create("mani", null,
                anBook());
        final UUID newBookId = book.id();
        Assertions.assertTrue(bookService.read("mani", null, newBookId).isPresent(),
                "Book Created");
    }

    @Test
    void update() {

        final Book book = bookService.create("mani", null,
                anBook());
        final UUID newBookId = book.id();
        Book newBook = new Book(null, "Book", LocalDateTime.now().toString(), "A " +
                "Book", null, "tom", null, null);
        Book updatedBook = bookService
                .update(newBookId, "mani", null, newBook);
        Assertions.assertEquals("Book", updatedBook.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            bookService
                    .update(UUID.randomUUID(), "mani", null, newBook);
        });
    }

    @Test
    void delete() {

        final Book book = bookService.create("mani", null,
                anBook());
        bookService.delete("mani", book.id());
        Assertions.assertFalse(bookService.read("mani", null, book.id()).isPresent(),
                "Deleted Book");

    }

    @Test
    void list() {

        final Book book = bookService.create("mani", null,
                anBook());
        Book newBook = new Book(null, "Book New", LocalDateTime.now().toString(), "A " +
                "Book", null, "tom", null, null);
        bookService.create("mani", null,
                newBook);
        List<Book> listofbook = bookService.list("manikanta", null);
        Assertions.assertEquals(2, listofbook.size());

    }

    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Book without locale
        final Book book = bookService.create("mani", null,
                anBook());

        testLocalization(book);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Book with locale
        final Book book = bookService.create("mani", Locale.GERMAN,
                anBook());

        testLocalization(book);

    }

    void testLocalization(Book book) {

        // Update for China Language
        bookService.update(book.id(), "mani", Locale.FRENCH, anBook(book,
                STATE_BOARD_TITLE_IN_FRENCH, LocalDateTime.now().toString(),
                STATE_BOARD_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Book createBook = bookService.read("mani", Locale.FRENCH,
                book.id()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBook.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createBook.description());

        final UUID id = createBook.id();
        createBook = bookService.list("mani", Locale.FRENCH)
                .stream()
                .filter(book1 -> book1.id().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBook.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createBook.description());

        // Get for France which does not have data
        createBook = bookService.read("mani", Locale.CHINESE,
                book.id()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBook.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBook.description());

        createBook = bookService.list("mani", Locale.CHINESE)
                .stream()
                .filter(book1 -> book1.id().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBook.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBook.description());

    }

    /**
     * Gets book.
     *
     * @return the book
     */
    Book anBook() {
        Book book = new Book(null, STATE_BOARD_IN_ENGLISH, LocalDateTime.now().toString(),
                STATE_BOARD_DESCRIPTION_IN_ENGLISH, null, null,
                null, null);
        return book;
    }

    /**
     * Gets book from reference book.
     *
     * @return the book
     */
    Book anBook(final Book ref, final String title, final String path, final String description) {
        return new Book(ref.id(), title, path,
                description, ref.created_at(), ref.created_by(),
                ref.modified_at(), ref.modified_by());
    }
}
