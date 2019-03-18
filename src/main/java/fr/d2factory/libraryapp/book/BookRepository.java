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

    //TODO javadoc

    private final Map<ISBN, Book> availableBooks = new HashMap<>();
    private final Map<Book, LocalDate> borrowedBooks = new HashMap<>();

    public void addBooks(final List<Book> books) {
        books.forEach(book -> availableBooks.put(book.getIsbn(), book));
    }

    public Optional<Book> findBook(final ISBN isbnCode) {
        return Optional.ofNullable(availableBooks.get(isbnCode));
    }

    // assumes that the book object is known because it was just taken from availableBooks
    // might tighten security after
    public void saveBookBorrow(final Book book, final LocalDate borrowedAt) {
        availableBooks.remove(book.getIsbn());
        borrowedBooks.put(book, borrowedAt);
    }

    // ditto
    public void saveBookReturn(final Book book) {
        borrowedBooks.remove(book);
        availableBooks.put(book.getIsbn(), book);
    }

    public Optional<LocalDate> findBorrowedBookDate(final Book book) {
        return Optional.ofNullable(borrowedBooks.get(book));
    }
}
