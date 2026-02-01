package com.hytale.server.plugin.chatquiz.listener;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hytale.server.plugin.chatquiz.ChatQuizPlugin;
import com.hytale.server.plugin.chatquiz.manager.QuizManager;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Listener de eventos para o ChatQuiz.
 * Captura mensagens de chat durante quizzes ativos.
 */
public class ChatQuizListener implements Consumer<PlayerChatEvent> {
    
    private final ChatQuizPlugin plugin;
    private final QuizManager quizManager;
    
    public ChatQuizListener(@Nonnull ChatQuizPlugin plugin, @Nonnull QuizManager quizManager) {
        this.plugin = plugin;
        this.quizManager = quizManager;
    }
    
    @Override
    public void accept(PlayerChatEvent event) {
        // Se não há quiz ativo, não faz nada
        if (!quizManager.isQuizAtivo()) {
            return;
        }
        
        // Processa a resposta
        // O evento NÃO é cancelado - todas as mensagens permanecem visíveis no chat
        quizManager.processarResposta(event);
    }
}
