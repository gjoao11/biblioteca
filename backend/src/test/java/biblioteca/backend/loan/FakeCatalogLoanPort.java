package biblioteca.backend.loan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dublê em memória de {@link CatalogLoanPort} para testes unitários do LoanService.
 */
class FakeCatalogLoanPort implements CatalogLoanPort {

    record CopyData(Long copyId, String bookTitle, boolean available) {
    }

    private final Map<Long, CopyData> store = new HashMap<>();

    void put(CopyData copy) {
        store.put(copy.copyId(), copy);
    }

    @Override
    public Optional<CopySnapshot> findCopyByIdForUpdate(Long copyId) {
        return Optional.ofNullable(store.get(copyId))
                .map(c -> new CopySnapshot(c.copyId(), c.bookTitle(), c.available()));
    }

    @Override
    public void markLoaned(Long copyId) {
        store.computeIfPresent(copyId, (id, c) -> new CopyData(id, c.bookTitle(), false));
    }

    @Override
    public void markAvailable(Long copyId) {
        store.computeIfPresent(copyId, (id, c) -> new CopyData(id, c.bookTitle(), true));
    }

    boolean isLoaned(Long copyId) {
        return store.containsKey(copyId) && !store.get(copyId).available();
    }
}
