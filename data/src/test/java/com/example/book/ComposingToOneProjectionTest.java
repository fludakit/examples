package com.example.book;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Read-side JOIN projection of a COMPOSING to-one reference: {@link Book#author} is a bare entity
 * field on an {@code @AggregateRoot} whose target is managed, so every CRUD read JOIN-projects it and
 * the target comes back FULL, not the id-only partial an {@code @Association} would produce.
 */
@ExtendWith(WeldJunit5Extension.class)
class ComposingToOneProjectionTest {

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
    void findByIdMaterialisesTheComposingTargetFully() {
        Author ursula = authors.save(author("Ursula"));
        Book saved = books.save(book("The Left Hand", ursula));

        Book back = books.findById(saved.getId()).orElseThrow();
        assertThat(back.getTitle()).isEqualTo("The Left Hand");
        assertThat(back.getAuthor()).isNotNull();
        assertThat(back.getAuthor().getId()).isEqualTo(ursula.getId());
        assertThat(back.getAuthor().getName())
                .as("a composing to-one read is JOIN-projected, so the target is fully materialised")
                .isEqualTo("Ursula");
    }

    @Test
    void findAllMaterialisesTheComposingTargetFully() {
        Author ursula = authors.save(author("Ursula"));
        Author ted = authors.save(author("Ted"));
        books.save(book("A Wizard", ursula));
        books.save(book("Lathe", ted));

        List<Book> all = books.findAll().toList();
        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(b -> {
            assertThat(b.getAuthor()).isNotNull();
            assertThat(b.getAuthor().getName()).isEqualTo(b.getTitle().equals("A Wizard") ? "Ursula" : "Ted");
        });
    }

    @Test
    void rootWithoutATargetComesBackWithANullReference() {
        Book saved = books.save(book("Anonymous", null));

        Book back = books.findById(saved.getId()).orElseThrow();
        assertThat(back.getTitle()).isEqualTo("Anonymous");
        assertThat(back.getAuthor()).isNull();
    }

    @Test
    void pagedReadCountsRootsAndMaterialisesTargets() {
        Author ursula = authors.save(author("Ursula"));
        books.save(book("Alpha", ursula));
        books.save(book("Beta", ursula));

        Page<Book> page = books.findAll(PageRequest.ofPage(1).size(10), Order.by(_Book.title.asc()));
        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(Book::getTitle).containsExactly("Alpha", "Beta");
        assertThat(page.content()).allSatisfy(b -> assertThat(b.getAuthor().getName()).isEqualTo("Ursula"));
    }

    @Test
    void pagedReadSortedThroughTheProjectedJoinDoesNotDuplicateIt() {
        Author ursula = authors.save(author("Ursula"));
        Author ted = authors.save(author("Ted"));
        books.save(book("Alpha", ursula));
        books.save(book("Beta", ted));

        Page<Book> page = books.findAll(PageRequest.ofPage(1).size(10), Order.by(_Book.author.name.asc()));
        assertThat(page.content()).extracting(Book::getTitle).containsExactly("Beta", "Alpha");
        assertThat(page.content()).allSatisfy(b -> assertThat(b.getAuthor().getName()).isNotNull());
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
