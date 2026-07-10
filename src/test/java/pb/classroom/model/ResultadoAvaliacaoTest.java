package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

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
}
