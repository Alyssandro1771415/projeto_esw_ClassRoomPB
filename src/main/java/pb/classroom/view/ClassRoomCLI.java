package pb.classroom.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pb.classroom.controller.AutenticacaoController;
import pb.classroom.controller.CursoController;
import pb.classroom.controller.DisciplinaController;
import pb.classroom.controller.PeriodoLetivoController;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Usuario;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.repository.UsuarioRepository;

public class ClassRoomCLI {

    private final Scanner scanner;
    private final AutenticacaoController autenticacaoController;
    private final CursoController cursoController;
    private final DisciplinaController disciplinaController;
    private final PeriodoLetivoController periodoLetivoController;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PeriodoLetivoRepository periodoLetivoRepository;

    public ClassRoomCLI() {
        this(
                new Scanner(System.in),
                new UsuarioRepository(),
                new DisciplinaRepository(),
                new CursoRepository(),
                new PeriodoLetivoRepository());
    }

    ClassRoomCLI(Scanner scanner, UsuarioRepository usuarioRepository) {
        this(scanner, usuarioRepository, new DisciplinaRepository(), new CursoRepository(), new PeriodoLetivoRepository());
    }

    ClassRoomCLI(
            Scanner scanner,
            UsuarioRepository usuarioRepository,
            DisciplinaRepository disciplinaRepository,
            CursoRepository cursoRepository,
            PeriodoLetivoRepository periodoLetivoRepository) {
        this.scanner = scanner;
        this.usuarioRepository = usuarioRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
        this.periodoLetivoRepository = periodoLetivoRepository;
        this.autenticacaoController = new AutenticacaoController(usuarioRepository.carregarUsuarios());
        this.cursoController = new CursoController(
                autenticacaoController,
                cursoRepository.carregarCursos());
        this.disciplinaController = new DisciplinaController(
                autenticacaoController,
                disciplinaRepository.carregarDisciplinas(),
                cursoController.getCursos());
        this.periodoLetivoController = new PeriodoLetivoController(
                autenticacaoController,
                periodoLetivoRepository.carregarPeriodosLetivos());
    }

