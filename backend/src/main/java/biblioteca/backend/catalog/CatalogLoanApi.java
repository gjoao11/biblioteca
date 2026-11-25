package biblioteca.backend.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        copies.findById(copyId).orElseThrow().markLoaned();
    }

    public void markAvailable(Long copyId) {
        copies.findById(copyId).orElseThrow().markAvailable();
    }
}
