package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

/** Notas de etapa1, etapa2 e recuperação de um aluno em uma turma (RF31, RF35). */
public class RegistroNota {

  private final String id;
  private final String idTurma;
  private final String idAluno;
  private Double notaEtapa1;
  private Double notaEtapa2;
  private Double notaRecuperacao;

  public RegistroNota(String idTurma, String idAluno) {
    this(UUID.randomUUID().toString(), idTurma, idAluno, null, null, null);
  }

  public RegistroNota(
      String id,
      String idTurma,
      String idAluno,
      Double notaEtapa1,
      Double notaEtapa2,
      Double notaRecuperacao) {
    this.id = validarCampoObrigatorio(id, "id");
    this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
    setNotaEtapa1(notaEtapa1);
    setNotaEtapa2(notaEtapa2);
    setNotaRecuperacao(notaRecuperacao);
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

  public Double getNotaEtapa1() {
    return notaEtapa1;
  }

  public void setNotaEtapa1(Double notaEtapa1) {
    this.notaEtapa1 = validarNotaOpcional(notaEtapa1);
  }

  public Double getNotaEtapa2() {
    return notaEtapa2;
  }

  public void setNotaEtapa2(Double notaEtapa2) {
    this.notaEtapa2 = validarNotaOpcional(notaEtapa2);
  }

  public Double getNotaRecuperacao() {
    return notaRecuperacao;
  }

  public void setNotaRecuperacao(Double notaRecuperacao) {
    this.notaRecuperacao = validarNotaOpcional(notaRecuperacao);
  }

  public void definirNota(EtapaAvaliacao etapa, double valor) {
    validarNota(valor);
    switch (etapa) {
      case ETAPA1:
        this.notaEtapa1 = valor;
        break;
      case ETAPA2:
        this.notaEtapa2 = valor;
        break;
      case RECUPERACAO:
        this.notaRecuperacao = valor;
        break;
      default:
        throw new IllegalArgumentException("Etapa de avaliação inválida.");
    }
  }

  public boolean possuiAmbasEtapas() {
    return notaEtapa1 != null && notaEtapa2 != null;
  }

  private static Double validarNotaOpcional(Double nota) {
    if (nota == null) {
      return null;
    }
    validarNota(nota);
    return nota;
  }

  private static void validarNota(double nota) {
    if (nota < 0.0 || nota > 10.0) {
      throw new IllegalArgumentException("Nota deve estar entre 0.0 e 10.0.");
    }
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
    RegistroNota that = (RegistroNota) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
