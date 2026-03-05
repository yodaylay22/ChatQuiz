package com.hytale.server.plugin.chatquiz.manager;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hytale.server.plugin.chatquiz.ChatQuizPlugin;
import com.hytale.server.plugin.chatquiz.TinyMsg;
import com.hytale.server.plugin.chatquiz.config.ChatQuizConfig;
import com.hytale.server.plugin.chatquiz.config.QuizType;
import com.hytale.server.plugin.chatquiz.TinyMsg;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Gerenciador do sistema de Quiz.
 * Suporta múltiplos tipos: QUIZ, TYPE, SCRAMBLE
 */
public class QuizManager {
    
    private final ChatQuizPlugin plugin;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler;
    
    private boolean quizAtivo = false;
    private QuizType tipoAtual = null;
    
    // Dados do quiz atual (depende do tipo)
    private ChatQuizConfig.QuizEntry quizEntryAtual = null;  // Para tipo QUIZ
    private String fraseAtual = null;  // Para tipo TYPE e SCRAMBLE
    private String embaralhadaAtual = null;  // Apenas para SCRAMBLE
    
    private ScheduledFuture<?> timeoutTask = null;
    private ScheduledFuture<?> proximoQuizTask = null;
    
    public static final String PERMISSION_ADMIN = "chatquiz.admin";
    
    // Placeholders
    private static final String PLACEHOLDER_PLAYER = "{player}";
    private static final String PLACEHOLDER_PERGUNTA = "{pergunta}";
    private static final String PLACEHOLDER_FRASE = "{frase}";
    private static final String PLACEHOLDER_EMBARALHADA = "{embaralhada}";
    private static final String PLACEHOLDER_RESPOSTA = "{resposta}";
    
