package pb.classroom.model;

import java.util.Objects;

/** Percentual de frequencia agregado de um aluno em uma disciplina (RF29, RF30). */
public class FrequenciaDisciplinaAluno {

  public static final double PERCENTUAL_MINIMO_EXIGIDO = 75.0;

  private final String idAluno;
  private final String idDisciplina;
  private final int totalAulasRegistradas;
  private final int totalPresencas;
  private final double percentual;

  public FrequenciaDisciplinaAluno(
      String idAluno, String idDisciplina, int totalAulasRegistradas, int totalPresencas) {
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.idDisciplina = validarCampoObrigatorio(idDisciplina, "id da disciplina");
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

  public String getIdDisciplina() {
    return idDisciplina;
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

  public double getPercentualMinimoExigido() {
    return PERCENTUAL_MINIMO_EXIGIDO;
  }

  public boolean isAbaixoDoMinimoExigido() {
    return totalAulasRegistradas > 0 && percentual < PERCENTUAL_MINIMO_EXIGIDO;
  }

  public String getMensagemAlerta() {
    if (!isAbaixoDoMinimoExigido()) {
      return "";
    }
    return "ALERTA: frequência abaixo do mínimo exigido de "
        + String.format("%.1f", PERCENTUAL_MINIMO_EXIGIDO)
        + "%";
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
    FrequenciaDisciplinaAluno that = (FrequenciaDisciplinaAluno) o;
    return idAluno.equals(that.idAluno) && idDisciplina.equals(that.idDisciplina);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idAluno, idDisciplina);
  }
}
