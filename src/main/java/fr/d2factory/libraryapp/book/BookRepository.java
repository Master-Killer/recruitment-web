package fr.d2factory.libraryapp.book;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The book repository emulates a database via 2 HashMaps
 */
public class BookRepository {

    private final Map<ISBN, Book> availableBooks = new HashMap<>();
    private final Map<Book, LocalDate> borrowedBooks = new HashMap<>();

    public void addBooks(final List<Book> books) {
        //TODO implement the missing feature
    }

    public Optional<Book> findBook(final long isbnCode) {
        //TODO implement the missing feature
        return null;
    }

    public void saveBookBorrow(final Book book, final LocalDate borrowedAt) {
        //TODO implement the missing feature
    }

    public LocalDate findBorrowedBookDate(final Book book) {
        //TODO implement the missing feature
        return null;
    }
}
