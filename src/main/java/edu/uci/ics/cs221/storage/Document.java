package edu.uci.ics.cs221.storage;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * In practice, Document might contain multiple fields of various types (string, int, double...),
 * in our project, Document contains a single string field and is not null for simplicity.
 */
public class Document {

    private final String text;

    public Document(String text) {
        checkNotNull(text);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(text, document.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "Doc{" +
                "text='" + text + '\'' +
                '}';
    }
}
