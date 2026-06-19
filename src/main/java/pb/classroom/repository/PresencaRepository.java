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
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusPresenca;

public class PresencaRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public PresencaRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public PresencaRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<RegistroPresenca> carregarPresencas() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String presencasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "presencas");
      return converterJsonParaPresencas(presencasJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar as presenças.", e);
    }
  }

  public void salvarPresencas(List<RegistroPresenca> presencas) {
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
              converterPresencasParaJson(presencas));
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar as presenças.", e);
    }
  }

  private List<RegistroPresenca> converterJsonParaPresencas(String conteudo) {
    List<RegistroPresenca> presencas = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"idTurma\"") || !objeto.contains("\"idAluno\"")
          || !objeto.contains("\"data\"")) {
        continue;
      }
      presencas.add(
          new RegistroPresenca(
              obterTexto(objeto, "id"),
              obterTexto(objeto, "idTurma"),
              obterTexto(objeto, "idAluno"),
              LocalDate.parse(obterTexto(objeto, "data")),
              StatusPresenca.valueOf(
                  obterTextoOuPadrao(objeto, "status", StatusPresenca.PRESENTE.name()))));
    }
    return presencas;
  }

  private String converterPresencasParaJson(List<RegistroPresenca> presencas) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < presencas.size(); i++) {
      RegistroPresenca presenca = presencas.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(presenca.getId())).append("\",\n");
      json.append("      \"idTurma\": \"").append(escapar(presenca.getIdTurma())).append("\",\n");
      json.append("      \"idAluno\": \"").append(escapar(presenca.getIdAluno())).append("\",\n");
      json.append("      \"data\": \"").append(presenca.getData()).append("\",\n");
      json.append("      \"status\": \"").append(presenca.getStatus().name()).append("\"\n");
      json.append("    }");
      if (i < presencas.size() - 1) {
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

  private String obterTextoOuPadrao(String objeto, String campo, String padrao) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      return padrao;
    }
    return desescapar(matcher.group(1));
  }

  private String escapar(String valor) {
    return valor.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String desescapar(String valor) {
    return valor.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
