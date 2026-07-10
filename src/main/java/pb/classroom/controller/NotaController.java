package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.EtapaAvaliacao;
import pb.classroom.model.FrequenciaAluno;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.RegistroNota;
import pb.classroom.model.ResultadoAvaliacao;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

/**
 * Controla lançamento e consulta de notas, cálculo de média/situação (RF31–RF35) e fechamento de
 * turma com geração de histórico.
 */
public class NotaController {

  private final AutenticacaoController autenticacaoController;
  private final PresencaController presencaController;
  private final List<RegistroNota> notas;
  private final List<HistoricoAcademico> historicos;
  private final List<Turma> turmas;
  private final List<Matricula> matriculas;

  public NotaController(
      AutenticacaoController autenticacaoController,
      PresencaController presencaController,
      List<RegistroNota> notas,
      List<HistoricoAcademico> historicos,
      List<Turma> turmas,
      List<Matricula> matriculas) {
    if (autenticacaoController == null || presencaController == null) {
      throw new IllegalArgumentException("controles de autenticação e presença são obrigatórios");
    }
    if (notas == null || historicos == null || turmas == null || matriculas == null) {
      throw new IllegalArgumentException("listas de apoio são obrigatórias");
    }
    this.autenticacaoController = autenticacaoController;
    this.presencaController = presencaController;
    this.notas = new ArrayList<>(notas);
    this.historicos = historicos;
    this.turmas = turmas;
    this.matriculas = matriculas;
  }

  /** RF31: Professor lança nota de etapa1 ou etapa2 para aluno matriculado. */
  public RegistroNota lancarNota(
      String idTurma, String idAluno, EtapaAvaliacao etapa, double valor) {
    return registrarOuAlterarNota(idTurma, idAluno, etapa, valor, false);
  }

  /** RF35: Professor altera nota antes do fechamento da turma. */
  public RegistroNota alterarNota(
      String idTurma, String idAluno, EtapaAvaliacao etapa, double valor) {
    return registrarOuAlterarNota(idTurma, idAluno, etapa, valor, true);
  }

  /** RF32 e RF34: Calcula média final e situação do aluno na turma. */
  public ResultadoAvaliacao calcularResultado(String idTurma, String idAluno) {
    Turma turma = buscarTurmaObrigatoria(idTurma);
    validarCampoObrigatorio(idAluno, "id do aluno");
    RegistroNota registro = buscarNotaPorTurmaEAluno(turma.getId(), idAluno.trim());
    FrequenciaAluno frequencia =
        presencaController.calcularFrequenciaAluno(turma.getId(), idAluno.trim());
    return montarResultado(registro, idAluno.trim(), turma.getId(), frequencia.getPercentual());
  }

  /** RF33: Aluno consulta suas notas em todas as turmas matriculadas. */
  public List<ResultadoAvaliacao> consultarMinhasNotas() {
    Usuario aluno = validarAlunoAutenticado();
    List<ResultadoAvaliacao> resultados = new ArrayList<>();
    for (Matricula matricula : matriculas) {
      if (matricula.getIdAluno().equals(aluno.getId()) && matricula.isConfirmada()) {
        resultados.add(calcularResultado(matricula.getIdTurma(), aluno.getId()));
      }
    }
    return Collections.unmodifiableList(resultados);
  }

  /** Professor ou coordenador consulta notas/resultados de uma turma. */
  public List<ResultadoAvaliacao> consultarResultadosPorTurma(String idTurma) {
    validarCoordenadorOuProfessorDaTurma(idTurma);
    Turma turma = buscarTurmaObrigatoria(idTurma);
    List<ResultadoAvaliacao> resultados = new ArrayList<>();
    for (String idAluno : listarAlunosConfirmadosNaTurma(turma.getId())) {
      resultados.add(calcularResultado(turma.getId(), idAluno));
    }
    return Collections.unmodifiableList(resultados);
  }

