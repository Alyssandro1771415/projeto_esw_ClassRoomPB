package pb.classroom.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import pb.classroom.controller.AutenticacaoController;
import pb.classroom.controller.CursoController;
import pb.classroom.controller.DisciplinaController;
import pb.classroom.controller.MatriculaController;
import pb.classroom.controller.PeriodoLetivoController;
import pb.classroom.controller.PresencaController;
import pb.classroom.controller.TurmaController;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.MatriculaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.repository.PresencaRepository;
import pb.classroom.repository.TurmaRepository;
import pb.classroom.repository.UsuarioRepository;

public class ClassRoomCLI {

  private final Scanner scanner;
  private final AutenticacaoController autenticacaoController;
  private final CursoController cursoController;
  private final DisciplinaController disciplinaController;
  private final PeriodoLetivoController periodoLetivoController;
  private final TurmaController turmaController;
  private final MatriculaController matriculaController;
  private final PresencaController presencaController;
  private final UsuarioRepository usuarioRepository;
  private final CursoRepository cursoRepository;
  private final DisciplinaRepository disciplinaRepository;
  private final PeriodoLetivoRepository periodoLetivoRepository;
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final PresencaRepository presencaRepository;

  public ClassRoomCLI() {
    this(
        new Scanner(System.in),
        new UsuarioRepository(),
        new DisciplinaRepository(),
        new CursoRepository(),
        new PeriodoLetivoRepository(),
        new TurmaRepository(),
        new MatriculaRepository(),
        new PresencaRepository());
  }

  ClassRoomCLI(Scanner scanner, UsuarioRepository usuarioRepository) {
    this(
        scanner,
        usuarioRepository,
        new DisciplinaRepository(),
        new CursoRepository(),
        new PeriodoLetivoRepository(),
        new TurmaRepository(),
        new MatriculaRepository(),
        new PresencaRepository());
  }

  ClassRoomCLI(
      Scanner scanner,
      UsuarioRepository usuarioRepository,
      DisciplinaRepository disciplinaRepository,
      CursoRepository cursoRepository,
      PeriodoLetivoRepository periodoLetivoRepository,
      TurmaRepository turmaRepository) {
    this(
        scanner,
        usuarioRepository,
        disciplinaRepository,
        cursoRepository,
        periodoLetivoRepository,
        turmaRepository,
        new MatriculaRepository(),
        new PresencaRepository());
  }

  ClassRoomCLI(
      Scanner scanner,
      UsuarioRepository usuarioRepository,
      DisciplinaRepository disciplinaRepository,
      CursoRepository cursoRepository,
      PeriodoLetivoRepository periodoLetivoRepository,
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository) {
    this(
        scanner,
        usuarioRepository,
        disciplinaRepository,
        cursoRepository,
        periodoLetivoRepository,
        turmaRepository,
        matriculaRepository,
        new PresencaRepository(
            matriculaRepository.getCaminhoArquivo() != null
                ? (matriculaRepository.getCaminhoArquivo().getFileName().toString().equals("armazenamento.json")
                    ? matriculaRepository.getCaminhoArquivo()
                    : (matriculaRepository.getCaminhoArquivo().getParent() != null
                        ? matriculaRepository.getCaminhoArquivo().getParent().resolve("presencas.json")
                        : java.nio.file.Paths.get("presencas.json")))
                : java.nio.file.Paths.get("presencas.json")));
  }

