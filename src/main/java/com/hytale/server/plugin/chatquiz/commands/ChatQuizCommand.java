package com.hytale.server.plugin.chatquiz.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hytale.server.plugin.chatquiz.ChatQuizPlugin;
import com.hytale.server.plugin.chatquiz.manager.QuizManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Comando /chatquiz
 * Permite iniciar quiz manualmente ou recarregar configuracao.
 */
public class ChatQuizCommand extends AbstractAsyncCommand {
    
    private final ChatQuizPlugin plugin;
    
    public ChatQuizCommand(@Nonnull ChatQuizPlugin plugin) {
        super("chatquiz", "server.commands.chatquiz.desc");
        this.plugin = plugin;
    }
    
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        return CompletableFuture.runAsync(() -> {
            // Comando simplificado - sempre inicia quiz
            // Cancela quiz atual se houver
            plugin.getQuizManager().cancelarQuizAtual();
            
            // Inicia quiz imediatamente
            plugin.getQuizManager().iniciarQuiz();
            
            LOGGER.atInfo().log("Quiz iniciado manualmente.");
            plugin.getLogger().atInfo().log("Quiz iniciado por comando.");
        });
    }
}
