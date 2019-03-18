package fr.d2factory.libraryapp.library.impl;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.library.HasLateBooksException;
import fr.d2factory.libraryapp.library.Library;
import fr.d2factory.libraryapp.member.Member;

import java.time.LocalDate;
import java.util.Optional;

public class TownsvilleLibrary implements Library {

    @Override
    public Optional<Book> borrowBook(final long isbnCode, final Member member, final LocalDate borrowedAt) throws HasLateBooksException {
        return Optional.empty();
    }

    public boolean hasLateBook(final Member member, final LocalDate borrowedAt) {
        return false;
    }

    @Override
    public void returnBook(final Book book, final Member member) {

    }
}
