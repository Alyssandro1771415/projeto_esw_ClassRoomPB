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

import pb.classroom.model.PeriodoLetivo;

public class PeriodoLetivoRepository {

    private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
    private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

    private final Path caminhoArquivo;

    public PeriodoLetivoRepository() {
        this(Paths.get(ARQUIVO_PADRAO));
    }

    public PeriodoLetivoRepository(Path caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public List<PeriodoLetivo> carregarPeriodosLetivos() {
        try {
            if (!Files.exists(caminhoArquivo)) {
                return new ArrayList<>();
            }

            String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            String periodosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "periodosLetivos");
            return converterJsonParaPeriodos(periodosJson);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar os períodos letivos.", e);
        }
    }

    public void salvarPeriodosLetivos(List<PeriodoLetivo> periodosLetivos) {
        try {
            String conteudoAtual = "";
            if (Files.exists(caminhoArquivo)) {
                conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            }

            String usuariosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios");
            String disciplinasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas");
            String cursosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "cursos");
            String turmasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "turmas");
            String documento = ArmazenamentoJson.montarDocumento(
                    usuariosJson,
                    disciplinasJson,
                    cursosJson,
                    converterPeriodosParaJson(periodosLetivos),
                    turmasJson);
            Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar os períodos letivos.", e);
        }
    }

    private List<PeriodoLetivo> converterJsonParaPeriodos(String conteudo) {
        List<PeriodoLetivo> periodos = new ArrayList<>();
        Matcher matcher = OBJETO_JSON.matcher(conteudo);

        while (matcher.find()) {
            String objeto = matcher.group(1);
            if (!objeto.contains("\"codigo\"") || !objeto.contains("\"ativo\"")) {
                continue;
            }

            String id = obterTexto(objeto, "id");
            String codigo = obterTexto(objeto, "codigo");
            boolean ativo = obterBooleano(objeto, "ativo");
            periodos.add(new PeriodoLetivo(id, codigo, ativo));
        }

        return periodos;
    }

    private String converterPeriodosParaJson(List<PeriodoLetivo> periodosLetivos) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < periodosLetivos.size(); i++) {
            PeriodoLetivo periodoLetivo = periodosLetivos.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapar(periodoLetivo.getId())).append("\",\n");
            json.append("      \"codigo\": \"").append(escapar(periodoLetivo.getCodigo())).append("\",\n");
            json.append("      \"ativo\": ").append(periodoLetivo.isAtivo()).append("\n");
            json.append("    }");

            if (i < periodosLetivos.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String obterTexto(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
        }
        return desescapar(matcher.group(1));
    }

    private boolean obterBooleano(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(true|false)").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
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
