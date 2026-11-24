package biblioteca.backend.student;

import jakarta.persistence.*;

@Entity
@Table(name = "student_categories")
class StudentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "max_loans")
    private int maxLoans;

    protected StudentCategory() {
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }

    int getMaxLoans() {
        return maxLoans;
    }
}
