package com.example.book;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Write-side counterpart of the composing to-one projection: the aggregate root {@link Book} OWNS its
 * {@link Book#author}, so a write of the root writes the target too — inserted with a key holder when
 * it has no id yet (so the root's fk bind sees the generated key) and upserted when it has one.
 */
@ExtendWith(WeldJunit5Extension.class)
class ComposingToOneWriteTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            AuthorRepositoryImpl.class,
            BookRepositoryImpl.class).build();

    @Inject AuthorRepository authors;
    @Inject BookRepository books;

    @Test
    void saveInsertsAnOwnedTargetThatHasNoIdYet() {
        Author ursula = author("Ursula");
        Book saved = books.save(book("The Left Hand", ursula));

        assertThat(ursula.getId())
                .as("the generated target id must be backfilled onto the object the root binds")
                .isNotNull();

        Book back = books.findById(saved.getId()).orElseThrow();
        assertThat(back.getAuthor()).isNotNull();
        assertThat(back.getAuthor().getId()).isEqualTo(ursula.getId());
        assertThat(back.getAuthor().getName()).isEqualTo("Ursula");
        assertThat(authors.findById(ursula.getId()).orElseThrow().getName()).isEqualTo("Ursula");
    }

    @Test
    void saveUpsertsAnOwnedTargetInPlace() {
        Author ursula = authors.save(author("Ursula"));
        Long authorId = ursula.getId();
        Book saved = books.save(book("A Wizard", ursula));

        ursula.setName("Ursula K. Le Guin");
        books.save(saved);

        assertThat(authors.findById(authorId).orElseThrow().getName()).isEqualTo("Ursula K. Le Guin");
        assertThat(books.findById(saved.getId()).orElseThrow().getAuthor().getName())
                .isEqualTo("Ursula K. Le Guin");
    }

    @Test
    void updateWritesAnOwnedTargetToo() {
        Author ursula = authors.save(author("Ursula"));
        Book saved = books.save(book("A Wizard", ursula));

        ursula.setName("Ursula K. Le Guin");
        saved.setTitle("A Wizard of Earthsea");
        books.update(saved);

        Book back = books.findById(saved.getId()).orElseThrow();
        assertThat(back.getTitle()).isEqualTo("A Wizard of Earthsea");
        assertThat(back.getAuthor().getName()).isEqualTo("Ursula K. Le Guin");
        assertThat(authors.findById(ursula.getId()).orElseThrow().getName())
                .isEqualTo("Ursula K. Le Guin");
    }

    @Test
    void aNullTargetIsSkippedNotWritten() {
        Book saved = books.save(book("Anonymous", null));

        Book back = books.findById(saved.getId()).orElseThrow();
        assertThat(back.getAuthor()).isNull();
    }

    private static Author author(String name) {
        Author a = new Author();
        a.setName(name);
        return a;
    }

    private static Book book(String title, Author author) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        return b;
    }
}
