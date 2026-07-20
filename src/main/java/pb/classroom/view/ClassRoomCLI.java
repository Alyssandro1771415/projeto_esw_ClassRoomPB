package pb.classroom.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import pb.classroom.controller.AutenticacaoController;
import pb.classroom.controller.CursoController;
import pb.classroom.controller.DisciplinaController;
import pb.classroom.controller.HistoricoAcademicoController;
import pb.classroom.controller.MatriculaController;
import pb.classroom.controller.NotaController;
import pb.classroom.controller.PeriodoLetivoController;
import pb.classroom.controller.PresencaController;
import pb.classroom.controller.TurmaController;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.EtapaAvaliacao;
import pb.classroom.model.FrequenciaAluno;
import pb.classroom.model.FrequenciaDisciplinaAluno;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.ResultadoAvaliacao;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.HistoricoAcademicoRepository;
import pb.classroom.repository.MatriculaRepository;
import pb.classroom.repository.NotaRepository;
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
  private final NotaController notaController;
  private final HistoricoAcademicoController historicoAcademicoController;
  private final UsuarioRepository usuarioRepository;
  private final CursoRepository cursoRepository;
  private final DisciplinaRepository disciplinaRepository;
  private final PeriodoLetivoRepository periodoLetivoRepository;
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final PresencaRepository presencaRepository;
  private final NotaRepository notaRepository;
  private final HistoricoAcademicoRepository historicoAcademicoRepository;
  private final pb.classroom.controller.RelatorioController relatorioController;

  public ClassRoomCLI() {
    this(
        new Scanner(System.in),
        new UsuarioRepository(),
        new DisciplinaRepository(),
        new CursoRepository(),
        new PeriodoLetivoRepository(),
        new TurmaRepository(),
        new MatriculaRepository(),
        new PresencaRepository(),
        new NotaRepository(),
        new HistoricoAcademicoRepository());
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
        new PresencaRepository(),
        new NotaRepository(),
        new HistoricoAcademicoRepository());
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
        new PresencaRepository(),
        new NotaRepository(),
        new HistoricoAcademicoRepository());
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
                ? (matriculaRepository
                        .getCaminhoArquivo()
                        .getFileName()
                        .toString()
                        .equals("armazenamento.json")
                    ? matriculaRepository.getCaminhoArquivo()
                    : (matriculaRepository.getCaminhoArquivo().getParent() != null
                        ? matriculaRepository
                            .getCaminhoArquivo()
                            .getParent()
                            .resolve("presencas.json")
                        : java.nio.file.Paths.get("presencas.json")))
                : java.nio.file.Paths.get("presencas.json")),
        new NotaRepository(
            matriculaRepository.getCaminhoArquivo() != null
                ? matriculaRepository.getCaminhoArquivo()
                : java.nio.file.Paths.get("armazenamento_interno.json")),
        new HistoricoAcademicoRepository(
            matriculaRepository.getCaminhoArquivo() != null
                ? matriculaRepository.getCaminhoArquivo()
                : java.nio.file.Paths.get("armazenamento_interno.json")));
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
    this(
        scanner,
        usuarioRepository,
        disciplinaRepository,
        cursoRepository,
        periodoLetivoRepository,
        turmaRepository,
        matriculaRepository,
        presencaRepository,
        new NotaRepository(
            matriculaRepository.getCaminhoArquivo() != null
                ? matriculaRepository.getCaminhoArquivo()
                : java.nio.file.Paths.get("armazenamento_interno.json")),
        new HistoricoAcademicoRepository(
            matriculaRepository.getCaminhoArquivo() != null
                ? matriculaRepository.getCaminhoArquivo()
                : java.nio.file.Paths.get("armazenamento_interno.json")));
  }

  ClassRoomCLI(
      Scanner scanner,
      UsuarioRepository usuarioRepository,
      DisciplinaRepository disciplinaRepository,
      CursoRepository cursoRepository,
      PeriodoLetivoRepository periodoLetivoRepository,
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PresencaRepository presencaRepository,
      NotaRepository notaRepository,
      HistoricoAcademicoRepository historicoAcademicoRepository) {
    this.scanner = scanner;
    this.usuarioRepository = usuarioRepository;
    this.disciplinaRepository = disciplinaRepository;
    this.cursoRepository = cursoRepository;
    this.periodoLetivoRepository = periodoLetivoRepository;
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.presencaRepository = presencaRepository;
    this.notaRepository = notaRepository;
    this.historicoAcademicoRepository = historicoAcademicoRepository;
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
    List<HistoricoAcademico> historicosCompartilhados =
        new ArrayList<>(historicoAcademicoRepository.carregarHistoricos());
    this.matriculaController =
        new MatriculaController(
            autenticacaoController,
            matriculaRepository.carregarMatriculas(),
            turmaController.getTurmas(),
            periodoLetivoController.getPeriodosLetivos(),
            disciplinaController.getDisciplinas(),
            historicosCompartilhados);
    this.presencaController =
        new PresencaController(
            autenticacaoController,
            presencaRepository.carregarPresencas(),
            turmaController.getTurmas(),
            matriculaController.getMatriculas());
    this.historicoAcademicoController =
        new HistoricoAcademicoController(
            autenticacaoController,
            historicosCompartilhados,
            disciplinaController.getDisciplinas(),
            autenticacaoController.getUsuarios());
    this.notaController =
        new NotaController(
            autenticacaoController,
            presencaController,
            notaRepository.carregarNotas(),
            historicosCompartilhados,
            turmaController.getTurmas(),
            matriculaController.getMatriculas());

    this.relatorioController =
        new pb.classroom.controller.RelatorioController(
            this.autenticacaoController,
            this.turmaController.getTurmas(),
            this.matriculaController.getMatriculas(),
            this.autenticacaoController.getUsuarios(),
            this.disciplinaController.getDisciplinas(),
            historicosCompartilhados);
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
        case "22":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            chamarProximosAlunosListaEspera();
          } else {
            consultarFrequencia();
          }
          break;
        case "23":
          consultarFrequencia();
          break;
        case "24":
          consultarMinhaFrequenciaPorDisciplina();
          break;
        case "25":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            fecharTurma();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
            lancarNotas();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
            consultarMinhasNotas();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "26":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
            alterarNota();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            consultarNotasPorTurma();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "27":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
            consultarMeuHistorico();
          } else if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            consultarHistoricoAluno();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "28":
          consultarHistoricoPorCurso();
          break;
        case "29":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            gerarRelatorioAlunosPorTurma();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "30":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            exibirRelatorioOcupacaoVagas();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "31":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
            exibirRelatorioReprovacaoDisciplina();
          } else {
            System.out.println("Opção inválida.");
          }
          break;
        case "32":
          if (usuarioLogadoPossuiPerfil(PerfilUsuario.ADMINISTRADOR)) {
            exibirRelatorioGeralUsuarios();
          } else {
            System.out.println("Opção inválida.");
          }
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
        System.out.println("32 - Gerar relatório geral de usuários");
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
        System.out.println("22 - Chamar próximos alunos da lista de espera");
        System.out.println("23 - Consultar percentual de frequência");
        System.out.println("25 - Fechar turma");
        System.out.println("26 - Consultar notas por turma");
        System.out.println("27 - Consultar histórico de aluno");
        System.out.println("28 - Consultar histórico por curso");
        System.out.println("29 - Gerar relatório de alunos por turma");
        System.out.println("30 - Gerar relatório de ocupação de vagas");
        System.out.println("31 - Gerar relatório de reprovação por disciplina");
        break;
      case PROFESSOR:
        System.out.println("6 - Listar disciplinas");
        System.out.println("7 - Listar turmas");
        System.out.println("10 - Listar períodos letivos");
        System.out.println("19 - Consultar lista de espera");
        System.out.println("20 - Registrar presença/falta");
        System.out.println("21 - Consultar presenças por turma");
        System.out.println("23 - Consultar percentual de frequência");
        System.out.println("25 - Lançar notas (etapa1/etapa2)");
        System.out.println("26 - Alterar nota");
        break;
      case ALUNO:
        System.out.println("24 - Consultar minha frequência por disciplina");
        System.out.println("6 - Listar disciplinas");
        System.out.println("7 - Listar turmas");
        System.out.println("8 - Solicitar matrícula");
        System.out.println("10 - Listar períodos letivos");
        System.out.println("15 - Cancelar matrícula");
        System.out.println("19 - Consultar posição na lista de espera");
        System.out.println("20 - Consultar minhas presenças");
        System.out.println("22 - Consultar meu percentual de frequência");
        System.out.println("25 - Consultar minhas notas");
        System.out.println("27 - Consultar meu histórico acadêmico");
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
      imprimirSeparador();
      System.out.println("Nome: " + usuario.getNome());
      System.out.println("Perfil: " + usuario.getPerfil());
      System.out.println("Matrícula: " + usuario.getMatricula());
      System.out.println("E-mail: " + usuario.getEmail());
      imprimirSeparador();
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
      imprimirSeparador();
      System.out.println("ID: " + disciplina.getId());
      System.out.println("Código: " + disciplina.getCodigo());
      System.out.println("Nome: " + disciplina.getNome());
      System.out.println("Carga horária: " + disciplina.getCargaHoraria());
      System.out.println("Créditos: " + disciplina.getCreditos());
      System.out.println("ID do curso: " + disciplina.getIdCurso());
      imprimirSeparador();
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
    imprimirSeparador();
    for (Disciplina disciplina : disciplinas) {
      exibirDisciplina(disciplina);
      imprimirSeparador();
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
      imprimirSeparador();
      for (Disciplina disciplina : disciplinas) {
        exibirDisciplina(disciplina);
        imprimirSeparador();
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
    imprimirSeparador();
    for (Usuario usuario : usuarios) {
      System.out.println("Nome: " + usuario.getNome());
      System.out.println("Matrícula: " + usuario.getMatricula());
      System.out.println("E-mail: " + usuario.getEmail());
      System.out.println("Perfil: " + usuario.getPerfil());
      System.out.println("Status: " + (usuario.isAtivo() ? "ativo" : "inativo"));
      imprimirSeparador();
    }
  }

  private void ofertarTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem gerenciar turmas.");
      return;
    }

    String idDisciplina =
        selecionarDisciplinaId("Escolha o número da disciplina (ou informe o ID): ");
    String idPeriodoLetivo =
        selecionarPeriodoId("Escolha o número do período letivo (ou informe o ID): ");
    String idProfessor =
        selecionarProfessorId("Escolha o número do professor responsável (ou informe o ID): ");
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
      imprimirSeparador();
      exibirTurma(turma);
      imprimirSeparador();
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
    imprimirSeparador();
    for (Turma turma : turmas) {
      exibirTurma(turma);
      imprimirSeparador();
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
      imprimirSeparador();
      for (Turma turma : turmas) {
        exibirTurma(turma);
        imprimirSeparador();
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

    String idTurma =
        selecionarTurmaId(
            turmasDisponiveis, "Escolha o número da turma para matrícula (ou informe o ID): ");
    try {
      Matricula matricula = matriculaController.solicitarMatricula(idTurma);
      matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
      System.out.println("Matrícula realizada com sucesso.");
      imprimirSeparador();
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
    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (Matricula matricula : matriculaController.getMatriculas()) {
      if (matricula.getIdAluno().equals(aluno.getId())) {
        Turma turma = buscarTurmaPorId(matricula.getIdTurma());
        String descricaoTurma =
            turma != null ? descreverTurma(turma) : "turma " + matricula.getIdTurma();
        descricoes.add(
            descricaoTurma
                + " - "
                + matricula.getStatus()
                + " (ID matrícula: "
                + matricula.getId()
                + ")");
        ids.add(matricula.getId());
      }
    }

    if (ids.isEmpty()) {
      System.out.println("Nenhuma matrícula encontrada.");
      return;
    }

    System.out.println("Suas matrículas:");
    imprimirSeparador();
    String idMatricula =
        selecionarPorLista(
            "Escolha o número da matrícula a cancelar (ou informe o ID): ", descricoes, ids);
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

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }
    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");
    String idProfessor =
        selecionarProfessorId("Escolha o número do novo professor responsável (ou informe o ID): ");
    String limiteVagasTexto = lerLinha("Novo limite de vagas: ");
    String sala = lerLinha("Nova sala: ");
    String dataInicioTexto = lerLinha("Nova data de início das aulas (AAAA-MM-DD): ");

    try {
      int limiteVagas = Integer.parseInt(limiteVagasTexto);
      LocalDate dataInicio = LocalDate.parse(dataInicioTexto);
      List<BlocoHorario> horarios = lerHorariosDaTurma();

      int limiteAnterior = 0;
      for (Turma turmaExistente : turmaController.getTurmas()) {
        if (turmaExistente.getId().equals(idTurma)) {
          limiteAnterior = turmaExistente.getLimiteVagas();
          break;
        }
      }

      Turma turma =
          turmaController.alterarTurma(
              idTurma, idProfessor, limiteVagas, sala, dataInicio, horarios);
      turmaRepository.salvarTurmas(turmaController.getTurmas());

      if (limiteVagas > limiteAnterior) {
        List<Matricula> promovidos =
            matriculaController.processarChamadaAutomaticaListaEspera(idTurma);
        if (!promovidos.isEmpty()) {
          matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
          System.out.println(
              "RF24: "
                  + promovidos.size()
                  + " aluno(s) chamado(s) automaticamente da lista de espera.");
        }
      }

      System.out.println("Turma alterada com sucesso.");
      imprimirSeparador();
      exibirTurma(turma);
      imprimirSeparador();
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

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }
    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      Turma turma = turmaController.cancelarTurma(idTurma);
      turmaRepository.salvarTurmas(turmaController.getTurmas());

      System.out.println("Turma cancelada com sucesso.");
      imprimirSeparador();
      exibirTurma(turma);
      imprimirSeparador();
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
      imprimirSeparador();
      System.out.println("ID: " + curso.getId());
      System.out.println("Nome: " + curso.getNome());
      System.out.println("Código: " + formatarValorOpcional(curso.getCodigo()));
      imprimirSeparador();
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
    imprimirSeparador();
    for (Curso curso : cursos) {
      System.out.println("ID: " + curso.getId());
      System.out.println("Nome: " + curso.getNome());
      System.out.println("Código: " + formatarValorOpcional(curso.getCodigo()));
      imprimirSeparador();
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
      imprimirSeparador();
      System.out.println("ID: " + periodoLetivo.getId());
      System.out.println("Código: " + periodoLetivo.getCodigo());
      System.out.println("Status: " + formatarStatusPeriodo(periodoLetivo));
      imprimirSeparador();
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
    imprimirSeparador();
    for (PeriodoLetivo periodoLetivo : periodosLetivos) {
      System.out.println("ID: " + periodoLetivo.getId());
      System.out.println("Código: " + periodoLetivo.getCodigo());
      System.out.println("Status: " + formatarStatusPeriodo(periodoLetivo));
      imprimirSeparador();
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

    List<PeriodoLetivo> periodos = periodoLetivoController.getPeriodosLetivos();
    if (periodos.isEmpty()) {
      System.out.println("Nenhum período letivo cadastrado.");
      return;
    }
    String id = selecionarPeriodoId("Escolha o número do período letivo (ou informe o ID): ");

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
    imprimirSeparador();
    for (Disciplina disciplina : disciplinas) {
      System.out.println(
          disciplina.getId() + " - " + disciplina.getCodigo() + " - " + disciplina.getNome());
      imprimirSeparador();
    }
  }

  private void exibirCursosParaDisciplina() {
    List<Curso> cursos = cursoController.getCursos();
    if (cursos.isEmpty()) {
      System.out.println("Nenhum curso cadastrado. Cadastre um curso antes de criar disciplinas.");
      System.out.println();
      return;
    }

    System.out.println("Cursos disponíveis para vincular a disciplina:");
    imprimirSeparador();
    for (Curso curso : cursos) {
      System.out.println(
          curso.getId()
              + " - "
              + curso.getNome()
              + " - "
              + formatarValorOpcional(curso.getCodigo()));
      imprimirSeparador();
    }
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
    System.out.println("Disciplina: " + descreverDisciplinaDaTurma(turma));
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

  // ==================== Apresentação e seleção ====================

  private static final String SEPARADOR =
      "------------------------------------------------------------";

  private void imprimirSeparador() {
    System.out.println(SEPARADOR);
  }

  private Integer tentarInteiro(String texto) {
    if (texto == null) {
      return null;
    }
    try {
      return Integer.parseInt(texto.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Disciplina buscarDisciplinaPorId(String id) {
    for (Disciplina disciplina : disciplinaController.getDisciplinas()) {
      if (disciplina.getId().equals(id)) {
        return disciplina;
      }
    }
    return null;
  }

  private PeriodoLetivo buscarPeriodoPorId(String id) {
    for (PeriodoLetivo periodo : periodoLetivoController.getPeriodosLetivos()) {
      if (periodo.getId().equals(id)) {
        return periodo;
      }
    }
    return null;
  }

  private Usuario buscarUsuarioPorId(String id) {
    for (Usuario usuario : autenticacaoController.getUsuarios()) {
      if (usuario.getId().equals(id)) {
        return usuario;
      }
    }
    return null;
  }

  private Turma buscarTurmaPorId(String id) {
    for (Turma turma : turmaController.getTurmas()) {
      if (turma.getId().equals(id)) {
        return turma;
      }
    }
    return null;
  }

  private String descreverDisciplinaDaTurma(Turma turma) {
    Disciplina disciplina = buscarDisciplinaPorId(turma.getIdDisciplina());
    if (disciplina != null) {
      return disciplina.getCodigo() + " - " + disciplina.getNome();
    }
    return "Disciplina " + turma.getIdDisciplina();
  }

  private String descreverTurma(Turma turma) {
    PeriodoLetivo periodo = buscarPeriodoPorId(turma.getIdPeriodoLetivo());
    String codigoPeriodo = periodo != null ? periodo.getCodigo() : turma.getIdPeriodoLetivo();
    StringBuilder descricao = new StringBuilder();
    descricao
        .append(descreverDisciplinaDaTurma(turma))
        .append(" | Período ")
        .append(codigoPeriodo)
        .append(" | Sala ")
        .append(turma.getSala())
        .append(" | ")
        .append(formatarHorarios(turma.getHorarios()));
    if (turma.isCancelada()) {
      descricao.append(" | CANCELADA");
    }
    return descricao.toString();
  }

  private String descreverAluno(String idAluno) {
    Usuario aluno = buscarUsuarioPorId(idAluno);
    if (aluno != null) {
      return aluno.getNome() + " (" + aluno.getMatricula() + ")";
    }
    return idAluno;
  }

  private List<Turma> turmasComMatriculaDoAlunoLogado() {
    List<Turma> resultado = new ArrayList<>();
    Set<String> vistos = new LinkedHashSet<>();
    Usuario aluno = autenticacaoController.getUsuarioLogado();
    for (Matricula matricula : matriculaController.getMatriculas()) {
      if (matricula.getIdAluno().equals(aluno.getId()) && vistos.add(matricula.getIdTurma())) {
        Turma turma = buscarTurmaPorId(matricula.getIdTurma());
        if (turma != null) {
          resultado.add(turma);
        }
      }
    }
    return resultado;
  }

  private List<Turma> turmasDoProfessorLogado() {
    List<Turma> resultado = new ArrayList<>();
    Usuario professor = autenticacaoController.getUsuarioLogado();
    for (Turma turma : turmaController.getTurmas()) {
      if (turma.getIdProfessor().equals(professor.getId())) {
        resultado.add(turma);
      }
    }
    return resultado;
  }

  /**
   * Exibe as turmas em uma lista numerada e lê a escolha do usuário. Aceita o número da opção ou o
   * próprio ID da turma (conveniência/automação).
   */
  private String selecionarTurmaId(List<Turma> turmas, String rotulo) {
    System.out.println("Turmas relacionadas:");
    imprimirSeparador();
    for (int i = 0; i < turmas.size(); i++) {
      Turma turma = turmas.get(i);
      System.out.println((i + 1) + ") " + descreverTurma(turma));
      System.out.println("   ID: " + turma.getId());
      imprimirSeparador();
    }
    return resolverSelecao(lerLinha(rotulo), turmas);
  }

  private String resolverSelecao(String entrada, List<Turma> turmas) {
    Integer indice = tentarInteiro(entrada);
    if (indice != null && indice >= 1 && indice <= turmas.size()) {
      return turmas.get(indice - 1).getId();
    }
    return entrada;
  }

  /**
   * Exibe uma lista numerada de opções (descrição na ordem dos ids) e lê a escolha. Aceita o número
   * ou o próprio ID informado.
   */
  private String selecionarPorLista(String rotulo, List<String> descricoes, List<String> ids) {
    for (int i = 0; i < descricoes.size(); i++) {
      System.out.println((i + 1) + ") " + descricoes.get(i));
      imprimirSeparador();
    }
    String entrada = lerLinha(rotulo);
    Integer indice = tentarInteiro(entrada);
    if (indice != null && indice >= 1 && indice <= ids.size()) {
      return ids.get(indice - 1);
    }
    return entrada;
  }

  private String selecionarDisciplinaId(String rotulo) {
    List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
    if (disciplinas.isEmpty()) {
      System.out.println("Nenhuma disciplina cadastrada.");
      return lerLinha(rotulo);
    }
    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (Disciplina disciplina : disciplinas) {
      descricoes.add(disciplina.getCodigo() + " - " + disciplina.getNome());
      ids.add(disciplina.getId());
    }
    System.out.println("Disciplinas disponíveis:");
    imprimirSeparador();
    return selecionarPorLista(rotulo, descricoes, ids);
  }

  private String selecionarPeriodoId(String rotulo) {
    List<PeriodoLetivo> periodos = periodoLetivoController.getPeriodosLetivos();
    if (periodos.isEmpty()) {
      System.out.println("Nenhum período letivo cadastrado.");
      return lerLinha(rotulo);
    }
    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (PeriodoLetivo periodo : periodos) {
      descricoes.add(periodo.getCodigo() + " (" + formatarStatusPeriodo(periodo) + ")");
      ids.add(periodo.getId());
    }
    System.out.println("Períodos letivos:");
    imprimirSeparador();
    return selecionarPorLista(rotulo, descricoes, ids);
  }

  private String selecionarProfessorId(String rotulo) {
    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (Usuario usuario : autenticacaoController.getUsuarios()) {
      if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
        descricoes.add(usuario.getNome() + " - " + usuario.getEmail());
        ids.add(usuario.getId());
      }
    }
    if (ids.isEmpty()) {
      System.out.println("Nenhum professor cadastrado.");
      return lerLinha(rotulo);
    }
    System.out.println("Professores disponíveis:");
    imprimirSeparador();
    return selecionarPorLista(rotulo, descricoes, ids);
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

    boolean coordenador = usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR);
    List<Turma> turmas = coordenador ? turmaController.getTurmas() : turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma disponível para consulta.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<Matricula> listaEspera =
          coordenador
              ? matriculaController.visualizarListaEsperaPorTurma(idTurma)
              : matriculaController.consultarListaEsperaCompleta(idTurma);
      if (listaEspera.isEmpty()) {
        System.out.println("Nenhum aluno na lista de espera desta turma.");
        return;
      }

      System.out.println("Lista de espera da turma " + idTurma + ":");
      imprimirSeparador();
      int posicao = 1;
      for (Matricula matricula : listaEspera) {
        System.out.println(
            posicao
                + "ª posição - Matrícula ID: "
                + matricula.getId()
                + " - Aluno: "
                + descreverAluno(matricula.getIdAluno()));
        imprimirSeparador();
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

    List<Turma> turmas = turmasComMatriculaDoAlunoLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não possui matrículas em nenhuma turma.");
      return;
    }

    System.out.println("Suas posições em listas de espera:");
    imprimirSeparador();
    boolean encontrou = false;
    for (Turma turma : turmas) {
      try {
        int posicao = matriculaController.consultarPosicaoAluno(turma.getId());
        if (posicao > 0) {
          System.out.println("Turma: " + descreverTurma(turma));
          System.out.println("ID da turma: " + turma.getId());
          System.out.println("Sua posição: " + posicao + "ª posição");
          imprimirSeparador();
          encontrou = true;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
        imprimirSeparador();
      }
    }

    if (!encontrou) {
      System.out.println("Você não está na lista de espera de nenhuma turma.");
    }
  }

  private void removerAlunoListaEspera() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem remover alunos da lista de espera.");
      return;
    }

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<Matricula> listaEspera = matriculaController.visualizarListaEsperaPorTurma(idTurma);
      if (listaEspera.isEmpty()) {
        System.out.println("Nenhum aluno na lista de espera desta turma.");
        return;
      }

      System.out.println("Alunos na lista de espera:");
      imprimirSeparador();
      List<String> descricoes = new ArrayList<>();
      List<String> ids = new ArrayList<>();
      int posicao = 1;
      for (Matricula matricula : listaEspera) {
        descricoes.add(
            posicao
                + "ª - "
                + descreverAluno(matricula.getIdAluno())
                + " (ID matrícula: "
                + matricula.getId()
                + ")");
        ids.add(matricula.getId());
        posicao++;
      }

      String idMatricula =
          selecionarPorLista(
              "Escolha o número do aluno a remover (ou informe o ID da matrícula): ",
              descricoes,
              ids);
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

    List<Turma> turmas = turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não é responsável por nenhuma turma.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");
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
      imprimirSeparador();
      Map<String, Boolean> presencas = new LinkedHashMap<>();
      for (Matricula matricula : matriculasConfirmadas) {
        String resposta =
            lerLinha("Aluno " + descreverAluno(matricula.getIdAluno()) + " (P/F): ").toUpperCase();
        presencas.put(matricula.getIdAluno(), resposta.equals("P"));
        imprimirSeparador();
      }

      List<RegistroPresenca> registrados =
          presencaController.registrarPresenca(idTurma, data, presencas);
      presencaRepository.salvarPresencas(presencaController.getRegistrosPresenca());
      imprimirSeparador();
      System.out.println(
          "Presença registrada com sucesso para " + registrados.size() + " aluno(s).");
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

    boolean coordenador = usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR);
    List<Turma> turmas = coordenador ? turmaController.getTurmas() : turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma disponível para consulta.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<RegistroPresenca> presencas = presencaController.consultarPresencasPorTurma(idTurma);
      if (presencas.isEmpty()) {
        System.out.println("Nenhum registro de presença para esta turma.");
        return;
      }

      System.out.println("Registros de presença da turma " + idTurma + ":");
      imprimirSeparador();
      for (RegistroPresenca registro : presencas) {
        System.out.println(
            "Data: "
                + registro.getData()
                + " - Aluno: "
                + descreverAluno(registro.getIdAluno())
                + " - Status: "
                + registro.getStatus());
        imprimirSeparador();
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

    List<Turma> turmas = turmasComMatriculaDoAlunoLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não possui matrículas em nenhuma turma.");
      return;
    }

    System.out.println("Suas presenças:");
    imprimirSeparador();
    for (Turma turma : turmas) {
      System.out.println("Turma: " + descreverTurma(turma));
      System.out.println("ID da turma: " + turma.getId());
      try {
        List<RegistroPresenca> presencas =
            presencaController.consultarPresencasDoAluno(turma.getId());
        if (presencas.isEmpty()) {
          System.out.println("Nenhum registro de presença nesta turma.");
        } else {
          for (RegistroPresenca registro : presencas) {
            System.out.println(
                "Data: " + registro.getData() + " - Status: " + registro.getStatus());
          }
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
      imprimirSeparador();
    }
  }

  // ==================== RF24 – Chamada automática da lista de espera ====================

  private void chamarProximosAlunosListaEspera() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem chamar alunos da lista de espera.");
      return;
    }

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<Matricula> promovidos =
          matriculaController.chamarProximosAlunosListaEsperaManualmente(idTurma);
      matriculaRepository.salvarMatriculas(matriculaController.getMatriculas());
      System.out.println(
          promovidos.size() + " aluno(s) chamado(s) da lista de espera com sucesso:");
      imprimirSeparador();
      for (Matricula matricula : promovidos) {
        System.out.println(
            "Matrícula ID: "
                + matricula.getId()
                + " - Aluno: "
                + descreverAluno(matricula.getIdAluno()));
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  // ==================== RF28 – Frequência ====================

  private void consultarFrequencia() {
    if (usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      consultarMinhaFrequencia();
      return;
    }

    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)
        && !usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Funcionalidade não disponível para seu perfil.");
      return;
    }

    boolean coordenador = usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR);
    List<Turma> turmas = coordenador ? turmaController.getTurmas() : turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma disponível para consulta.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<FrequenciaAluno> frequencias = presencaController.calcularFrequenciaPorTurma(idTurma);
      if (frequencias.isEmpty()) {
        System.out.println("Nenhum aluno matriculado confirmado nesta turma.");
        return;
      }

      System.out.println("Percentual de frequência da turma " + idTurma + ":");
      imprimirSeparador();
      for (FrequenciaAluno frequencia : frequencias) {
        exibirFrequencia(frequencia);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarMinhaFrequencia() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar o próprio percentual de frequência.");
      return;
    }

    List<Turma> turmas = turmasComMatriculaDoAlunoLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não possui matrículas em nenhuma turma.");
      return;
    }

    System.out.println("Seu percentual de frequência:");
    imprimirSeparador();
    for (Turma turma : turmas) {
      System.out.println("Turma: " + descreverTurma(turma));
      System.out.println("ID da turma: " + turma.getId());
      try {
        FrequenciaAluno frequencia = presencaController.consultarMinhaFrequencia(turma.getId());
        exibirFrequencia(frequencia);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
      imprimirSeparador();
    }
  }

  private void consultarMinhaFrequenciaPorDisciplina() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar a própria frequência por disciplina.");
      return;
    }

    try {
      List<FrequenciaDisciplinaAluno> frequencias =
          presencaController.consultarMinhasFrequenciasPorDisciplina();
      if (frequencias.isEmpty()) {
        System.out.println("Você não possui matrículas confirmadas em nenhuma disciplina.");
        return;
      }

      System.out.println("Sua frequência por disciplina:");
      imprimirSeparador();
      for (FrequenciaDisciplinaAluno frequencia : frequencias) {
        Disciplina disciplina = buscarDisciplinaPorId(frequencia.getIdDisciplina());
        if (disciplina != null) {
          System.out.println(
              "Disciplina: " + disciplina.getCodigo() + " - " + disciplina.getNome());
        }
        exibirFrequenciaDisciplina(frequencia);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void exibirFrequencia(FrequenciaAluno frequencia) {
    System.out.println(
        "Aluno: "
            + frequencia.getIdAluno()
            + " - Presenças: "
            + frequencia.getTotalPresencas()
            + "/"
            + frequencia.getTotalAulasRegistradas()
            + " - Frequência: "
            + String.format("%.1f", frequencia.getPercentual())
            + "%");
    if (frequencia.isAbaixoDoMinimoExigido()) {
      System.out.println(frequencia.getMensagemAlerta());
    }
  }

  private void exibirFrequenciaDisciplina(FrequenciaDisciplinaAluno frequencia) {
    System.out.println(
        "Aluno: "
            + frequencia.getIdAluno()
            + " - Disciplina: "
            + frequencia.getIdDisciplina()
            + " - Presenças: "
            + frequencia.getTotalPresencas()
            + "/"
            + frequencia.getTotalAulasRegistradas()
            + " - Frequência: "
            + String.format("%.1f", frequencia.getPercentual())
            + "%");
    if (frequencia.isAbaixoDoMinimoExigido()) {
      System.out.println(frequencia.getMensagemAlerta());
    }
  }

  // ==================== RF31–RF35 – Notas e Avaliação ====================

  private void lancarNotas() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Apenas professores podem lançar notas.");
      return;
    }

    List<Turma> turmas = turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não é responsável por nenhuma turma.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");
    String etapaTexto = lerLinha("Etapa (1 ou 2): ").trim();
    EtapaAvaliacao etapa = parseEtapa(etapaTexto);

    List<Matricula> matriculasConfirmadas = listarMatriculasConfirmadas(idTurma);
    if (matriculasConfirmadas.isEmpty()) {
      System.out.println("Nenhum aluno matriculado (confirmado) nesta turma.");
      return;
    }

    try {
      System.out.println("Informe a nota (0.0 a 10.0) para cada aluno:");
      imprimirSeparador();
      for (Matricula matricula : matriculasConfirmadas) {
        String notaTexto =
            lerLinha("Aluno " + descreverAluno(matricula.getIdAluno()) + ": ").trim();
        double nota = Double.parseDouble(notaTexto.replace(',', '.'));
        notaController.lancarNota(idTurma, matricula.getIdAluno(), etapa, nota);
        imprimirSeparador();
      }
      notaRepository.salvarNotas(notaController.getNotas());
      System.out.println("Notas lançadas com sucesso.");
    } catch (NumberFormatException e) {
      System.out.println("Nota inválida. Use um número entre 0.0 e 10.0.");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void alterarNota() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Apenas professores podem alterar notas.");
      return;
    }

    List<Turma> turmas = turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Você não é responsável por nenhuma turma.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");
    List<Matricula> matriculasConfirmadas = listarMatriculasConfirmadas(idTurma);
    if (matriculasConfirmadas.isEmpty()) {
      System.out.println("Nenhum aluno matriculado (confirmado) nesta turma.");
      return;
    }

    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (Matricula matricula : matriculasConfirmadas) {
      descricoes.add(descreverAluno(matricula.getIdAluno()));
      ids.add(matricula.getIdAluno());
    }

    String idAluno = selecionarPorLista("Escolha o aluno (ou informe o ID): ", descricoes, ids);
    String etapaTexto = lerLinha("Etapa (1 ou 2): ").trim();
    String notaTexto = lerLinha("Nova nota (0.0 a 10.0): ").trim();

    try {
      EtapaAvaliacao etapa = parseEtapa(etapaTexto);
      double nota = Double.parseDouble(notaTexto.replace(',', '.'));
      notaController.alterarNota(idTurma, idAluno, etapa, nota);
      notaRepository.salvarNotas(notaController.getNotas());
      System.out.println("Nota alterada com sucesso.");
    } catch (NumberFormatException e) {
      System.out.println("Nota inválida. Use um número entre 0.0 e 10.0.");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarMinhasNotas() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar suas notas.");
      return;
    }

    try {
      List<ResultadoAvaliacao> resultados = notaController.consultarMinhasNotas();
      if (resultados.isEmpty()) {
        System.out.println("Você não possui matrículas confirmadas.");
        return;
      }

      System.out.println("Suas notas e situação:");
      imprimirSeparador();
      for (ResultadoAvaliacao resultado : resultados) {
        exibirResultadoAvaliacao(resultado);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarNotasPorTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)
        && !usuarioLogadoPossuiPerfil(PerfilUsuario.PROFESSOR)) {
      System.out.println("Funcionalidade não disponível para seu perfil.");
      return;
    }

    boolean coordenador = usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR);
    List<Turma> turmas = coordenador ? turmaController.getTurmas() : turmasDoProfessorLogado();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma disponível para consulta.");
      return;
    }

    String idTurma = selecionarTurmaId(turmas, "Escolha o número da turma (ou informe o ID): ");

    try {
      List<ResultadoAvaliacao> resultados = notaController.consultarResultadosPorTurma(idTurma);
      if (resultados.isEmpty()) {
        System.out.println("Nenhum aluno matriculado nesta turma.");
        return;
      }

      System.out.println("Notas e situação da turma " + idTurma + ":");
      imprimirSeparador();
      for (ResultadoAvaliacao resultado : resultados) {
        exibirResultadoAvaliacao(resultado);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void fecharTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem fechar turmas.");
      return;
    }

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }

    String idTurma =
        selecionarTurmaId(turmas, "Escolha o número da turma a fechar (ou informe o ID): ");

    try {
      List<HistoricoAcademico> gerados = notaController.fecharTurma(idTurma);
      turmaRepository.salvarTurmas(turmaController.getTurmas());
      notaRepository.salvarNotas(notaController.getNotas());
      historicoAcademicoRepository.salvarHistoricos(historicoAcademicoController.getHistoricos());
      System.out.println(
          "Turma fechada com sucesso. " + gerados.size() + " registro(s) de histórico gerado(s).");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  // ==================== RF36–RF39 – Histórico Acadêmico ====================

  private void consultarMeuHistorico() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.ALUNO)) {
      System.out.println("Apenas alunos podem consultar seu histórico acadêmico.");
      return;
    }

    try {
      List<HistoricoAcademico> historicos = historicoAcademicoController.consultarMeuHistorico();
      if (historicos.isEmpty()) {
        System.out.println("Nenhum registro no histórico acadêmico.");
        return;
      }

      System.out.println("Seu histórico acadêmico:");
      imprimirSeparador();
      for (HistoricoAcademico historico : historicos) {
        exibirHistorico(historico);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarHistoricoAluno() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem consultar histórico de alunos.");
      return;
    }

    String idAluno = lerLinha("ID do aluno: ").trim();

    try {
      List<HistoricoAcademico> historicos =
          historicoAcademicoController.consultarHistoricoAluno(idAluno);
      if (historicos.isEmpty()) {
        System.out.println("Nenhum registro no histórico acadêmico deste aluno.");
        return;
      }

      System.out.println("Histórico acadêmico de " + descreverAluno(idAluno) + ":");
      imprimirSeparador();
      for (HistoricoAcademico historico : historicos) {
        exibirHistorico(historico);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void consultarHistoricoPorCurso() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem consultar histórico por curso.");
      return;
    }

    List<Curso> cursos = cursoController.getCursos();
    if (cursos.isEmpty()) {
      System.out.println("Nenhum curso cadastrado.");
      return;
    }

    List<String> descricoes = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    for (Curso curso : cursos) {
      descricoes.add(curso.getNome() + " (" + curso.getCodigo() + ")");
      ids.add(curso.getId());
    }

    String idCurso = selecionarPorLista("Escolha o curso (ou informe o ID): ", descricoes, ids);

    try {
      List<HistoricoAcademico> historicos =
          historicoAcademicoController.consultarHistoricoPorCurso(idCurso);
      if (historicos.isEmpty()) {
        System.out.println("Nenhum registro no histórico acadêmico deste curso.");
        return;
      }

      System.out.println("Histórico acadêmico do curso:");
      imprimirSeparador();
      for (HistoricoAcademico historico : historicos) {
        exibirHistorico(historico);
        imprimirSeparador();
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private List<Matricula> listarMatriculasConfirmadas(String idTurma) {
    List<Matricula> confirmadas = new ArrayList<>();
    for (Matricula matricula : matriculaController.getMatriculas()) {
      if (matricula.getIdTurma().equals(idTurma) && matricula.isConfirmada()) {
        confirmadas.add(matricula);
      }
    }
    return confirmadas;
  }

  private EtapaAvaliacao parseEtapa(String etapaTexto) {
    if ("1".equals(etapaTexto)) {
      return EtapaAvaliacao.ETAPA1;
    }
    if ("2".equals(etapaTexto)) {
      return EtapaAvaliacao.ETAPA2;
    }
    throw new IllegalArgumentException("Etapa inválida. Informe 1 ou 2.");
  }

  private void exibirResultadoAvaliacao(ResultadoAvaliacao resultado) {
    Turma turma = buscarTurmaPorId(resultado.getIdTurma());
    if (turma != null) {
      System.out.println("Turma: " + descreverTurma(turma));
    }
    System.out.println("Aluno: " + descreverAluno(resultado.getIdAluno()));
    System.out.println(
        "Etapa 1: "
            + formatarNota(resultado.getNotaEtapa1())
            + " | Etapa 2: "
            + formatarNota(resultado.getNotaEtapa2())
            + " | Recuperação: "
            + formatarNota(resultado.getNotaRecuperacao()));
    System.out.println(
        "Média final: "
            + formatarNota(resultado.getMediaFinal())
            + " | Frequência: "
            + String.format("%.1f", resultado.getPercentualFrequencia())
            + "%");
    System.out.println("Situação: " + resultado.getSituacao());
  }

  private void exibirHistorico(HistoricoAcademico historico) {
    Disciplina disciplina = buscarDisciplinaPorId(historico.getIdDisciplina());
    PeriodoLetivo periodo = buscarPeriodoPorId(historico.getIdPeriodoLetivo());
    System.out.println("Aluno: " + descreverAluno(historico.getIdAluno()));
    if (disciplina != null) {
      System.out.println("Disciplina: " + disciplina.getCodigo() + " - " + disciplina.getNome());
    }
    if (periodo != null) {
      System.out.println("Período: " + periodo.getCodigo());
    }
    System.out.println("Professor: " + descreverProfessor(historico.getIdProfessor()));
    System.out.println(
        "Média final: "
            + formatarNota(historico.getMediaFinal())
            + " | Frequência: "
            + String.format("%.1f", historico.getPercentualFrequencia())
            + "%");
    System.out.println("Situação: " + historico.getSituacao());
    System.out.println("Data do registro: " + historico.getDataRegistro());
  }

  private String formatarNota(Double nota) {
    return nota == null ? "-" : String.format("%.1f", nota);
  }

  private String descreverProfessor(String idProfessor) {
    for (Usuario usuario : autenticacaoController.getUsuarios()) {
      if (usuario.getId().equals(idProfessor)) {
        return usuario.getNome() + " (" + usuario.getMatricula() + ")";
      }
    }
    return idProfessor;
  }

  private void gerarRelatorioAlunosPorTurma() {
    if (!usuarioLogadoPossuiPerfil(PerfilUsuario.COORDENADOR)) {
      System.out.println("Apenas coordenadores podem gerar relatórios.");
      return;
    }

    List<Turma> turmas = turmaController.getTurmas();
    if (turmas.isEmpty()) {
      System.out.println("Nenhuma turma cadastrada.");
      return;
    }

    String idTurma =
        selecionarTurmaId(turmas, "Escolha o número da turma para o relatório (ou informe o ID): ");

    try {
      List<Usuario> alunos = relatorioController.gerarRelatorioAlunosPorTurma(idTurma);

      limparTerminal();
      System.out.println("====== RELATÓRIO DE ALUNOS MATRICULADOS ======");
      System.out.println("ID da Turma: " + idTurma);
      System.out.println("Total de Alunos Confirmados: " + alunos.size());
      imprimirSeparador();

      if (alunos.isEmpty()) {
        System.out.println("Não há alunos com matrícula CONFIRMADA nesta turma.");
      } else {
        System.out.printf("%-15s | %-30s | %-30s%n", "MATRÍCULA", "NOME", "E-MAIL");
        imprimirSeparador();
        for (Usuario aluno : alunos) {
          System.out.printf(
              "%-15s | %-30s | %-30s%n", aluno.getMatricula(), aluno.getNome(), aluno.getEmail());
        }
      }
      imprimirSeparador();
      salvarRelatorioPdf(
          "relatorios/rf40-alunos-turma-" + idTurma + ".pdf",
          destino -> relatorioController.exportarRelatorioAlunosPorTurmaPdf(idTurma, destino));
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void exibirRelatorioOcupacaoVagas() {
    limparTerminal();
    System.out.println("====== RELATÓRIO DE OCUPAÇÃO DE VAGAS ======");
    imprimirSeparador();
    try {
      List<String> linhas = relatorioController.gerarRelatorioOcupacaoVagas();
      if (linhas.isEmpty()) {
        System.out.println("Nenhuma turma ativa encontrada para monitorar.");
      } else {
        for (String linha : linhas) {
          System.out.println(linha);
        }
      }
      imprimirSeparador();
      salvarRelatorioPdf(
          "relatorios/rf41-ocupacao-vagas.pdf",
          destino -> relatorioController.exportarRelatorioOcupacaoVagasPdf(destino));
    } catch (IllegalArgumentException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void exibirRelatorioReprovacaoDisciplina() {
    String idDisciplina =
        selecionarDisciplinaId(
            "Escolha a disciplina para analisar a taxa de reprovação (ou informe o ID): ");
    limparTerminal();
    System.out.println("====== RELATÓRIO DE REPROVAÇÃO POR DISCIPLINA ======");
    imprimirSeparador();
    try {
      List<String> linhas = relatorioController.gerarRelatorioReprovacaoPorDisciplina(idDisciplina);
      for (String linha : linhas) {
        System.out.println(linha);
      }
      imprimirSeparador();
      salvarRelatorioPdf(
          "relatorios/rf42-reprovacao-" + idDisciplina + ".pdf",
          destino ->
              relatorioController.exportarRelatorioReprovacaoPorDisciplinaPdf(
                  idDisciplina, destino));
    } catch (IllegalArgumentException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void exibirRelatorioGeralUsuarios() {
    limparTerminal();
    System.out.println("====== RELATÓRIO GERAL DE USUÁRIOS CADASTRADOS ======");
    imprimirSeparador();
    try {
      List<String> linhas = relatorioController.gerarRelatorioGeralUsuarios();
      if (linhas.isEmpty()) {
        System.out.println("Nenhum usuário cadastrado no sistema.");
      } else {
        for (String linha : linhas) {
          System.out.println(linha);
        }
      }
      imprimirSeparador();
      salvarRelatorioPdf(
          "relatorios/rf43-usuarios-cadastrados.pdf",
          destino -> relatorioController.exportarRelatorioGeralUsuariosPdf(destino));
    } catch (IllegalArgumentException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void salvarRelatorioPdf(
      String caminhoPadrao,
      java.util.function.Function<java.nio.file.Path, java.nio.file.Path> exportador) {
    String resposta = lerLinha("Deseja baixar este relatório em PDF? (S/N): ").trim();
    if (!resposta.equalsIgnoreCase("S") && !resposta.equalsIgnoreCase("SIM")) {
      return;
    }

    String caminhoInformado = lerLinha("Caminho do arquivo PDF [" + caminhoPadrao + "]: ").trim();
    String caminhoFinal = caminhoInformado.isEmpty() ? caminhoPadrao : caminhoInformado;
    if (!caminhoFinal.toLowerCase().endsWith(".pdf")) {
      caminhoFinal = caminhoFinal + ".pdf";
    }

    try {
      java.nio.file.Path destino = java.nio.file.Paths.get(caminhoFinal);
      java.nio.file.Path gerado = exportador.apply(destino);
      System.out.println("PDF gerado com sucesso: " + gerado);
    } catch (IllegalStateException | IllegalArgumentException e) {
      System.out.println("Erro ao gerar PDF: " + e.getMessage());
    }
  }
}
