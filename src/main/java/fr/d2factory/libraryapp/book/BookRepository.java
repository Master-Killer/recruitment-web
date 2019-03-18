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

    /**
     * Add books as available books
     */
    public void addBooks(final List<Book> books) {
        books.forEach(book -> availableBooks.put(book.getIsbn(), book));
    }

    /**
     * Find a specific book if available
     *
     * @param isbnCode the code of the book
     * @return the book if available
     */
    public Optional<Book> findBook(final ISBN isbnCode) {
        return Optional.ofNullable(availableBooks.get(isbnCode));
    }

    /**
     * Borrow a book at the given date and make it unavailable
     *
     * @param book       the book to borrow
     * @param borrowedAt the date of the borrow
     */
    public void saveBookBorrow(final Book book, final LocalDate borrowedAt) {
        availableBooks.remove(book.getIsbn());
        borrowedBooks.put(book, borrowedAt);
    }

    /**
     * return a book and make it available
     */
    public void saveBookReturn(final Book book) {
        borrowedBooks.remove(book);
        availableBooks.put(book.getIsbn(), book);
    }

    /**
     * Find the date at which a book was borrowed, if indeed it was
     *
     * @return empty if book is not borrowed, otherwise the date of the borrow
     */
    public Optional<LocalDate> findBorrowedBookDate(final Book book) {
        return Optional.ofNullable(borrowedBooks.get(book));
    }
}
