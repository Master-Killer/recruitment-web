package fr.d2factory.libraryapp.member;

import fr.d2factory.libraryapp.library.Library;

import java.math.BigDecimal;

/**
 * A member is a person who can borrow and return books to a {@link Library}
 * A member can be either a first year student, a student or a resident
 */
public abstract class Member {

    /**
     * An initial sum of money the member has
     */
    private BigDecimal wallet;

    protected Member(final BigDecimal wallet) {
        this.wallet = wallet;
    }

    /**
     * The member should pay their books when they are returned to the library
     *
     * @param numberOfDays the number of days they kept the book
     */
    //TODO what happens when the member doesn't have any more money to pay the returned book?
    // should they not be allowed to return the book? Should their balance be negative?
    // should payBook be able fail? Or partially succeed?
    // if this.wallet is the wallet of the Member, shouldn't the Library has its own balance to keep accounts?
    // For now allow wallet to be negative, talk to business to resolve this
    public void payBook(final long numberOfDays) {
        if (numberOfDays < 0) throw new IllegalArgumentException("numberOfDays was negative " + numberOfDays);

        setWallet(getWallet().subtract(priceForBook(numberOfDays)));
    }

    /**
     * The price to pay for a book when they are returned to the library
     *
     * @param numberOfDays the number of days they kept the book
     * @return the price to pay for the book
     */
    protected abstract BigDecimal priceForBook(final long numberOfDays);

    /**
     * The number of day at which point the member becomes late
     */
    public abstract long dayOfLateness();

    public BigDecimal getWallet() {
        return wallet;
    }

    protected void setWallet(final BigDecimal wallet) {
        this.wallet = wallet;
    }
}
