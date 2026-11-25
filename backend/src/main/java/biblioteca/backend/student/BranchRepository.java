package biblioteca.backend.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findAllByOrderByNameAsc();
}
