package fr.d2factory.libraryapp.library;

import fr.d2factory.libraryapp.member.Member;
import fr.d2factory.libraryapp.member.impl.FirstYearStudent;
import fr.d2factory.libraryapp.member.impl.Resident;
import fr.d2factory.libraryapp.member.impl.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {

    static final BigDecimal _1000 = new BigDecimal("1000");

    static BigDecimal memberPays(final Member member, final int numberOfDays) {

        final BigDecimal initial = member.getWallet();
        member.payBook(numberOfDays);

        return initial.subtract(member.getWallet());
    }

    @Test
    void members_are_not_taxed_for_weird_days() {

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

    static Object[][] residentPrice() {
        return new Object[][] {
                { 0, "0" },
                { 1, "0.1" },
                { 10, "1" },
                { 37, "3.7" },
                { 59, "5.9" },
                { 60, "6" },

                // after 60 they pay more
                { 61, "6.2" },
                { 62, "6.4" },
                { 100, "14" }, // 60 * 0.1 + 40 * 0.2

        };
    }

    @ParameterizedTest
    @MethodSource("residentPrice")
    void residents_pay_fees_for_book(final int numberOfDays, final String price) {

        // can write ParameterizedTest to test multiple values that have the same effect

        assertThat(memberPays(new Resident(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    static Object[][] studentPrice() {
        return new Object[][] {
                { 0, "0" },
                { 1, "0.1" },
                { 10, "1" },
                { 14, "1.4" },
                { 29, "2.9" },
                { 30, "3" },

                // after 30 days they pay more
                { 31, "3.15" },
                { 32, "3.30" },
                { 130, "18" },
        };
    }

    @ParameterizedTest
    @MethodSource("studentPrice")
    void students_pay_fees_for_book(final int numberOfDays, final String price) {

        assertThat(memberPays(new Student(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    static Object[][] firstYearStudent() {
        return new Object[][] {
                // before 15 days they don't pay
                { 15, "0" },

                // after 15 days they start to pay
                { 16, "0.1" },
                { 17, "0.2" },
                { 29, "1.4" },
                { 30, "1.5" },

                //after 30 days they pay more
                { 31, "1.65" },
                { 32, "1.8" },
                { 130, "16.5" },
        };
    }

    @ParameterizedTest
    @MethodSource("firstYearStudent")
    void students_in_1st_year_pay_fees_for_book(final Integer numberOfDays, final String price) {

        assertThat(memberPays(new FirstYearStudent(_1000), numberOfDays), comparesEqualTo(new BigDecimal(price)));
    }

    static class MyMember extends Member {

        MyMember(final BigDecimal wallet) {
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
