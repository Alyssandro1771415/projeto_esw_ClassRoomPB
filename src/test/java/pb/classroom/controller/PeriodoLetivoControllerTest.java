package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Usuario;

@DisplayName("PeriodoLetivoController - cadastro e status")
class PeriodoLetivoControllerTest {

  private static final String SENHA = "senha";

  private AutenticacaoController autenticacaoController;
  private PeriodoLetivoController periodoLetivoController;

  @BeforeEach
  void setUp() {
    autenticacaoController =
        new AutenticacaoController(
            List.of(
                new Usuario(
                    PerfilUsuario.ADMINISTRADOR,
                    "Admin Sistema",
                    "0001",
                    "admin@classroompb.com",
                    SENHA),
                new Usuario(
                    PerfilUsuario.COORDENADOR,
                    "Coord Curso",
                    "C001",
                    "coord@classroompb.com",
                    SENHA)));
    periodoLetivoController = new PeriodoLetivoController(autenticacaoController, List.of());
  }

  @Test
  @DisplayName("coordenador autenticado cadastra periodo letivo")
  void coordenadorAutenticadoCadastraPeriodoLetivo() {
    autenticacaoController.login("C001", SENHA);

    PeriodoLetivo periodoLetivo = periodoLetivoController.cadastrarPeriodoLetivo("2026.2");

    assertAll(
        () -> assertEquals("2026.2", periodoLetivo.getCodigo()),
        () -> assertFalse(periodoLetivo.isAtivo()),
        () -> assertTrue(periodoLetivoController.getPeriodosLetivos().contains(periodoLetivo)));
  }

  @Test
  @DisplayName("usuario sem perfil coordenador nao cadastra periodo letivo")
  void usuarioSemPerfilCoordenadorNaoCadastraPeriodoLetivo() {
    autenticacaoController.login("0001", SENHA);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> periodoLetivoController.cadastrarPeriodoLetivo("2026.2"));

    assertEquals("Apenas coordenadores podem gerenciar períodos letivos.", ex.getMessage());
    assertTrue(periodoLetivoController.getPeriodosLetivos().isEmpty());
  }

  @Test
  @DisplayName("ativa e encerra periodo letivo")
  void ativaEEncerraPeriodoLetivo() {
    autenticacaoController.login("C001", SENHA);
    PeriodoLetivo periodoLetivo = periodoLetivoController.cadastrarPeriodoLetivo("2026.2");

    periodoLetivoController.ativarPeriodoLetivo(periodoLetivo.getId());
    assertTrue(periodoLetivo.isAtivo());

    periodoLetivoController.encerrarPeriodoLetivo(periodoLetivo.getId());
    assertFalse(periodoLetivo.isAtivo());
  }

  @Test
  @DisplayName("nao permite periodo letivo duplicado")
  void naoPermitePeriodoLetivoDuplicado() {
    autenticacaoController.login("C001", SENHA);
    periodoLetivoController.cadastrarPeriodoLetivo("2026.2");

    assertThrows(
        IllegalArgumentException.class,
        () -> periodoLetivoController.cadastrarPeriodoLetivo("2026.2"));
  }

  @Test
  @DisplayName("valida formato do periodo letivo")
  void validaFormatoDoPeriodoLetivo() {
    autenticacaoController.login("C001", SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () -> periodoLetivoController.cadastrarPeriodoLetivo("2026/2"));
  }
}
