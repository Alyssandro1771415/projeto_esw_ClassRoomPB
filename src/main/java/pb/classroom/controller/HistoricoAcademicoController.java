package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.Disciplina;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.SituacaoAcademica;
import pb.classroom.model.Usuario;

/** Consulta de histórico acadêmico (RF36–RF39). */
public class HistoricoAcademicoController {

  private final AutenticacaoController autenticacaoController;
  private final List<HistoricoAcademico> historicos;
  private final List<Disciplina> disciplinas;
  private final List<Usuario> usuarios;

  public HistoricoAcademicoController(
      AutenticacaoController autenticacaoController,
      List<HistoricoAcademico> historicos,
      List<Disciplina> disciplinas,
      List<Usuario> usuarios) {
    if (autenticacaoController == null) {
      throw new IllegalArgumentException("controle de autenticação é obrigatório");
    }
    if (historicos == null || disciplinas == null || usuarios == null) {
      throw new IllegalArgumentException("listas de apoio são obrigatórias");
    }
    this.autenticacaoController = autenticacaoController;
    this.historicos = historicos;
    this.disciplinas = disciplinas;
    this.usuarios = usuarios;
  }

  /** RF38: Aluno consulta seu histórico acadêmico. */
  public List<HistoricoAcademico> consultarMeuHistorico() {
    Usuario aluno = validarAlunoAutenticado();
    return filtrarPorAluno(aluno.getId());
  }

  /** RF39: Coordenador consulta histórico de um aluno. */
  public List<HistoricoAcademico> consultarHistoricoAluno(String idAluno) {
    validarCoordenadorAutenticado();
    String idAlunoNormalizado = validarCampoObrigatorio(idAluno, "id do aluno");
    validarAlunoExistente(idAlunoNormalizado);
    return filtrarPorAluno(idAlunoNormalizado);
  }

  /** RF39: Coordenador consulta histórico dos alunos de um curso. */
  public List<HistoricoAcademico> consultarHistoricoPorCurso(String idCurso) {
    validarCoordenadorAutenticado();
    String idCursoNormalizado = validarCampoObrigatorio(idCurso, "id do curso");
    List<HistoricoAcademico> resultado = new ArrayList<>();
    for (HistoricoAcademico historico : historicos) {
      Disciplina disciplina = buscarDisciplinaPorId(historico.getIdDisciplina());
      if (disciplina != null && disciplina.getIdCurso().equals(idCursoNormalizado)) {
        resultado.add(historico);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  public boolean alunoAprovadoEmDisciplina(String idAluno, String idDisciplina) {
    for (HistoricoAcademico historico : historicos) {
      if (historico.getIdAluno().equals(idAluno)
          && historico.getIdDisciplina().equals(idDisciplina)
          && historico.getSituacao() == SituacaoAcademica.APROVADO) {
        return true;
      }
    }
    return false;
  }

  public List<HistoricoAcademico> getHistoricos() {
    return Collections.unmodifiableList(historicos);
  }

  private List<HistoricoAcademico> filtrarPorAluno(String idAluno) {
    List<HistoricoAcademico> resultado = new ArrayList<>();
    for (HistoricoAcademico historico : historicos) {
      if (historico.getIdAluno().equals(idAluno)) {
        resultado.add(historico);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  private Disciplina buscarDisciplinaPorId(String idDisciplina) {
    for (Disciplina disciplina : disciplinas) {
      if (disciplina.getId().equals(idDisciplina)) {
        return disciplina;
      }
    }
    return null;
  }

  private void validarAlunoExistente(String idAluno) {
    for (Usuario usuario : usuarios) {
      if (usuario.getId().equals(idAluno) && usuario.getPerfil() == PerfilUsuario.ALUNO) {
        return;
      }
    }
    throw new IllegalArgumentException("Aluno não encontrado: " + idAluno);
  }

  private String validarCampoObrigatorio(String valor, String campo) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException(campo + " é obrigatório");
    }
    return valor.trim();
  }

  private Usuario validarAlunoAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ALUNO) {
      throw new IllegalArgumentException(
          "Apenas alunos podem consultar seu próprio histórico acadêmico.");
    }
    return autenticacaoController.getUsuarioLogado();
  }

  private void validarCoordenadorAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
      throw new IllegalArgumentException(
          "Apenas coordenadores podem consultar histórico de alunos.");
    }
  }
}
