package fr.d2factory.libraryapp.book;

/**
 * A simple representation of a book
 */
//TODO Make it as data class. Add equals/hashCode. Necessary to use in Map
public class Book {

    String title;
    String author;
    ISBN isbn;

    public Book(final String title, final String author, final ISBN isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }
}
