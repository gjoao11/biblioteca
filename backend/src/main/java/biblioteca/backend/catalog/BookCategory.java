package biblioteca.backend.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "book_categories")
class BookCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    protected BookCategory() {
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }
}
