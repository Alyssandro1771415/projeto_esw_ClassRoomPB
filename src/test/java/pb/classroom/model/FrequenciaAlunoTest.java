package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FrequenciaAluno - Model")
class FrequenciaAlunoTest {

  @Test
  @DisplayName("calcula percentual corretamente")
  void calculaPercentualCorretamente() {
    FrequenciaAluno frequencia = new FrequenciaAluno("aluno-1", "turma-1", 4, 3);

    assertAll(
        () -> assertEquals("aluno-1", frequencia.getIdAluno()),
        () -> assertEquals("turma-1", frequencia.getIdTurma()),
        () -> assertEquals(4, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(3, frequencia.getTotalPresencas()),
        () -> assertEquals(1, frequencia.getTotalFaltas()),
        () -> assertEquals(75.0, frequencia.getPercentual(), 0.01));
  }

  @Test
  @DisplayName("percentual zero quando não há aulas registradas")
  void percentualZeroQuandoNaoHaAulas() {
    FrequenciaAluno frequencia = new FrequenciaAluno("aluno-1", "turma-1", 0, 0);

    assertEquals(0.0, frequencia.getPercentual(), 0.01);
  }

  @Test
  @DisplayName("rejeita totais negativos")
  void rejeitaTotaisNegativos() {
    assertThrows(
        IllegalArgumentException.class, () -> new FrequenciaAluno("aluno-1", "turma-1", -1, 0));
  }

  @Test
  @DisplayName("rejeita presenças maiores que total de aulas")
  void rejeitaPresencasMaioresQueTotal() {
    assertThrows(
        IllegalArgumentException.class, () -> new FrequenciaAluno("aluno-1", "turma-1", 2, 3));
  }

  @Test
  @DisplayName("igualdade por aluno e turma")
  void igualdadePorAlunoETurma() {
    FrequenciaAluno primeira = new FrequenciaAluno("aluno-1", "turma-1", 2, 1);
    FrequenciaAluno segunda = new FrequenciaAluno("aluno-1", "turma-1", 5, 4);

    assertEquals(primeira, segunda);
    assertEquals(primeira.hashCode(), segunda.hashCode());
  }
}
