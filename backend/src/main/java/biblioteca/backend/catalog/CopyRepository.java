package biblioteca.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

interface CopyRepository extends JpaRepository<Copy, Long> {
}
