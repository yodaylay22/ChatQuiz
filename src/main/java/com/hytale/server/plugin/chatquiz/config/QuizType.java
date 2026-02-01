package com.hytale.server.plugin.chatquiz.config;

/**
 * Tipos de quiz suportados pelo plugin.
 */
public enum QuizType {
    /**
     * TYPE - Primeiro jogador a digitar a frase exata ganha.
     * A resposta deve ser digitada exatamente como configurada (case insensitive).
     */
    TYPE,
    
    /**
     * SCRAMBLE - Desembaralhar uma palavra corretamente.
     * O plugin embaralha a palavra e mostra aos jogadores.
     */
    SCRAMBLE,
    
    /**
     * QUIZ - Responder uma pergunta simples corretamente.
     * Formato tradicional de pergunta e resposta.
     */
    QUIZ
}
