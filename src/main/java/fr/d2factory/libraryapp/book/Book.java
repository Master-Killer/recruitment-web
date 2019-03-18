package fr.d2factory.libraryapp.book;

import lombok.Value;

/**
 * A simple representation of a book
 */

@Value
public class Book {

    String title;
    String author;
    ISBN isbn;
}
