package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FrequenciaDisciplinaAluno - Model")
class FrequenciaDisciplinaAlunoTest {

  @Test
  @DisplayName("RF29: calcula percentual agregado por disciplina")
  void calculaPercentualAgregadoPorDisciplina() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 4, 3);

    assertAll(
        () -> assertEquals("aluno-1", frequencia.getIdAluno()),
        () -> assertEquals("disc-1", frequencia.getIdDisciplina()),
        () -> assertEquals(4, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(3, frequencia.getTotalPresencas()),
        () -> assertEquals(1, frequencia.getTotalFaltas()),
        () -> assertEquals(75.0, frequencia.getPercentual(), 0.01));
  }

  @Test
  @DisplayName("percentual zero quando nao ha aulas registradas")
  void percentualZeroQuandoNaoHaAulas() {
    FrequenciaDisciplinaAluno frequencia =
        new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 0, 0);

    assertEquals(0.0, frequencia.getPercentual(), 0.01);
  }

  @Test
  @DisplayName("rejeita totais negativos")
  void rejeitaTotaisNegativos() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new FrequenciaDisciplinaAluno("aluno-1", "disc-1", -1, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 0, -1));
  }

  @Test
  @DisplayName("rejeita presencas maiores que total de aulas")
  void rejeitaPresencasMaioresQueTotal() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 2, 3));
  }

  @Test
  @DisplayName("rejeita ids obrigatorios vazios")
  void rejeitaIdsObrigatoriosVazios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new FrequenciaDisciplinaAluno(null, "disc-1", 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> new FrequenciaDisciplinaAluno("aluno-1", " ", 1, 1));
  }

  @Test
  @DisplayName("remove espacos dos ids")
  void removeEspacosDosIds() {
    FrequenciaDisciplinaAluno frequencia =
        new FrequenciaDisciplinaAluno("  aluno-1  ", "  disc-1  ", 2, 1);

    assertEquals("aluno-1", frequencia.getIdAluno());
    assertEquals("disc-1", frequencia.getIdDisciplina());
  }

  @Test
  @DisplayName("RF30: alerta quando percentual fica abaixo do minimo exigido")
  void alertaQuandoPercentualAbaixoDoMinimo() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 4, 2);

    assertAll(
        () -> assertTrue(frequencia.isAbaixoDoMinimoExigido()),
        () -> assertTrue(frequencia.getMensagemAlerta().contains("ALERTA")),
        () -> assertTrue(frequencia.getMensagemAlerta().contains("75,0")),
        () -> assertEquals(75.0, frequencia.getPercentualMinimoExigido(), 0.01));
  }

  @Test
  @DisplayName("RF30: nao alerta no limite minimo exato")
  void naoAlertaNoLimiteMinimoExato() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 4, 3);

    assertAll(
        () -> assertFalse(frequencia.isAbaixoDoMinimoExigido()),
        () -> assertEquals("", frequencia.getMensagemAlerta()));
  }

  @Test
  @DisplayName("RF30: nao alerta acima do minimo exigido")
  void naoAlertaAcimaDoMinimoExigido() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 5, 5);

    assertAll(
        () -> assertEquals(100.0, frequencia.getPercentual(), 0.01),
        () -> assertFalse(frequencia.isAbaixoDoMinimoExigido()),
        () -> assertEquals("", frequencia.getMensagemAlerta()));
  }

  @Test
  @DisplayName("RF30: nao alerta sem aulas registradas")
  void naoAlertaSemAulasRegistradas() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 0, 0);

    assertAll(
        () -> assertFalse(frequencia.isAbaixoDoMinimoExigido()),
        () -> assertEquals("", frequencia.getMensagemAlerta()));
  }

  @Test
  @DisplayName("igualdade por aluno e disciplina")
  void igualdadePorAlunoEDisciplina() {
    FrequenciaDisciplinaAluno primeira =
        new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 2, 1);
    FrequenciaDisciplinaAluno segunda =
        new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 5, 4);
    FrequenciaDisciplinaAluno diferente =
        new FrequenciaDisciplinaAluno("aluno-2", "disc-1", 2, 1);

    assertAll(
        () -> assertEquals(primeira, segunda),
        () -> assertEquals(primeira.hashCode(), segunda.hashCode()),
        () -> assertNotEquals(primeira, diferente),
        () -> assertNotEquals(primeira, null),
        () -> assertNotEquals(primeira, "outro tipo"));
  }
}
