package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Disciplina;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

public class MatriculaController {

    private final AutenticacaoController autenticacaoController;
    private final List<Matricula> matriculas;
    private final List<Turma> turmas;
    private final List<PeriodoLetivo> periodosLetivos;
    private final List<Disciplina> disciplinas;

    public MatriculaController(
            AutenticacaoController autenticacaoController,
            List<Matricula> matriculas,
            List<Turma> turmas,
            List<PeriodoLetivo> periodosLetivos,
            List<Disciplina> disciplinas) {
        if (autenticacaoController == null) {
            throw new IllegalArgumentException("controle de autenticação é obrigatório");
        }
        if (matriculas == null || turmas == null || periodosLetivos == null || disciplinas == null) {
            throw new IllegalArgumentException("listas de apoio são obrigatórias");
        }
        this.autenticacaoController = autenticacaoController;
        this.matriculas = new ArrayList<>(matriculas);
        this.turmas = turmas;
        this.periodosLetivos = periodosLetivos;
        this.disciplinas = disciplinas;
    }

    /**
     * Construtor de compatibilidade — sem lista de disciplinas, desabilita
     * validação de pré-requisitos.
     */
    public MatriculaController(
            AutenticacaoController autenticacaoController,
            List<Matricula> matriculas,
            List<Turma> turmas,
            List<PeriodoLetivo> periodosLetivos) {
        this(autenticacaoController, matriculas, turmas, periodosLetivos, new ArrayList<>());
    }

    /**
     * RF20 — Confirmar matrícula automática.
     * Valida: aluno autenticado, turma disponível, duplicidade,
     * vagas disponíveis, pré-requisitos atendidos e choque de horário.
     */
    public Matricula solicitarMatricula(String idTurma) {
        Usuario aluno = validarAlunoAutenticado();
        Turma turma = buscarTurmaObrigatoria(idTurma);
        validarTurmaDisponivel(turma);
        validarMatriculaDuplicada(aluno.getId(), turma.getId());
        validarVagasDisponiveis(turma);
        validarPreRequisitos(aluno.getId(), turma);
        validarChoqueHorario(aluno.getId(), turma);

        Matricula matricula = new Matricula(aluno.getId(), turma.getId());
        matriculas.add(matricula);
        return matricula;
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }

    private Usuario validarAlunoAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ALUNO) {
            throw new IllegalArgumentException("Apenas alunos podem solicitar matrícula.");
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

    private void validarTurmaDisponivel(Turma turma) {
        if (turma.isCancelada() || !periodoLetivoEstaAtivo(turma.getIdPeriodoLetivo())) {
            throw new IllegalArgumentException("Turma não está disponível para matrícula.");
        }
    }

    private boolean periodoLetivoEstaAtivo(String idPeriodoLetivo) {
        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            if (periodoLetivo.getId().equals(idPeriodoLetivo) && periodoLetivo.isAtivo()) {
                return true;
            }
        }
        return false;
    }

    private void validarMatriculaDuplicada(String idAluno, String idTurma) {
        for (Matricula matricula : matriculas) {
            if (matricula.getIdAluno().equals(idAluno) && matricula.getIdTurma().equals(idTurma)) {
                throw new IllegalArgumentException("Aluno já está matriculado nesta turma.");
            }
        }
    }

    private void validarVagasDisponiveis(Turma turma) {
        int matriculados = 0;
        for (Matricula matricula : matriculas) {
            if (matricula.getIdTurma().equals(turma.getId())) {
                matriculados++;
            }
        }
        if (matriculados >= turma.getLimiteVagas()) {
            throw new IllegalArgumentException("Turma sem vagas disponíveis.");
        }
    }

    private void validarPreRequisitos(String idAluno, Turma turma) {
        Disciplina disciplinaDaTurma = buscarDisciplinaPorId(turma.getIdDisciplina());
        if (disciplinaDaTurma == null) {
            return;
        }

        for (String idPreRequisito : disciplinaDaTurma.getPreRequisitosIds()) {
            if (!alunoPossuiMatriculaEmDisciplina(idAluno, idPreRequisito)) {
                Disciplina preReq = buscarDisciplinaPorId(idPreRequisito);
                String descricao = preReq != null ? preReq.getCodigo() : idPreRequisito;
                throw new IllegalArgumentException("Pré-requisito não atendido: " + descricao);
            }
        }
    }

    private boolean alunoPossuiMatriculaEmDisciplina(String idAluno, String idDisciplina) {
        for (Matricula matricula : matriculas) {
            if (!matricula.getIdAluno().equals(idAluno)) {
                continue;
            }
            Turma turmaMatriculada = buscarTurmaPorId(matricula.getIdTurma());
            if (turmaMatriculada != null
                    && turmaMatriculada.getIdDisciplina().equals(idDisciplina)) {
                return true;
            }
        }
        return false;
    }

    private Disciplina buscarDisciplinaPorId(String idDisciplina) {
        for (Disciplina disciplina : disciplinas) {
            if (disciplina.getId().equals(idDisciplina)) {
                return disciplina;
            }
        }
        return null;
    }

    private void validarChoqueHorario(String idAluno, Turma novaTurma) {
        for (Matricula matricula : matriculas) {
            if (!matricula.getIdAluno().equals(idAluno)) {
                continue;
            }

            Turma turmaMatriculada = buscarTurmaPorId(matricula.getIdTurma());
            if (turmaMatriculada == null
                    || turmaMatriculada.isCancelada()
                    || !turmaMatriculada.getIdPeriodoLetivo().equals(novaTurma.getIdPeriodoLetivo())) {
                continue;
            }

            for (BlocoHorario existente : turmaMatriculada.getHorarios()) {
                for (BlocoHorario novo : novaTurma.getHorarios()) {
                    if (temChoque(existente, novo)) {
                        throw new IllegalArgumentException(
                                "Choque de horário com outra turma em que o aluno está matriculado.");
                    }
                }
            }
        }
    }

    private Turma buscarTurmaPorId(String idTurma) {
        for (Turma turma : turmas) {
            if (turma.getId().equals(idTurma)) {
                return turma;
            }
        }
        return null;
    }

    private boolean temChoque(BlocoHorario existente, BlocoHorario novo) {
        return existente.getDiaSemana() == novo.getDiaSemana()
                && existente.getHoraInicio().isBefore(novo.getHoraFim())
                && novo.getHoraInicio().isBefore(existente.getHoraFim());
    }
}
