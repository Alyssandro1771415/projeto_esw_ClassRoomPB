package pb.classroom.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Turma;

public class TurmaRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public TurmaRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public TurmaRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<Turma> carregarTurmas() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String turmasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "turmas");
      return converterJsonParaTurmas(turmasJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar as turmas.", e);
    }
  }

  public void salvarTurmas(List<Turma> turmas) {
    try {
      String conteudoAtual = "";
      if (Files.exists(caminhoArquivo)) {
        conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      }

      String usuariosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios");
      String disciplinasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas");
      String cursosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "cursos");
      String periodosLetivosJson =
          ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "periodosLetivos");
      String matriculasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "matriculas");
      String documento =
          ArmazenamentoJson.montarDocumento(
              usuariosJson,
              disciplinasJson,
              cursosJson,
              periodosLetivosJson,
              converterTurmasParaJson(turmas),
              matriculasJson,
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "presencas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "notas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "historicos"));
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar as turmas.", e);
    }
  }

  private List<Turma> converterJsonParaTurmas(String conteudo) {
    List<Turma> turmas = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"idDisciplina\"") || !objeto.contains("\"idProfessor\"")) {
        continue;
      }

      String id = obterTexto(objeto, "id");
      String idDisciplina = obterTexto(objeto, "idDisciplina");
      String idPeriodoLetivo = obterTexto(objeto, "idPeriodoLetivo");
      String idProfessor = obterTexto(objeto, "idProfessor");
      int limiteVagas = obterInteiro(objeto, "limiteVagas");
      String sala = obterTexto(objeto, "sala");
      LocalDate dataInicioAulas = LocalDate.parse(obterTexto(objeto, "dataInicioAulas"));
      List<BlocoHorario> horarios = obterHorarios(objeto);
      boolean cancelada = obterBooleano(objeto, "cancelada");
      boolean fechada = obterBooleanoOpcional(objeto, "fechada");

      turmas.add(
          new Turma(
              id,
              idDisciplina,
              idPeriodoLetivo,
              idProfessor,
              limiteVagas,
              sala,
              dataInicioAulas,
              horarios,
              cancelada,
              fechada));
    }

    return turmas;
  }

  private String converterTurmasParaJson(List<Turma> turmas) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < turmas.size(); i++) {
      Turma turma = turmas.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(turma.getId())).append("\",\n");
      json.append("      \"idDisciplina\": \"")
          .append(escapar(turma.getIdDisciplina()))
          .append("\",\n");
      json.append("      \"idPeriodoLetivo\": \"")
          .append(escapar(turma.getIdPeriodoLetivo()))
          .append("\",\n");
      json.append("      \"idProfessor\": \"")
          .append(escapar(turma.getIdProfessor()))
          .append("\",\n");
      json.append("      \"limiteVagas\": ").append(turma.getLimiteVagas()).append(",\n");
      json.append("      \"sala\": \"").append(escapar(turma.getSala())).append("\",\n");
      json.append("      \"dataInicioAulas\": \"")
          .append(turma.getDataInicioAulas())
          .append("\",\n");
      json.append("      \"horarios\": ");
      adicionarHorarios(json, turma.getHorarios());
      json.append(",\n");
      json.append("      \"cancelada\": ").append(turma.isCancelada()).append(",\n");
      json.append("      \"fechada\": ").append(turma.isFechada()).append("\n");
      json.append("    }");

      if (i < turmas.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }

    json.append("  ]");
    return json.toString();
  }

  private List<BlocoHorario> obterHorarios(String objeto) {
    String array = ArmazenamentoJson.extrairArrayOuVazio(objeto, "horarios");
    List<BlocoHorario> horarios = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(array);

    while (matcher.find()) {
      String[] partes = desescapar(matcher.group(1)).split("\\|");
      if (partes.length != 3) {
        throw new IllegalArgumentException("Horário inválido no armazenamento.");
      }
      horarios.add(
          new BlocoHorario(
              DayOfWeek.valueOf(partes[0]),
              LocalTime.parse(partes[1]),
              LocalTime.parse(partes[2])));
    }
    return horarios;
  }

  private void adicionarHorarios(StringBuilder json, List<BlocoHorario> horarios) {
    json.append("[\n");
    for (int i = 0; i < horarios.size(); i++) {
      BlocoHorario horario = horarios.get(i);
      String valor =
          horario.getDiaSemana() + "|" + horario.getHoraInicio() + "|" + horario.getHoraFim();
      json.append("        \"").append(escapar(valor)).append("\"");
      if (i < horarios.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("      ]");
  }

  private String obterTexto(String objeto, String campo) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return desescapar(matcher.group(1));
  }

  private int obterInteiro(String objeto, String campo) {
    Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+)").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return Integer.parseInt(matcher.group(1));
  }

  private boolean obterBooleano(String objeto, String campo) {
    Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(true|false)").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return Boolean.parseBoolean(matcher.group(1));
  }

  private boolean obterBooleanoOpcional(String objeto, String campo) {
    Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(true|false)").matcher(objeto);
    if (!matcher.find()) {
      return false;
    }
    return Boolean.parseBoolean(matcher.group(1));
  }

  private String escapar(String valor) {
    return valor.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String desescapar(String valor) {
    return valor.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
