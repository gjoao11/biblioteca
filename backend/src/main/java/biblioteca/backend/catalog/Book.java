package biblioteca.backend.catalog;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "books")
class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private BookCategory category;
    @Column(name = "added_by")
    private Long addedBy;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected Book() {
    }

    Book(String title, String author, String description, BookCategory category, Long addedBy) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.addedBy = addedBy;
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getAuthor() {
        return author;
    }

    String getDescription() {
        return description;
    }

    BookCategory getCategory() {
        return category;
    }

    Long getAddedBy() {
        return addedBy;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
