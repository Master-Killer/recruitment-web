package fr.d2factory.libraryapp.library;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.member.Member;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The library class is in charge of stocking the books and managing the return delays and members
 * <p>
 * The books are available via the {@link fr.d2factory.libraryapp.book.BookRepository}
 */
public interface Library {

    /**
     * A member is borrowing a book from our library.
     *
     * @param isbnCode   the isbn code of the book
     * @param member     the member who is borrowing the book
     * @param borrowedAt the date when the book was borrowed
     * @return An Optional containing the book the member wishes to obtain if found, an empty Optional otherwise
     * @throws HasLateBooksException in case the member has books that are late
     * @see fr.d2factory.libraryapp.book.ISBN
     * @see Member
     */
    Optional<Book> borrowBook(ISBN isbnCode, Member member, LocalDate borrowedAt) throws HasLateBooksException;

    /**
     * A member returns a book to the library.
     * We should calculate the tarif and 'probably' charge the member
     *
     * @param book       the {@link Book} they return
     * @param member     the {@link Member} who is returning the book
     * @param returnedAt the date when the book was returned
     * @throws IllegalStateException in case the returned book is form another member or not a real book
     * @see Member#payBook(long)
     */
    void returnBook(Book book, Member member, final LocalDate returnedAt);
}
