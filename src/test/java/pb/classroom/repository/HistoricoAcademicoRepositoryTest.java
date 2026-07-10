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
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.SituacaoAcademica;

@DisplayName("HistoricoAcademicoRepository")
class HistoricoAcademicoRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Salva e carrega histórico acadêmico")
  void salvaECarregaHistoricos() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    HistoricoAcademicoRepository repository = new HistoricoAcademicoRepository(arquivo);

    List<HistoricoAcademico> historicos = new ArrayList<>();
    historicos.add(
        new HistoricoAcademico(
            "hist-1",
            "aluno-1",
            "disc-1",
            "periodo-1",
            "prof-1",
            "turma-1",
            8.5,
            90.0,
            SituacaoAcademica.APROVADO,
            LocalDate.of(2026, 7, 1)));
    historicos.add(
        new HistoricoAcademico(
            "hist-2",
            "aluno-2",
            "disc-1",
            "periodo-1",
            "prof-1",
            "turma-1",
            null,
            60.0,
            SituacaoAcademica.REPROVADO_FALTA,
            LocalDate.of(2026, 7, 2)));

    repository.salvarHistoricos(historicos);
    List<HistoricoAcademico> carregados = repository.carregarHistoricos();

    assertAll(
        () -> assertEquals(2, carregados.size()),
        () -> assertEquals("hist-1", carregados.get(0).getId()),
        () -> assertEquals("aluno-1", carregados.get(0).getIdAluno()),
        () -> assertEquals(8.5, carregados.get(0).getMediaFinal()),
        () -> assertEquals(SituacaoAcademica.APROVADO, carregados.get(0).getSituacao()),
        () -> assertEquals("hist-2", carregados.get(1).getId()),
        () -> assertNull(carregados.get(1).getMediaFinal()),
        () -> assertEquals(SituacaoAcademica.REPROVADO_FALTA, carregados.get(1).getSituacao()));
  }

  @Test
  @DisplayName("Preserva demais coleções ao salvar histórico")
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
            + "  \"notas\": [{\"id\":\"n1\",\"idTurma\":\"t1\",\"idAluno\":\"a1\","
            + "\"notaEtapa1\":7.0,\"notaEtapa2\":null,\"notaRecuperacao\":null}],\n"
            + "  \"historicos\": []\n"
            + "}\n";
    Files.write(arquivo, conteudoInicial.getBytes(StandardCharsets.UTF_8));

    HistoricoAcademicoRepository repository = new HistoricoAcademicoRepository(arquivo);
    List<HistoricoAcademico> historicos = new ArrayList<>();
    historicos.add(
        new HistoricoAcademico(
            "hist-1",
            "aluno-1",
            "disc-1",
            "periodo-1",
            "prof-1",
            "turma-1",
            7.0,
            80.0,
            SituacaoAcademica.APROVADO,
            LocalDate.of(2026, 7, 1)));
    repository.salvarHistoricos(historicos);

    String conteudoSalvo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(conteudoSalvo.contains("\"notas\"")),
        () -> assertTrue(conteudoSalvo.contains("\"n1\"")),
        () -> assertTrue(conteudoSalvo.contains("\"historicos\"")),
        () -> assertTrue(conteudoSalvo.contains("\"hist-1\"")));
  }

  @Test
  @DisplayName("Carregar de arquivo inexistente retorna lista vazia")
  void carregarDeArquivoInexistente() {
    Path arquivo = tempDir.resolve("inexistente.json");
    HistoricoAcademicoRepository repository = new HistoricoAcademicoRepository(arquivo);

    assertTrue(repository.carregarHistoricos().isEmpty());
  }
}
