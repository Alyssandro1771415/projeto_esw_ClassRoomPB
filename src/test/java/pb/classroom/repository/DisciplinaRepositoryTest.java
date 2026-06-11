package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.Disciplina;

@DisplayName("DisciplinaRepository - persistencia de disciplinas")
class DisciplinaRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("salva e carrega disciplina com pre-requisitos")
  void salvaECarregaDisciplina() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    DisciplinaRepository repository = new DisciplinaRepository(arquivo);
    Disciplina disciplina =
        new Disciplina("d1", "MAT01", "Calculo I", 60, 4, "c1", List.of("pre-1"));

    repository.salvarDisciplinas(List.of(disciplina));

    List<Disciplina> carregadas = repository.carregarDisciplinas();
    assertEquals(1, carregadas.size());
    assertEquals(List.of("pre-1"), carregadas.get(0).getPreRequisitosIds());
    assertEquals("MAT01", carregadas.get(0).getCodigo());
  }

  @Test
  @DisplayName("carrega arquivo inexistente como lista vazia")
  void carregaVazioSemArquivo() {
    DisciplinaRepository repository = new DisciplinaRepository(tempDir.resolve("novo.json"));
    assertTrue(repository.carregarDisciplinas().isEmpty());
  }
}
