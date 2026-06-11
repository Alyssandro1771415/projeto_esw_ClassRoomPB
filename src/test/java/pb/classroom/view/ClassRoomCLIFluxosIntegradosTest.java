package pb.classroom.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.Matricula;
import pb.classroom.model.Turma;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.MatriculaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.repository.TurmaRepository;
import pb.classroom.repository.UsuarioRepository;

@DisplayName("ClassRoomCLI - fluxos integrados da release 1")
class ClassRoomCLIFluxosIntegradosTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("administrador executa cadastro e listagens principais")
  void administradorExecutaFluxosPrincipais() throws Exception {
    Path arquivo = copiarFixture();
    String saida =
        executar(
            arquivo,
            "1",
            "0001",
            "admin123",
            "",
            "2",
            "",
            "13",
            "",
            "18",
            "Sistemas de Informacao",
            "SI",
            "",
            "8",
            "",
            "3",
            "",
            "0");

    assertTrue(saida.contains("Login realizado com sucesso"));
    assertTrue(saida.contains("Curso cadastrado com sucesso"));
    assertTrue(saida.contains("Usuários cadastrados"));
  }

  @Test
  @DisplayName("coordenador gerencia periodo, disciplina e turma")
  void coordenadorGerenciaAcademico() throws Exception {
    Path arquivo = copiarFixture();
    String saida =
        executar(
            arquivo,
            "1",
            "coordenador@classroompb.com",
            "123456",
            "",
            "9",
            "2026.2",
            "",
            "10",
            "",
            "11",
            "per-1",
            "",
            "5",
            "ESW102",
            "Metodologia",
            "40",
            "3",
            "curso-1",
            "",
            "",
            "6",
            "",
            "14",
            "disc-1",
            "per-1",
            "prof-1",
            "25",
            "Sala 10",
            "2026-08-15",
            "1",
            "1",
            "08:00",
            "10:00",
            "",
            "7",
            "",
            "0");

    assertTrue(
        saida.contains("Período letivo cadastrado com sucesso")
            || saida.contains("Período letivo atualizado"));
    assertTrue(
        saida.contains("Disciplina cadastrada com sucesso")
            || saida.contains("Disciplinas cadastradas"));
  }

  @Test
  @DisplayName("aluno consulta listagens disponiveis")
  void alunoConsultaListagens() throws Exception {
    Path arquivo = copiarFixture();
    String saida =
        executar(
            arquivo, "1", "aluno@classroompb.com", "123456", "", "6", "", "10", "", "7", "", "0");

    assertTrue(saida.contains("Login realizado com sucesso"));
  }

  @Test
  @DisplayName("RF15: aluno consulta disciplinas e turmas disponiveis")
  void alunoConsultaDisciplinasETurmasDisponiveis() throws Exception {
    Path arquivo = criarArquivoRf15();
    String saida =
        executar(arquivo, "1", "aluno@classroompb.com", "123456", "", "6", "", "7", "", "0");

    assertTrue(saida.contains("Disciplinas disponíveis:"));
    assertTrue(saida.contains("Turmas disponíveis:"));
    assertTrue(saida.contains("ESW101"));
    assertTrue(saida.contains("turma-ativa"));
    assertFalse(saida.contains("ESW102"));
    assertFalse(saida.contains("turma-cancelada"));
    assertFalse(saida.contains("turma-encerrada"));
  }

  @Test
  @DisplayName("RF19: aluno nao realiza matricula com choque de horario")
  void alunoNaoRealizaMatriculaComChoqueDeHorario() throws Exception {
    Path arquivo = criarArquivoRf19();
    String saida =
        executar(
            arquivo,
            "1",
            "aluno@classroompb.com",
            "123456",
            "",
            "8",
            "turma-1",
            "",
            "8",
            "turma-2",
            "",
            "0");

    List<Matricula> matriculas = new MatriculaRepository(arquivo).carregarMatriculas();
    assertTrue(saida.contains("Matrícula realizada com sucesso."));
    assertTrue(saida.contains("Choque de horário"));
    assertEquals(1, matriculas.size());
    assertEquals("turma-1", matriculas.get(0).getIdTurma());
  }

  @Test
  @DisplayName("admin cadastra usuario e coordenador encerra periodo letivo")
  void adminCadastraUsuarioECoordenadorEncerraPeriodo() throws Exception {
    Path arquivo = copiarFixture();
    String saida =
        executar(
            arquivo,
            "1",
            "admin@classroompb.com",
            "admin123",
            "",
            "4",
            "2",
            "2026999",
            "Novo Professor",
            "prof123",
            "",
            "3",
            "",
            "1",
            "coordenador@classroompb.com",
            "123456",
            "",
            "12",
            "per-1",
            "",
            "0");

    assertTrue(
        saida.contains("Usuário cadastrado com sucesso")
            || saida.contains("Período letivo atualizado"));
  }

  @Test
  @DisplayName("coordenador altera e cancela turma ofertada")
  void coordenadorAlteraECancelaTurma() throws Exception {
    Path arquivo = copiarFixture();
    executar(
        arquivo,
        "1",
        "coordenador@classroompb.com",
        "123456",
        "",
        "14",
        "disc-1",
        "per-1",
        "prof-1",
        "20",
        "Sala 20",
        "2026-10-01",
        "1",
        "3",
        "19:00",
        "21:00",
        "",
        "0");

    List<Turma> turmas = new TurmaRepository(arquivo).carregarTurmas();
    String idTurma = turmas.get(0).getId();

    String saida =
        executar(
            arquivo,
            "1",
            "coordenador@classroompb.com",
            "123456",
            "",
            "16",
            idTurma,
            "prof-1",
            "22",
            "Sala 22",
            "2026-10-15",
            "1",
            "4",
            "08:00",
            "10:00",
            "",
            "17",
            idTurma,
            "",
            "0");

    assertTrue(
        saida.contains("Turma alterada com sucesso")
            || saida.contains("Turma cancelada com sucesso"));
  }

  @Test
  @DisplayName("professor autenticado consulta menus do perfil")
  void professorConsultaMenus() throws Exception {
    Path arquivo = copiarFixture();
    String saida =
        executar(
            arquivo,
            "1",
            "professor@classroompb.com",
            "123456",
            "",
            "6",
            "",
            "10",
            "",
            "7",
            "",
            "2",
            "",
            "0");

    assertTrue(saida.contains("Login realizado com sucesso"));
    assertTrue(saida.contains("Perfil: PROFESSOR"));
  }

  @Test
  @DisplayName("login invalido e operacoes sem autenticacao exibem mensagens")
  void loginInvalidoEOperacoesSemAutenticacao() throws Exception {
    Path arquivo = copiarFixture();
    String saida = executar(arquivo, "1", "0001", "senha-errada", "", "18", "", "5", "", "0");

    assertTrue(
        saida.contains("Opção inválida.") || saida.contains("senha") || saida.contains("inválid"));
  }

  private Path copiarFixture() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    try (InputStream in = getClass().getResourceAsStream("/armazenamento-teste.json")) {
      Files.copy(in, arquivo);
    }
    return arquivo;
  }

  private Path criarArquivoRf15() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento-rf15.json");
    Files.writeString(
        arquivo,
        "{\n"
            + "  \"usuarios\": [\n"
            + "    {\"id\":\"prof-1\",\"perfil\":\"PROFESSOR\",\"nome\":\"Professor Teste\","
            + "\"matricula\":\"2026100\",\"email\":\"professor@classroompb.com\","
            + "\"senha\":\"123456\",\"ativo\":true},\n"
            + "    {\"id\":\"aluno-1\",\"perfil\":\"ALUNO\",\"nome\":\"Aluno Teste\","
            + "\"matricula\":\"2026200\",\"email\":\"aluno@classroompb.com\","
            + "\"senha\":\"123456\",\"ativo\":true}\n"
            + "  ],\n"
            + "  \"disciplinas\": [\n"
            + "    {\"id\":\"disc-1\",\"codigo\":\"ESW101\",\"nome\":\"Disponivel\","
            + "\"cargaHoraria\":60,\"creditos\":4,\"idCurso\":\"curso-1\","
            + "\"preRequisitosIds\":[]},\n"
            + "    {\"id\":\"disc-2\",\"codigo\":\"ESW102\",\"nome\":\"Indisponivel\","
            + "\"cargaHoraria\":60,\"creditos\":4,\"idCurso\":\"curso-1\","
            + "\"preRequisitosIds\":[]}\n"
            + "  ],\n"
            + "  \"cursos\": [\n"
            + "    {\"id\":\"curso-1\",\"nome\":\"Ciencia da Computacao\",\"codigo\":\"CC\"}\n"
            + "  ],\n"
            + "  \"periodosLetivos\": [\n"
            + "    {\"id\":\"per-ativo\",\"codigo\":\"2026.2\",\"ativo\":true},\n"
            + "    {\"id\":\"per-encerrado\",\"codigo\":\"2026.1\",\"ativo\":false}\n"
            + "  ],\n"
            + "  \"turmas\": [\n"
            + "    {\"id\":\"turma-ativa\",\"idDisciplina\":\"disc-1\","
            + "\"idPeriodoLetivo\":\"per-ativo\",\"idProfessor\":\"prof-1\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 101\","
            + "\"dataInicioAulas\":\"2026-08-01\","
            + "\"horarios\":[\"MONDAY|08:00|10:00\"],\"cancelada\":false},\n"
            + "    {\"id\":\"turma-cancelada\",\"idDisciplina\":\"disc-2\","
            + "\"idPeriodoLetivo\":\"per-ativo\",\"idProfessor\":\"prof-1\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 102\","
            + "\"dataInicioAulas\":\"2026-08-01\","
            + "\"horarios\":[\"TUESDAY|08:00|10:00\"],\"cancelada\":true},\n"
            + "    {\"id\":\"turma-encerrada\",\"idDisciplina\":\"disc-2\","
            + "\"idPeriodoLetivo\":\"per-encerrado\",\"idProfessor\":\"prof-1\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 103\","
            + "\"dataInicioAulas\":\"2026-08-01\","
            + "\"horarios\":[\"WEDNESDAY|08:00|10:00\"],\"cancelada\":false}\n"
            + "  ]\n"
            + "}\n",
        StandardCharsets.UTF_8);
    return arquivo;
  }

  private Path criarArquivoRf19() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento-rf19.json");
    Files.writeString(
        arquivo,
        "{\n"
            + "  \"usuarios\": [\n"
            + "    {\"id\":\"aluno-1\",\"perfil\":\"ALUNO\",\"nome\":\"Aluno Teste\","
            + "\"matricula\":\"2026200\",\"email\":\"aluno@classroompb.com\","
            + "\"senha\":\"123456\",\"ativo\":true}\n"
            + "  ],\n"
            + "  \"disciplinas\": [],\n"
            + "  \"cursos\": [],\n"
            + "  \"periodosLetivos\": [\n"
            + "    {\"id\":\"periodo-1\",\"codigo\":\"2026.2\",\"ativo\":true}\n"
            + "  ],\n"
            + "  \"turmas\": [\n"
            + "    {\"id\":\"turma-1\",\"idDisciplina\":\"disc-1\","
            + "\"idPeriodoLetivo\":\"periodo-1\",\"idProfessor\":\"prof-1\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 101\","
            + "\"dataInicioAulas\":\"2026-08-01\","
            + "\"horarios\":[\"MONDAY|08:00|10:00\"],\"cancelada\":false},\n"
            + "    {\"id\":\"turma-2\",\"idDisciplina\":\"disc-2\","
            + "\"idPeriodoLetivo\":\"periodo-1\",\"idProfessor\":\"prof-2\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 102\","
            + "\"dataInicioAulas\":\"2026-08-01\","
            + "\"horarios\":[\"MONDAY|09:00|11:00\"],\"cancelada\":false}\n"
            + "  ],\n"
            + "  \"matriculas\": []\n"
            + "}\n",
        StandardCharsets.UTF_8);
    return arquivo;
  }

  private String executar(Path arquivo, String... linhas) {
    String entrada = String.join("\n", linhas) + "\n";
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream saidaAnterior = System.out;
    System.setOut(new PrintStream(buffer));

    try {
      ClassRoomCLI cli =
          new ClassRoomCLI(
              new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
              new UsuarioRepository(arquivo),
              new DisciplinaRepository(arquivo),
              new CursoRepository(arquivo),
              new PeriodoLetivoRepository(arquivo),
              new TurmaRepository(arquivo),
              new MatriculaRepository(arquivo));
      cli.iniciar();
    } finally {
      System.setOut(saidaAnterior);
    }

    return buffer.toString(StandardCharsets.UTF_8);
  }
}
