package pb.classroom.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.Disciplina;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;
import pb.classroom.report.PdfRelatorioWriter;

public class RelatorioController {

  private final AutenticacaoController autenticacaoController;
  private final List<Turma> turmas;
  private final List<Matricula> matriculas;
  private final List<Usuario> usuarios;
  private final List<Disciplina> disciplinas;
  private final List<HistoricoAcademico> historicos;

  // CORREÇÃO: Mude de List<List<HistoricoAcademico>> para List<HistoricoAcademico>
  public RelatorioController(
      AutenticacaoController autenticacaoController,
      List<Turma> turmas,
      List<Matricula> matriculas,
      List<Usuario> usuarios,
      List<Disciplina> disciplinas,
      List<HistoricoAcademico> historicos) {

    if (autenticacaoController == null) {
      throw new IllegalArgumentException("controle de autenticação é obrigatório");
    }
    if (turmas == null
        || matriculas == null
        || usuarios == null
        || disciplinas == null
        || historicos == null) {
      throw new IllegalArgumentException("listas de apoio são obrigatórias");
    }
    this.autenticacaoController = autenticacaoController;
    this.turmas = turmas;
    this.matriculas = matriculas;
    this.usuarios = usuarios;
    this.disciplinas = disciplinas;
    this.historicos = historicos;
  }

  /**
   * RF40: O coordenador deve gerar relatório de alunos matriculados por turma. Retorna uma lista
   * com os Usuários (Alunos) confirmados na turma informada.
   */
  public List<Usuario> gerarRelatorioAlunosPorTurma(String idTurma) {
    validarCoordenadorAutenticado();
    validarTurmaExistente(idTurma);

    List<Usuario> alunosMatriculados = new ArrayList<>();
    for (Matricula matricula : matriculas) {
      if (matricula.getIdTurma().equals(idTurma.trim()) && matricula.isConfirmada()) {
        Usuario aluno = buscarUsuarioPorId(matricula.getIdAluno());
        if (aluno != null && aluno.isAtivo()) {
          alunosMatriculados.add(aluno);
        }
      }
    }
    return Collections.unmodifiableList(alunosMatriculados);
  }

  /**
   * RF41: O coordenador deve gerar relatório de ocupação de vagas. Calcula a quantidade de
   * matrículas confirmadas comparado ao limite de vagas de cada turma ativa.
   */
  public List<String> gerarRelatorioOcupacaoVagas() {
    validarCoordenadorAutenticado();
    List<String> linhasRelatorio = new ArrayList<>();

    for (Turma turma : turmas) {
      if (turma.isCancelada()) {
        continue;
      }

      int matriculados = 0;
      for (Matricula matricula : matriculas) {
        if (matricula.getIdTurma().equals(turma.getId()) && matricula.isConfirmada()) {
          matriculados++;
        }
      }

      double percentualOcupacao =
          (turma.getLimiteVagas() > 0)
              ? ((double) matriculados / turma.getLimiteVagas()) * 100
              : 0.0;

      String item =
          String.format(
              "Turma ID: %s | Vagas Máximas: %d | Matriculados Confirmados: %d | Ocupação: %.1f%%",
              turma.getId(), turma.getLimiteVagas(), matriculados, percentualOcupacao);
      linhasRelatorio.add(item);
    }

    return Collections.unmodifiableList(linhasRelatorio);
  }

  /**
   * RF42: O coordenador deve gerar relatório de reprovação por disciplina. Mapeia os registros
   * consolidados do histórico acadêmico para calcular a porcentagem de reprovações.
   */
  public List<String> gerarRelatorioReprovacaoPorDisciplina(String idDisciplina) {
    validarCoordenadorAutenticado();
    validarDisciplinaExistente(idDisciplina);

    int totalRegistros = 0;
    int totalReprovados = 0;

    for (HistoricoAcademico historico : historicos) {
      if (historico.getIdDisciplina().equals(idDisciplina.trim())) {
        totalRegistros++;
        // Captura situações contendo REPROVADO (ex: REPROVADO_POR_NOTA, REPROVADO_POR_FALTA)
        String situacao = historico.getSituacao().toString().toUpperCase();
        if (situacao.contains("REPROVADO")) {
          totalReprovados++;
        }
      }
    }

    double taxaReprovacao =
        (totalRegistros > 0) ? ((double) totalReprovados / totalRegistros) * 100 : 0.0;

    List<String> dados = new ArrayList<>();
    dados.add(String.format("Total de Alunos Avaliados no Histórico: %d", totalRegistros));
    dados.add(String.format("Total de Alunos Reprovados: %d", totalReprovados));
    dados.add(String.format("Taxa Geral de Reprovação: %.1f%%", taxaReprovacao));

    return Collections.unmodifiableList(dados);
  }

