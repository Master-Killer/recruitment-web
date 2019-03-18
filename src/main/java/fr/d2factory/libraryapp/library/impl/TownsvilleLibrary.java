package fr.d2factory.libraryapp.library.impl;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.library.HasLateBooksException;
import fr.d2factory.libraryapp.library.Library;
import fr.d2factory.libraryapp.member.Member;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class TownsvilleLibrary implements Library {

    private final BookRepository bookRepository;

    public TownsvilleLibrary(final BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Optional<Book> borrowBook(final ISBN isbnCode, final Member member, final LocalDate borrowedAt) throws HasLateBooksException {

        final Optional<Book> maybeBook = bookRepository.findBook(isbnCode);
        return maybeBook.map(book -> {
            bookRepository.saveBookBorrow(book, borrowedAt);
            //TODO member tardiness
            return book;
        });
    }

    private boolean hasLateBook(final Member member) {
        final int dayOfLateness = member.dayOfLateness();
        // find the highest elapsed time of a borrowed book
        // compare it to dayOfLateness
        return false;
    }

    @Override
    public void returnBook(final Book book, final Member member, final LocalDate returnedAt) {
        final Optional<LocalDate> maybeBorrowedBookDate = bookRepository.findBorrowedBookDate(book);

        final LocalDate borrowDate = maybeBorrowedBookDate
                .orElseThrow(() -> new IllegalArgumentException("Cannot return a book that was not borrowed: " + book));

        final Period elapsed = Period.between(borrowDate, returnedAt);
        member.payBook(elapsed.getDays());

        bookRepository.saveBookReturn(book);
    }
}
