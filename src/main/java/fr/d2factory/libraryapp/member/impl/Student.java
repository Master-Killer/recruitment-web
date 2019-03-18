package fr.d2factory.libraryapp.member.impl;

import fr.d2factory.libraryapp.member.Member;

import java.math.BigDecimal;

public class Student extends Member {

    public Student(final BigDecimal wallet) {
        super(wallet);
    }

    @Override
    public void payBook(final int numberOfDays) {

    }

    @Override
    public int dayOfLateness() {
        return 30;
    }
}
