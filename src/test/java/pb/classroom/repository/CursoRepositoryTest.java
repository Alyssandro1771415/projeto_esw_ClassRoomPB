package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.Curso;

@DisplayName("CursoRepository - persistencia de cursos")
class CursoRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("salva e carrega cursos com codigo opcional")
  void salvaECarregaCursos() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    CursoRepository repository = new CursoRepository(arquivo);
    Curso curso = new Curso("c1", "Ciencia da Computacao", "CC");

    repository.salvarCursos(List.of(curso));

    List<Curso> carregados = repository.carregarCursos();
    assertEquals(1, carregados.size());
    assertEquals("CC", carregados.get(0).getCodigo());
    assertEquals("Ciencia da Computacao", carregados.get(0).getNome());
  }

  @Test
  @DisplayName("retorna lista vazia quando arquivo nao existe")
  void retornaVazioSemArquivo() {
    CursoRepository repository = new CursoRepository(tempDir.resolve("inexistente.json"));
    assertTrue(repository.carregarCursos().isEmpty());
  }

  @Test
  @DisplayName("salvar cursos preserva demais colecoes do armazenamento")
  void preservaOutrasColecoes() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    Files.writeString(
        arquivo,
        "{\"usuarios\":[{\"id\":\"u1\"}],\"disciplinas\":[],\"periodosLetivos\":[],\"turmas\":[]}",
        StandardCharsets.UTF_8);
    new CursoRepository(arquivo).salvarCursos(List.of(new Curso("c1", "Curso A", null)));
    String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
    assertTrue(conteudo.contains("\"usuarios\""));
    assertTrue(conteudo.contains("\"cursos\""));
  }
}