  ClassRoomCLI(
      Scanner scanner,
      UsuarioRepository usuarioRepository,
      DisciplinaRepository disciplinaRepository,
      CursoRepository cursoRepository,
      PeriodoLetivoRepository periodoLetivoRepository,
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PresencaRepository presencaRepository) {
    this.scanner = scanner;
    this.usuarioRepository = usuarioRepository;
    this.disciplinaRepository = disciplinaRepository;
    this.cursoRepository = cursoRepository;
    this.periodoLetivoRepository = periodoLetivoRepository;
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.presencaRepository = presencaRepository;
    this.autenticacaoController = new AutenticacaoController(usuarioRepository.carregarUsuarios());
    this.cursoController =
        new CursoController(autenticacaoController, cursoRepository.carregarCursos());
    this.disciplinaController =
        new DisciplinaController(
            autenticacaoController,
            disciplinaRepository.carregarDisciplinas(),
            cursoController.getCursos());
    this.periodoLetivoController =
        new PeriodoLetivoController(
            autenticacaoController, periodoLetivoRepository.carregarPeriodosLetivos());
    this.turmaController =
        new TurmaController(
            autenticacaoController,
            turmaRepository.carregarTurmas(),
            disciplinaController.getDisciplinas(),
            periodoLetivoController.getPeriodosLetivos(),
            autenticacaoController.getUsuarios());
    this.matriculaController =
        new MatriculaController(
            autenticacaoController,
            matriculaRepository.carregarMatriculas(),
            turmaController.getTurmas(),
            periodoLetivoController.getPeriodosLetivos(),
            disciplinaController.getDisciplinas());
    this.presencaController =
        new PresencaController(
            autenticacaoController,
            presencaRepository.carregarPresencas(),
            turmaController.getTurmas(),
            matriculaController.getMatriculas());
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
          listarTurmas();
          break;
        case "8":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
            solicitarMatricula();
          } else {
            listarCursos();
          }
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
        case "14":
          ofertarTurma();
          break;
        case "15":
          cancelarMatricula();
          break;
        case "16":
          alterarTurma();
          break;
        case "17":
          cancelarTurma();
          break;
        case "18":
          cadastrarCurso();
          break;
        case "19":
          consultarListaEspera();
          break;
        case "20":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            removerAlunoListaEspera();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
            registrarPresenca();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
            consultarMinhasPresencas();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "21":
          consultarPresencasPorTurma();
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
        System.out.println("8 - Listar cursos");
        System.out.println("13 - Listar usuários");
        System.out.println("18 - Cadastrar curso");
        break;
      case COORDENADOR:
        System.out.println("5 - Cadastrar disciplina");
        System.out.println("6 - Listar disciplinas");
        System.out.println("7 - Listar turmas");
        System.out.println("8 - Listar cursos");
        System.out.println("9 - Cadastrar período letivo");
        System.out.println("10 - Listar períodos letivos");
        System.out.println("11 - Ativar período letivo");
        System.out.println("12 - Encerrar período letivo");
        System.out.println("14 - Ofertar turma");
        System.out.println("16 - Alterar turma");
        System.out.println("17 - Cancelar turma");
        System.out.println("19 - Consultar lista de espera");
        System.out.println("20 - Remover aluno da lista de espera");
        System.out.println("21 - Consultar presenças por turma");
        break;
      case PROFESSOR:
        System.out.println("6 - Listar disciplinas");
        System.out.println("7 - Listar turmas");
        System.out.println("10 - Listar períodos letivos");
        System.out.println("19 - Consultar lista de espera");
        System.out.println("20 - Registrar presença/falta");
        System.out.println("21 - Consultar presenças por turma");
        break;
      case ALUNO:
        System.out.println("6 - Listar disciplinas");
        System.out.println("15 - Cancelar matrícula");
        System.out.println("7 - Listar turmas");
        System.out.println("8 - Solicitar matrícula");
        System.out.println("10 - Listar períodos letivos");
        System.out.println("19 - Consultar posição na lista de espera");
        System.out.println("20 - Consultar minhas presenças");
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
    String preRequisitosTexto =
        lerLinha("IDs dos pré-requisitos separados por vírgula (opcional): ");

    try {
      int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
      int creditos = Integer.parseInt(creditosTexto);
      List<String> preRequisitosIds = converterTextoParaLista(preRequisitosTexto);

      Disciplina disciplina =
          disciplinaController.cadastrarDisciplina(
              codigo, nome, cargaHoraria, creditos, idCurso, preRequisitosIds);
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
    if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      listarDisciplinasDisponiveisParaAluno();
      return;
    }

    List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
    if (disciplinas.isEmpty()) {
      System.out.println("Nenhuma disciplina cadastrada.");
      return;
    }

