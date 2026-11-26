package biblioteca.backend.loan;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Porta de saída do módulo loan para acessar dados de alunos.
 * Implementada por um adaptador em loan que chama a API pública de student.
 * Em testes, substituída por um dublê (fake ou mock).
 */
interface StudentLoanPort {

    /**
     * Busca o aluno pelo ID com lock pessimista (FOR UPDATE) e retorna um
     * snapshot com as informações necessárias para validar o empréstimo.
     * Retorna {@link Optional#empty()} se o aluno não existir.
     */
    Optional<StudentSnapshot> findByIdForUpdate(Long studentId);

    /**
     * Busca os nomes dos alunos para um conjunto de IDs.
     * Usado para evitar N+1 na listagem de empréstimos.
     */
    Map<Long, String> findStudentNames(Collection<Long> studentIds);

    record StudentSnapshot(Long id, String name, Status status, int maxLoans) {
        enum Status {
            PENDING, APPROVED, REJECTED
        }
    }
}
