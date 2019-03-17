package fr.d2factory.libraryapp.book;

//TODO Make it as data class. Add equals/hashCode. Necessary to use in Map
public class ISBN {

    long isbnCode;

    public ISBN(final long isbnCode) {
        this.isbnCode = isbnCode;
    }
}
