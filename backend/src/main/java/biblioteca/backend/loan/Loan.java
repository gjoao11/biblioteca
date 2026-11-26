package biblioteca.backend.loan;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loans")
class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "copy_id")
    private Long copyId;
    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "issued_by")
    private Long issuedBy;
    @Column(name = "issued_at")
    private Instant issuedAt;
    @Column(name = "due_at")
    private Instant dueAt;
    @Column(name = "returned_at")
    private Instant returnedAt;

    protected Loan() {
    }

    Loan(Long copyId, Long studentId, Long issuedBy, Instant issuedAt, Instant dueAt) {
        this.copyId = copyId;
        this.studentId = studentId;
        this.issuedBy = issuedBy;
        this.issuedAt = issuedAt;
        this.dueAt = dueAt;
    }

    Long getId() {
        return id;
    }

    Long getCopyId() {
        return copyId;
    }

    Long getStudentId() {
        return studentId;
    }

    Long getIssuedBy() {
        return issuedBy;
    }

    Instant getIssuedAt() {
        return issuedAt;
    }

    Instant getDueAt() {
        return dueAt;
    }

    Instant getReturnedAt() {
        return returnedAt;
    }

    void markReturned(java.time.Instant returnedAt) {
        this.returnedAt = returnedAt;
    }
}
