package biblioteca.backend.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BookRepository extends JpaRepository<Book, Long> {
    @Query(value = """
            select new biblioteca.backend.catalog.BookSummary(
                b.id, b.title, b.author, c.id, c.name,
                count(cp.id), count(case when cp.status = :available then 1 end))
            from Book b
            join b.category c
            left join Copy cp on cp.book = b
            where (:categoryId is null or c.id = :categoryId)
              and (lower(b.title) like :pattern escape '\\' or lower(b.author) like :pattern escape '\\')
            group by b.id, b.title, b.author, c.id, c.name
            order by lower(b.title), b.id
            """,
            countQuery = """
            select count(b)
            from Book b
            where (:categoryId is null or b.category.id = :categoryId)
              and (lower(b.title) like :pattern escape '\\' or lower(b.author) like :pattern escape '\\')
            """)
    Page<BookSummary> findSummaries(
            @Param("categoryId") Long categoryId,
            @Param("pattern") String pattern,
            @Param("available") CopyStatus available,
            Pageable pageable);
}
