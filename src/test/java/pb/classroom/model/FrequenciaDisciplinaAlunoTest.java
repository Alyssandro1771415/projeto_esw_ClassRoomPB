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
  @DisplayName("RF30: alerta quando percentual fica abaixo do minimo exigido")
  void alertaQuandoPercentualAbaixoDoMinimo() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 4, 2);

    assertAll(
        () -> assertTrue(frequencia.isAbaixoDoMinimoExigido()),
        () -> assertTrue(frequencia.getMensagemAlerta().contains("ALERTA")),
        () -> assertEquals(75.0, frequencia.getPercentualMinimoExigido(), 0.01));
  }

  @Test
  @DisplayName("RF30: nao alerta sem aulas registradas")
  void naoAlertaSemAulasRegistradas() {
    FrequenciaDisciplinaAluno frequencia = new FrequenciaDisciplinaAluno("aluno-1", "disc-1", 0, 0);

    assertFalse(frequencia.isAbaixoDoMinimoExigido());
    assertEquals("", frequencia.getMensagemAlerta());
  }
}
