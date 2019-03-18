package fr.d2factory.libraryapp.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.d2factory.libraryapp.book.Book;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<Book> loadTestBooks() throws IOException {
        final Type type = new TypeToken<ArrayList<Book>>() {
        }.getType();

        try (final InputStream is = LibraryTest.class.getClassLoader().getResourceAsStream("books.json")) {
            if (is == null) {
                throw new IllegalStateException("File not found");
            }

            return new Gson().fromJson(new InputStreamReader(is), type);
        }
    }

    public static <T> T gsout(final T t) {
        System.out.println(GSON.toJson(t));
        return t;
    }
}
