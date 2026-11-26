package biblioteca.backend.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByStudentIdAndReturnedAtIsNull(Long studentId);

    Optional<Loan> findByCopyIdAndReturnedAtIsNull(Long copyId);

    @Query("""
            SELECT l FROM Loan l
            WHERE (:status IS NULL
                   OR (:status = 'open'   AND l.returnedAt IS NULL)
                   OR (:status = 'closed' AND l.returnedAt IS NOT NULL))
              AND (:studentId IS NULL OR l.studentId = :studentId)
              AND (:copyId    IS NULL OR l.copyId    = :copyId)
            ORDER BY l.issuedAt DESC
            """)
    Page<Loan> findFiltered(
            @Param("status") String status,
            @Param("studentId") Long studentId,
            @Param("copyId") Long copyId,
            Pageable pageable);
}
