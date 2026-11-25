package biblioteca.backend.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class CatalogLoanApi {
    private final CopyRepository copies;

    CatalogLoanApi(CopyRepository copies) {
        this.copies = copies;
    }

    public Optional<LockedCopy> findCopyByIdForUpdate(Long copyId) {
        return copies.findByIdForUpdate(copyId).map(copy -> new LockedCopy(
                copy.getId(), copy.getBook().getTitle(), copy.getStatus() == CopyStatus.AVAILABLE));
    }

    public void markLoaned(Long copyId) {
        find(copyId).markLoaned();
    }

    public void markAvailable(Long copyId) {
        find(copyId).markAvailable();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<Long, String> findBookTitles(Collection<Long> copyIds) {
        if (copyIds.isEmpty()) {
            return Map.of();
        }
        return copies.findTitles(copyIds).stream()
                .collect(Collectors.toMap(CopyTitle::copyId, CopyTitle::title));
    }

    private Copy find(Long copyId) {
        return copies.findById(copyId)
                .orElseThrow(() -> new IllegalStateException("Copy " + copyId + " does not exist"));
    }
}
