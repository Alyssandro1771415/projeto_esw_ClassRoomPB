package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultadoAvaliacao - cálculo de média e situação")
class ResultadoAvaliacaoTest {

  @Test
  @DisplayName("calcula média final como média das etapas")
  void calculaMediaFinalDasEtapas() {
    assertEquals(7.0, ResultadoAvaliacao.calcularMediaFinal(8.0, 6.0, null), 0.001);
  }

  @Test
  @DisplayName("recuperação substitui média quando maior")
  void recuperacaoSubstituiMediaQuandoMaior() {
    assertEquals(7.0, ResultadoAvaliacao.calcularMediaFinal(5.0, 5.0, 7.0), 0.001);
  }

  @Test
  @DisplayName("recuperação menor não reduz média das etapas")
  void recuperacaoMenorNaoReduzMedia() {
    assertEquals(6.0, ResultadoAvaliacao.calcularMediaFinal(5.0, 7.0, 4.0), 0.001);
  }

  @Test
  @DisplayName("reprovado por falta tem prioridade sobre nota")
  void reprovadoPorFaltaTemPrioridade() {
    SituacaoAcademica situacao = ResultadoAvaliacao.calcularSituacao(9.0, 9.0, 9.0, 50.0);
    assertEquals(SituacaoAcademica.REPROVADO_FALTA, situacao);
  }

  @Test
  @DisplayName("em andamento quando etapas incompletas")
  void emAndamentoQuandoEtapasIncompletas() {
    SituacaoAcademica situacao = ResultadoAvaliacao.calcularSituacao(null, null, 8.0, 100.0);
    assertEquals(SituacaoAcademica.EM_ANDAMENTO, situacao);
  }

  @Test
  @DisplayName("construtor calcula aprovado e expõe getters")
  void construtorCalculaAprovadoEExpoeGetters() {
    ResultadoAvaliacao resultado =
        new ResultadoAvaliacao("aluno-1", "turma-1", 8.0, 8.0, null, 90.0);

    assertAll(
        () -> assertEquals("aluno-1", resultado.getIdAluno()),
        () -> assertEquals("turma-1", resultado.getIdTurma()),
        () -> assertEquals(8.0, resultado.getNotaEtapa1()),
        () -> assertEquals(8.0, resultado.getNotaEtapa2()),
        () -> assertNull(resultado.getNotaRecuperacao()),
        () -> assertEquals(8.0, resultado.getMediaFinal(), 0.001),
        () -> assertEquals(90.0, resultado.getPercentualFrequencia(), 0.001),
        () -> assertEquals(SituacaoAcademica.APROVADO, resultado.getSituacao()));
  }

  @Test
  @DisplayName("situações de recuperação e reprovação por nota")
  void situacoesRecuperacaoEReprovacaoPorNota() {
    ResultadoAvaliacao recuperacao = new ResultadoAvaliacao("a1", "t1", 6.0, 5.0, null, 100.0);
    ResultadoAvaliacao reprovado = new ResultadoAvaliacao("a2", "t1", 3.0, 4.0, null, 100.0);

    assertEquals(SituacaoAcademica.EM_RECUPERACAO, recuperacao.getSituacao());
    assertEquals(SituacaoAcademica.REPROVADO_NOTA, reprovado.getSituacao());
  }

  @Test
  @DisplayName("equals e hashCode por aluno e turma")
  void equalsEHashCode() {
    ResultadoAvaliacao a = new ResultadoAvaliacao("a1", "t1", 7.0, 7.0, null, 80.0);
    ResultadoAvaliacao b = new ResultadoAvaliacao("a1", "t1", 5.0, 5.0, null, 90.0);
    ResultadoAvaliacao c = new ResultadoAvaliacao("a2", "t1", 7.0, 7.0, null, 80.0);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
  }
}

@DisplayName("HistoricoAcademico - modelo RF37")
class HistoricoAcademicoCoberturaTest {

  @Test
  @DisplayName("expõe todos os campos do RF37")
  void expoeCamposDoHistorico() {
    HistoricoAcademico historico =
        new HistoricoAcademico(
            "hist-1",
            "aluno-1",
            "disc-1",
            "periodo-1",
            "prof-1",
            "turma-1",
            7.5,
            85.0,
            SituacaoAcademica.APROVADO,
            LocalDate.of(2026, 6, 30));

    assertAll(
        () -> assertEquals("hist-1", historico.getId()),
        () -> assertEquals("aluno-1", historico.getIdAluno()),
        () -> assertEquals("disc-1", historico.getIdDisciplina()),
        () -> assertEquals("periodo-1", historico.getIdPeriodoLetivo()),
        () -> assertEquals("prof-1", historico.getIdProfessor()),
        () -> assertEquals("turma-1", historico.getIdTurma()),
        () -> assertEquals(7.5, historico.getMediaFinal()),
        () -> assertEquals(85.0, historico.getPercentualFrequencia()),
        () -> assertEquals(SituacaoAcademica.APROVADO, historico.getSituacao()),
        () -> assertEquals(LocalDate.of(2026, 6, 30), historico.getDataRegistro()));
  }

  @Test
  @DisplayName("equals por id")
  void equalsPorId() {
    HistoricoAcademico a =
        new HistoricoAcademico(
            "h1",
            "a1",
            "d1",
            "p1",
            "pr1",
            "t1",
            7.0,
            80.0,
            SituacaoAcademica.APROVADO,
            LocalDate.now());
    HistoricoAcademico b =
        new HistoricoAcademico(
            "h1",
            "a2",
            "d2",
            "p2",
            "pr2",
            "t2",
            3.0,
            50.0,
            SituacaoAcademica.REPROVADO_NOTA,
            LocalDate.now());
    assertEquals(a, b);
  }
}
