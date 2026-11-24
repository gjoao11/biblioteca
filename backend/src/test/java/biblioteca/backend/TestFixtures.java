package biblioteca.backend;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.atomic.AtomicLong;

public final class TestFixtures {
    private static final AtomicLong SEQUENCE = new AtomicLong(System.nanoTime());

    private final JdbcTemplate jdbc;

    public TestFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static String unique(String prefix) {
        return prefix + "-" + SEQUENCE.incrementAndGet();
    }

    public long user() {
        return jdbc.queryForObject(
                "INSERT INTO users (username, name, password_hash, role) VALUES (?, 'Fixture', 'x', 'LIBRARIAN') RETURNING id",
                Long.class, unique("fixture"));
    }

    public long bookCategory(String name) {
        return jdbc.queryForObject("INSERT INTO book_categories (name) VALUES (?) RETURNING id", Long.class, name);
    }

    public long book(long categoryId, String title, String author, long addedBy) {
        return jdbc.queryForObject(
                "INSERT INTO books (title, author, category_id, added_by) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, title, author, categoryId, addedBy);
    }

    public long copy(long bookId, String status) {
        return jdbc.queryForObject(
                "INSERT INTO copies (book_id, status) VALUES (?, ?) RETURNING id", Long.class, bookId, status);
    }

    public long student() {
        String roll = unique("R");
        return student(roll, roll + "@example.com");
    }

    public long student(String email) {
        return student(unique("R"), email);
    }

    private long student(String roll, String email) {
        return jdbc.queryForObject("""
                INSERT INTO students (first_name, last_name, roll_number, email, branch_id, category_id, year)
                VALUES ('Fixture', 'Student', ?, ?,
                        (SELECT id FROM branches WHERE name = 'Computer Science and Engineering'),
                        (SELECT id FROM student_categories WHERE name = 'General'), 2024)
                RETURNING id
                """, Long.class, roll, email);
    }

    public long openLoan(long copyId, long studentId, long issuedBy) {
        return jdbc.queryForObject("""
                INSERT INTO loans (copy_id, student_id, issued_by, issued_at, due_at)
                VALUES (?, ?, ?, NOW(), NOW() + INTERVAL '14 days')
                RETURNING id
                """, Long.class, copyId, studentId, issuedBy);
    }

    public void returnLoan(long loanId) {
        jdbc.update("UPDATE loans SET returned_at = NOW() WHERE id = ?", loanId);
    }
}
