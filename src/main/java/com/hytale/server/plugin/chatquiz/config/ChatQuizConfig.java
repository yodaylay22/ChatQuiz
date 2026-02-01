package com.hytale.server.plugin.chatquiz.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuracao do plugin ChatQuiz.
 * 
 * Exemplo de arquivo ChatQuizConfig.json:
 * {
 *   "intervaloInicioQuizSegundos": 300,
 *   "duracaoQuizSegundos": 60,
 *   "mensagemAntesQuizGlobal": [
 *     "&6&lQUIZ",
 *     "&fPergunta: &e{pergunta}",
 *     "&7Digite sua resposta no chat"
 *   ],
 *   "mensagemGlobalAoAcertar": [
 *     "&6&lQUIZ",
 *     "&fParabens &a{player}&f!",
 *     "&fVoce acertou a pergunta corretamente."
 *   ],
 *   "bannerIniciarQuiz": {
 *     "ativo": true,
 *     "titulo": "QUIZ",
 *     "subtitulo": "Responda a pergunta no chat!"
 *   },
 *   "bannerGanhadorQuiz": {
 *     "ativo": true,
 *     "titulo": "QUIZ",
 *     "subtitulo": "{player} acertou a pergunta!"
 *   },
 *   "comandosGlobaisAoAcertar": ["give {player} Ingredient_Bar_Iron --quantity=10"],
 *   "perguntas": [
 *     {
 *       "Tipo": "QUIZ",
 *       "Pergunta": "Qual é a capital do Brasil?",
 *       "Respostas": ["brasilia", "brasília"],
 *       "ComandosAoAcertar": ["give {player} Ingredient_Bar_Gold --quantity=1"]
 *     },
 *     {
 *       "Tipo": "TYPE",
 *       "Pergunta": "Digite rapidamente: Hytale é incrível!",
 *       "Respostas": ["Hytale é incrível!"],
 *       "ComandosAoAcertar": ["give {player} Ingredient_Bar_Gold --quantity=1"]
 *     },
 *     {
 *       "Tipo": "SCRAMBLE",
 *       "Pergunta": "hytaleserver",
 *       "Respostas": ["hytaleserver"],
 *       "ComandosAoAcertar": ["give {player} Ingredient_Bar_Gold --quantity=1"]
 *     }
 *   ]
 * }
 * 
 * Cores disponiveis:
 * &0 = preto       &8 = cinza escuro
 * &1 = azul escuro &9 = azul
 * &2 = verde       &a = verde claro
 * &3 = aqua        &b = ciano
 * &4 = vermelho    &c = vermelho claro
 * &5 = roxo        &d = rosa
 * &6 = dourado     &e = amarelo
 * &7 = cinza       &f = branco
 * &l = negrito     &n = sublinhado
 * &o = italico     &m = tachado
 * &r = reset
 * 
 * Tipos de Quiz:
 * - QUIZ: Pergunta tradicional com múltiplas respostas possíveis
 * - TYPE: Primeiro a digitar a frase exata (rápido!)
 * - SCRAMBLE: Desembaralhe a palavra mostrada
 */
public class ChatQuizConfig {
    
    private int intervaloInicioQuizSegundos = 300;
    private int duracaoQuizSegundos = 60;
    private List<String> mensagemAntesQuizGlobal = Arrays.asList(
        "&6&lQUIZ",
        "&fPergunta: &e{pergunta}",
        "&7Digite sua resposta no chat"
    );
    private List<String> mensagemGlobalAoAcertar = Arrays.asList(
        "&6&lQUIZ",
        "&fParabens &a{player}&f!",
        "&fVoce acertou a pergunta corretamente."
    );
    private BannerConfig bannerIniciarQuiz = new BannerConfig(true, "QUIZ", "Responda a pergunta no chat!");
    private BannerConfig bannerGanhadorQuiz = new BannerConfig(true, "QUIZ", "{player} acertou a pergunta!");
    private List<String> comandosGlobaisAoAcertar = Arrays.asList("give {player} Ingredient_Bar_Iron --quantity=10");
    private List<PerguntaConfig> perguntas = new ArrayList<>();
    
