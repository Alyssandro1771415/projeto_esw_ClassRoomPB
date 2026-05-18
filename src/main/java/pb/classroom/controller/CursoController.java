package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pb.classroom.model.Curso;
import pb.classroom.model.PerfilUsuario;

public class CursoController {

    private final AutenticacaoController autenticacaoController;
    private final List<Curso> cursos;

    public CursoController(AutenticacaoController autenticacaoController, List<Curso> cursos) {
        if (autenticacaoController == null) {
            throw new IllegalArgumentException("controle de autenticação é obrigatório");
        }
        if (cursos == null) {
            throw new IllegalArgumentException("lista de cursos é obrigatória");
        }
        this.autenticacaoController = autenticacaoController;
        this.cursos = new ArrayList<>(cursos);
    }

    public Curso cadastrarCurso(String nome, String codigo) {
        validarAdministradorAutenticado();
        validarCursoDisponivel(nome, codigo);

        Curso curso = new Curso(nome);
        curso.setCodigo(codigo);
        cursos.add(curso);
        return curso;
    }

    public List<Curso> getCursos() {
        return Collections.unmodifiableList(cursos);
    }

    private void validarAdministradorAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new IllegalArgumentException("Apenas administradores podem cadastrar cursos.");
        }
    }

    private void validarCursoDisponivel(String nome, String codigo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("nome do curso é obrigatório");
        }

        String nomeLimpo = nome.trim();
        String codigoLimpo = codigo == null ? null : codigo.trim();
        for (Curso curso : cursos) {
            if (curso.getNome().equalsIgnoreCase(nomeLimpo)) {
                throw new IllegalArgumentException("Já existe curso cadastrado com esse nome.");
            }
            if (codigoLimpo != null
                    && !codigoLimpo.isEmpty()
                    && curso.getCodigo() != null
                    && curso.getCodigo().equalsIgnoreCase(codigoLimpo)) {
                throw new IllegalArgumentException("Já existe curso cadastrado com esse código.");
            }
        }
    }
}
