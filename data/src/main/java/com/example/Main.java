package com.example;

import com.example.book.Author;
import com.example.book.AuthorRepository;
import com.example.book.Book;
import com.example.book.BookRepository;
import com.example.estore.Customer;
import com.example.estore.CustomerRepository;
import com.example.estore.Order;
import com.example.estore.OrderRepository;
import com.example.estore.Product;
import com.example.estore.ProductRepository;
import com.example.estore._Customer;
import com.example.team.Player;
import com.example.team.PlayerRepository;
import com.example.team.Team;
import com.example.team.TeamRepository;
import io.github.fludakit.jdbc.JdbcClient;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.math.BigDecimal;

/**
 * Boots Weld SE and drives every generated {@code @Repository} implementation through one scenario
 * per feature this example covers. Run it with {@code mvn -pl data exec:java} — or just from the IDE.
 */
public class Main {

    public static void main(String[] args) {
        try (WeldContainer container = new Weld().initialize()) {
            CustomerRepository customers = container.select(CustomerRepository.class).get();
            ProductRepository products = container.select(ProductRepository.class).get();
            OrderRepository orders = container.select(OrderRepository.class).get();
            BookRepository books = container.select(BookRepository.class).get();
            AuthorRepository authors = container.select(AuthorRepository.class).get();
            TeamRepository teams = container.select(TeamRepository.class).get();
            PlayerRepository players = container.select(PlayerRepository.class).get();

            Customer ana = customers.insert(customer("Ada", "ada@example.com"));
            System.out.println("inserted customer id = " + ana.getId());
            System.out.println("findByName(Ada)      = " + customers.findByName("Ada").size());

            Product ddd = products.insert(product("DDD", "39.99"));
            System.out.println("findByPrice(39.99)   = " + products.findByPrice(new BigDecimal("39.99")).size());

            Order order = new Order(ana);
            order.setAmount(new BigDecimal("79.98"));
            order.addItem(ddd, 2);
            orders.save(order);
            Order reloaded = orders.findByCustomerName("Ada").get(0);
            System.out.println("order customer name  = " + reloaded.getCustomer().getName()
                    + ", items = " + reloaded.getOrderItems().size());

            Page<Customer> page = customers.findAll(
                    PageRequest.ofSize(10), jakarta.data.Order.by(_Customer.name.asc()));
            System.out.println("page size            = " + page.numberOfElements());

            CursoredPage<Customer> first = customers.paged(PageRequest.ofSize(1));
            System.out.println("cursored first page  = " + first.content().size());

            Author twain = authors.insert(author("Mark Twain"));
            Book book = new Book();
            book.setTitle("Tom Sawyer");
            book.setAuthor(twain);
            books.save(book);
            System.out.println("book author (joined) = "
                    + books.findById(book.getId()).orElseThrow().getAuthor().getName());

            Team team = teams.insert(team("Rockets"));
            Player r = new Player();
            r.setName("Grace");
            r.setTeam(team);
            players.save(r);
            System.out.println("team players hydr.   = "
                    + teams.findById(team.getId()).orElseThrow().getPlayers().size());
            System.out.println("teams by player name = " + teams.findByPlayersName("Grace").size());

            JdbcClient client = customers.getJdbcClient();
            System.out.println("row count via hatch  = "
                    + client.sql("SELECT COUNT(*) FROM \"customer\"").singleValue(Long.class));
        }
    }

    private static Customer customer(String name, String email) {
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    private static Product product(String name, String price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        return p;
    }

    private static Author author(String name) {
        Author a = new Author();
        a.setName(name);
        return a;
    }

    private static Team team(String name) {
        Team t = new Team();
        t.setName(name);
        return t;
    }
}
