package pb.classroom.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/** Um intervalo de aula em um dia da semana (RF11: horário da turma; base para RN06). */
public final class BlocoHorario {

  private final DayOfWeek diaSemana;
  private final LocalTime horaInicio;
  private final LocalTime horaFim;

  public BlocoHorario(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
    this.diaSemana = Objects.requireNonNull(diaSemana, "diaSemana");
    this.horaInicio = Objects.requireNonNull(horaInicio, "horaInicio");
    this.horaFim = Objects.requireNonNull(horaFim, "horaFim");
    if (!horaFim.isAfter(horaInicio)) {
      throw new IllegalArgumentException("hora fim deve ser depois da hora início");
    }
  }

  public DayOfWeek getDiaSemana() {
    return diaSemana;
  }

  public LocalTime getHoraInicio() {
    return horaInicio;
  }

  public LocalTime getHoraFim() {
    return horaFim;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlocoHorario that = (BlocoHorario) o;
    return diaSemana == that.diaSemana
        && horaInicio.equals(that.horaInicio)
        && horaFim.equals(that.horaFim);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diaSemana, horaInicio, horaFim);
  }
}
