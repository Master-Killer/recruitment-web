package fr.d2factory.libraryapp.member.impl;

import fr.d2factory.libraryapp.member.Member;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class FirstYearStudent extends Member {

    private static final int SPECIAL_DAYS = 15;
    private static final BigDecimal STANDARD_RATE = new BigDecimal("0.10");
    private static final BigDecimal LATE_RATE = new BigDecimal("0.15");

    public FirstYearStudent(final BigDecimal wallet) {
        super(wallet);
    }

    @Override
    protected BigDecimal priceForBook(final long numberOfDays) {

        final long daysAtSpecialRate = Math.min(numberOfDays, SPECIAL_DAYS);
        final long daysLeftToPay = numberOfDays - daysAtSpecialRate;

        final long standardDays = dayOfLateness() - SPECIAL_DAYS;
        return leftToPayAtStandardAndLateRate(daysLeftToPay, standardDays, STANDARD_RATE, LATE_RATE);
    }

    /**
     * @param numberOfDays number of days to pay at standard and late rate
     * @param standardDays duration of the standard paying rate period
     * @param standardRate daily rate of standard period
     * @param lateRate     daily rate of late period
     * @return the price to pay for a book during the standard and late period
     */
    static BigDecimal leftToPayAtStandardAndLateRate(final long numberOfDays, final long standardDays,
                                                     final BigDecimal standardRate, final BigDecimal lateRate) {

        BigDecimal price = ZERO;

        long daysLeftToPay = numberOfDays;
        final long daysAtStandardRate = Math.min(daysLeftToPay, standardDays);
        price = price.add(BigDecimal.valueOf(daysAtStandardRate).multiply(standardRate));

        daysLeftToPay -= daysAtStandardRate;

        price = price.add(BigDecimal.valueOf(daysLeftToPay).multiply(lateRate));
        return price;
    }

    @Override
    public long dayOfLateness() {
        return 30;
    }
}
