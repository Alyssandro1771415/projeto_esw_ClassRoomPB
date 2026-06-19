package pb.classroom.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusPresenca;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

/**
 * Controla o registro e consulta de presença/falta dos alunos por turma (RF27). O professor da
 * turma registra presença; coordenador e professor consultam; aluno consulta suas próprias.
 */
public class PresencaController {

  private final AutenticacaoController autenticacaoController;
  private final List<RegistroPresenca> registros;
  private final List<Turma> turmas;
  private final List<Matricula> matriculas;

  public PresencaController(
      AutenticacaoController autenticacaoController,
      List<RegistroPresenca> registros,
      List<Turma> turmas,
      List<Matricula> matriculas) {
    if (autenticacaoController == null) {
      throw new IllegalArgumentException("controle de autenticacao e obrigatorio");
    }
    if (registros == null || turmas == null || matriculas == null) {
      throw new IllegalArgumentException("listas de apoio são obrigatórias");
    }
    this.autenticacaoController = autenticacaoController;
    this.registros = new ArrayList<>(registros);
    this.turmas = turmas;
    this.matriculas = matriculas;
  }

  /**
   * RF27: Professor da turma registra presença/falta de múltiplos alunos em uma data.
   *
   * @param idTurma id da turma
   * @param data data da aula
   * @param presencas mapa de idAluno → true (presente) ou false (falta)
   * @return lista dos registros criados
   */
  public List<RegistroPresenca> registrarPresenca(
      String idTurma, LocalDate data, Map<String, Boolean> presencas) {
    Usuario professor = validarProfessorAutenticado();
    Turma turma = buscarTurmaObrigatoria(idTurma);
    validarProfessorDaTurma(professor, turma);
    validarTurmaNaoCancelada(turma);
    validarDataPresenca(data, turma);

    List<RegistroPresenca> novosRegistros = new ArrayList<>();
    for (Map.Entry<String, Boolean> entry : presencas.entrySet()) {
      String idAluno = entry.getKey();
      boolean presente = entry.getValue();
      validarAlunoMatriculadoNaTurma(idAluno, turma);
      validarRegistroDuplicado(idTurma, idAluno, data);

      StatusPresenca status = presente ? StatusPresenca.PRESENTE : StatusPresenca.FALTA;
      RegistroPresenca registro = new RegistroPresenca(idTurma, idAluno, data, status);
      registros.add(registro);
      novosRegistros.add(registro);
    }
    return Collections.unmodifiableList(novosRegistros);
  }

  /** RF27: Consulta todas as presenças de uma turma. Apenas coordenador ou professor da turma. */
  public List<RegistroPresenca> consultarPresencasPorTurma(String idTurma) {
    validarCoordenadorOuProfessorDaTurma(idTurma);
    Turma turma = buscarTurmaObrigatoria(idTurma);
    List<RegistroPresenca> resultado = new ArrayList<>();
    for (RegistroPresenca registro : registros) {
      if (registro.getIdTurma().equals(turma.getId())) {
        resultado.add(registro);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  /** RF27: Aluno consulta suas próprias presenças em uma turma. */
  public List<RegistroPresenca> consultarPresencasDoAluno(String idTurma) {
    Usuario aluno = validarAlunoAutenticado();
    Turma turma = buscarTurmaObrigatoria(idTurma);
    List<RegistroPresenca> resultado = new ArrayList<>();
    for (RegistroPresenca registro : registros) {
      if (registro.getIdTurma().equals(turma.getId())
          && registro.getIdAluno().equals(aluno.getId())) {
        resultado.add(registro);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  public List<RegistroPresenca> getRegistrosPresenca() {
    return Collections.unmodifiableList(registros);
  }

  private Usuario validarProfessorAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.PROFESSOR) {
      throw new IllegalArgumentException("Apenas professores podem registrar presença.");
    }
    return autenticacaoController.getUsuarioLogado();
  }

  private Usuario validarAlunoAutenticado() {
    if (!autenticacaoController.isAutenticado()
        || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ALUNO) {
      throw new IllegalArgumentException("Apenas alunos podem consultar suas próprias presenças.");
    }
    return autenticacaoController.getUsuarioLogado();
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
      throw new IllegalArgumentException(
          "Professor só pode registrar presença em suas próprias turmas.");
    }
  }

  private void validarTurmaNaoCancelada(Turma turma) {
    if (turma.isCancelada()) {
      throw new IllegalArgumentException("Não é possível registrar presença em turma cancelada.");
    }
  }

  private void validarDataPresenca(LocalDate data, Turma turma) {
    if (data == null) {
      throw new IllegalArgumentException("data é obrigatória");
    }
    if (data.isBefore(turma.getDataInicioAulas())) {
      throw new IllegalArgumentException(
          "Data da presença não pode ser anterior ao início das aulas.");
    }
    if (data.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Data da presença não pode ser futura.");
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

  private void validarRegistroDuplicado(String idTurma, String idAluno, LocalDate data) {
    for (RegistroPresenca registro : registros) {
      if (registro.getIdTurma().equals(idTurma)
          && registro.getIdAluno().equals(idAluno)
          && registro.getData().equals(data)) {
        throw new IllegalArgumentException(
            "Já existe registro de presença para este aluno nesta data.");
      }
    }
  }

  private void validarCoordenadorOuProfessorDaTurma(String idTurma) {
    if (!autenticacaoController.isAutenticado()) {
      throw new IllegalArgumentException(
          "Apenas coordenadores ou o professor da turma podem consultar presenças.");
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
          "Professor só pode consultar presenças de suas próprias turmas.");
    }
    throw new IllegalArgumentException(
        "Apenas coordenadores ou o professor da turma podem consultar presenças.");
  }
}
