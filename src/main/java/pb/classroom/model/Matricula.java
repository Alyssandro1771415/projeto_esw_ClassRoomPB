package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

public class Matricula {

  private final String id;
  private final String idAluno;
  private final String idTurma;
  private StatusMatricula status;

  public Matricula(String idAluno, String idTurma) {
    this(UUID.randomUUID().toString(), idAluno, idTurma, StatusMatricula.CONFIRMADA);
  }

  public Matricula(String id, String idAluno, String idTurma) {
    this(id, idAluno, idTurma, StatusMatricula.CONFIRMADA);
  }

  public Matricula(String idAluno, String idTurma, StatusMatricula status) {
    this(UUID.randomUUID().toString(), idAluno, idTurma, status);
  }

  public Matricula(String id, String idAluno, String idTurma, StatusMatricula status) {
    this.id = validarCampoObrigatorio(id, "id");
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    setStatus(status);
  }

  public String getId() {
    return id;
  }

  public String getIdAluno() {
    return idAluno;
  }

  public String getIdTurma() {
    return idTurma;
  }

  public StatusMatricula getStatus() {
    return status;
  }

  public void setStatus(StatusMatricula status) {
    if (status == null) {
      throw new IllegalArgumentException("status da matrícula é obrigatório");
    }
    this.status = status;
  }

  public boolean isConfirmada() {
    return status == StatusMatricula.CONFIRMADA;
  }

  public boolean isEmEspera() {
    return status == StatusMatricula.EM_ESPERA;
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
    Matricula matricula = (Matricula) o;
    return id.equals(matricula.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
