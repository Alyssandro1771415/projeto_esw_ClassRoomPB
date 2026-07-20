package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RegistroNota")
class RegistroNotaTest {

  @Test
  @DisplayName("cria registro e define notas por etapa")
  void criaRegistroEDefineNotas() {
    RegistroNota nota = new RegistroNota("turma-1", "aluno-1");
    nota.definirNota(EtapaAvaliacao.ETAPA1, 8.0);
    nota.definirNota(EtapaAvaliacao.ETAPA2, 7.0);
    nota.definirNota(EtapaAvaliacao.RECUPERACAO, 7.5);

    assertAll(
        () -> assertEquals("turma-1", nota.getIdTurma()),
        () -> assertEquals("aluno-1", nota.getIdAluno()),
        () -> assertEquals(8.0, nota.getNotaEtapa1()),
        () -> assertEquals(7.0, nota.getNotaEtapa2()),
        () -> assertEquals(7.5, nota.getNotaRecuperacao()),
        () -> assertTrue(nota.possuiAmbasEtapas()));
  }

  @Test
  @DisplayName("rejeita nota fora do intervalo")
  void rejeitaNotaForaDoIntervalo() {
    RegistroNota nota = new RegistroNota("turma-1", "aluno-1");
    assertThrows(
        IllegalArgumentException.class, () -> nota.definirNota(EtapaAvaliacao.ETAPA1, 11.0));
    assertThrows(
        IllegalArgumentException.class, () -> nota.definirNota(EtapaAvaliacao.ETAPA2, -1.0));
  }

  @Test
  @DisplayName("equals por id")
  void equalsPorId() {
    RegistroNota a = new RegistroNota("id-1", "turma-1", "aluno-1", 7.0, 8.0, null);
    RegistroNota b = new RegistroNota("id-1", "turma-2", "aluno-2", 5.0, 5.0, null);
    assertEquals(a, b);
  }
}

@DisplayName("HistoricoAcademico")
class HistoricoAcademicoTest {

  @Test
  @DisplayName("cria histórico com campos obrigatórios")
  void criaHistoricoComCamposObrigatorios() {
    HistoricoAcademico historico =
        new HistoricoAcademico(
            "aluno-1",
            "disc-1",
            "periodo-1",
            "prof-1",
            "turma-1",
            7.5,
            85.0,
            SituacaoAcademica.APROVADO);

    assertAll(
        () -> assertEquals("aluno-1", historico.getIdAluno()),
        () -> assertEquals("disc-1", historico.getIdDisciplina()),
        () -> assertEquals("periodo-1", historico.getIdPeriodoLetivo()),
        () -> assertEquals("prof-1", historico.getIdProfessor()),
        () -> assertEquals("turma-1", historico.getIdTurma()),
        () -> assertEquals(7.5, historico.getMediaFinal()),
        () -> assertEquals(85.0, historico.getPercentualFrequencia()),
        () -> assertEquals(SituacaoAcademica.APROVADO, historico.getSituacao()),
        () -> assertNotNull(historico.getDataRegistro()));
  }

  @Test
  @DisplayName("rejeita percentual de frequência inválido")
  void rejeitaPercentualInvalido() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new HistoricoAcademico(
                "hist-1",
                "aluno-1",
                "disc-1",
                "periodo-1",
                "prof-1",
                "turma-1",
                7.0,
                150.0,
                SituacaoAcademica.APROVADO,
                LocalDate.now()));
  }
}
