package biblioteca.backend.student;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "students")
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "roll_number")
    private String rollNumber;
    private String email;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private StudentCategory category;
    private int year;
    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.PENDING;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected Student() {
    }

    Student(String firstName, String lastName, String rollNumber, String email,
            Branch branch, StudentCategory category, int year) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.rollNumber = rollNumber;
        this.email = email;
        this.branch = branch;
        this.category = category;
        this.year = year;
    }

    Long getId() {
        return id;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    String getRollNumber() {
        return rollNumber;
    }

    String getEmail() {
        return email;
    }

    Branch getBranch() {
        return branch;
    }

    StudentCategory getCategory() {
        return category;
    }

    int getYear() {
        return year;
    }

    StudentStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
