package biblioteca.backend.student;

import org.springframework.data.jpa.repository.JpaRepository;

interface StudentCategoryRepository extends JpaRepository<StudentCategory, Long> {
}
