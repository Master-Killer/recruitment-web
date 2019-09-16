package fr.d2factory.libraryapp.library;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.library.MemberTest.MyMember;
import fr.d2factory.libraryapp.library.impl.TownsvilleLibrary;
import fr.d2factory.libraryapp.member.Member;
import fr.d2factory.libraryapp.member.impl.Resident;
import fr.d2factory.libraryapp.member.impl.Student;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static fr.d2factory.libraryapp.library.MemberTest._1000;
import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private Library library;
    private BookRepository bookRepository;

    @BeforeEach
    void setup() throws IOException {

        bookRepository = new BookRepository();
        bookRepository.addBooks(TestUtils.loadTestBooks());

        library = new TownsvilleLibrary(bookRepository);
    }

    @Test
    void member_cannot_borrow_a_non_existent_book() {
        final Optional<Book> book = library.borrowBook(new ISBN(0), new MyMember(_1000), LocalDate.now());
        assertFalse(book.isPresent());
    }

    @Test
    void member_can_borrow_a_book_if_book_is_available() {

        final ISBN isbnCode = new ISBN(46578964513L);

        assertTrue(bookRepository.findBook(isbnCode).isPresent());

        final Member member = new MyMember(_1000);
        final Optional<Book> book = library.borrowBook(isbnCode, member, LocalDate.now());

        assertTrue(book.isPresent());
        assertFalse(bookRepository.findBook(isbnCode).isPresent());
        assertTrue(bookRepository.findBorrowedBookDate(book.get()).isPresent());
    }

    @Test
    void borrowed_book_is_no_longer_available() {
        final ISBN sameBook = new ISBN(46578964513L);

        final Member member = new MyMember(_1000);
        final LocalDate borrowedAt = LocalDate.now();
        library.borrowBook(sameBook, member, borrowedAt);

        // same member
        final Optional<Book> book2 = library.borrowBook(sameBook, member, borrowedAt);

        assertFalse(book2.isPresent());

        final MyMember differentMember = new MyMember(_1000);
        final Optional<Book> book3 = library.borrowBook(sameBook, differentMember, borrowedAt);

        assertFalse(book3.isPresent());
    }

    @Test
    void member_can_return_a_borrowed_book() {
        final ISBN isbn = new ISBN(46578964513L);

        final BigDecimal initial = _1000;
        final Member member = new Student(initial);
        final LocalDate borrowedAt = LocalDate.now();
        final Book book = library.borrowBook(isbn, member, borrowedAt).get();

        // same member
        library.returnBook(book, member, borrowedAt.plusDays(20));

        assertTrue(bookRepository.findBook(isbn).isPresent());
        assertFalse(bookRepository.findBorrowedBookDate(book).isPresent());
        // assert that member has been charged
        MatcherAssert.assertThat(member.getWallet(), Matchers.lessThan(initial));
    }

    @Test
    void member_cannot_return_a_non_existent_book() {
        final Book notABook = new Book("this is not a book", "No one", new ISBN(Long.MIN_VALUE));
        assertThrows(IllegalStateException.class, () -> library.returnBook(notABook, new MyMember(_1000), LocalDate.now()));
    }

    @Test
    void member_cannot_return_a_book_borrowed_by_someone_else() {
        final ISBN isbn = new ISBN(46578964513L);
        final Member borrower = new MyMember(_1000);
        final LocalDate borrowedAt = LocalDate.now();

        final Book book = library.borrowBook(isbn, borrower, borrowedAt).get();

        // different member
        final Member anotherMember = new MyMember(_1000);

        assertThrows(IllegalStateException.class, () -> library.returnBook(book, anotherMember, borrowedAt));

        // borrow another book for misdirection
        final Book bookNotReturning = library.borrowBook(new ISBN(968787565445L), anotherMember, borrowedAt).get();

        // still try to return the book from the first member
        assertThrows(IllegalStateException.class, () -> library.returnBook(book, anotherMember, borrowedAt));

        assertFalse(bookRepository.findBook(isbn).isPresent());
        assertTrue(bookRepository.findBorrowedBookDate(book).isPresent());
    }

    @Test
    public void members_can_borrow_book_if_they_dont_have_late_books() {
        Student student = new Student(_1000);
        library.borrowBook(new ISBN(46578964513L), student, LocalDate.now().minusDays(35));
        library.borrowBook(new ISBN(3326456467846L), student, LocalDate.now().minusDays(35));
    }

    @Test
    void members_cannot_borrow_book_if_they_have_late_books() {
        final ISBN firstCode = new ISBN(3326456467846L);
        final ISBN secondCode = new ISBN(465789453149L);

        final Member member = new Resident(_1000);
        final LocalDate borrowedAt = LocalDate.now();
        final Book book = library.borrowBook(firstCode, member, borrowedAt).get();

        assertThrows(HasLateBooksException.class, () -> library.borrowBook(secondCode, member, borrowedAt.plusDays(member.dayOfLateness() + 10)));
        // assert that the book is not borrowed
        assertTrue(bookRepository.findBook(secondCode).isPresent());
    }

    @Test
    void members_can_borrow_book_after_they_return_late_book() {
        final ISBN firstCode = new ISBN(3326456467846L);
        final ISBN secondCode = new ISBN(465789453149L);

        final Member member = new Resident(_1000);
        final LocalDate borrowedAt = LocalDate.now();
        final Book book = library.borrowBook(firstCode, member, borrowedAt).get();

        assertThrows(HasLateBooksException.class, () -> library.borrowBook(secondCode, member, borrowedAt.plusDays(member.dayOfLateness() + 40)));

        library.returnBook(book, member, borrowedAt.plusDays(101));

        final Optional<Book> book2 = library.borrowBook(secondCode, member, borrowedAt.plusDays(2 * (member.dayOfLateness() + 40)));
        assertTrue(book2.isPresent());
    }
}