    public ChatQuizConfig() {
        // Tipo QUIZ - Pergunta tradicional
        PerguntaConfig p1 = new PerguntaConfig();
        p1.setTipo(QuizType.QUIZ);
        p1.setPergunta("Qual é a capital do Brasil?");
        p1.setRespostas(Arrays.asList("brasilia", "brasília"));
        p1.setComandosAoAcertar(Arrays.asList("give {player} Ingredient_Bar_Gold --quantity=1"));
        perguntas.add(p1);
        
        // Tipo TYPE - Digitação rápida
        PerguntaConfig p2 = new PerguntaConfig();
        p2.setTipo(QuizType.TYPE);
        p2.setPergunta("Digite rapidamente: Hytale é incrível!");
        p2.setRespostas(Arrays.asList("Hytale é incrível!"));
        p2.setComandosAoAcertar(Arrays.asList("give {player} Ingredient_Bar_Gold --quantity=1"));
        perguntas.add(p2);
        
        // Tipo SCRAMBLE - Desembaralhar
        PerguntaConfig p3 = new PerguntaConfig();
        p3.setTipo(QuizType.SCRAMBLE);
        p3.setPergunta("hytaleserver");  // Palavra original (será embaralhada pelo plugin)
        p3.setRespostas(Arrays.asList("hytaleserver"));
        p3.setComandosAoAcertar(Arrays.asList("give {player} Ingredient_Bar_Gold --quantity=1"));
        perguntas.add(p3);
    }
    
    /**
     * Embaralha uma palavra para o modo SCRAMBLE.
     * @param palavra Palavra original
     * @return Palavra com letras embaralhadas
     */
    public static String embaralharPalavra(String palavra) {
        List<Character> letras = new ArrayList<>();
        for (char c : palavra.toCharArray()) {
            letras.add(c);
        }
        Collections.shuffle(letras);
        StringBuilder embaralhada = new StringBuilder();
        for (char c : letras) {
            embaralhada.append(c);
        }
        return embaralhada.toString();
    }
    
    public int getIntervaloInicioQuizSegundos() { return intervaloInicioQuizSegundos; }
    public int getDuracaoQuizSegundos() { return duracaoQuizSegundos; }
    public List<String> getMensagemAntesQuizGlobal() { return mensagemAntesQuizGlobal; }
    public List<String> getMensagemGlobalAoAcertar() { return mensagemGlobalAoAcertar; }
    public BannerConfig getBannerIniciarQuiz() { return bannerIniciarQuiz; }
    public BannerConfig getBannerGanhadorQuiz() { return bannerGanhadorQuiz; }
    public List<String> getComandosGlobaisAoAcertar() { return comandosGlobaisAoAcertar; }
    public List<PerguntaConfig> getPerguntas() { return perguntas; }
    
    public void setIntervaloInicioQuizSegundos(int intervalo) { this.intervaloInicioQuizSegundos = intervalo; }
    public void setDuracaoQuizSegundos(int duracao) { this.duracaoQuizSegundos = duracao; }
    public void setMensagemAntesQuizGlobal(List<String> mensagem) { this.mensagemAntesQuizGlobal = mensagem; }
    public void setMensagemGlobalAoAcertar(List<String> mensagem) { this.mensagemGlobalAoAcertar = mensagem; }
    public void setBannerIniciarQuiz(BannerConfig banner) { this.bannerIniciarQuiz = banner; }
    public void setBannerGanhadorQuiz(BannerConfig banner) { this.bannerGanhadorQuiz = banner; }
    public void setComandosGlobaisAoAcertar(List<String> comandos) { this.comandosGlobaisAoAcertar = comandos; }
    public void setPerguntas(List<PerguntaConfig> perguntas) { this.perguntas = perguntas; }
    
    public static class BannerConfig {
        private boolean ativo;
        private String titulo;
        private String subtitulo;
        
        public BannerConfig() {
            this(true, "QUIZ", "Responda a pergunta no chat!");
        }
        
        public BannerConfig(boolean ativo, String titulo, String subtitulo) {
            this.ativo = ativo;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
        }
        
        public boolean isAtivo() { return ativo; }
        public String getTitulo() { return titulo; }
        public String getSubtitulo() { return subtitulo; }
        
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }
    }
    
    public static class PerguntaConfig {
        @SerializedName("Tipo")
        private QuizType tipo = QuizType.QUIZ;
        
        @SerializedName("Pergunta")
        private String pergunta = "";
        
        @SerializedName("Respostas")
        private List<String> respostas = new ArrayList<>();
        
        @SerializedName("ComandosAoAcertar")
        private List<String> comandosAoAcertar = new ArrayList<>();
        
        public PerguntaConfig() {}
        
        public QuizType getTipo() { return tipo; }
        public String getPergunta() { return pergunta; }
        public List<String> getRespostas() { return respostas; }
        public List<String> getComandosAoAcertar() { return comandosAoAcertar; }
        
        public void setTipo(QuizType tipo) { this.tipo = tipo; }
        public void setPergunta(String pergunta) { this.pergunta = pergunta; }
        public void setRespostas(List<String> respostas) { this.respostas = respostas; }
        public void setComandosAoAcertar(List<String> comandos) { this.comandosAoAcertar = comandos; }
    }
}
