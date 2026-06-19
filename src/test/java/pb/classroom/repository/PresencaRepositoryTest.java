package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusPresenca;

@DisplayName("PresencaRepository")
class PresencaRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Salva e carrega registros de presença")
  void salvaECarregaPresencas() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    PresencaRepository repository = new PresencaRepository(arquivo);

    List<RegistroPresenca> presencas = new ArrayList<>();
    presencas.add(
        new RegistroPresenca(
            "reg-1", "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE));
    presencas.add(
        new RegistroPresenca(
            "reg-2", "turma-1", "aluno-2", LocalDate.of(2026, 3, 2), StatusPresenca.FALTA));

    repository.salvarPresencas(presencas);
    List<RegistroPresenca> carregadas = repository.carregarPresencas();

    assertAll(
        () -> assertEquals(2, carregadas.size()),
        () -> assertEquals("reg-1", carregadas.get(0).getId()),
        () -> assertEquals("turma-1", carregadas.get(0).getIdTurma()),
        () -> assertEquals("aluno-1", carregadas.get(0).getIdAluno()),
        () -> assertEquals(LocalDate.of(2026, 3, 2), carregadas.get(0).getData()),
        () -> assertEquals(StatusPresenca.PRESENTE, carregadas.get(0).getStatus()),
        () -> assertEquals("reg-2", carregadas.get(1).getId()),
        () -> assertEquals(StatusPresenca.FALTA, carregadas.get(1).getStatus()));
  }

  @Test
  @DisplayName("Preserva demais coleções ao salvar presenças")
  void preservaDemaisColecoes() throws IOException {
    Path arquivo = tempDir.resolve("armazenamento.json");
    String conteudoInicial =
        "{\n"
            + "  \"usuarios\": [{\"id\":\"u1\",\"perfil\":\"ALUNO\",\"nome\":\"Teste\","
            + "\"matricula\":\"0001\",\"email\":\"t@t.com\",\"senha\":\"s\",\"ativo\":true}],\n"
            + "  \"disciplinas\": [],\n"
            + "  \"cursos\": [],\n"
            + "  \"periodosLetivos\": [],\n"
            + "  \"turmas\": [],\n"
            + "  \"matriculas\": [],\n"
            + "  \"presencas\": []\n"
            + "}\n";
    Files.write(arquivo, conteudoInicial.getBytes(StandardCharsets.UTF_8));

    PresencaRepository repository = new PresencaRepository(arquivo);
    List<RegistroPresenca> presencas = new ArrayList<>();
    presencas.add(
        new RegistroPresenca(
            "reg-1", "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE));
    repository.salvarPresencas(presencas);

    String conteudoSalvo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(conteudoSalvo.contains("\"usuarios\"")),
        () -> assertTrue(conteudoSalvo.contains("\"u1\"")),
        () -> assertTrue(conteudoSalvo.contains("\"presencas\"")),
        () -> assertTrue(conteudoSalvo.contains("\"reg-1\"")));
  }

  @Test
  @DisplayName("Carregar de arquivo inexistente retorna lista vazia")
  void carregarDeArquivoInexistente() {
    Path arquivo = tempDir.resolve("inexistente.json");
    PresencaRepository repository = new PresencaRepository(arquivo);

    List<RegistroPresenca> presencas = repository.carregarPresencas();

    assertTrue(presencas.isEmpty());
  }
}
