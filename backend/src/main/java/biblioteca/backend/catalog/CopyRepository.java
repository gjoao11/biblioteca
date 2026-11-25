package biblioteca.backend.catalog;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface CopyRepository extends JpaRepository<Copy, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Copy c where c.id = :id")
    Optional<Copy> findByIdForUpdate(@Param("id") Long id);

    @Query("select c from Copy c join fetch c.book b join fetch b.category where c.id = :id")
    Optional<Copy> findWithBookById(@Param("id") Long id);

    @Query("select new biblioteca.backend.catalog.CopyTitle(c.id, b.title) from Copy c join c.book b where c.id in :ids")
    List<CopyTitle> findTitles(@Param("ids") Collection<Long> ids);
}
