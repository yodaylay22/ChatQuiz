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
    private ChatQuizConfig.PerguntaConfig perguntaAtual = null;
    private String palavraEmbaralhada = null; // Usado no modo SCRAMBLE
    private ScheduledFuture<?> timeoutTask = null;
    private ScheduledFuture<?> proximoQuizTask = null;
    
    public static final String PERMISSION_ADMIN = "chatquiz.admin";
    
    // Placeholders para mensagens dinâmicas
    private static final String PLACEHOLDER_PERGUNTA = "{pergunta}";
    private static final String PLACEHOLDER_PLAYER = "{player}";
    private static final String PLACEHOLDER_SCRAMBLE = "{scramble}";
    private static final String PLACEHOLDER_TIPO = "{tipo}";
    
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
        if (config.getPerguntas().isEmpty()) {
            plugin.getLogger().atWarning().log("Nenhuma pergunta configurada!");
            return;
        }
        
        agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
        plugin.getLogger().atInfo().log("Ciclo iniciado. Proximo quiz em " + config.getIntervaloInicioQuizSegundos() + " segundos.");
    }
    
    private void agendarProximoQuiz(int segundos) {
        cancelarTarefas();
        proximoQuizTask = scheduler.schedule(this::iniciarQuiz, segundos, TimeUnit.SECONDS);
    }
    
    /**
     * Inicia um novo quiz
     */
    public void iniciarQuiz() {
        ChatQuizConfig config = plugin.getConfiguracao();
        List<ChatQuizConfig.PerguntaConfig> perguntas = config.getPerguntas();
        
        if (perguntas.isEmpty()) {
            plugin.getLogger().atWarning().log("Sem perguntas disponiveis.");
            agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
            return;
        }
        
        perguntaAtual = perguntas.get(random.nextInt(perguntas.size()));
        quizAtivo = true;
        palavraEmbaralhada = null;
        
        // Se for SCRAMBLE, embaralha a primeira resposta
        String textoExibido = perguntaAtual.getPergunta();
        if (perguntaAtual.getTipo() == QuizType.SCRAMBLE && !perguntaAtual.getRespostas().isEmpty()) {
            palavraEmbaralhada = ChatQuizConfig.embaralharPalavra(perguntaAtual.getRespostas().get(0));
            textoExibido = palavraEmbaralhada;
        }
        
        // Envia mensagem de início com placeholders substituídos
        enviarMensagemInicio(config, perguntaAtual, textoExibido);
        
        if (config.getBannerIniciarQuiz().isAtivo()) {
            mostrarBannerParaTodos(
                config.getBannerIniciarQuiz().getTitulo(),
                substituirPlaceholders(config.getBannerIniciarQuiz().getSubtitulo(), "", textoExibido, perguntaAtual.getTipo())
            );
        }
        
        plugin.getLogger().atInfo().log("Quiz iniciado! Tipo: " + perguntaAtual.getTipo() + " | " + perguntaAtual.getPergunta());
        
        timeoutTask = scheduler.schedule(this::finalizarQuizSemVencedor, config.getDuracaoQuizSegundos(), TimeUnit.SECONDS);
    }
    
    /**
     * Envia mensagem de início do quiz com formatação apropriada para cada tipo
     */
    private void enviarMensagemInicio(ChatQuizConfig config, ChatQuizConfig.PerguntaConfig pergunta, String textoExibido) {
        QuizType tipo = pergunta.getTipo();
        
        for (String linha : config.getMensagemAntesQuizGlobal()) {
            String mensagem = linha;
            
            // Substitui placeholders
            mensagem = mensagem.replace(PLACEHOLDER_PERGUNTA, textoExibido);
            mensagem = mensagem.replace(PLACEHOLDER_SCRAMBLE, palavraEmbaralhada != null ? palavraEmbaralhada : "");
            mensagem = mensagem.replace(PLACEHOLDER_TIPO, tipo.name());
            
            // Ajusta mensagens conforme o tipo
            if (tipo == QuizType.TYPE && mensagem.contains("Pergunta:")) {
                mensagem = mensagem.replace("Pergunta:", "Digite:");
            } else if (tipo == QuizType.SCRAMBLE && mensagem.contains("Pergunta:")) {
                mensagem = mensagem.replace("Pergunta:", "Desembaralhe:");
            }
            
            // Adiciona dica sobre o tipo no final da mensagem
            if (mensagem.contains("Digite sua resposta") && tipo != QuizType.QUIZ) {
                if (tipo == QuizType.TYPE) {
                    mensagem = mensagem + " &7(rapido!)";
                } else if (tipo == QuizType.SCRAMBLE) {
                    mensagem = mensagem + " &7(desembaralhe!)";
                }
            }
            
            Universe.get().sendMessage(criarMensagem(mensagem));
        }
    }
    
    /**
     * Processa resposta do chat
     */
    public void processarResposta(@Nonnull PlayerChatEvent event) {
        if (!quizAtivo || perguntaAtual == null) {
            return;
        }
        
        com.hypixel.hytale.server.core.universe.PlayerRef sender = event.getSender();
        String mensagem = event.getContent();
        
        if (sender == null || mensagem == null) {
            return;
        }
        
        String nomeJogador = sender.getUsername();
        QuizType tipo = perguntaAtual.getTipo();
        
        boolean acertou = false;
        
        switch (tipo) {
            case TYPE:
                // TYPE: comparação exata, sem normalização (apenas trim)
                acertou = validarTipo(mensagem, perguntaAtual.getRespostas());
                break;
            case SCRAMBLE:
                // SCRAMBLE: validação normalizada
                acertou = validarScramble(mensagem, perguntaAtual.getRespostas());
                break;
            case QUIZ:
            default:
                // QUIZ: validação normalizada (case insensitive, sem acentos)
                acertou = validarQuiz(mensagem, perguntaAtual.getRespostas());
                break;
        }
        
        if (acertou) {
            processarVitoria(sender, nomeJogador);
        } else {
            // Resposta errada - mensagem conforme o tipo
            enviarMensagemErro(sender, tipo);
        }
    }
    
    /**
     * Valida resposta para tipo TYPE (digitação exata, case insensitive mas preserva espaços/pontuação)
     */
    private boolean validarTipo(String mensagem, List<String> respostas) {
        String respostaDigitada = mensagem.trim();
        for (String respostaCorreta : respostas) {
            if (respostaDigitada.equalsIgnoreCase(respostaCorreta.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Valida resposta para tipo SCRAMBLE (normalizado)
     */
    private boolean validarScramble(String mensagem, List<String> respostas) {
        String respostaNormalizada = normalizarTexto(mensagem);
        for (String respostaCorreta : respostas) {
            if (respostaNormalizada.equals(normalizarTexto(respostaCorreta))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Valida resposta para tipo QUIZ (normalizado, suporta múltiplas variações)
     */
    private boolean validarQuiz(String mensagem, List<String> respostas) {
        String respostaNormalizada = normalizarTexto(mensagem);
        for (String respostaCorreta : respostas) {
            if (respostaNormalizada.equals(normalizarTexto(respostaCorreta))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Envia mensagem de erro conforme o tipo do quiz
     */
    private void enviarMensagemErro(com.hypixel.hytale.server.core.universe.PlayerRef player, QuizType tipo) {
        scheduler.schedule(() -> {
            String mensagem;
            switch (tipo) {
                case TYPE:
                    mensagem = "&cErrado! &fDigite exatamente como mostrado.";
                    break;
                case SCRAMBLE:
                    mensagem = "&cErrado! &fTente reorganizar as letras.";
                    break;
                case QUIZ:
                default:
                    mensagem = "&cResposta errada! &fTente novamente.";
                    break;
            }
            player.sendMessage(criarMensagem(mensagem));
        }, 100, TimeUnit.MILLISECONDS);
    }
    
    private void processarVitoria(@Nonnull com.hypixel.hytale.server.core.universe.PlayerRef vencedor, @Nonnull String nomeJogador) {
        quizAtivo = false;
        cancelarTarefas();
        
        ChatQuizConfig config = plugin.getConfiguracao();
        
        // Envia mensagem global para todos (incluindo o ganhador) com delay
        scheduler.schedule(() -> {
            enviarMensagemGlobalComPlayer(config.getMensagemGlobalAoAcertar(), nomeJogador, "");
        }, 200, TimeUnit.MILLISECONDS);
        
        if (config.getBannerGanhadorQuiz().isAtivo()) {
            mostrarBannerParaTodos(
                config.getBannerGanhadorQuiz().getTitulo(),
                substituirPlaceholders(config.getBannerGanhadorQuiz().getSubtitulo(), nomeJogador, "", perguntaAtual.getTipo())
            );
        }
        
        executarComandos(config.getComandosGlobaisAoAcertar(), nomeJogador);
        
        if (perguntaAtual != null) {
            executarComandos(perguntaAtual.getComandosAoAcertar(), nomeJogador);
        }
        
        plugin.getLogger().atInfo().log("Quiz finalizado! Vencedor: " + nomeJogador);
        
        perguntaAtual = null;
        palavraEmbaralhada = null;
        agendarProximoQuiz(config.getIntervaloInicioQuizSegundos());
    }
    
    private void finalizarQuizSemVencedor() {
        if (!quizAtivo) {
            return;
        }
        
        quizAtivo = false;
        
        // Mostra a resposta correta dependendo do tipo
        String mensagemTimeout = "&6&lQUIZ &cNinguem acertou a pergunta!";
        if (perguntaAtual != null) {
            QuizType tipo = perguntaAtual.getTipo();
            if (tipo == QuizType.SCRAMBLE && !perguntaAtual.getRespostas().isEmpty()) {
                mensagemTimeout = "&6&lQUIZ &cNinguem acertou! &fA palavra era: &e" + perguntaAtual.getRespostas().get(0);
            } else if (tipo == QuizType.TYPE && !perguntaAtual.getRespostas().isEmpty()) {
                mensagemTimeout = "&6&lQUIZ &cNinguem digitou a frase corretamente!";
            }
        }
        
        Universe.get().sendMessage(criarMensagem(mensagemTimeout));
        
        plugin.getLogger().atInfo().log("Quiz finalizado sem vencedor.");
        
        perguntaAtual = null;
        palavraEmbaralhada = null;
        agendarProximoQuiz(plugin.getConfiguracao().getIntervaloInicioQuizSegundos());
    }
    
    public void cancelarQuizAtual() {
        quizAtivo = false;
        perguntaAtual = null;
        palavraEmbaralhada = null;
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
    
    private void enviarMensagemGlobal(@Nonnull List<String> mensagens, @Nonnull String pergunta) {
        enviarMensagemGlobalComPlayer(mensagens, "", pergunta);
    }
    
    private void enviarMensagemGlobalComPlayer(@Nonnull List<String> mensagens, @Nonnull String player, @Nonnull String pergunta) {
        enviarMensagemGlobalComPlayer(mensagens, player, pergunta, QuizType.QUIZ);
    }
    
    private void enviarMensagemGlobalComPlayer(@Nonnull List<String> mensagens, @Nonnull String player, @Nonnull String pergunta, QuizType tipo) {
        try {
            for (String linha : mensagens) {
                String mensagem = substituirPlaceholders(linha, player, pergunta, tipo);
                Universe.get().sendMessage(criarMensagem(mensagem));
            }
        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Erro ao enviar mensagem: " + e.getMessage());
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
    
    private void executarComandos(@Nonnull List<String> comandos, @Nonnull String nomeJogador) {
        for (String comando : comandos) {
            String comandoFinal = substituirPlaceholders(comando, nomeJogador, "", QuizType.QUIZ);
            
            try {
                com.hypixel.hytale.server.core.command.system.CommandManager.get()
                    .handleCommand(com.hypixel.hytale.server.core.console.ConsoleSender.INSTANCE, comandoFinal);
            } catch (Exception e) {
                plugin.getLogger().atWarning().log("Erro ao executar comando: " + comandoFinal);
            }
        }
    }
    
    @Nonnull
    private String substituirPlaceholders(@Nonnull String texto, @Nonnull String player, @Nonnull String pergunta, QuizType tipo) {
        return texto.replace(PLACEHOLDER_PLAYER, player)
                    .replace(PLACEHOLDER_PERGUNTA, pergunta)
                    .replace(PLACEHOLDER_TIPO, tipo.name());
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
    public ChatQuizConfig.PerguntaConfig getPerguntaAtual() {
        return perguntaAtual;
    }
    
    @Nullable
    public String getPalavraEmbaralhada() {
        return palavraEmbaralhada;
    }
}
