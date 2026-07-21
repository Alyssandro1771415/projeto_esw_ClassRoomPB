package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pb.classroom.model.*;

public class RelatorioControllerTest {

  private AutenticacaoController authController;
  private RelatorioController relatorioController;
  private List<Turma> turmas;
  private List<Matricula> matriculas;
  private List<Usuario> usuarios;
  private List<Disciplina> disciplinas;
  private List<HistoricoAcademico> historicos;

  private Usuario coordenador;
  private Usuario aluno1;
  private Usuario aluno2;
  private Turma turma;
  private Usuario administrador;

  @BeforeEach
  public void setUp() {
    usuarios = new ArrayList<>();
    turmas = new ArrayList<>();
    matriculas = new ArrayList<>();
    disciplinas = new ArrayList<>();
    historicos = new ArrayList<>();

    coordenador =
        new Usuario(PerfilUsuario.COORDENADOR, "Coord Geral", "1001", "coord@pb.com", "senha123");
    aluno1 = new Usuario(PerfilUsuario.ALUNO, "José Silva", "2001", "jose@pb.com", "senha123");
    aluno2 = new Usuario(PerfilUsuario.ALUNO, "Maria Santos", "2002", "maria@pb.com", "senha123");
    administrador =
        new Usuario(
            PerfilUsuario.ADMINISTRADOR, "Admin Central", "9001", "admin@pb.com", "senhaAdmin");

    usuarios.add(coordenador);
    usuarios.add(aluno1);
    usuarios.add(aluno2);
    usuarios.add(administrador);

    List<BlocoHorario> horariosFicticios = new ArrayList<>();
    horariosFicticios.add(
        new BlocoHorario(
            java.time.DayOfWeek.MONDAY,
            java.time.LocalTime.of(8, 0),
            java.time.LocalTime.of(10, 0)));

    turma =
        new Turma(
            "disc-101",
            "2026.2",
            "prof-55",
            30,
            "Sala 402",
            LocalDate.now().plusDays(10),
            horariosFicticios);
    turmas.add(turma);

    authController = new AutenticacaoController(usuarios);
    relatorioController =
        new RelatorioController(
            authController, turmas, matriculas, usuarios, disciplinas, historicos);
  }

  @Test
  public void deveGerarRelatorioComSucessoQuandoCoordenadorSolicitar() {
    authController.login("coord@pb.com", "senha123");

    Matricula mat1 = new Matricula(aluno1.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula mat2 = new Matricula(aluno2.getId(), turma.getId(), StatusMatricula.EM_ESPERA);

    matriculas.add(mat1);
    matriculas.add(mat2);

    List<Usuario> resultado = relatorioController.gerarRelatorioAlunosPorTurma(turma.getId());

    assertEquals(
        1, resultado.size(), "O relatório deveria trazer apenas 1 aluno com matrícula CONFIRMADA.");
    assertEquals(
        aluno1.getMatricula(),
        resultado.get(0).getMatricula(),
        "O aluno retornado deveria ser o José Silva.");
  }

  @Test
  public void deveRetornarListaVaziaSeNaoHouverAlunosConfirmados() {
    authController.login("coord@pb.com", "senha123");

    List<Usuario> resultado = relatorioController.gerarRelatorioAlunosPorTurma(turma.getId());

    assertTrue(
        resultado.isEmpty(),
        "O relatório deveria retornar uma lista vazia se ninguém se matriculou.");
  }

  @Test
  public void deveLancarExcecaoQuandoUsuarioLogadoNaoForCoordenador() {
    // Autentica o aluno tentando burlar a permissão (RF03)
    authController.login("jose@pb.com", "senha123");

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              relatorioController.gerarRelatorioAlunosPorTurma(turma.getId());
            });