    public void iniciar() {
        boolean executando = true;
        limparTerminal();
        while (executando) {
            exibirMenu();
            String opcao = lerLinha("Opção: ");
            limparTerminal();

            switch (opcao) {
                case "1":
                    realizarLogin();
                    break;
                case "2":
                    exibirUsuarioLogado();
                    break;
                case "3":
                    realizarLogout();
                    break;
                case "4":
                    cadastrarUsuario();
                    break;
                case "5":
                    cadastrarDisciplina();
                    break;
                case "6":
                    listarDisciplinas();
                    break;
                case "7":
                    cadastrarCurso();
                    break;
                case "8":
                    listarCursos();
                    break;
                case "9":
                    cadastrarPeriodoLetivo();
                    break;
                case "10":
                    listarPeriodosLetivos();
                    break;
                case "11":
                    ativarPeriodoLetivo();
                    break;
                case "12":
                    encerrarPeriodoLetivo();
                    break;
                case "13":
                    listarUsuarios();
                    break;
                case "0":
                    executando = false;
                    System.out.println("Sistema encerrado.");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }

            if (executando) {
                System.out.println();
                lerLinha("Pressione Enter para continuar...");
                limparTerminal();
            }
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("===== ClassRoomPB =====");
        if (autenticacaoController.isAutenticado()) {
            Usuario usuario = autenticacaoController.getUsuarioLogado();
            System.out.println("Sessão atual: " + usuario.getEmail() + " (" + usuario.getPerfil() + ")");
            System.out.println("1 - Trocar login");
            System.out.println("2 - Ver dados do usuário logado");
            System.out.println("3 - Logout");
            exibirFuncionalidadesPorPerfil(usuario.getPerfil());
        } else {
            System.out.println("Sessão atual: nenhum usuário logado");
            System.out.println("1 - Login");
        }
        System.out.println("0 - Sair");
        System.out.println();
    }

    private void exibirFuncionalidadesPorPerfil(PerfilUsuario perfil) {
        switch (perfil) {
            case ADMINISTRADOR:
                System.out.println("4 - Cadastrar usuário");
                System.out.println("7 - Cadastrar curso");
                System.out.println("8 - Listar cursos");
                System.out.println("13 - Listar usuários");
                break;
            case COORDENADOR:
                System.out.println("5 - Cadastrar disciplina");
                System.out.println("6 - Listar disciplinas");
                System.out.println("8 - Listar cursos");
                System.out.println("9 - Cadastrar período letivo");
                System.out.println("10 - Listar períodos letivos");
                System.out.println("11 - Ativar período letivo");
                System.out.println("12 - Encerrar período letivo");
                break;
            case PROFESSOR:
                System.out.println("6 - Listar disciplinas");
                System.out.println("10 - Listar períodos letivos");
                break;
            case ALUNO:
                System.out.println("6 - Listar disciplinas");
                System.out.println("10 - Listar períodos letivos");
                break;
            default:
                break;
        }
    }

    private void realizarLogin() {
        String identificador = lerLinha("Matrícula/e-mail: ");
        String senha = lerLinha("Senha: ");

        try {
            Usuario usuario = autenticacaoController.login(identificador, senha);
            System.out.println("Login realizado com sucesso.");
            System.out.println("Perfil: " + usuario.getPerfil());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void exibirUsuarioLogado() {
        if (!autenticacaoController.isAutenticado()) {
            System.out.println("Nenhum usuário logado. Use a opção 1 para fazer login primeiro.");
            return;
        }

        Usuario usuario = autenticacaoController.getUsuarioLogado();
        System.out.println("Usuário logado:");
        System.out.println("Matrícula: " + usuario.getMatricula());
        System.out.println("Nome: " + usuario.getNome());
        System.out.println("E-mail: " + usuario.getEmail());
        System.out.println("Perfil: " + usuario.getPerfil());
    }

    private void realizarLogout() {
        if (!autenticacaoController.isAutenticado()) {
            System.out.println("Nenhum usuário logado. Use a opção 1 para fazer login primeiro.");
            return;
        }

        autenticacaoController.logout();
        System.out.println("Logout realizado com sucesso.");
    }

    private void cadastrarUsuario() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            System.out.println("Apenas administradores podem cadastrar usuários.");
            return;
        }

        PerfilUsuario perfil = lerPerfil();
        if (perfil == null) {
            System.out.println("Tipo de usuário inválido.");
            return;
        }

        String matricula = lerLinha("Matrícula: ");
        String nome = lerLinha("Nome completo: ");
        String senha = lerLinha("Senha: ");

        try {
            Usuario usuario = autenticacaoController.cadastrarUsuario(perfil, matricula, nome, senha);
            usuarioRepository.salvarUsuarios(autenticacaoController.getUsuarios());
            System.out.println("Usuário cadastrado com sucesso.");
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Perfil: " + usuario.getPerfil());
            System.out.println("Matrícula: " + usuario.getMatricula());
            System.out.println("E-mail: " + usuario.getEmail());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarDisciplina() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            System.out.println("Apenas coordenadores podem cadastrar disciplinas.");
            return;
        }

        exibirCursosParaDisciplina();
        exibirDisciplinasParaPreRequisito();
        String codigo = lerLinha("Código da disciplina: ");
        String nome = lerLinha("Nome: ");
        String cargaHorariaTexto = lerLinha("Carga horária: ");
        String creditosTexto = lerLinha("Créditos: ");
        String idCurso = lerLinha("ID do curso: ");
        String preRequisitosTexto = lerLinha("IDs dos pré-requisitos separados por vírgula (opcional): ");

        try {
            int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
            int creditos = Integer.parseInt(creditosTexto);
            List<String> preRequisitosIds = converterTextoParaLista(preRequisitosTexto);

            Disciplina disciplina = disciplinaController.cadastrarDisciplina(
                    codigo,
                    nome,
                    cargaHoraria,
                    creditos,
                    idCurso,
                    preRequisitosIds);
            disciplinaRepository.salvarDisciplinas(disciplinaController.getDisciplinas());

            System.out.println("Disciplina cadastrada com sucesso.");
            System.out.println("ID: " + disciplina.getId());
            System.out.println("Código: " + disciplina.getCodigo());
            System.out.println("Nome: " + disciplina.getNome());
            System.out.println("Carga horária: " + disciplina.getCargaHoraria());
            System.out.println("Créditos: " + disciplina.getCreditos());
            System.out.println("ID do curso: " + disciplina.getIdCurso());
        } catch (NumberFormatException e) {
            System.out.println("Carga horária e créditos devem ser números inteiros.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarDisciplinas() {
        List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
            return;
        }

        System.out.println("Disciplinas cadastradas:");
        for (Disciplina disciplina : disciplinas) {
            System.out.println();
            System.out.println("ID: " + disciplina.getId());
            System.out.println("Código: " + disciplina.getCodigo());
            System.out.println("Nome: " + disciplina.getNome());
            System.out.println("Carga horária: " + disciplina.getCargaHoraria());
            System.out.println("Créditos: " + disciplina.getCreditos());
            System.out.println("ID do curso: " + disciplina.getIdCurso());
            System.out.println("Pré-requisitos: " + formatarPreRequisitos(disciplina.getPreRequisitosIds()));
        }
    }

    private void listarUsuarios() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            System.out.println("Apenas administradores podem listar usuários.");
            return;
        }

        List<Usuario> usuarios = autenticacaoController.getUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return;
        }

