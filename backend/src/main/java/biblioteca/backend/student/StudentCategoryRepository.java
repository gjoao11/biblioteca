package biblioteca.backend.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface StudentCategoryRepository extends JpaRepository<StudentCategory, Long> {
    List<StudentCategory> findAllByOrderByNameAsc();
}
