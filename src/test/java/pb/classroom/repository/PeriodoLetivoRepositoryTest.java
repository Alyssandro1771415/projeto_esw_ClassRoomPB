package pb.classroom.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.PeriodoLetivo;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PeriodoLetivoRepository - persistencia de periodos")
class PeriodoLetivoRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("salva e carrega periodo letivo com status ativo")
    void salvaECarregaPeriodo() {
        Path arquivo = tempDir.resolve("armazenamento.json");
        PeriodoLetivoRepository repository = new PeriodoLetivoRepository(arquivo);
        PeriodoLetivo periodo = new PeriodoLetivo("p1", "2026.2", true);

        repository.salvarPeriodosLetivos(List.of(periodo));

        List<PeriodoLetivo> carregados = repository.carregarPeriodosLetivos();
        assertEquals(1, carregados.size());
        assertTrue(carregados.get(0).isAtivo());
        assertEquals("2026.2", carregados.get(0).getCodigo());
    }

    @Test
    @DisplayName("retorna vazio quando nao ha arquivo")
    void retornaVazioSemArquivo() {
        PeriodoLetivoRepository repository =
                new PeriodoLetivoRepository(tempDir.resolve("inexistente.json"));
        assertTrue(repository.carregarPeriodosLetivos().isEmpty());
    }
}
