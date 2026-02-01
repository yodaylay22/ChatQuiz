package com.hytale.server.plugin.chatquiz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hytale.server.plugin.chatquiz.commands.ChatQuizCommand;
import com.hytale.server.plugin.chatquiz.config.ChatQuizConfig;
import com.hytale.server.plugin.chatquiz.listener.ChatQuizListener;
import com.hytale.server.plugin.chatquiz.manager.QuizManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plugin ChatQuiz para Hytale.
 * Sistema de quiz automático baseado em chat.
 */
public class ChatQuizPlugin extends JavaPlugin {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String CONFIG_FILE = "ChatQuizConfig.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private ChatQuizConfig configuracao;
    private QuizManager quizManager;
    
    public ChatQuizPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }
    
    @Override
    protected void setup() {
        LOGGER.atInfo().log("Inicializando ChatQuiz...");
        
        // Carrega configuração
        try {
            carregarConfiguracao();
        } catch (Exception e) {
            LOGGER.atWarning().log("Erro ao carregar configuracao inicial: " + e.getMessage());
            // Cria configuração padrão
            configuracao = new ChatQuizConfig();
            salvarConfiguracaoPadrao();
        }
        
        // Inicializa manager
        quizManager = new QuizManager(this);
        
        // Registra comandos
        getCommandRegistry().registerCommand(new ChatQuizCommand(this));
        
        // Registra eventos
        getEventRegistry().registerGlobal(PlayerChatEvent.class, new ChatQuizListener(this, quizManager));
        
        LOGGER.atInfo().log("ChatQuiz inicializado com sucesso!");
    }
    
    @Override
    protected void start() {
        LOGGER.atInfo().log("Iniciando ciclo de quiz...");
        
        // Inicia o ciclo automático
        quizManager.iniciarCiclo();
        
        LOGGER.atInfo().log("ChatQuiz ativo!");
    }
    
    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Desligando ChatQuiz...");
        
        // Cancela quiz em andamento
        if (quizManager != null) {
            quizManager.cancelarQuizAtual();
        }
        
        LOGGER.atInfo().log("ChatQuiz desligado.");
    }
    
    /**
     * Carrega a configuração do arquivo
     */
    public void carregarConfiguracao() throws IOException {
        Path configPath = getConfigPath();
        
        // Cria pasta do plugin se não existir
        Path dataFolder = configPath.getParent();
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder);
        }
        
        // Cria configuração padrão se não existir
        if (!Files.exists(configPath)) {
            LOGGER.atInfo().log("Arquivo de configuracao nao encontrado. Criando configuracao padrao...");
            configuracao = new ChatQuizConfig();
            salvarConfiguracaoPadrao();
            return;
        }
        
        // Lê arquivo e converte
        String json = Files.readString(configPath);
        configuracao = GSON.fromJson(json, ChatQuizConfig.class);
        
        LOGGER.atInfo().log("Configuracao carregada com sucesso!");
        LOGGER.atInfo().log("Intervalo: " + configuracao.getIntervaloInicioQuizSegundos() + "s");
        LOGGER.atInfo().log("Duracao: " + configuracao.getDuracaoQuizSegundos() + "s");
        LOGGER.atInfo().log("Perguntas: " + configuracao.getPerguntas().size());
    }
    
    /**
     * Salva a configuração padrão no arquivo
     */
    private void salvarConfiguracaoPadrao() {
        try {
            Path configPath = getConfigPath();
            Path dataFolder = configPath.getParent();
            
            // Cria pasta do plugin se não existir
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }
            
            // Salva configuração como JSON
            String json = GSON.toJson(configuracao);
            Files.writeString(configPath, json);
            
            LOGGER.atInfo().log("Configuracao padrao salva em: " + configPath);
            
        } catch (IOException e) {
            LOGGER.atWarning().log("Erro ao salvar configuracao padrao: " + e.getMessage());
        }
    }
    
    /**
     * Obtém o caminho do arquivo de configuração
     */
    private Path getConfigPath() {
        // Usa o diretório de trabalho do servidor como base
        String workingDir = System.getProperty("user.dir");
        return Path.of(workingDir, "mods", "ChatQuiz", CONFIG_FILE);
    }
    
    /**
     * Obtém o logger
     */
    public HytaleLogger getLogger() {
        return LOGGER;
    }
    
    /**
     * Obtém a configuração atual
     */
    @Nonnull
    public ChatQuizConfig getConfiguracao() {
        return configuracao;
    }
    
    /**
     * Obtém o gerenciador de quiz
     */
    @Nonnull
    public QuizManager getQuizManager() {
        return quizManager;
    }
}
