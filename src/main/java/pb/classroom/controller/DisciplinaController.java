package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;

public class DisciplinaController {

    private final AutenticacaoController autenticacaoController;
    private final List<Disciplina> disciplinas;

    public DisciplinaController(AutenticacaoController autenticacaoController, List<Disciplina> disciplinas) {
        if (autenticacaoController == null) {
            throw new IllegalArgumentException("controle de autenticação é obrigatório");
        }
        if (disciplinas == null) {
            throw new IllegalArgumentException("lista de disciplinas é obrigatória");
        }
        this.autenticacaoController = autenticacaoController;
        this.disciplinas = new ArrayList<>(disciplinas);
    }

    public Disciplina cadastrarDisciplina(
            String codigo,
            String nome,
            int cargaHoraria,
            int creditos,
            String idCurso,
            List<String> preRequisitosIds) {
        validarCoordenadorAutenticado();
        validarCodigoDisponivel(codigo);
        validarPreRequisitosExistentes(preRequisitosIds);

        Disciplina disciplina = new Disciplina(codigo, nome, cargaHoraria, creditos, idCurso);
        disciplina.setPreRequisitosIds(preRequisitosIds);
        disciplinas.add(disciplina);
        return disciplina;
    }

    public List<Disciplina> getDisciplinas() {
        return Collections.unmodifiableList(disciplinas);
    }

    private void validarCoordenadorAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            throw new IllegalArgumentException("Apenas coordenadores podem cadastrar disciplinas.");
        }
    }

    private void validarCodigoDisponivel(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("código da disciplina é obrigatório");
        }

        for (Disciplina disciplina : disciplinas) {
            if (disciplina.getCodigo().equalsIgnoreCase(codigo.trim())) {
                throw new IllegalArgumentException("Já existe disciplina cadastrada com esse código.");
            }
        }
    }

    private void validarPreRequisitosExistentes(List<String> preRequisitosIds) {
        if (preRequisitosIds == null) {
            return;
        }

        for (String idPreRequisito : preRequisitosIds) {
            if (buscarPorId(idPreRequisito) == null) {
                throw new IllegalArgumentException("Pré-requisito não encontrado: " + idPreRequisito);
            }
        }
    }

    private Disciplina buscarPorId(String id) {
        if (id == null) {
            return null;
        }

        for (Disciplina disciplina : disciplinas) {
            if (disciplina.getId().equals(id.trim())) {
                return disciplina;
            }
        }
        return null;
    }
}
