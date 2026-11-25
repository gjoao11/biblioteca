package biblioteca.backend.student;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface StudentRepository extends JpaRepository<Student, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Student s where s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @Query("select s from Student s join fetch s.branch join fetch s.category where s.id = :id")
    Optional<Student> findWithReferencesById(@Param("id") Long id);

    @Query(value = """
            select s from Student s
            join fetch s.branch b
            join fetch s.category c
            where (:status is null or s.status = :status)
              and (:branchId is null or b.id = :branchId)
              and (:year is null or s.year = :year)
              and (:categoryId is null or c.id = :categoryId)
            order by lower(s.firstName), lower(s.lastName), s.id
            """,
            countQuery = """
            select count(s) from Student s
            where (:status is null or s.status = :status)
              and (:branchId is null or s.branch.id = :branchId)
              and (:year is null or s.year = :year)
              and (:categoryId is null or s.category.id = :categoryId)
            """)
    Page<Student> findFiltered(
            @Param("status") StudentStatus status,
            @Param("branchId") Long branchId,
            @Param("year") Integer year,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("select distinct s.year from Student s order by s.year")
    List<Integer> findYears();

    boolean existsByRollNumber(String rollNumber);

    boolean existsByEmailIgnoreCase(String email);
}