    System.out.println("Disciplinas cadastradas:");
    for (Disciplina disciplina : disciplinas) {
      System.out.println();
      exibirDisciplina(disciplina);
    }
  }

  private void listarDisciplinasDisponiveisParaAluno() {
    try {
      List<Disciplina> disciplinas = turmaController.consultarDisciplinasDisponiveisParaAluno();
      if (disciplinas.isEmpty()) {
        System.out.println("Nenhuma disciplina disponível.");
        return;
      }

      System.out.println("Disciplinas disponíveis:");
      for (Disciplina disciplina : disciplinas) {
        System.out.println();
        exibirDisciplina(disciplina);
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
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

  private void ofertarTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem gerenciar turmas.");
      return;
    }

    exibirDisciplinasParaTurma();
    listarPeriodosLetivos();
    exibirProfessoresParaTurma();

    String idDisciplina = lerLinha("ID da disciplina: ");
    String idPeriodoLetivo = lerLinha("ID do período letivo: ");
    String idProfessor = lerLinha("ID do professor responsavel: ");
    String limiteVagasTexto = lerLinha("Limite de vagas: ");
    String sala = lerLinha("Sala: ");
    String dataInicioTexto = lerLinha("Data de início das aulas (AAAA-MM-DD): ");

    try {
      int limiteVagas = Integer.parseInt(limiteVagasTexto);
      LocalDate dataInicio = LocalDate.parse(dataInicioTexto);
      List<BlocoHorario> horarios = lerHorariosDaTurma();

      Turma turma =
          turmaController.ofertarTurma(
              idDisciplina, idPeriodoLetivo, idProfessor, limiteVagas, sala, dataInicio, horarios);
      turmaRepository.salvarTurmas(turmaController.getTurmas());

      System.out.println("Turma ofertada com sucesso.");
      exibirTurma(turma);
    } catch (NumberFormatException e) {
      System.out.println("Limite de vagas deve ser um número inteiro.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }

  private void listarTurmas() {
    if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      listarTurmasDisponiveisParaAluno();
      return;
    }

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }

    System.out.println("Turmas cadastradas:");
    for (Turma turma : turmas) {
      System.out.println();
      exibirTurma(turma);
    }
  }

  private void listarTurmasDisponiveisParaAluno() {
    try {
      List<Turma> turmas = turmaController.consultarTurmasDisponiveisParaAluno();
      if (turmas.isEmpty()) {
        System.out.println("Nenhuma turma disponível.");
        return;
      }

      System.out.println("Turmas disponíveis:");
      for (Turma turma : turmas) {
        System.out.println();
        exibirTurma(turma);
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void solicitarMatricula() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem solicitar matrícula.");
      return;
    }

    List<Turma> turmasDisponiveis;
    try {
      turmasDisponiveis = turmaController.consultarTurmasDisponiveisParaAluno();
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return;
    }

    if (turmasDisponiveis.isEmpty()) {
      System.out.println("Nenhuma turma disponível.");
      return;
    }

    System.out.println("Turmas disponíveis:");
    for (Turma turma : turmasDisponiveis) {
      System.out.println();
      exibirTurma(turma);
    }

    String idTurma = lerLinha("ID da turma: ");
    try {
      Matricula matricula = matriculaController.solicitarMatricula(idTurma);
      matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
      System.out.println("Matrícula realizada com sucesso.");
      System.out.println("ID da matrícula: " + matricula.getId());
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }

  private void cancelarMatricula() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem cancelar matrícula.");
      return;
    }

    Usuario aluno = autenticacaoController.getUsuarioLogado();
    boolean encontrou = false;
    System.out.println("Matrículas do aluno:");
    for (Matricula matricula : matriculaController.getMatriculas()) {
      if (matricula.getIdAluno().equals(aluno.getId())) {
        encontrou = true;
        System.out.println(
            matricula.getId()
                + " - turma "
                + matricula.getIdTurma()
                + " - "
                + matricula.getStatus());
      }
    }

    if (!encontrou) {
      System.out.println("Nenhuma matrícula encontrada.");
      return;
    }

    String idMatricula = lerLinha("ID da matrícula: ");
    try {
      Matricula cancelada = matriculaController.cancelarMatricula(idMatricula);
      matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
      System.out.println("Matrícula cancelada com sucesso.");
      System.out.println("ID da matrícula: " + cancelada.getId());
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }

  private void alterarTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem gerenciar turmas.");
      return;
    }

    listarTurmas();
    exibirProfessoresParaTurma();
    String idTurma = lerLinha("ID da turma: ");
    String idProfessor = lerLinha("Novo ID do professor responsavel: ");
    String limiteVagasTexto = lerLinha("Novo limite de vagas: ");
    String sala = lerLinha("Nova sala: ");
    String dataInicioTexto = lerLinha("Nova data de início das aulas (AAAA-MM-DD): ");

    try {
      int limiteVagas = Integer.parseInt(limiteVagasTexto);
      LocalDate dataInicio = LocalDate.parse(dataInicioTexto);
      List<BlocoHorario> horarios = lerHorariosDaTurma();

      Turma turma =
          turmaController.alterarTurma(
              idTurma, idProfessor, limiteVagas, sala, dataInicio, horarios);
      turmaRepository.salvarTurmas(turmaController.getTurmas());

      System.out.println("Turma alterada com sucesso.");
      exibirTurma(turma);
    } catch (NumberFormatException e) {
      System.out.println("Limite de vagas deve ser um número inteiro.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }

  private void cancelarTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem gerenciar turmas.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      Turma turma = turmaController.cancelarTurma(idTurma);
      turmaRepository.salvarTurmas(turmaController.getTurmas());

      System.out.println("Turma cancelada com sucesso.");
      exibirTurma(turma);
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
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
      PeriodoLetivo periodoLetivo =
          ativar
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
      System.out.println(
          disciplina.getId() + " - " + disciplina.getCodigo() + " - " + disciplina.getNome());
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
      System.out.println(
          curso.getId()
              + " - "
              + curso.getNome()
              + " - "
              + formatarValorOpcional(curso.getCodigo()));
    }
    System.out.println();
  }

  private void exibirDisciplinasParaTurma() {
    List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
    if (disciplinas.isEmpty()) {
      System.out.println("Nenhuma disciplina cadastrada para ofertar turma.");
      System.out.println();
      return;
    }

    System.out.println("Disciplinas disponíveis para turma:");
    for (Disciplina disciplina : disciplinas) {
      System.out.println(
          disciplina.getId() + " - " + disciplina.getCodigo() + " - " + disciplina.getNome());
    }
    System.out.println();
  }

  private void exibirProfessoresParaTurma() {
    System.out.println("Professores disponíveis:");
    boolean encontrouProfessor = false;
    for (Usuario usuario : autenticacaoController.getUsuarios()) {
      if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
        encontrouProfessor = true;
        System.out.println(
            usuario.getId() + " - " + usuario.getNome() + " - " + usuario.getEmail());
      }
    }
    if (!encontrouProfessor) {
      System.out.println("Nenhum professor cadastrado.");
    }
    System.out.println();
  }

  private List<BlocoHorario> lerHorariosDaTurma() {
    String quantidadeTexto = lerLinha("Quantidade de blocos de horário: ");
    int quantidade = Integer.parseInt(quantidadeTexto);
    List<BlocoHorario> horarios = new ArrayList<>();

    for (int i = 1; i <= quantidade; i++) {
      System.out.println("Bloco " + i + ":");
      int dia = Integer.parseInt(lerLinha("Dia da semana (1=segunda, 7=domingo): "));
      LocalTime inicio = LocalTime.parse(lerLinha("Hora início (HH:mm): "));
      LocalTime fim = LocalTime.parse(lerLinha("Hora fim (HH:mm): "));
      horarios.add(new BlocoHorario(DayOfWeek.of(dia), inicio, fim));
    }

    return horarios;
  }

  private void exibirTurma(Turma turma) {
    System.out.println("ID: " + turma.getId());
    System.out.println("ID da disciplina: " + turma.getIdDisciplina());
    System.out.println("ID do periodo letivo: " + turma.getIdPeriodoLetivo());
    System.out.println("ID do professor: " + turma.getIdProfessor());
    System.out.println("Limite de vagas: " + turma.getLimiteVagas());
    System.out.println("Sala: " + turma.getSala());
    System.out.println("Data de início: " + turma.getDataInicioAulas());
    System.out.println("Horários: " + formatarHorarios(turma.getHorarios()));
    System.out.println("Status: " + (turma.isCancelada() ? "cancelada" : "ativa"));
  }

  private void exibirDisciplina(Disciplina disciplina) {
    System.out.println("ID: " + disciplina.getId());
    System.out.println("Código: " + disciplina.getCodigo());
    System.out.println("Nome: " + disciplina.getNome());
    System.out.println("Carga horária: " + disciplina.getCargaHoraria());
    System.out.println("Créditos: " + disciplina.getCreditos());
    System.out.println("ID do curso: " + disciplina.getIdCurso());
    System.out.println(
        "Pré-requisitos: " + formatarPreRequisitos(disciplina.getPreRequisitosIds()));
  }

  private String formatarHorarios(List<BlocoHorario> horarios) {
    List<String> textos = new ArrayList<>();
    for (BlocoHorario horario : horarios) {
      textos.add(
          horario.getDiaSemana() + " " + horario.getHoraInicio() + "-" + horario.getHoraFim());
    }
    return String.join(", ", textos);
  }

  private boolean usuarioLogadoPossuiPerfil(PerfilUsuario perfil) {
    return autenticacaoController.isAutenticado()
        && autenticacaoController.getUsuarioLogado().getPerfil() == perfil;
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

  // ==================== RF23 – Lista de Espera ====================

  private void consultarListaEspera() {
    if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      consultarPosicaoListaEspera();
      return;
    }

    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)
        && !usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Funcionalidade não disponível para seu perfil.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      List<Matricula> listaEspera = matriculaController.consultarListaEsperaCompleta(idTurma);
      if (listaEspera.isEmpty()) {
        System.out.println("Nenhum aluno na lista de espera desta turma.");
        return;
      }

      System.out.println("Lista de espera da turma " + idTurma + ":");
      int posicao = 1;
      for (Matricula matricula : listaEspera) {
        System.out.println(
            posicao + "ª posição - Matrícula ID: " + matricula.getId()
                + " - Aluno ID: " + matricula.getIdAluno());
        posicao++;
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarPosicaoListaEspera() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar sua posição na lista de espera.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      int posicao = matriculaController.consultarPosicaoAluno(idTurma);
      if (posicao == 0) {
        System.out.println("Você não está na lista de espera desta turma.");
      } else {
        System.out.println("Sua posição na lista de espera: " + posicao + "ª posição.");
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void removerAlunoListaEspera() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem remover alunos da lista de espera.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      List<Matricula> listaEspera = matriculaController.consultarListaEsperaCompleta(idTurma);
      if (listaEspera.isEmpty()) {
        System.out.println("Nenhum aluno na lista de espera desta turma.");
        return;
      }

      System.out.println("Alunos na lista de espera:");
      int posicao = 1;
      for (Matricula matricula : listaEspera) {
        System.out.println(
            posicao + "ª - ID matrícula: " + matricula.getId()
                + " - Aluno ID: " + matricula.getIdAluno());
        posicao++;
      }

      String idMatricula = lerLinha("ID da matrícula a remover: ");
      Matricula removida = matriculaController.removerAlunoListaEspera(idTurma, idMatricula);
      matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
      System.out.println("Aluno removido da lista de espera com sucesso.");
      System.out.println("ID da matrícula: " + removida.getId());
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  // ==================== RF27 – Presença ====================

  private void registrarPresenca() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Apenas professores podem registrar presença.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");
    String dataTexto = lerLinha("Data da aula (AAAA-MM-DD): ");

    try {
      LocalDate data = LocalDate.parse(dataTexto);

      List<Matricula> matriculasConfirmadas = new ArrayList<>();
      for (Matricula matricula : matriculaController.getMatriculas()) {
        if (matricula.getIdTurma().equals(idTurma) && matricula.isConfirmada()) {
          matriculasConfirmadas.add(matricula);
        }
      }

      if (matriculasConfirmadas.isEmpty()) {
        System.out.println("Nenhum aluno matriculado (confirmado) nesta turma.");
        return;
      }

      System.out.println("Registre P (presente) ou F (falta) para cada aluno:");
      Map<String, Boolean> presencas = new LinkedHashMap<>();
      for (Matricula matricula : matriculasConfirmadas) {
        String resposta =
            lerLinha("Aluno " + matricula.getIdAluno() + " (P/F): ").toUpperCase();
        presencas.put(matricula.getIdAluno(), resposta.equals("P"));
      }

      List<RegistroPresenca> registrados =
          presencaController.registrarPresenca(idTurma, data, presencas);
      presencaRepository.salvarPresencas(presencaController.getRegistrosPresenca());
      System.out.println("Presença registrada com sucesso para " + registrados.size() + " aluno(s).");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println("Erro ao registrar presença: " + e.getMessage());
    }
  }

  private void consultarPresencasPorTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)
        && !usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Funcionalidade não disponível para seu perfil.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      List<RegistroPresenca> presencas = presencaController.consultarPresencasPorTurma(idTurma);
      if (presencas.isEmpty()) {
        System.out.println("Nenhum registro de presença para esta turma.");
        return;
      }

      System.out.println("Registros de presença da turma " + idTurma + ":");
      for (RegistroPresenca registro : presencas) {
        System.out.println(
            "Data: " + registro.getData()
                + " - Aluno: " + registro.getIdAluno()
                + " - Status: " + registro.getStatus());
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarMinhasPresencas() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar suas próprias presenças.");
      return;
    }

    listarTurmas();
    String idTurma = lerLinha("ID da turma: ");

    try {
      List<RegistroPresenca> presencas = presencaController.consultarPresencasDoAluno(idTurma);
      if (presencas.isEmpty()) {
        System.out.println("Nenhum registro de presença encontrado para esta turma.");
        return;
      }

      System.out.println("Suas presenças na turma " + idTurma + ":");
      for (RegistroPresenca registro : presencas) {
        System.out.println("Data: " + registro.getData() + " - Status: " + registro.getStatus());
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
}