  /**
   * Fecha a turma (coordenador), bloqueia alteração de notas e gera histórico acadêmico (RF35,
   * RF36).
   */
  public List<HistoricoAcademico> fecharTurma(String idTurma) {
    validarCoordenadorAutenticado();
    Turma turma = buscarTurmaObrigatoria(idTurma);
    validarTurmaNaoCancelada(turma);
    if (turma.isFechada()) {
      throw new IllegalArgumentException("Turma já está fechada.");
    }

    List<HistoricoAcademico> gerados = new ArrayList<>();
    for (String idAluno : listarAlunosConfirmadosNaTurma(turma.getId())) {
      if (possuiHistoricoParaTurma(idAluno, turma.getId())) {
        continue;
      }
      ResultadoAvaliacao resultado = calcularResultado(turma.getId(), idAluno);
      HistoricoAcademico historico =
          new HistoricoAcademico(
              idAluno,
              turma.getIdDisciplina(),
              turma.getIdPeriodoLetivo(),
              turma.getIdProfessor(),
              turma.getId(),
              resultado.getMediaFinal(),
              resultado.getPercentualFrequencia(),
              resultado.getSituacao());
      historicos.add(historico);
      gerados.add(historico);
    }
    turma.setFechada(true);
    return Collections.unmodifiableList(gerados);
  }

  public List<RegistroNota> getNotas() {
    return Collections.unmodifiableList(notas);
  }

  private RegistroNota registrarOuAlterarNota(
      String idTurma, String idAluno, EtapaAvaliacao etapa, double valor, boolean alteracao) {
    if (etapa == null) {
      throw new IllegalArgumentException("etapa de avaliação é obrigatória");
    }
    if (etapa == EtapaAvaliacao.RECUPERACAO) {
      throw new IllegalArgumentException(
          "Use lancarNotaRecuperacao para registrar nota de recuperação.");
    }

    Usuario professor = validarProfessorAutenticado();
    Turma turma = buscarTurmaObrigatoria(idTurma);
    validarProfessorDaTurma(professor, turma);
    validarTurmaNaoCancelada(turma);
    validarTurmaNaoFechada(turma);
    String idAlunoNormalizado = validarCampoObrigatorio(idAluno, "id do aluno");
    validarAlunoMatriculadoNaTurma(idAlunoNormalizado, turma);

    RegistroNota registro = buscarOuCriarNota(turma.getId(), idAlunoNormalizado);
    if (alteracao) {
      validarNotaExistenteParaAlteracao(registro, etapa);
    }
    registro.definirNota(etapa, valor);
    return registro;
  }

  /** Professor lança nota de recuperação quando aluno está em recuperação. */
  public RegistroNota lancarNotaRecuperacao(String idTurma, String idAluno, double valor) {
    Usuario professor = validarProfessorAutenticado();
    Turma turma = buscarTurmaObrigatoria(idTurma);
    validarProfessorDaTurma(professor, turma);
    validarTurmaNaoCancelada(turma);
    validarTurmaNaoFechada(turma);
    String idAlunoNormalizado = validarCampoObrigatorio(idAluno, "id do aluno");
    validarAlunoMatriculadoNaTurma(idAlunoNormalizado, turma);

    RegistroNota registro = buscarOuCriarNota(turma.getId(), idAlunoNormalizado);
    ResultadoAvaliacao antesRecuperacao =
        montarResultado(
            registro,
            idAlunoNormalizado,
            turma.getId(),
            presencaController
                .calcularFrequenciaAluno(turma.getId(), idAlunoNormalizado)
                .getPercentual());
    if (antesRecuperacao.getSituacao() != pb.classroom.model.SituacaoAcademica.EM_RECUPERACAO) {
      throw new IllegalArgumentException(
          "Nota de recuperação só pode ser lançada para alunos em recuperação.");
    }
    registro.definirNota(EtapaAvaliacao.RECUPERACAO, valor);
    return registro;
  }

  private void validarNotaExistenteParaAlteracao(RegistroNota registro, EtapaAvaliacao etapa) {
    Double notaAtual =
        etapa == EtapaAvaliacao.ETAPA1 ? registro.getNotaEtapa1() : registro.getNotaEtapa2();
    if (notaAtual == null) {
      throw new IllegalArgumentException(
          "Não existe nota lançada para alteração na etapa informada.");
    }
  }

  private RegistroNota buscarOuCriarNota(String idTurma, String idAluno) {
    RegistroNota existente = buscarNotaPorTurmaEAluno(idTurma, idAluno);
    if (existente != null) {
      return existente;
    }
    RegistroNota novo = new RegistroNota(idTurma, idAluno);
    notas.add(novo);
    return novo;
  }

  private RegistroNota buscarNotaPorTurmaEAluno(String idTurma, String idAluno) {
    for (RegistroNota nota : notas) {
      if (nota.getIdTurma().equals(idTurma) && nota.getIdAluno().equals(idAluno)) {
        return nota;
      }
    }
    return null;
  }

