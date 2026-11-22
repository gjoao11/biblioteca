package biblioteca.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {
    @Test
    void verificaFronteirasDosModulos() {
        ApplicationModules.of(BackendApplication.class).verify();
    }
}
