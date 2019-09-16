package fr.d2factory.libraryapp.library.impl;

import static java.time.temporal.ChronoUnit.DAYS;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.library.HasLateBooksException;
import fr.d2factory.libraryapp.library.Library;
import fr.d2factory.libraryapp.member.Member;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

public class TownsvilleLibrary implements Library {

    private final BookRepository bookRepository;

    private final IdentityHashMap<Member, Set<Book>> memberBorrowings = new IdentityHashMap<>();

    public TownsvilleLibrary(final BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Optional<Book> borrowBook(final ISBN isbnCode, final Member member, final LocalDate borrowedAt) throws HasLateBooksException {
        if (hasLateBook(member, borrowedAt)) throw new HasLateBooksException();

        final Optional<Book> maybeBook = bookRepository.findBook(isbnCode);
        return maybeBook.map(book -> {

            memberBorrowings.computeIfAbsent(member, k -> new HashSet<>()).add(book);

            bookRepository.saveBookBorrow(book, borrowedAt);

            return book;
        });
    }

    private boolean hasLateBook(final Member member, final LocalDate now) {
        final long dayOfLateness = member.dayOfLateness();
        // find the highest elapsed time of a borrowed book
        // compare it to dayOfLateness

        // no book no lateness
        final Set<Book> borrowings = memberBorrowings.getOrDefault(member, new HashSet<>());
        return borrowings.stream().anyMatch(
            book -> bookRepository.findBorrowedBookDate(book)
                .map(borrowedDate -> borrowedDate.until(now, DAYS) > dayOfLateness)
                .orElse(false));
    }

    @Override
    public void returnBook(final Book book, final Member member, final LocalDate returnedAt) {
        final Optional<LocalDate> maybeBorrowedBookDate = bookRepository.findBorrowedBookDate(book);

        final LocalDate borrowDate = maybeBorrowedBookDate
                .orElseThrow(() -> new IllegalStateException("Cannot return a book that was not borrowed: " + book));

        final Set<Book> borrowings = memberBorrowings.getOrDefault(member, new HashSet<>());
        final boolean removed = borrowings.remove(book);
        if (!removed)
            throw new IllegalStateException(String.format("Cannot return a book that was not borrowed by the same member: %s, member %s", book, member));

        final long elapsed = borrowDate.until(returnedAt, DAYS);
        member.payBook(elapsed);

        bookRepository.saveBookReturn(book);
    }
}
