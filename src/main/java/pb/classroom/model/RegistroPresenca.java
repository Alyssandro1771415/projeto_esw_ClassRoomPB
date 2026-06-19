package pb.classroom.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Registro individual de presença ou falta de um aluno em uma turma em determinada data (RF27). */
public class RegistroPresenca {

  private final String id;
  private final String idTurma;
  private final String idAluno;
  private final LocalDate data;
  private StatusPresenca status;

  public RegistroPresenca(String idTurma, String idAluno, LocalDate data, StatusPresenca status) {
    this(UUID.randomUUID().toString(), idTurma, idAluno, data, status);
  }

  public RegistroPresenca(
      String id, String idTurma, String idAluno, LocalDate data, StatusPresenca status) {
    this.id = validarCampoObrigatorio(id, "id");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.data = Objects.requireNonNull(data, "data é obrigatória");
    setStatus(status);
  }

  public String getId() {
    return id;
  }

  public String getIdTurma() {
    return idTurma;
  }

  public String getIdAluno() {
    return idAluno;
  }

  public LocalDate getData() {
    return data;
  }

  public StatusPresenca getStatus() {
    return status;
  }

  public void setStatus(StatusPresenca status) {
    if (status == null) {
      throw new IllegalArgumentException("status da presença é obrigatório");
    }
    this.status = status;
  }

  public boolean isPresente() {
    return status == StatusPresenca.PRESENTE;
  }

  public boolean isFalta() {
    return status == StatusPresenca.FALTA;
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
    RegistroPresenca that = (RegistroPresenca) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
