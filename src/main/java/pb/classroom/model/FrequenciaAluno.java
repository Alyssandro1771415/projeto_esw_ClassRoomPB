package pb.classroom.model;

import java.util.Objects;

/** Percentual de frequência de um aluno em uma turma (RF28). */
public class FrequenciaAluno {

  private final String idAluno;
  private final String idTurma;
  private final int totalAulasRegistradas;
  private final int totalPresencas;
  private final double percentual;

  public FrequenciaAluno(
      String idAluno, String idTurma, int totalAulasRegistradas, int totalPresencas) {
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    if (totalAulasRegistradas < 0 || totalPresencas < 0) {
      throw new IllegalArgumentException("totais de aulas e presenças não podem ser negativos");
    }
    if (totalPresencas > totalAulasRegistradas) {
      throw new IllegalArgumentException(
          "presenças não podem exceder o total de aulas registradas");
    }
    this.totalAulasRegistradas = totalAulasRegistradas;
    this.totalPresencas = totalPresencas;
    this.percentual =
        totalAulasRegistradas == 0 ? 0.0 : (totalPresencas * 100.0) / totalAulasRegistradas;
  }

  public String getIdAluno() {
    return idAluno;
  }

  public String getIdTurma() {
    return idTurma;
  }

  public int getTotalAulasRegistradas() {
    return totalAulasRegistradas;
  }

  public int getTotalPresencas() {
    return totalPresencas;
  }

  public int getTotalFaltas() {
    return totalAulasRegistradas - totalPresencas;
  }

  public double getPercentual() {
    return percentual;
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
    FrequenciaAluno that = (FrequenciaAluno) o;
    return idAluno.equals(that.idAluno) && idTurma.equals(that.idTurma);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idAluno, idTurma);
  }
}
