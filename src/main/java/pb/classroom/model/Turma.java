package pb.classroom.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Turma ofertada para uma disciplina em um período letivo (RF10, RF11). Professor responsável,
 * vagas, horários, sala; cancelamento e início para RF14.
 */
public class Turma {

  private final String id;
  private String idDisciplina;
  private String idPeriodoLetivo;
  private String idProfessor;
  private int limiteVagas;
  private String sala;
  private LocalDate dataInicioAulas;
  private final List<BlocoHorario> horarios;
  private boolean cancelada;

  public Turma(
      String idDisciplina,
      String idPeriodoLetivo,
      String idProfessor,
      int limiteVagas,
      String sala,
      LocalDate dataInicioAulas,
      List<BlocoHorario> horarios) {
    this(
        UUID.randomUUID().toString(),
        idDisciplina,
        idPeriodoLetivo,
        idProfessor,
        limiteVagas,
        sala,
        dataInicioAulas,
        horarios,
        false);
  }

  public Turma(
      String id,
      String idDisciplina,
      String idPeriodoLetivo,
      String idProfessor,
      int limiteVagas,
      String sala,
      LocalDate dataInicioAulas,
      List<BlocoHorario> horarios,
      boolean cancelada) {
    this.id = Objects.requireNonNull(id, "id").trim();
    if (this.id.isEmpty()) {
      throw new IllegalArgumentException("id não pode ser vazio");
    }
    setIdDisciplina(idDisciplina);
    setIdPeriodoLetivo(idPeriodoLetivo);
    setIdProfessor(idProfessor);
    setLimiteVagas(limiteVagas);
    setSala(sala);
    setDataInicioAulas(dataInicioAulas);
    this.horarios = new ArrayList<>(validarHorarios(horarios));
    this.cancelada = cancelada;
  }

  public String getId() {
    return id;
  }

  public String getIdDisciplina() {
    return idDisciplina;
  }

  public void setIdDisciplina(String idDisciplina) {
    if (idDisciplina == null || idDisciplina.trim().isEmpty()) {
      throw new IllegalArgumentException("id da disciplina é obrigatório");
    }
    this.idDisciplina = idDisciplina.trim();
  }

  public String getIdPeriodoLetivo() {
    return idPeriodoLetivo;
  }

  public void setIdPeriodoLetivo(String idPeriodoLetivo) {
    if (idPeriodoLetivo == null || idPeriodoLetivo.trim().isEmpty()) {
      throw new IllegalArgumentException("id do período letivo é obrigatório");
    }
    this.idPeriodoLetivo = idPeriodoLetivo.trim();
  }

  /** Id do {@link Usuario} com perfil professor (RF11). */
  public String getIdProfessor() {
    return idProfessor;
  }

  public void setIdProfessor(String idProfessor) {
    if (idProfessor == null || idProfessor.trim().isEmpty()) {
      throw new IllegalArgumentException("id do professor responsável é obrigatório");
    }
    this.idProfessor = idProfessor.trim();
  }

  public int getLimiteVagas() {
    return limiteVagas;
  }

  public void setLimiteVagas(int limiteVagas) {
    if (limiteVagas <= 0) {
      throw new IllegalArgumentException("limite de vagas deve ser positivo (RN03)");
    }
    this.limiteVagas = limiteVagas;
  }

  public String getSala() {
    return sala;
  }

  public void setSala(String sala) {
    if (sala == null || sala.trim().isEmpty()) {
      throw new IllegalArgumentException("sala é obrigatória");
    }
    this.sala = sala.trim();
  }

  public LocalDate getDataInicioAulas() {
    return dataInicioAulas;
  }

  public void setDataInicioAulas(LocalDate dataInicioAulas) {
    this.dataInicioAulas = Objects.requireNonNull(dataInicioAulas, "dataInicioAulas");
  }

  public List<BlocoHorario> getHorarios() {
    return Collections.unmodifiableList(horarios);
  }

  public void setHorarios(List<BlocoHorario> novos) {
    horarios.clear();
    horarios.addAll(validarHorarios(novos));
  }

  public boolean isCancelada() {
    return cancelada;
  }

  public void setCancelada(boolean cancelada) {
    this.cancelada = cancelada;
  }

  private static List<BlocoHorario> validarHorarios(List<BlocoHorario> lista) {
    if (lista == null || lista.isEmpty()) {
      throw new IllegalArgumentException("a turma deve ter ao menos um bloco de horário");
    }
    return new ArrayList<>(lista);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Turma turma = (Turma) o;
    return id.equals(turma.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