    assertTrue(
        exception.getMessage().contains("Apenas coordenadores"),
        "Deveria barrar o acesso informando a restrição de perfil.");
  }

  @Test
  public void deveLancarExcecaoParaTurmaInexistente() {
    authController.login("coord@pb.com", "senha123");

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              relatorioController.gerarRelatorioAlunosPorTurma("id-invalido");
            });

    assertTrue(
        exception.getMessage().contains("Turma não encontrada"),
        "Deveria falhar informando que o ID é inválido.");
  }

  @Test
  public void deveLancarExcecaoSeIdDaTurmaForVazioOuNulo() {
    authController.login("coord@pb.com", "senha123");

    assertThrows(
        IllegalArgumentException.class, () -> relatorioController.gerarRelatorioAlunosPorTurma(""));
    assertThrows(
        IllegalArgumentException.class,
        () -> relatorioController.gerarRelatorioAlunosPorTurma(null));
  }

  @Test
  public void deveCalcularOcupacaoDeVagasComSucessoRF41() {
    authController.login("coord@pb.com", "senha123");

    Matricula mat = new Matricula(aluno1.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    matriculas.add(mat);

    List<String> resultado = relatorioController.gerarRelatorioOcupacaoVagas();

    assertFalse(resultado.isEmpty(), "O relatório não deve vir vazio.");
    assertTrue(
        resultado.get(0).contains("Ocupação: 3,3%") || resultado.get(0).contains("Ocupação: 3.3%"),
        "Deveria calcular a porcentagem correta de ocupação das vagas.");
  }

  @Test
  public void deveCalcularTaxaDeReprovacaoPorDisciplinaRF42() {
    authController.login("coord@pb.com", "senha123");

    String idDisciplina = turma.getIdDisciplina();
    Disciplina disciplina =
        new Disciplina(
            idDisciplina, "ED101", "Estrutura de Dados", 60, 4, "curso-1", new ArrayList<>());
    disciplinas.add(disciplina);

    HistoricoAcademico historicoReprovado =
        new HistoricoAcademico(
            aluno1.getId(),
            idDisciplina,
            "2026.2",
            "prof-55",
            turma.getId(),
            3.0,
            70.0,
            SituacaoAcademica.REPROVADO_NOTA);
    historicos.add(historicoReprovado);

    List<String> resultado =
        relatorioController.gerarRelatorioReprovacaoPorDisciplina(idDisciplina);

    assertNotNull(resultado, "A lista de resultados não pode ser nula.");
    assertTrue(
        resultado.get(2).contains("100.0%") || resultado.get(2).contains("100,0%"),
        "Com 1 aluno avaliado e 1 reprovado, a taxa deve ser 100%.");
  }

  @Test
  public void deveLancarExcecaoParaDisciplinaInexistenteRF42() {
    authController.login("coord@pb.com", "senha123");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          relatorioController.gerarRelatorioReprovacaoPorDisciplina("disciplina-inexistente");
        },
        "Deveria falhar informando que a disciplina buscada não existe.");
  }

  @Test
  public void deveGerarRelatorioDeUsuariosComSucessoParaAdministradorRF43() {
    Usuario admin =
        new Usuario(
            PerfilUsuario.ADMINISTRADOR, "Admin Central", "9001", "admin@pb.com", "senhaAdmin");
    usuarios.add(admin);

    authController.login("admin@pb.com", "senhaAdmin");

    List<String> resultado = relatorioController.gerarRelatorioGeralUsuarios();

    assertFalse(resultado.isEmpty());
    assertEquals(
        usuarios.size(),
        resultado.size(),
        "O relatório deve conter todos os usuários registrados.");
  }

  @Test
  public void deveExportarRelatoriosEmPdfRF40aRF43(
      @org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws Exception {
    authController.login("coord@pb.com", "senha123");
    Matricula mat = new Matricula(aluno1.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    matriculas.add(mat);

    String idDisciplina = "disciplina-1";
    disciplinas.add(
        new Disciplina(
            idDisciplina, "ED101", "Estrutura de Dados", 60, 4, "curso-1", new ArrayList<>()));

    java.nio.file.Path pdf40 = tempDir.resolve("rf40.pdf");
    java.nio.file.Path pdf41 = tempDir.resolve("rf41.pdf");
    java.nio.file.Path pdf42 = tempDir.resolve("rf42.pdf");

    assertTrue(
        Files.exists(relatorioController.exportarRelatorioAlunosPorTurmaPdf(turma.getId(), pdf40)));
    assertTrue(Files.exists(relatorioController.exportarRelatorioOcupacaoVagasPdf(pdf41)));
    assertTrue(
        Files.exists(
            relatorioController.exportarRelatorioReprovacaoPorDisciplinaPdf(idDisciplina, pdf42)));

    authController.logout();
    authController.login("admin@pb.com", "senhaAdmin");
    java.nio.file.Path pdf43 = tempDir.resolve("rf43.pdf");
    assertTrue(Files.exists(relatorioController.exportarRelatorioGeralUsuariosPdf(pdf43)));

    assertEquals("%PDF", lerCabecalhoPdf(pdf40));
    assertEquals("%PDF", lerCabecalhoPdf(pdf41));
    assertEquals("%PDF", lerCabecalhoPdf(pdf42));
    assertEquals("%PDF", lerCabecalhoPdf(pdf43));
  }

  private String lerCabecalhoPdf(java.nio.file.Path arquivo) throws Exception {
    byte[] bytes = Files.readAllBytes(arquivo);
    return new String(bytes, 0, Math.min(4, bytes.length), StandardCharsets.US_ASCII);
  }
}