  /** RF43: O administrador deve gerar relatório geral de usuários cadastrados. */
  public List<String> gerarRelatorioGeralUsuarios() {
    // Validação de acesso do perfil administrador (RF03, RF43)
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
      throw new IllegalArgumentException(
          "Apenas administradores podem gerar o relatório geral de usuários.");
    }

    List<String> linhas = new ArrayList<>();
    for (Usuario usuario : usuarios) {
      String status = usuario.isAtivo() ? "ATIVO" : "INATIVO";
      String item =
          String.format(
              "Matrícula: %s | Nome: %s | E-mail: %s | Perfil: %s | Status: %s",
              usuario.getMatricula(),
              usuario.getNome(),
              usuario.getEmail(),
              usuario.getPerfil(),
              status);
      linhas.add(item);
    }
    return Collections.unmodifiableList(linhas);
  }

  /** RF40: Exporta o relatório de alunos matriculados por turma em PDF. */
  public Path exportarRelatorioAlunosPorTurmaPdf(String idTurma, Path destino) {
    List<Usuario> alunos = gerarRelatorioAlunosPorTurma(idTurma);
    List<String> linhas = new ArrayList<>();
    linhas.add("ID da Turma: " + idTurma.trim());
    linhas.add("Total de Alunos Confirmados: " + alunos.size());
    linhas.add("");
    if (alunos.isEmpty()) {
      linhas.add("Não há alunos com matrícula CONFIRMADA nesta turma.");
    } else {
      for (Usuario aluno : alunos) {
        linhas.add(
            String.format(
                "Matrícula: %s | Nome: %s | E-mail: %s",
                aluno.getMatricula(), aluno.getNome(), aluno.getEmail()));
      }
    }
    return PdfRelatorioWriter.escrever(
        destino, "Relatório de Alunos Matriculados por Turma (RF40)", linhas);
  }

  /** RF41: Exporta o relatório de ocupação de vagas em PDF. */
  public Path exportarRelatorioOcupacaoVagasPdf(Path destino) {
    return PdfRelatorioWriter.escrever(
        destino, "Relatório de Ocupação de Vagas (RF41)", gerarRelatorioOcupacaoVagas());
  }

  /** RF42: Exporta o relatório de reprovação por disciplina em PDF. */
  public Path exportarRelatorioReprovacaoPorDisciplinaPdf(String idDisciplina, Path destino) {
    List<String> linhas = new ArrayList<>();
    Disciplina disciplina = buscarDisciplinaPorId(idDisciplina.trim());
    if (disciplina != null) {
      linhas.add("Disciplina: " + disciplina.getCodigo() + " - " + disciplina.getNome());
      linhas.add("");
    }
    linhas.addAll(gerarRelatorioReprovacaoPorDisciplina(idDisciplina));
    return PdfRelatorioWriter.escrever(
        destino, "Relatório de Reprovação por Disciplina (RF42)", linhas);
  }

  /** RF43: Exporta o relatório geral de usuários cadastrados em PDF. */
  public Path exportarRelatorioGeralUsuariosPdf(Path destino) {
    return PdfRelatorioWriter.escrever(
        destino, "Relatório Geral de Usuários Cadastrados (RF43)", gerarRelatorioGeralUsuarios());
  }

  // ==================== Métodos Auxiliares de Validação ====================

  private void validarCoordenadorAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
      throw new IllegalArgumentException("Apenas coordenadores podem gerar relatórios acadêmicos.");
    }
  }

  private void validarTurmaExistente(String idTurma) {
    if (idTurma == null || idTurma.trim().isEmpty()) {
      throw new IllegalArgumentException("id da turma é obrigatório");
    }
    for (Turma turma : turmas) {
      if (turma.getId().equals(idTurma.trim())) {
        return;
      }
    }
    throw new IllegalArgumentException("Turma não encontrada: " + idTurma);
  }

  private void validarDisciplinaExistente(String idDisciplina) {
    if (idDisciplina == null || idDisciplina.trim().isEmpty()) {
      throw new IllegalArgumentException("id da disciplina é obrigatório");
    }
    if (buscarDisciplinaPorId(idDisciplina.trim()) == null) {
      throw new IllegalArgumentException("Disciplina não encontrada: " + idDisciplina);
    }
  }

  private Disciplina buscarDisciplinaPorId(String idDisciplina) {
    for (Disciplina disciplina : disciplinas) {
      if (disciplina.getId().equals(idDisciplina)) {
        return disciplina;
      }
    }
    return null;
  }

  private Usuario buscarUsuarioPorId(String idUsuario) {
    for (Usuario usuario : usuarios) {
      if (usuario.getId().equals(idUsuario)) {
        return usuario;
      }
    }
    return null;
  }
}
