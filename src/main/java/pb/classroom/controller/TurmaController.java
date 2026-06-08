package pb.classroom.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

public class TurmaController {

    private final AutenticacaoController autenticacaoController;
    private final List<Turma> turmas;
    private final List<Disciplina> disciplinas;
    private final List<PeriodoLetivo> periodosLetivos;
    private final List<Usuario> usuarios;

    public TurmaController(
            AutenticacaoController autenticacaoController,
            List<Turma> turmas,
            List<Disciplina> disciplinas,
            List<PeriodoLetivo> periodosLetivos,
            List<Usuario> usuarios) {
        if (autenticacaoController == null) {
            throw new IllegalArgumentException("controle de autenticação é obrigatório");
        }
        if (turmas == null || disciplinas == null || periodosLetivos == null || usuarios == null) {
            throw new IllegalArgumentException("listas de apoio são obrigatórias");
        }
        this.autenticacaoController = autenticacaoController;
        this.turmas = new ArrayList<>(turmas);
        this.disciplinas = disciplinas;
        this.periodosLetivos = periodosLetivos;
        this.usuarios = usuarios;
    }

    public Turma ofertarTurma(
            String idDisciplina,
            String idPeriodoLetivo,
            String idProfessor,
            int limiteVagas,
            String sala,
            LocalDate dataInicioAulas,
            List<BlocoHorario> horarios) {
        validarCoordenadorAutenticado();
        validarDisciplinaExistente(idDisciplina);
        validarPeriodoLetivoExistente(idPeriodoLetivo);
        validarProfessorResponsavel(idProfessor);
        validarChoqueProfessor(null, idProfessor, horarios);

        Turma turma = new Turma(
                idDisciplina,
                idPeriodoLetivo,
                idProfessor,
                limiteVagas,
                sala,
                dataInicioAulas,
                horarios);
        turmas.add(turma);
        return turma;
    }

    public Turma alterarTurma(
            String idTurma,
            String idProfessor,
            int limiteVagas,
            String sala,
            LocalDate dataInicioAulas,
            List<BlocoHorario> horarios) {
        validarCoordenadorAutenticado();
        Turma turma = buscarTurmaObrigatoria(idTurma);
        validarAntesDoInicio(turma);
        validarProfessorResponsavel(idProfessor);
        validarChoqueProfessor(turma.getId(), idProfessor, horarios);

        turma.setIdProfessor(idProfessor);
        turma.setLimiteVagas(limiteVagas);
        turma.setSala(sala);
        turma.setDataInicioAulas(dataInicioAulas);
        turma.setHorarios(horarios);
        return turma;
    }

    public Turma cancelarTurma(String idTurma) {
        validarCoordenadorAutenticado();
        Turma turma = buscarTurmaObrigatoria(idTurma);
        validarAntesDoInicio(turma);
        turma.setCancelada(true);
        return turma;
    }

    public List<Turma> consultarTurmasDisponiveisParaAluno() {
        validarAlunoAutenticado();
        return Collections.unmodifiableList(filtrarTurmasDisponiveis());
    }

    public List<Disciplina> consultarDisciplinasDisponiveisParaAluno() {
        validarAlunoAutenticado();
        Set<String> idsDisciplinasComTurmaDisponivel = new HashSet<>();
        for (Turma turma : filtrarTurmasDisponiveis()) {
            idsDisciplinasComTurmaDisponivel.add(turma.getIdDisciplina());
        }

        List<Disciplina> disponiveis = new ArrayList<>();
        for (Disciplina disciplina : disciplinas) {
            if (idsDisciplinasComTurmaDisponivel.contains(disciplina.getId())) {
                disponiveis.add(disciplina);
            }
        }
        return Collections.unmodifiableList(disponiveis);
    }

    public List<Turma> getTurmas() {
        return Collections.unmodifiableList(turmas);
    }

    private void validarCoordenadorAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            throw new IllegalArgumentException("Apenas coordenadores podem gerenciar turmas.");
        }
    }

    private void validarAlunoAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ALUNO) {
            throw new IllegalArgumentException("Apenas alunos podem consultar disciplinas e turmas disponíveis.");
        }
    }

    private List<Turma> filtrarTurmasDisponiveis() {
        List<Turma> disponiveis = new ArrayList<>();
        for (Turma turma : turmas) {
            if (!turma.isCancelada() && periodoLetivoEstaAtivo(turma.getIdPeriodoLetivo())) {
                disponiveis.add(turma);
            }
        }
        return disponiveis;
    }

    private boolean periodoLetivoEstaAtivo(String idPeriodoLetivo) {
        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            if (periodoLetivo.getId().equals(idPeriodoLetivo) && periodoLetivo.isAtivo()) {
                return true;
            }
        }
        return false;
    }

    private void validarDisciplinaExistente(String idDisciplina) {
        if (idDisciplina == null || idDisciplina.trim().isEmpty()) {
            throw new IllegalArgumentException("id da disciplina é obrigatório");
        }
        for (Disciplina disciplina : disciplinas) {
            if (disciplina.getId().equals(idDisciplina.trim())) {
                return;
            }
        }
        throw new IllegalArgumentException("Disciplina não encontrada: " + idDisciplina);
    }

    private void validarPeriodoLetivoExistente(String idPeriodoLetivo) {
        if (idPeriodoLetivo == null || idPeriodoLetivo.trim().isEmpty()) {
            throw new IllegalArgumentException("id do periodo letivo é obrigatório");
        }
        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            if (periodoLetivo.getId().equals(idPeriodoLetivo.trim())) {
                return;
            }
        }
        throw new IllegalArgumentException("Periodo letivo não encontrado: " + idPeriodoLetivo);
    }

    private void validarProfessorResponsavel(String idProfessor) {
        if (idProfessor == null || idProfessor.trim().isEmpty()) {
            throw new IllegalArgumentException("id do professor responsavel é obrigatório");
        }
        for (Usuario usuario : usuarios) {
            if (usuario.getId().equals(idProfessor.trim())) {
                if (usuario.getPerfil() != PerfilUsuario.PROFESSOR) {
                    throw new IllegalArgumentException("Usuario informado não possui perfil PROFESSOR.");
                }
                return;
            }
        }
        throw new IllegalArgumentException("Professor não encontrado: " + idProfessor);
    }

    private void validarChoqueProfessor(String idTurmaIgnorada, String idProfessor, List<BlocoHorario> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            throw new IllegalArgumentException("a turma deve ter ao menos um bloco de horário");
        }

        for (Turma turma : turmas) {
            if (turma.isCancelada()
                    || turma.getId().equals(idTurmaIgnorada)
                    || !turma.getIdProfessor().equals(idProfessor.trim())) {
                continue;
            }

            for (BlocoHorario existente : turma.getHorarios()) {
                for (BlocoHorario novo : horarios) {
                    if (temChoque(existente, novo)) {
                        throw new IllegalArgumentException("Professor já possui turma nesse horário.");
                    }
                }
            }
        }
    }

    private boolean temChoque(BlocoHorario existente, BlocoHorario novo) {
        return existente.getDiaSemana() == novo.getDiaSemana()
                && existente.getHoraInicio().isBefore(novo.getHoraFim())
                && novo.getHoraInicio().isBefore(existente.getHoraFim());
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

    private void validarAntesDoInicio(Turma turma) {
        if (!LocalDate.now().isBefore(turma.getDataInicioAulas())) {
            throw new IllegalArgumentException("Turma não pode ser alterada ou cancelada após o início das aulas.");
        }
    }
}
