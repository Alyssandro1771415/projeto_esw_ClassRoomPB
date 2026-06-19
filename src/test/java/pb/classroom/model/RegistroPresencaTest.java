package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RegistroPresenca - Model")
class RegistroPresencaTest {

  @Test
  @DisplayName("Criação com todos os campos válidos")
  void criacaoComCamposValidos() {
    RegistroPresenca registro =
        new RegistroPresenca(
            "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE);

    assertAll(
        () -> assertNotNull(registro.getId()),
        () -> assertFalse(registro.getId().isEmpty()),
        () -> assertEquals("turma-1", registro.getIdTurma()),
        () -> assertEquals("aluno-1", registro.getIdAluno()),
        () -> assertEquals(LocalDate.of(2026, 3, 2), registro.getData()),
        () -> assertEquals(StatusPresenca.PRESENTE, registro.getStatus()),
        () -> assertTrue(registro.isPresente()),
        () -> assertFalse(registro.isFalta()));
  }

  @Test
  @DisplayName("Criação com status FALTA")
  void criacaoComStatusFalta() {
    RegistroPresenca registro =
        new RegistroPresenca("turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.FALTA);

    assertAll(
        () -> assertEquals(StatusPresenca.FALTA, registro.getStatus()),
        () -> assertFalse(registro.isPresente()),
        () -> assertTrue(registro.isFalta()));
  }

  @Test
  @DisplayName("idTurma obrigatório")
  void idTurmaObrigatorio() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RegistroPresenca(
                null, "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE));
  }

  @Test
  @DisplayName("idTurma não pode ser vazio")
  void idTurmaNaoPodeSerVazio() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RegistroPresenca(
                "  ", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE));
  }

  @Test
  @DisplayName("idAluno obrigatório")
  void idAlunoObrigatorio() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RegistroPresenca(
                "turma-1", null, LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE));
  }

  @Test
  @DisplayName("data obrigatória")
  void dataObrigatoria() {
    assertThrows(
        NullPointerException.class,
        () -> new RegistroPresenca("turma-1", "aluno-1", null, StatusPresenca.PRESENTE));
  }

  @Test
  @DisplayName("status obrigatório")
  void statusObrigatorio() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new RegistroPresenca("turma-1", "aluno-1", LocalDate.of(2026, 3, 2), null));
  }

  @Test
  @DisplayName("Igualdade por id")
  void igualdadePorId() {
    RegistroPresenca reg1 =
        new RegistroPresenca(
            "reg-1", "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE);
    RegistroPresenca reg2 =
        new RegistroPresenca(
            "reg-1", "turma-2", "aluno-2", LocalDate.of(2026, 4, 1), StatusPresenca.FALTA);
    RegistroPresenca reg3 =
        new RegistroPresenca(
            "reg-2", "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE);

    assertAll(
        () -> assertEquals(reg1, reg2),
        () -> assertNotEquals(reg1, reg3),
        () -> assertEquals(reg1.hashCode(), reg2.hashCode()));
  }

  @Test
  @DisplayName("Construtor com id explícito preserva o id")
  void construtorComIdExplicito() {
    RegistroPresenca registro =
        new RegistroPresenca(
            "meu-id", "turma-1", "aluno-1", LocalDate.of(2026, 3, 2), StatusPresenca.PRESENTE);

    assertEquals("meu-id", registro.getId());
  }
}
