package fr.d2factory.libraryapp.library;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.library.impl.TownsvilleLibrary;
import fr.d2factory.libraryapp.member.Member;
import fr.d2factory.libraryapp.member.impl.FirstYearStudent;
import fr.d2factory.libraryapp.member.impl.Resident;
import fr.d2factory.libraryapp.member.impl.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    public static BigDecimal _1000 = new BigDecimal("1000");

    private Library library;
    private BookRepository bookRepository;

    @BeforeEach
    public void setup() throws IOException {

        bookRepository = new BookRepository();
        bookRepository.addBooks(TestUtils.loadTestBooks());

        library = new TownsvilleLibrary(bookRepository);
    }

    @Test
    public void member_can_borrow_a_book_if_book_is_available() {

        final ISBN isbnCode = new ISBN(46578964513L);

        assertTrue(bookRepository.findBook(isbnCode).isPresent());

        final Member member = new MyMember(_1000);
        final Optional<Book> book = library.borrowBook(isbnCode, member, LocalDate.now());

        assertTrue(book.isPresent());
        assertFalse(bookRepository.findBook(isbnCode).isPresent());
        assertTrue(bookRepository.findBorrowedBookDate(book.get()).isPresent());
    }

    @Test
    public void borrowed_book_is_no_longer_available() {
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
    public void member_can_return_a_borrowed_book() {
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
        assertThat(member.getWallet(), lessThan(initial));
    }

    @Test
    public void member_cannot_return_a_book_borrowed_by_someone_else() {
        final ISBN isbn = new ISBN(46578964513L);
        final Member borrower = new MyMember(_1000);
        final LocalDate borrowedAt = LocalDate.now();

        final Book book = library.borrowBook(isbn, borrower, borrowedAt).get();

        // different member
        final Member anotherMember = new MyMember(_1000);

        assertThrows(IllegalArgumentException.class, () -> library.returnBook(book, anotherMember, borrowedAt));

        // borrow another book for misdirection
        final Book bookNotReturning = library.borrowBook(new ISBN(968787565445L), anotherMember, borrowedAt).get();

        // still try to return the book from the first member
        assertThrows(IllegalArgumentException.class, () -> library.returnBook(book, anotherMember, borrowedAt));

        assertFalse(bookRepository.findBook(isbn).isPresent());
        assertTrue(bookRepository.findBorrowedBookDate(book).isPresent());
    }

    public static BigDecimal memberPays(final Member member, final int numberOfDays) {

        final BigDecimal initial = member.getWallet();
        member.payBook(numberOfDays);

        return initial.subtract(member.getWallet());
    }

    public static Object[][] residentPrice() {
        return new Object[][] {
                { 0, "0" },
                { 1, "0.1" },
                { 10, "1" },
                { 37, "3.7" },
                { 59, "5.9" },
                { 60, "6" },
        };
    }

    @ParameterizedTest
    @MethodSource("residentPrice")
    public void residents_are_taxed_10cents_for_each_day_they_keep_a_book(final int numberOfDays, final String price) {

        // can write ParameterizedTest to test multiple values that have the same effect

        assertThat(memberPays(new Resident(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    @Test
    public void members_are_not_taxed_for_weird_days() {

        // can use assertAll to gather all results from all assertions and not stop after the first fail

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Resident(_1000).payBook(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Resident(_1000).payBook(Integer.MIN_VALUE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Student(_1000).payBook(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Student(_1000).payBook(Integer.MIN_VALUE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new FirstYearStudent(_1000).payBook(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new FirstYearStudent(_1000).payBook(Integer.MIN_VALUE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new MyMember(_1000).payBook(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new MyMember(_1000).payBook(Integer.MIN_VALUE))
        );
    }

    public static Object[][] studentPrice() {
        return new Object[][] {
                { 0, "0" },
                { 1, "0.1" },
                { 10, "1" },
                { 14, "1.4" },
                { 29, "2.9" },
                { 30, "3" },
        };
    }

    @ParameterizedTest
    @MethodSource("studentPrice")
    public void students_pay_10_cents_the_first_30days(final int numberOfDays, final String price) {

        assertThat(memberPays(new Student(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    @Test
    public void students_pay_10_cents_the_first_30days_2() {

        for (int numberOfDays = 0; numberOfDays < 30; numberOfDays++) {
            final Student student = new Student(_1000);
            final BigDecimal initial = student.getWallet();
            student.payBook(numberOfDays);
            final BigDecimal wallet2 = student.getWallet();
            final BigDecimal afterNDays = initial.subtract(wallet2);
            student.payBook(numberOfDays + 1);
            final BigDecimal wallet3 = student.getWallet();
            final BigDecimal afterN1Days = wallet2.subtract(wallet3);

            assertThat(afterN1Days.subtract(afterNDays), comparesEqualTo(new BigDecimal("0.10")));
        }
    }

    public static IntStream firstYearStudentSpecialDays() {
        return IntStream.rangeClosed(0, 15);
    }

    @ParameterizedTest
    @MethodSource("firstYearStudentSpecialDays")
    public void students_in_1st_year_are_not_taxed_for_the_first_15days(final Integer numberOfDays) {

        assertThat(memberPays(new FirstYearStudent(_1000), numberOfDays), comparesEqualTo(ZERO));
    }

    public static Object[][] firstYearStudent() {
        return new Object[][] {
                // still free
                { 15, "0" },
                // starts to pay
                { 16, "0.1" },
                { 17, "0.2" },
                { 29, "1.4" },
                { 30, "1.5" },
                // starts to be late
                { 31, "1.65" },
                { 32, "1.8" },
                { 130, "16.5" },
        };
    }

    @ParameterizedTest
    @MethodSource("firstYearStudent")
    public void students_in_1st_year_pay_fees(final Integer numberOfDays, final String price) {

        assertThat(memberPays(new FirstYearStudent(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    public static Object[][] studentLate() {
        return new Object[][] {
                { 30, "0.0" },
                { 31, "0.15" },
                { 32, "0.30" },
                { 130, "15" },
        };
    }

    @ParameterizedTest
    @MethodSource("studentLate")
    public void students_pay_15cents_for_each_day_they_keep_a_book_after_the_initial_30days(final int numberOfDays, final String price) {

        final Student member = new Student(_1000);
        final BigDecimal after30Days = memberPays(member, 30);
        final BigDecimal afterNDays = memberPays(member, numberOfDays);
        assertThat(afterNDays.subtract(after30Days), comparesEqualTo(new BigDecimal(price)));
    }

    public static Object[][] residentLate() {
        return new Object[][] {
                { 61, "6.2" },
                { 62, "6.4" },
                { 100, "14" }, // 60 * 0.1 + 40 * 0.2
        };
    }

    @ParameterizedTest
    @MethodSource("residentLate")
    public void residents_pay_20cents_for_each_day_they_keep_a_book_after_the_initial_60days(final int numberOfDays, final String price) {
        assertThat(memberPays(new Resident(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    @Test
    public void members_cannot_borrow_book_if_they_have_late_books() {
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
    public void members_can_borrow_book_after_they_return_late_book() {
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

    private static class MyMember extends Member {

        public MyMember(final BigDecimal wallet) {
            super(wallet);
        }

        @Override
        protected BigDecimal priceForBook(final long numberOfDays) {
            return ZERO;
        }

        @Override
        public long dayOfLateness() {
            return Integer.MAX_VALUE;
        }
    }
}
