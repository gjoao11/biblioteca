package biblioteca.backend.loan;

import biblioteca.backend.catalog.CatalogLoanApi;
import biblioteca.backend.catalog.LockedCopy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador que implementa {@link CatalogLoanPort} chamando a API pública do módulo catalog.
 * Registrado como bean real — substitui o stub {@code @ConditionalOnMissingBean} do {@link LoanConfig}.
 */
@Component
class CatalogModuleLoanAdapter implements CatalogLoanPort {

    private final CatalogLoanApi catalogLoanApi;

    CatalogModuleLoanAdapter(CatalogLoanApi catalogLoanApi) {
        this.catalogLoanApi = catalogLoanApi;
    }

    @Override
    public Optional<CopySnapshot> findCopyByIdForUpdate(Long copyId) {
        return catalogLoanApi.findCopyByIdForUpdate(copyId)
                .map(lc -> new CopySnapshot(lc.copyId(), lc.bookTitle(), lc.available()));
    }

    @Override
    public void markLoaned(Long copyId) {
        catalogLoanApi.markLoaned(copyId);
    }

    @Override
    public void markAvailable(Long copyId) {
        catalogLoanApi.markAvailable(copyId);
    }

    @Override
    public Map<Long, String> findBookTitles(Collection<Long> copyIds) {
        return catalogLoanApi.findBookTitles(copyIds);
    }
}