        System.out.println("Usuários cadastrados:");
        for (Usuario usuario : usuarios) {
            System.out.println();
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Matrícula: " + usuario.getMatricula());
            System.out.println("E-mail: " + usuario.getEmail());
            System.out.println("Perfil: " + usuario.getPerfil());
            System.out.println("Status: " + (usuario.isAtivo() ? "ativo" : "inativo"));
        }
    }

    private void cadastrarCurso() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            System.out.println("Apenas administradores podem cadastrar cursos.");
            return;
        }

        String nome = lerLinha("Nome do curso: ");
        String codigo = lerLinha("Código do curso (opcional): ");

        try {
            Curso curso = cursoController.cadastrarCurso(nome, codigo);
            cursoRepository.salvarCursos(cursoController.getCursos());

            System.out.println("Curso cadastrado com sucesso.");
            System.out.println("ID: " + curso.getId());
            System.out.println("Nome: " + curso.getNome());
            System.out.println("Código: " + formatarValorOpcional(curso.getCodigo()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarCursos() {
        List<Curso> cursos = cursoController.getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Nenhum curso cadastrado.");
            return;
        }

        System.out.println("Cursos cadastrados:");
        for (Curso curso : cursos) {
            System.out.println();
            System.out.println("ID: " + curso.getId());
            System.out.println("Nome: " + curso.getNome());
            System.out.println("Código: " + formatarValorOpcional(curso.getCodigo()));
        }
    }

    private void cadastrarPeriodoLetivo() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            System.out.println("Apenas coordenadores podem gerenciar períodos letivos.");
            return;
        }

        String codigo = lerLinha("Período letivo (ex.: 2026.2): ");

        try {
            PeriodoLetivo periodoLetivo = periodoLetivoController.cadastrarPeriodoLetivo(codigo);
            periodoLetivoRepository.salvarPeriodosLetivos(periodoLetivoController.getPeriodosLetivos());

            System.out.println("Período letivo cadastrado com sucesso.");
            System.out.println("ID: " + periodoLetivo.getId());
            System.out.println("Código: " + periodoLetivo.getCodigo());
            System.out.println("Status: " + formatarStatusPeriodo(periodoLetivo));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarPeriodosLetivos() {
        List<PeriodoLetivo> periodosLetivos = periodoLetivoController.getPeriodosLetivos();
        if (periodosLetivos.isEmpty()) {
            System.out.println("Nenhum período letivo cadastrado.");
            return;
        }

        System.out.println("Períodos letivos cadastrados:");
        for (PeriodoLetivo periodoLetivo : periodosLetivos) {
            System.out.println();
            System.out.println("ID: " + periodoLetivo.getId());
            System.out.println("Código: " + periodoLetivo.getCodigo());
            System.out.println("Status: " + formatarStatusPeriodo(periodoLetivo));
        }
    }

    private void ativarPeriodoLetivo() {
        alterarStatusPeriodoLetivo(true);
    }

    private void encerrarPeriodoLetivo() {
        alterarStatusPeriodoLetivo(false);
    }

    private void alterarStatusPeriodoLetivo(boolean ativar) {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            System.out.println("Apenas coordenadores podem gerenciar períodos letivos.");
            return;
        }

        listarPeriodosLetivos();
        String id = lerLinha("ID do período letivo: ");

        try {
            PeriodoLetivo periodoLetivo = ativar
                    ? periodoLetivoController.ativarPeriodoLetivo(id)
                    : periodoLetivoController.encerrarPeriodoLetivo(id);
            periodoLetivoRepository.salvarPeriodosLetivos(periodoLetivoController.getPeriodosLetivos());

            System.out.println("Período letivo atualizado com sucesso.");
            System.out.println("Código: " + periodoLetivo.getCodigo());
            System.out.println("Status: " + formatarStatusPeriodo(periodoLetivo));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void exibirDisciplinasParaPreRequisito() {
        List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada para usar como pré-requisito.");
            System.out.println();
            return;
        }

        System.out.println("Disciplinas disponíveis para pré-requisito:");
        for (Disciplina disciplina : disciplinas) {
            System.out.println(disciplina.getId() + " - " + disciplina.getCodigo() + " - " + disciplina.getNome());
        }
        System.out.println();
    }

    private void exibirCursosParaDisciplina() {
        List<Curso> cursos = cursoController.getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Nenhum curso cadastrado. Cadastre um curso antes de criar disciplinas.");
            System.out.println();
            return;
        }

        System.out.println("Cursos disponíveis para vincular a disciplina:");
        for (Curso curso : cursos) {
            System.out.println(curso.getId() + " - " + curso.getNome() + " - " + formatarValorOpcional(curso.getCodigo()));
        }
        System.out.println();
    }

    private List<String> converterTextoParaLista(String texto) {
        List<String> valores = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return valores;
        }

        String[] partes = texto.split(",");
        for (String parte : partes) {
            String valor = parte.trim();
            if (!valor.isEmpty()) {
                valores.add(valor);
            }
        }
        return valores;
    }

    private String formatarPreRequisitos(List<String> preRequisitosIds) {
        if (preRequisitosIds.isEmpty()) {
            return "nenhum";
        }
        return String.join(", ", preRequisitosIds);
    }

    private String formatarValorOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "não informado";
        }
        return valor;
    }

    private String formatarStatusPeriodo(PeriodoLetivo periodoLetivo) {
        return periodoLetivo.isAtivo() ? "ativo" : "encerrado";
    }

    private PerfilUsuario lerPerfil() {
        System.out.println("Tipo de usuário:");
        System.out.println("1 - Aluno");
        System.out.println("2 - Professor");
        System.out.println("3 - Coordenador");
        System.out.println("4 - Administrador");

        String opcao = lerLinha("Opção: ");
        switch (opcao) {
            case "1":
                return PerfilUsuario.ALUNO;
            case "2":
                return PerfilUsuario.PROFESSOR;
            case "3":
                return PerfilUsuario.COORDENADOR;
            case "4":
                return PerfilUsuario.ADMINISTRADOR;
            default:
                return null;
        }
    }

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private void limparTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
