package pb.classroom.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro permanente de disciplina cursada pelo aluno (RF36, RF37). Contém período, disciplina,
 * professor, nota final, frequência e situação.
 */
public class HistoricoAcademico {

  private final String id;
  private final String idAluno;
  private final String idDisciplina;
  private final String idPeriodoLetivo;
  private final String idProfessor;
  private final String idTurma;
  private final Double mediaFinal;
  private final double percentualFrequencia;
  private final SituacaoAcademica situacao;
  private final LocalDate dataRegistro;

  public HistoricoAcademico(
      String idAluno,
      String idDisciplina,
      String idPeriodoLetivo,
      String idProfessor,
      String idTurma,
      Double mediaFinal,
      double percentualFrequencia,
      SituacaoAcademica situacao) {
    this(
        UUID.randomUUID().toString(),
        idAluno,
        idDisciplina,
        idPeriodoLetivo,
        idProfessor,
        idTurma,
        mediaFinal,
        percentualFrequencia,
        situacao,
        LocalDate.now());
  }

  public HistoricoAcademico(
      String id,
      String idAluno,
      String idDisciplina,
      String idPeriodoLetivo,
      String idProfessor,
      String idTurma,
      Double mediaFinal,
      double percentualFrequencia,
      SituacaoAcademica situacao,
      LocalDate dataRegistro) {
    this.id = validarCampoObrigatorio(id, "id");
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.idDisciplina = validarCampoObrigatorio(idDisciplina, "id da disciplina");
    this.idPeriodoLetivo = validarCampoObrigatorio(idPeriodoLetivo, "id do período letivo");
    this.idProfessor = validarCampoObrigatorio(idProfessor, "id do professor");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    this.mediaFinal = mediaFinal;
    if (percentualFrequencia < 0.0 || percentualFrequencia > 100.0) {
      throw new IllegalArgumentException("percentual de frequência deve estar entre 0 e 100");
    }
    this.percentualFrequencia = percentualFrequencia;
    if (situacao == null) {
      throw new IllegalArgumentException("situação acadêmica é obrigatória");
    }
    this.situacao = situacao;
    this.dataRegistro = Objects.requireNonNull(dataRegistro, "data de registro é obrigatória");
  }

  public String getId() {
    return id;
  }

  public String getIdAluno() {
    return idAluno;
  }

  public String getIdDisciplina() {
    return idDisciplina;
  }

  public String getIdPeriodoLetivo() {
    return idPeriodoLetivo;
  }

  public String getIdProfessor() {
    return idProfessor;
  }

  public String getIdTurma() {
    return idTurma;
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

  public LocalDate getDataRegistro() {
    return dataRegistro;
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
    HistoricoAcademico that = (HistoricoAcademico) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
