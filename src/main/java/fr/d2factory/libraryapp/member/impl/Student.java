package fr.d2factory.libraryapp.member.impl;

import fr.d2factory.libraryapp.member.Member;

import java.math.BigDecimal;

import static fr.d2factory.libraryapp.member.impl.FirstYearStudent.leftToPayAtStandardAndLateRate;

public class Student extends Member {

    private static final BigDecimal STANDARD_RATE = new BigDecimal("0.10");
    private static final BigDecimal LATE_RATE = new BigDecimal("0.15");

    public Student(final BigDecimal wallet) {
        super(wallet);
    }

    @Override
    protected BigDecimal priceForBook(final long numberOfDays) {

        final int standardDays = dayOfLateness();
        return leftToPayAtStandardAndLateRate(numberOfDays, standardDays, STANDARD_RATE, LATE_RATE);
    }

    @Override
    public int dayOfLateness() {
        return 30;
    }
}
