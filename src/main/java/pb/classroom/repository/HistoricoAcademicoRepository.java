package pb.classroom.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.SituacaoAcademica;

public class HistoricoAcademicoRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public HistoricoAcademicoRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public HistoricoAcademicoRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<HistoricoAcademico> carregarHistoricos() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String historicosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "historicos");
      return converterJsonParaHistoricos(historicosJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar o histórico acadêmico.", e);
    }
  }

  public void salvarHistoricos(List<HistoricoAcademico> historicos) {
    try {
      String conteudoAtual = "";
      if (Files.exists(caminhoArquivo)) {
        conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      }

      String documento =
          ArmazenamentoJson.montarDocumento(
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "cursos"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "periodosLetivos"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "turmas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "matriculas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "presencas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "notas"),
              converterHistoricosParaJson(historicos));
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar o histórico acadêmico.", e);
    }
  }

  private List<HistoricoAcademico> converterJsonParaHistoricos(String conteudo) {
    List<HistoricoAcademico> historicos = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"idAluno\"") || !objeto.contains("\"idDisciplina\"")) {
        continue;
      }
      historicos.add(
          new HistoricoAcademico(
              obterTexto(objeto, "id"),
              obterTexto(objeto, "idAluno"),
              obterTexto(objeto, "idDisciplina"),
              obterTexto(objeto, "idPeriodoLetivo"),
              obterTexto(objeto, "idProfessor"),
              obterTexto(objeto, "idTurma"),
              obterDoubleOpcional(objeto, "mediaFinal"),
              obterDouble(objeto, "percentualFrequencia"),
              SituacaoAcademica.valueOf(obterTexto(objeto, "situacao")),
              LocalDate.parse(obterTexto(objeto, "dataRegistro"))));
    }
    return historicos;
  }

  private String converterHistoricosParaJson(List<HistoricoAcademico> historicos) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < historicos.size(); i++) {
      HistoricoAcademico historico = historicos.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(historico.getId())).append("\",\n");
      json.append("      \"idAluno\": \"").append(escapar(historico.getIdAluno())).append("\",\n");
      json.append("      \"idDisciplina\": \"")
          .append(escapar(historico.getIdDisciplina()))
          .append("\",\n");
      json.append("      \"idPeriodoLetivo\": \"")
          .append(escapar(historico.getIdPeriodoLetivo()))
          .append("\",\n");
      json.append("      \"idProfessor\": \"")
          .append(escapar(historico.getIdProfessor()))
          .append("\",\n");
      json.append("      \"idTurma\": \"").append(escapar(historico.getIdTurma())).append("\",\n");
      if (historico.getMediaFinal() == null) {
        json.append("      \"mediaFinal\": null,\n");
      } else {
        json.append("      \"mediaFinal\": ").append(historico.getMediaFinal()).append(",\n");
      }
      json.append("      \"percentualFrequencia\": ")
          .append(historico.getPercentualFrequencia())
          .append(",\n");
      json.append("      \"situacao\": \"").append(historico.getSituacao().name()).append("\",\n");
      json.append("      \"dataRegistro\": \"").append(historico.getDataRegistro()).append("\"\n");
      json.append("    }");
      if (i < historicos.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }

    json.append("  ]");
    return json.toString();
  }

  private String obterTexto(String objeto, String campo) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return desescapar(matcher.group(1));
  }

  private double obterDouble(String objeto, String campo) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return Double.parseDouble(matcher.group(1));
  }

  private Double obterDoubleOpcional(String objeto, String campo) {
    Matcher matcherNulo = Pattern.compile("\"" + campo + "\"\\s*:\\s*null").matcher(objeto);
    if (matcherNulo.find()) {
      return null;
    }
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(objeto);
    if (!matcher.find()) {
      return null;
    }
    return Double.parseDouble(matcher.group(1));
  }

  private String escapar(String valor) {
    return valor.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String desescapar(String valor) {
    return valor.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
