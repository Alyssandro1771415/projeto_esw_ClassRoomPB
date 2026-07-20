package pb.classroom.model;

import java.util.Objects;

/** Resultado calculado de avaliação de um aluno em uma turma (RF32, RF34). */
public class ResultadoAvaliacao {

  public static final double MEDIA_APROVACAO = 7.0;
  public static final double MEDIA_MINIMA_RECUPERACAO = 5.0;

  private final String idAluno;
  private final String idTurma;
  private final Double notaEtapa1;
  private final Double notaEtapa2;
  private final Double notaRecuperacao;
  private final Double mediaFinal;
  private final double percentualFrequencia;
  private final SituacaoAcademica situacao;

  public ResultadoAvaliacao(
      String idAluno,
      String idTurma,
      Double notaEtapa1,
      Double notaEtapa2,
      Double notaRecuperacao,
      double percentualFrequencia) {
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    this.notaEtapa1 = notaEtapa1;
    this.notaEtapa2 = notaEtapa2;
    this.notaRecuperacao = notaRecuperacao;
    this.percentualFrequencia = percentualFrequencia;
    this.mediaFinal = calcularMediaFinal(notaEtapa1, notaEtapa2, notaRecuperacao);
    this.situacao = calcularSituacao(mediaFinal, notaEtapa1, notaEtapa2, percentualFrequencia);
  }

  public String getIdAluno() {
    return idAluno;
  }

  public String getIdTurma() {
    return idTurma;
  }

  public Double getNotaEtapa1() {
    return notaEtapa1;
  }

  public Double getNotaEtapa2() {
    return notaEtapa2;
  }

  public Double getNotaRecuperacao() {
    return notaRecuperacao;
  }

  public Double getMediaFinal() {
    return mediaFinal;
  }

  public double getPercentualFrequencia() {
    return percentualFrequencia;
  }

  public SituacaoAcademica getSituacao() {
    return situacao;
  }

  static Double calcularMediaFinal(Double notaEtapa1, Double notaEtapa2, Double notaRecuperacao) {
    if (notaEtapa1 == null || notaEtapa2 == null) {
      return null;
    }
    double mediaEtapas = (notaEtapa1 + notaEtapa2) / 2.0;
    if (notaRecuperacao == null) {
      return mediaEtapas;
    }
    return Math.max(mediaEtapas, notaRecuperacao);
  }

  static SituacaoAcademica calcularSituacao(
      Double mediaFinal, Double notaEtapa1, Double notaEtapa2, double percentualFrequencia) {
    if (percentualFrequencia > 0
        && percentualFrequencia < FrequenciaAluno.PERCENTUAL_MINIMO_EXIGIDO) {
      return SituacaoAcademica.REPROVADO_FALTA;
    }
    if (notaEtapa1 == null || notaEtapa2 == null || mediaFinal == null) {
      return SituacaoAcademica.EM_ANDAMENTO;
    }
    if (mediaFinal >= MEDIA_APROVACAO) {
      return SituacaoAcademica.APROVADO;
    }
    if (mediaFinal >= MEDIA_MINIMA_RECUPERACAO) {
      return SituacaoAcademica.EM_RECUPERACAO;
    }
    return SituacaoAcademica.REPROVADO_NOTA;
  }

  private static String validarCampoObrigatorio(String valor, String campo) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException(campo + " é obrigatório");
    }
    return valor.trim();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultadoAvaliacao that = (ResultadoAvaliacao) o;
    return idAluno.equals(that.idAluno) && idTurma.equals(that.idTurma);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idAluno, idTurma);
  }
}
