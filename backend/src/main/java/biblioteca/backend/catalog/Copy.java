package biblioteca.backend.catalog;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "copies")
class Copy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;
    @Enumerated(EnumType.STRING)
    private CopyStatus status = CopyStatus.AVAILABLE;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected Copy() {
    }

    Copy(Book book) {
        this.book = book;
    }

    Long getId() {
        return id;
    }

    Book getBook() {
        return book;
    }

    CopyStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
