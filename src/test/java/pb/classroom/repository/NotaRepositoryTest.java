package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.RegistroNota;

@DisplayName("NotaRepository")
class NotaRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Salva e carrega registros de nota")
  void salvaECarregaNotas() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    NotaRepository repository = new NotaRepository(arquivo);

    List<RegistroNota> notas = new ArrayList<>();
    notas.add(new RegistroNota("nota-1", "turma-1", "aluno-1", 8.0, 7.0, null));
    notas.add(new RegistroNota("nota-2", "turma-1", "aluno-2", 5.0, 5.5, 6.0));

    repository.salvarNotas(notas);
    List<RegistroNota> carregadas = repository.carregarNotas();

    assertAll(
        () -> assertEquals(2, carregadas.size()),
        () -> assertEquals("nota-1", carregadas.get(0).getId()),
        () -> assertEquals("turma-1", carregadas.get(0).getIdTurma()),
        () -> assertEquals("aluno-1", carregadas.get(0).getIdAluno()),
        () -> assertEquals(8.0, carregadas.get(0).getNotaEtapa1()),
        () -> assertEquals(7.0, carregadas.get(0).getNotaEtapa2()),
        () -> assertNull(carregadas.get(0).getNotaRecuperacao()),
        () -> assertEquals("nota-2", carregadas.get(1).getId()),
        () -> assertEquals(6.0, carregadas.get(1).getNotaRecuperacao()));
  }

  @Test
  @DisplayName("Preserva demais coleções ao salvar notas")
  void preservaDemaisColecoes() throws IOException {
    Path arquivo = tempDir.resolve("armazenamento.json");
    String conteudoInicial =
        "{\n"
            + "  \"usuarios\": [],\n"
            + "  \"disciplinas\": [],\n"
            + "  \"cursos\": [],\n"
            + "  \"periodosLetivos\": [],\n"
            + "  \"turmas\": [],\n"
            + "  \"matriculas\": [],\n"
            + "  \"presencas\": [],\n"
            + "  \"notas\": [],\n"
            + "  \"historicos\": [{\"id\":\"h1\",\"idAluno\":\"a1\",\"idDisciplina\":\"d1\","
            + "\"idPeriodoLetivo\":\"p1\",\"idProfessor\":\"pr1\",\"idTurma\":\"t1\","
            + "\"mediaFinal\":7.0,\"percentualFrequencia\":80.0,"
            + "\"situacao\":\"APROVADO\",\"dataRegistro\":\"2026-07-01\"}]\n"
            + "}\n";
    Files.write(arquivo, conteudoInicial.getBytes(StandardCharsets.UTF_8));

    NotaRepository repository = new NotaRepository(arquivo);
    List<RegistroNota> notas = new ArrayList<>();
    notas.add(new RegistroNota("nota-1", "turma-1", "aluno-1", 9.0, 8.0, null));
    repository.salvarNotas(notas);

    String conteudoSalvo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(conteudoSalvo.contains("\"historicos\"")),
        () -> assertTrue(conteudoSalvo.contains("\"h1\"")),
        () -> assertTrue(conteudoSalvo.contains("\"notas\"")),
        () -> assertTrue(conteudoSalvo.contains("\"nota-1\"")));
  }

  @Test
  @DisplayName("Carregar de arquivo inexistente retorna lista vazia")
  void carregarDeArquivoInexistente() {
    Path arquivo = tempDir.resolve("inexistente.json");
    NotaRepository repository = new NotaRepository(arquivo);

    assertTrue(repository.carregarNotas().isEmpty());
  }
}
