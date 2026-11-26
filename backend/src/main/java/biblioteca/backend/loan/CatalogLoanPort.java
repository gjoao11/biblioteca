package biblioteca.backend.loan;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Porta de saída do módulo loan para acessar dados de exemplares (copies).
 * Implementada por um adaptador em loan que chama a API pública de catalog.
 * Em testes, substituída por um dublê (fake ou mock).
 */
interface CatalogLoanPort {

    /**
     * Busca o exemplar pelo ID com lock pessimista (FOR UPDATE) e retorna um
     * snapshot com as informações necessárias para validar o empréstimo.
     * Retorna {@link Optional#empty()} se o exemplar não existir.
     */
    Optional<CopySnapshot> findCopyByIdForUpdate(Long copyId);

    /** Muda o status do exemplar para LOANED. */
    void markLoaned(Long copyId);

    /** Muda o status do exemplar para AVAILABLE. */
    void markAvailable(Long copyId);

    /**
     * Busca os títulos dos livros para um conjunto de IDs de exemplares.
     * Usado para evitar N+1 na listagem de empréstimos.
     */
    Map<Long, String> findBookTitles(Collection<Long> copyIds);

    record CopySnapshot(Long copyId, String bookTitle, boolean available) {
    }
}
