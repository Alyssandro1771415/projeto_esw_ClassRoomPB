package pb.classroom.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pb.classroom.model.Matricula;
import pb.classroom.model.StatusMatricula;

public class MatriculaRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public MatriculaRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public MatriculaRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<Matricula> carregarMatriculas() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String matriculasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "matriculas");
      return converterJsonParaMatriculas(matriculasJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar as matrículas.", e);
    }
  }

  public void salvarMatriculas(List<Matricula> matriculas) {
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
              converterMatriculasParaJson(matriculas));
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar as matrículas.", e);
    }
  }

  private List<Matricula> converterJsonParaMatriculas(String conteudo) {
    List<Matricula> matriculas = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"idAluno\"") || !objeto.contains("\"idTurma\"")) {
        continue;
      }
      matriculas.add(
          new Matricula(
              obterTexto(objeto, "id"),
              obterTexto(objeto, "idAluno"),
              obterTexto(objeto, "idTurma"),
              StatusMatricula.valueOf(
                  obterTextoOuPadrao(objeto, "status", StatusMatricula.CONFIRMADA.name()))));
    }
    return matriculas;
  }

  private String converterMatriculasParaJson(List<Matricula> matriculas) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < matriculas.size(); i++) {
      Matricula matricula = matriculas.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(matricula.getId())).append("\",\n");
      json.append("      \"idAluno\": \"").append(escapar(matricula.getIdAluno())).append("\",\n");
      json.append("      \"idTurma\": \"").append(escapar(matricula.getIdTurma())).append("\",\n");
      json.append("      \"status\": \"").append(matricula.getStatus().name()).append("\"\n");
      json.append("    }");
      if (i < matriculas.size() - 1) {
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