  private ResultadoAvaliacao montarResultado(
      RegistroNota registro, String idAluno, String idTurma, double percentualFrequencia) {
    if (registro == null) {
      return new ResultadoAvaliacao(idAluno, idTurma, null, null, null, percentualFrequencia);
    }
    return new ResultadoAvaliacao(
        idAluno,
        idTurma,
        registro.getNotaEtapa1(),
        registro.getNotaEtapa2(),
        registro.getNotaRecuperacao(),
        percentualFrequencia);
  }

  private boolean possuiHistoricoParaTurma(String idAluno, String idTurma) {
    for (HistoricoAcademico historico : historicos) {
      if (historico.getIdAluno().equals(idAluno) && historico.getIdTurma().equals(idTurma)) {
        return true;
      }
    }
    return false;
  }

  private List<String> listarAlunosConfirmadosNaTurma(String idTurma) {
    List<String> alunos = new ArrayList<>();
    for (Matricula matricula : matriculas) {
      if (matricula.getIdTurma().equals(idTurma) && matricula.isConfirmada()) {
        alunos.add(matricula.getIdAluno());
      }
    }
    return alunos;
  }

  private String validarCampoObrigatorio(String valor, String campo) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException(campo + " é obrigatório");
    }
    return valor.trim();
  }

  private Usuario validarProfessorAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.PROFESSOR) {
      throw new IllegalArgumentException("Apenas professores podem lançar ou alterar notas.");
    }
    return autenticacaoController.getUsuarioLogado();
  }

  private Usuario validarAlunoAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ALUNO) {
      throw new IllegalArgumentException("Apenas alunos podem consultar suas próprias notas.");
    }
    return autenticacaoController.getUsuarioLogado();
  }

  private void validarCoordenadorAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
      throw new IllegalArgumentException("Apenas coordenadores podem fechar turmas.");
    }
  }

  private Turma buscarTurmaObrigatoria(String idTurma) {
    if (idTurma == null || idTurma.trim().isEmpty()) {
      throw new IllegalArgumentException("id da turma é obrigatório");
    }
    for (Turma turma : turmas) {
      if (turma.getId().equals(idTurma.trim())) {
        return turma;
      }
    }
    throw new IllegalArgumentException("Turma não encontrada: " + idTurma);
  }

  private void validarProfessorDaTurma(Usuario professor, Turma turma) {
    if (!turma.getIdProfessor().equals(professor.getId())) {
      throw new IllegalArgumentException("Professor só pode lançar notas em suas próprias turmas.");
    }
  }

  private void validarTurmaNaoCancelada(Turma turma) {
    if (turma.isCancelada()) {
      throw new IllegalArgumentException("Não é possível lançar notas em turma cancelada.");
    }
  }

  private void validarTurmaNaoFechada(Turma turma) {
    if (turma.isFechada()) {
      throw new IllegalArgumentException("Turma fechada: notas não podem ser alteradas (RF35).");
    }
  }

  private void validarAlunoMatriculadoNaTurma(String idAluno, Turma turma) {
    for (Matricula matricula : matriculas) {
      if (matricula.getIdAluno().equals(idAluno)
          && matricula.getIdTurma().equals(turma.getId())
          && matricula.isConfirmada()) {
        return;
      }
    }
    throw new IllegalArgumentException(
        "Aluno não possui matrícula confirmada na turma: " + idAluno);
  }

  private void validarCoordenadorOuProfessorDaTurma(String idTurma) {
    if (!autenticacaoController.isAutenticado()) {
      throw new IllegalArgumentException(
          "Apenas coordenadores ou o professor da turma podem consultar notas.");
    }
    PerfilUsuario perfil = autenticacaoController.getUsuarioLogado().getPerfil();
    if (perfil == PerfilUsuario.COORDENADOR) {
      return;
    }
    if (perfil == PerfilUsuario.PROFESSOR) {
      Turma turma = buscarTurmaObrigatoria(idTurma);
      if (turma.getIdProfessor().equals(autenticacaoController.getUsuarioLogado().getId())) {
        return;
      }
      throw new IllegalArgumentException(
          "Professor só pode consultar notas de suas próprias turmas.");
    }
    throw new IllegalArgumentException(
        "Apenas coordenadores ou o professor da turma podem consultar notas.");
  }
}
