package biblioteca.backend.student;

import jakarta.persistence.*;

@Entity
@Table(name = "branches")
class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    protected Branch() {
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }
}
