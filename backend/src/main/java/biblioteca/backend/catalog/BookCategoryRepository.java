package biblioteca.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findAllByOrderByNameAsc();
}
