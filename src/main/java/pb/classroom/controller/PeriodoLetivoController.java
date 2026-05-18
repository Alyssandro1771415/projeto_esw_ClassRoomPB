package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;

public class PeriodoLetivoController {

    private final AutenticacaoController autenticacaoController;
    private final List<PeriodoLetivo> periodosLetivos;

    public PeriodoLetivoController(
            AutenticacaoController autenticacaoController,
            List<PeriodoLetivo> periodosLetivos) {
        if (autenticacaoController == null) {
            throw new IllegalArgumentException("controle de autenticação é obrigatório");
        }
        if (periodosLetivos == null) {
            throw new IllegalArgumentException("lista de períodos letivos é obrigatória");
        }
        this.autenticacaoController = autenticacaoController;
        this.periodosLetivos = new ArrayList<>(periodosLetivos);
    }

    public PeriodoLetivo cadastrarPeriodoLetivo(String codigo) {
        validarCoordenadorAutenticado();
        validarCodigoDisponivel(codigo);

        PeriodoLetivo periodoLetivo = new PeriodoLetivo(codigo);
        periodosLetivos.add(periodoLetivo);
        return periodoLetivo;
    }

    public PeriodoLetivo ativarPeriodoLetivo(String id) {
        validarCoordenadorAutenticado();
        PeriodoLetivo periodoLetivo = buscarPorIdObrigatorio(id);
        periodoLetivo.ativar();
        return periodoLetivo;
    }

    public PeriodoLetivo encerrarPeriodoLetivo(String id) {
        validarCoordenadorAutenticado();
        PeriodoLetivo periodoLetivo = buscarPorIdObrigatorio(id);
        periodoLetivo.encerrar();
        return periodoLetivo;
    }

    public List<PeriodoLetivo> getPeriodosLetivos() {
        return Collections.unmodifiableList(periodosLetivos);
    }

    private void validarCoordenadorAutenticado() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            throw new IllegalArgumentException("Apenas coordenadores podem gerenciar períodos letivos.");
        }
    }

    private void validarCodigoDisponivel(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("codigo do período letivo é obrigatório");
        }

        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            if (periodoLetivo.getCodigo().equalsIgnoreCase(codigo.trim())) {
                throw new IllegalArgumentException("Já existe período letivo cadastrado com esse código.");
            }
        }
    }

    private PeriodoLetivo buscarPorIdObrigatorio(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id do período letivo é obrigatório");
        }

        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            if (periodoLetivo.getId().equals(id.trim())) {
                return periodoLetivo;
            }
        }
        throw new IllegalArgumentException("Período letivo não encontrado.");
    }
}