    public QuizManager(@Nonnull ChatQuizPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ChatQuiz-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Inicia o ciclo automatico de quizzes
     */
    public void iniciarCiclo() {
        cancelarTarefas();
        
        ChatQuizConfig config = plugin.getConfiguracao();
        if (!temPerguntasDisponiveis(config)) {
            plugin.getLogger().atWarning().log("Nenhuma pergunta/frase/palavra configurada!");
            return;
        }
        
        agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
        plugin.getLogger().atInfo().log("Ciclo iniciado. Proximo quiz em " + config.getIntervaloInicioQuizSegundos() + " segundos.");
    }
    
    /**
     * Verifica se existe algum jogo configurado
     */
    private boolean temPerguntasDisponiveis(ChatQuizConfig config) {
        return !config.getQuizEntries().isEmpty() 
            || !config.getTypeFrases().isEmpty() 
            || !config.getScramblePalavras().isEmpty();
    }
    
    private void agendarProximoQuiz(int segundos) {
        cancelarTarefas();
        proximoQuizTask = scheduler.schedule(this::iniciarQuiz, segundos, TimeUnit.SECONDS);
    }
    
    /**
     * Inicia um novo quiz aleatório (escolhe o tipo baseado no que está disponível)
     */
    public void iniciarQuiz() {
        ChatQuizConfig config = plugin.getConfiguracao();
        
        if (!temPerguntasDisponiveis(config)) {
            plugin.getLogger().atWarning().log("Sem jogos disponiveis.");
            agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
            return;
        }
        
        // Escolhe aleatoriamente um tipo que tenha entradas
        QuizType tipo = escolherTipoAleatorio(config);
        if (tipo == null) {
            agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
            return;
        }
        
        tipoAtual = tipo;
        quizAtivo = true;
        limparDadosAtuais();
        
        // Carrega os dados específicos do tipo
        switch (tipo) {
            case QUIZ:
                carregarQuiz(config);
                break;
            case TYPE:
                carregarType(config);
                break;
            case SCRAMBLE:
                carregarScramble(config);
                break;
        }
        
        // Envia mensagens de início
        enviarMensagemInicio(config);
        
        if (config.getBannerIniciarQuiz().isAtivo()) {
            mostrarBannerParaTodos(
                config.getBannerIniciarQuiz().getTitulo(),
                config.getBannerIniciarQuiz().getSubtitulo()
            );
        }
        
        plugin.getLogger().atInfo().log("Quiz iniciado! Tipo: " + tipo);
        
        timeoutTask = scheduler.schedule(this::finalizarQuizSemVencedor, config.getDuracaoQuizSegundos(), TimeUnit.SECONDS);
    }
    
    /**
     * Escolhe um tipo aleatório que tenha entradas disponíveis
     */
    @Nullable
    private QuizType escolherTipoAleatorio(ChatQuizConfig config) {
        List<QuizType> tiposDisponiveis = new java.util.ArrayList<>();
        
        if (!config.getQuizEntries().isEmpty()) tiposDisponiveis.add(QuizType.QUIZ);
        if (!config.getTypeFrases().isEmpty()) tiposDisponiveis.add(QuizType.TYPE);
        if (!config.getScramblePalavras().isEmpty()) tiposDisponiveis.add(QuizType.SCRAMBLE);
        
        if (tiposDisponiveis.isEmpty()) return null;
        return tiposDisponiveis.get(random.nextInt(tiposDisponiveis.size()));
    }
    
    private void limparDadosAtuais() {
        quizEntryAtual = null;
        fraseAtual = null;
        embaralhadaAtual = null;
    }
    
    private void carregarQuiz(ChatQuizConfig config) {
        List<ChatQuizConfig.QuizEntry> entries = config.getQuizEntries();
        if (!entries.isEmpty()) {
            quizEntryAtual = entries.get(random.nextInt(entries.size()));
        }
    }
    
    private void carregarType(ChatQuizConfig config) {
        List<String> frases = config.getTypeFrases();
        if (!frases.isEmpty()) {
            fraseAtual = frases.get(random.nextInt(frases.size()));
        }
    }
    
    private void carregarScramble(ChatQuizConfig config) {
        List<String> palavras = config.getScramblePalavras();
        if (!palavras.isEmpty()) {
            fraseAtual = palavras.get(random.nextInt(palavras.size()));
            embaralhadaAtual = ChatQuizConfig.embaralharPalavra(fraseAtual);
        }
    }
    
    /**
     * Envia mensagem de início do quiz
     */
    private void enviarMensagemInicio(ChatQuizConfig config) {
        ChatQuizConfig.MensagensConfig msg = config.getMensagens();
        
        // Prefixo
        Universe.get().sendMessage(criarMensagem(msg.getPrefixoQuiz()));
        
        // Linha principal conforme o tipo
        String linhaPrincipal;
        switch (tipoAtual) {
            case QUIZ:
                linhaPrincipal = msg.getQuizIniciou().replace(PLACEHOLDER_PERGUNTA, 
                    quizEntryAtual != null ? quizEntryAtual.getPergunta() : "");
                break;
            case TYPE:
                linhaPrincipal = msg.getTypeIniciou().replace(PLACEHOLDER_FRASE, 
                    fraseAtual != null ? fraseAtual : "");
                break;
            case SCRAMBLE:
                linhaPrincipal = msg.getScrambleIniciou().replace(PLACEHOLDER_EMBARALHADA, 
                    embaralhadaAtual != null ? embaralhadaAtual : "");
                break;
            default:
                linhaPrincipal = "";
        }
        Universe.get().sendMessage(criarMensagem(linhaPrincipal));
        
        // Dica
        String dica;
        switch (tipoAtual) {
            case QUIZ:
                dica = msg.getDicaQuiz();
                break;
            case TYPE:
                dica = msg.getDicaType();
                break;
            case SCRAMBLE:
                dica = msg.getDicaScramble();
                break;
            default:
                dica = "";
        }
        Universe.get().sendMessage(criarMensagem(dica));
    }
    
    /**
     * Processa resposta do chat
     */
    public void processarResposta(@Nonnull PlayerChatEvent event) {
        if (!quizAtivo || tipoAtual == null) {
            return;
        }
        
        com.hypixel.hytale.server.core.universe.PlayerRef sender = event.getSender();
        String mensagem = event.getContent();
        
        if (sender == null || mensagem == null) {
            return;
        }
        
        String nomeJogador = sender.getUsername();
        boolean acertou = false;
        
        switch (tipoAtual) {
            case QUIZ:
                acertou = validarQuiz(mensagem);
                break;
            case TYPE:
                acertou = validarType(mensagem);
                break;
            case SCRAMBLE:
                acertou = validarScramble(mensagem);
                break;
        }
        
        if (acertou) {
            processarVitoria(sender, nomeJogador);
        } else {
            enviarMensagemErro(sender);
        }
    }
    
    /**
     * Valida resposta para tipo QUIZ
     */
    private boolean validarQuiz(String mensagem) {
        if (quizEntryAtual == null) return false;
        String respostaNormalizada = normalizarTexto(mensagem);
        for (String respostaCorreta : quizEntryAtual.getRespostas()) {
            if (respostaNormalizada.equals(normalizarTexto(respostaCorreta))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Valida resposta para tipo TYPE (digitação exata, case insensitive)
     */
    private boolean validarType(String mensagem) {
        if (fraseAtual == null) return false;
        return mensagem.trim().equalsIgnoreCase(fraseAtual.trim());
    }
    
    /**
     * Valida resposta para tipo SCRAMBLE
     */
    private boolean validarScramble(String mensagem) {
        if (fraseAtual == null) return false;
        return normalizarTexto(mensagem).equals(normalizarTexto(fraseAtual));
    }
    
    /**
     * Envia mensagem de erro conforme o tipo do quiz
     */
    private void enviarMensagemErro(com.hypixel.hytale.server.core.universe.PlayerRef player) {
        scheduler.schedule(() -> {
            ChatQuizConfig.MensagensConfig msg = plugin.getConfiguracao().getMensagens();
            String mensagem;
            switch (tipoAtual) {
                case QUIZ:
                    mensagem = msg.getErroQuiz();
                    break;
                case TYPE:
                    mensagem = msg.getErroType();
                    break;
                case SCRAMBLE:
                    mensagem = msg.getErroScramble();
                    break;
                default:
                    mensagem = "&cErrado!";
            }
            player.sendMessage(criarMensagem(mensagem));
        }, 100, TimeUnit.MILLISECONDS);
    }
    
    private void processarVitoria(@Nonnull com.hypixel.hytale.server.core.universe.PlayerRef vencedor, @Nonnull String nomeJogador) {
        quizAtivo = false;
        cancelarTarefas();
        
        ChatQuizConfig config = plugin.getConfiguracao();
        ChatQuizConfig.MensagensConfig msg = config.getMensagens();
        
        // Envia mensagem de vitória
        String mensagemVitoria = msg.getAcertou().replace(PLACEHOLDER_PLAYER, nomeJogador);
        scheduler.schedule(() -> {
            Universe.get().sendMessage(criarMensagem(msg.getPrefixoQuiz()));
            Universe.get().sendMessage(criarMensagem(mensagemVitoria));
        }, 200, TimeUnit.MILLISECONDS);
        
        if (config.getBannerGanhadorQuiz().isAtivo()) {
            mostrarBannerParaTodos(
                config.getBannerGanhadorQuiz().getTitulo(),
                config.getBannerGanhadorQuiz().getSubtitulo().replace(PLACEHOLDER_PLAYER, nomeJogador)
            );
        }
        
        // Executa comandos
        for (String comando : config.getComandosAoAcertar()) {
            String comandoFinal = comando.replace(PLACEHOLDER_PLAYER, nomeJogador);
            try {
                com.hypixel.hytale.server.core.command.system.CommandManager.get()
                    .handleCommand(com.hypixel.hytale.server.core.console.ConsoleSender.INSTANCE, comandoFinal);
            } catch (Exception e) {
                plugin.getLogger().atWarning().log("Erro ao executar comando: " + comandoFinal);
            }
        }
        
        plugin.getLogger().atInfo().log("Quiz finalizado! Tipo: " + tipoAtual + " | Vencedor: " + nomeJogador);
        
        limparEstado();
        agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
    }
    
    private void finalizarQuizSemVencedor() {
        if (!quizAtivo) return;
        
        quizAtivo = false;
        
        ChatQuizConfig.MensagensConfig msg = plugin.getConfiguracao().getMensagens();
        
        // Mensagem de timeout
        String resposta = "";
        switch (tipoAtual) {
            case QUIZ:
                if (quizEntryAtual != null && !quizEntryAtual.getRespostas().isEmpty()) {
                    resposta = quizEntryAtual.getRespostas().get(0);
                }
                break;
            case TYPE:
            case SCRAMBLE:
                resposta = fraseAtual != null ? fraseAtual : "";
                break;
        }
        
        Universe.get().sendMessage(criarMensagem(msg.getPrefixoQuiz()));
        Universe.get().sendMessage(criarMensagem(msg.getNinguemAcertou()));
        if (!resposta.isEmpty()) {
            Universe.get().sendMessage(criarMensagem(msg.getRespostaEra().replace(PLACEHOLDER_RESPOSTA, resposta)));
        }
        
        plugin.getLogger().atInfo().log("Quiz finalizado sem vencedor. Tipo: " + tipoAtual);
        
        limparEstado();
        agendarProximoQuiz(plugin.getConfiguracao().getIntervaloInicioQuizSegundos());
    }
    
    private void limparEstado() {
        tipoAtual = null;
        quizEntryAtual = null;
        fraseAtual = null;
        embaralhadaAtual = null;
    }
    
    public void cancelarQuizAtual() {
        quizAtivo = false;
        limparEstado();
        cancelarTarefas();
    }
    
    private void cancelarTarefas() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
        if (proximoQuizTask != null) {
            proximoQuizTask.cancel(false);
            proximoQuizTask = null;
        }
    }
    
    private void mostrarBannerParaTodos(@Nonnull String titulo, @Nonnull String subtitulo) {
        try {
            Message titleMsg = criarMensagem(titulo);
            Message subtitleMsg = criarMensagem(subtitulo);
            
            for (com.hypixel.hytale.server.core.universe.PlayerRef ref : Universe.get().getPlayers()) {
                EventTitleUtil.showEventTitleToPlayer(ref, titleMsg, subtitleMsg, true);
            }
        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Erro ao mostrar banner: " + e.getMessage());
        }
    }
    
    @Nonnull
    private String normalizarTexto(@Nonnull String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalizado.toLowerCase().trim();
    }
    
    @Nonnull
    private Message criarMensagem(@Nonnull String texto) {
        try {
            return TinyMsg.parse(texto);
        } catch (Exception e) {
            return Message.raw(texto);
        }
    }
    
    public boolean isQuizAtivo() {
        return quizAtivo;
    }
    
    @Nullable
    public QuizType getTipoAtual() {
        return tipoAtual;
    }
    
    @Nullable
    public String getFraseAtual() {
        return fraseAtual;
    }
    
    @Nullable
    public String getEmbaralhadaAtual() {
        return embaralhadaAtual;
    }
    
    @Nullable
    public ChatQuizConfig.QuizEntry getQuizEntryAtual() {
        return quizEntryAtual;
    }
}
