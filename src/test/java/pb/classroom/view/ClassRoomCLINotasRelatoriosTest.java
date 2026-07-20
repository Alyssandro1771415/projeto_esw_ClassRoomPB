package pb.classroom.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.MatriculaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.repository.PresencaRepository;
import pb.classroom.repository.TurmaRepository;
import pb.classroom.repository.UsuarioRepository;

@DisplayName("ClassRoomCLI - notas, histórico e relatórios PDF")
class ClassRoomCLINotasRelatoriosTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("professor lança notas e aluno consulta")
  void professorLancaNotasEAlunoConsulta() throws Exception {
    Path arquivo = criarFixtureNotas();
    Path pdfDir = tempDir.resolve("pdfs");
    Files.createDirectories(pdfDir);

    String saidaProf =
        executar(arquivo, "1", "PROF001", "demo123", "", "25", "1", "1", "8.0", "", "3", "", "0");
    assertTrue(saidaProf.contains("Notas lançadas com sucesso"));

    String saidaAluno = executar(arquivo, "1", "ALU001", "demo123", "", "25", "", "27", "", "0");
    assertTrue(saidaAluno.contains("Suas notas") || saidaAluno.contains("Situação"));
    assertTrue(
        saidaAluno.contains("histórico")
            || saidaAluno.contains("Histórico")
            || saidaAluno.contains("Nenhum registro"));
  }

  @Test
  @DisplayName("coordenador gera relatórios e baixa PDF")
  void coordenadorGeraRelatoriosPdf() throws Exception {
    Path arquivo = criarFixtureNotas();
    Path pdf40 = tempDir.resolve("rf40.pdf");
    Path pdf41 = tempDir.resolve("rf41.pdf");
    Path pdf42 = tempDir.resolve("rf42.pdf");

    String saida =
        executar(
            arquivo,
            "1",
            "COORD001",
            "demo123",
            "",
            "29",
            "1",
            "S",
            pdf40.toString(),
            "",
            "30",
            "S",
            pdf41.toString(),
            "",
            "31",
            "1",
            "S",
            pdf42.toString(),
            "",
            "0");

    assertTrue(saida.contains("RELATÓRIO DE ALUNOS") || saida.contains("MATRÍCULA"));
    assertTrue(saida.contains("OCUPAÇÃO") || saida.contains("Ocupação"));
    assertTrue(saida.contains("REPROVAÇÃO") || saida.contains("Reprovação"));
    assertTrue(saida.contains("PDF gerado com sucesso"));
    assertTrue(Files.exists(pdf40));
    assertTrue(Files.exists(pdf41));
    assertTrue(Files.exists(pdf42));
    assertTrue(Files.size(pdf40) > 0);
  }

  @Test
  @DisplayName("administrador gera relatório geral de usuários em PDF")
  void administradorGeraRelatorioUsuariosPdf() throws Exception {
    Path arquivo = criarFixtureNotas();
    Path pdf43 = tempDir.resolve("rf43.pdf");

    String saida =
        executar(arquivo, "1", "ADM001", "demo123", "", "32", "S", pdf43.toString(), "", "0");

    assertTrue(saida.contains("RELATÓRIO GERAL DE USUÁRIOS") || saida.contains("Matrícula"));
    assertTrue(saida.contains("PDF gerado com sucesso"));
    assertTrue(Files.exists(pdf43));
  }

  @Test
  @DisplayName("coordenador consulta histórico por curso")
  void coordenadorConsultaHistoricoPorCurso() throws Exception {
    Path arquivo = criarFixtureNotas();
    String saida = executar(arquivo, "1", "COORD001", "demo123", "", "28", "1", "", "0");
    assertTrue(
        saida.contains("Histórico")
            || saida.contains("histórico")
            || saida.contains("APROVADO")
            || saida.contains("Nenhum registro"));
  }

  private Path criarFixtureNotas() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento-notas.json");
    Files.writeString(
        arquivo,
        "{\n"
            + "  \"usuarios\": [\n"
            + "    {\"id\":\"admin-demo\",\"perfil\":\"ADMINISTRADOR\",\"nome\":\"Marina\","
            + "\"matricula\":\"ADM001\",\"email\":\"admin@demo.com\",\"senha\":\"demo123\",\"ativo\":true},\n"
            + "    {\"id\":\"coord-demo\",\"perfil\":\"COORDENADOR\",\"nome\":\"Carlos\","
            + "\"matricula\":\"COORD001\",\"email\":\"coord@demo.com\",\"senha\":\"demo123\",\"ativo\":true},\n"
            + "    {\"id\":\"prof-demo\",\"perfil\":\"PROFESSOR\",\"nome\":\"Paulo\","
            + "\"matricula\":\"PROF001\",\"email\":\"prof@demo.com\",\"senha\":\"demo123\",\"ativo\":true},\n"
            + "    {\"id\":\"aluno-ana\",\"perfil\":\"ALUNO\",\"nome\":\"Ana Costa\","
            + "\"matricula\":\"ALU001\",\"email\":\"ana@demo.com\",\"senha\":\"demo123\",\"ativo\":true}\n"
            + "  ],\n"
            + "  \"disciplinas\": [\n"
            + "    {\"id\":\"disc-fundamentos\",\"codigo\":\"ESW101\",\"nome\":\"Fundamentos\","
            + "\"cargaHoraria\":60,\"creditos\":4,\"idCurso\":\"curso-esw\",\"preRequisitosIds\":[]}\n"
            + "  ],\n"
            + "  \"cursos\": [\n"
            + "    {\"id\":\"curso-esw\",\"nome\":\"Engenharia de Software\",\"codigo\":\"ESW\"}\n"
            + "  ],\n"
            + "  \"periodosLetivos\": [\n"
            + "    {\"id\":\"periodo-2026-2\",\"codigo\":\"2026.2\",\"ativo\":true}\n"
            + "  ],\n"
            + "  \"turmas\": [\n"
            + "    {\"id\":\"turma-aberta\",\"idDisciplina\":\"disc-fundamentos\","
            + "\"idPeriodoLetivo\":\"periodo-2026-2\",\"idProfessor\":\"prof-demo\","
            + "\"limiteVagas\":30,\"sala\":\"Sala 101\",\"dataInicioAulas\":\"2026-07-01\","
            + "\"horarios\":[\"MONDAY|08:00|10:00\"],\"cancelada\":false,\"fechada\":false}\n"
            + "  ],\n"
            + "  \"matriculas\": [\n"
            + "    {\"id\":\"mat-ana\",\"idAluno\":\"aluno-ana\",\"idTurma\":\"turma-aberta\","
            + "\"status\":\"CONFIRMADA\"}\n"
            + "  ],\n"
            + "  \"presencas\": [],\n"
            + "  \"notas\": [],\n"
            + "  \"historicos\": [\n"
            + "    {\"id\":\"hist-1\",\"idAluno\":\"aluno-ana\",\"idDisciplina\":\"disc-fundamentos\","
            + "\"idPeriodoLetivo\":\"periodo-2026-2\",\"idProfessor\":\"prof-demo\","
            + "\"idTurma\":\"turma-aberta\",\"mediaFinal\":8.5,\"percentualFrequencia\":100.0,"
            + "\"situacao\":\"APROVADO\",\"dataRegistro\":\"2026-06-30\"}\n"
            + "  ]\n"
            + "}\n",
        StandardCharsets.UTF_8);
    return arquivo;
  }

  private String executar(Path arquivo, String... linhas) {
    String entrada = String.join("\n", linhas) + "\n";
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream anterior = System.out;
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
              new MatriculaRepository(arquivo),
              new PresencaRepository(arquivo));
      cli.iniciar();
    } finally {
      System.setOut(anterior);
    }
    return buffer.toString(StandardCharsets.UTF_8);
  }
}
