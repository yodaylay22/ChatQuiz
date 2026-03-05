package com.hytale.server.plugin.chatquiz.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuracao do plugin ChatQuiz com suporte a múltiplos tipos de jogos.
 * 
 * Exemplo de arquivo ChatQuizConfig.json:
 * {
 *   "intervaloInicioQuizSegundos": 300,
 *   "duracaoQuizSegundos": 60,
 *   
 *   "mensagens": {
 *     "prefixoQuiz": "&6&lQUIZ",
 *     "quizIniciou": "&fPergunta: &e{pergunta}",
 *     "typeIniciou": "&fDigite rapidamente: &e{frase}",
 *     "scrambleIniciou": "&fDesembaralhe a palavra: &e{embaralhada}",
 *     "dicaQuiz": "&7Digite sua resposta no chat",
 *     "dicaType": "&7Digite exatamente como mostrado acima!",
 *     "dicaScramble": "&7Reorganize as letras para formar a palavra correta!",
 *     "acertou": "&fParabens &a{player}&f! Voce foi o mais rapido!",
 *     "ninguemAcertou": "&cNinguem acertou!",
 *     "respostaEra": "&fA resposta era: &e{resposta}"
 *   },
 *   
 *   "bannerIniciarQuiz": {
 *     "ativo": true,
 *     "titulo": "QUIZ",
 *     "subtitulo": "Novo desafio no chat!"
 *   },
 *   "bannerGanhadorQuiz": {
 *     "ativo": true,
 *     "titulo": "QUIZ",
 *     "subtitulo": "{player} venceu!"
 *   },
 *   
 *   "comandosAoAcertar": ["give {player} Ingredient_Bar_Iron --quantity=10"],
 *   
 *   "quiz": [
 *     {
 *       "pergunta": "Qual é a capital do Brasil?",
 *       "respostas": ["brasilia", "brasília"]
 *     },
 *     {
 *       "pergunta": "Quanto é 2 + 2?",
 *       "respostas": ["4", "quatro"]
 *     }
 *   ],
 *   
 *   "type": [
 *     "Hytale é incrível!",
 *     "O servidor é o melhor!",
 *     "Eu amo jogar aqui!"
 *   ],
 *   
 *   "scramble": [
 *     "minecraft",
 *     "hytale",
 *     "aventura",
 *     "dragao"
 *   ]
 * }
 */
public class ChatQuizConfig {
    
    private int intervaloInicioQuizSegundos = 300;
    private int duracaoQuizSegundos = 60;
    
    private MensagensConfig mensagens = new MensagensConfig();
    private BannerConfig bannerIniciarQuiz = new BannerConfig(true, "QUIZ", "Novo desafio no chat!");
    private BannerConfig bannerGanhadorQuiz = new BannerConfig(true, "QUIZ", "{player} venceu!");
    
    private List<String> comandosAoAcertar = Arrays.asList("give {player} Ingredient_Bar_Iron --quantity=10");
    
    // Seções por tipo de jogo
    @SerializedName("quiz")
    private List<QuizEntry> quizEntries = new ArrayList<>();
    
    @SerializedName("type")
    private List<String> typeFrases = new ArrayList<>();
    
    @SerializedName("scramble")
    private List<String> scramblePalavras = new ArrayList<>();
    
    public ChatQuizConfig() {
        // Perguntas QUIZ padrão
        quizEntries.add(new QuizEntry("Qual é a capital do Brasil?", Arrays.asList("brasilia", "brasília")));
        quizEntries.add(new QuizEntry("Quanto é 2 + 2?", Arrays.asList("4", "quatro")));
        
        // Frases TYPE padrão
        typeFrases.addAll(Arrays.asList(
            "Hytale é incrível!",
            "O servidor é o melhor!",
            "Eu amo jogar aqui!"
        ));
        
        // Palavras SCRAMBLE padrão
        scramblePalavras.addAll(Arrays.asList(
            "minecraft",
            "hytale",
            "aventura",
            "dragao"
        ));
    }
    
    /**
     * Embaralha uma palavra.
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
    
    // Getters e Setters
    public int getIntervaloInicioQuizSegundos() { return intervaloInicioQuizSegundos; }
    public int getDuracaoQuizSegundos() { return duracaoQuizSegundos; }
    public MensagensConfig getMensagens() { return mensagens; }
    public BannerConfig getBannerIniciarQuiz() { return bannerIniciarQuiz; }
    public BannerConfig getBannerGanhadorQuiz() { return bannerGanhadorQuiz; }
    public List<String> getComandosAoAcertar() { return comandosAoAcertar; }
    public List<QuizEntry> getQuizEntries() { return quizEntries; }
    public List<String> getTypeFrases() { return typeFrases; }
    public List<String> getScramblePalavras() { return scramblePalavras; }
    
    public void setIntervaloInicioQuizSegundos(int intervalo) { this.intervaloInicioQuizSegundos = intervalo; }
    public void setDuracaoQuizSegundos(int duracao) { this.duracaoQuizSegundos = duracao; }
    public void setMensagens(MensagensConfig mensagens) { this.mensagens = mensagens; }
    public void setBannerIniciarQuiz(BannerConfig banner) { this.bannerIniciarQuiz = banner; }
    public void setBannerGanhadorQuiz(BannerConfig banner) { this.bannerGanhadorQuiz = banner; }
    public void setComandosAoAcertar(List<String> comandos) { this.comandosAoAcertar = comandos; }
    public void setQuizEntries(List<QuizEntry> entries) { this.quizEntries = entries; }
    public void setTypeFrases(List<String> frases) { this.typeFrases = frases; }
    public void setScramblePalavras(List<String> palavras) { this.scramblePalavras = palavras; }
    
    /**
     * Configurações de mensagens personalizáveis
     */
    public static class MensagensConfig {
        private String prefixoQuiz = "&6&lQUIZ";
        private String quizIniciou = "&fPergunta: &e{pergunta}";
        private String typeIniciou = "&fDigite rapidamente: &e{frase}";
        private String scrambleIniciou = "&fDesembaralhe a palavra: &e{embaralhada}";
        private String dicaQuiz = "&7Digite sua resposta no chat";
        private String dicaType = "&7Digite exatamente como mostrado acima!";
        private String dicaScramble = "&7Reorganize as letras para formar a palavra correta!";
        private String acertou = "&fParabens &a{player}&f! Voce foi o mais rapido!";
        private String ninguemAcertou = "&cNinguem acertou!";
        private String respostaEra = "&fA resposta era: &e{resposta}";
        private String erroQuiz = "&cResposta errada! &fTente novamente.";
        private String erroType = "&cErrado! &fDigite exatamente como mostrado.";
        private String erroScramble = "&cErrado! &fTente reorganizar as letras.";
        
        public String getPrefixoQuiz() { return prefixoQuiz; }
        public String getQuizIniciou() { return quizIniciou; }
        public String getTypeIniciou() { return typeIniciou; }
        public String getScrambleIniciou() { return scrambleIniciou; }
        public String getDicaQuiz() { return dicaQuiz; }
        public String getDicaType() { return dicaType; }
        public String getDicaScramble() { return dicaScramble; }
        public String getAcertou() { return acertou; }
        public String getNinguemAcertou() { return ninguemAcertou; }
        public String getRespostaEra() { return respostaEra; }
        public String getErroQuiz() { return erroQuiz; }
        public String getErroType() { return erroType; }
        public String getErroScramble() { return erroScramble; }
        
        public void setPrefixoQuiz(String s) { this.prefixoQuiz = s; }
        public void setQuizIniciou(String s) { this.quizIniciou = s; }
        public void setTypeIniciou(String s) { this.typeIniciou = s; }
        public void setScrambleIniciou(String s) { this.scrambleIniciou = s; }
        public void setDicaQuiz(String s) { this.dicaQuiz = s; }
        public void setDicaType(String s) { this.dicaType = s; }
        public void setDicaScramble(String s) { this.dicaScramble = s; }
        public void setAcertou(String s) { this.acertou = s; }
        public void setNinguemAcertou(String s) { this.ninguemAcertou = s; }
        public void setRespostaEra(String s) { this.respostaEra = s; }
        public void setErroQuiz(String s) { this.erroQuiz = s; }
        public void setErroType(String s) { this.erroType = s; }
        public void setErroScramble(String s) { this.erroScramble = s; }
    }
    
    /**
     * Entry para Quiz tradicional (pergunta + lista de respostas)
     */
    public static class QuizEntry {
        @SerializedName("pergunta")
        private String pergunta = "";
        
        @SerializedName("respostas")
        private List<String> respostas = new ArrayList<>();
        
        public QuizEntry() {}
        
        public QuizEntry(String pergunta, List<String> respostas) {
            this.pergunta = pergunta;
            this.respostas = respostas;
        }
        
        public String getPergunta() { return pergunta; }
        public List<String> getRespostas() { return respostas; }
        
        public void setPergunta(String pergunta) { this.pergunta = pergunta; }
        public void setRespostas(List<String> respostas) { this.respostas = respostas; }
    }
    
    /**
     * Config de Banner
     */
    public static class BannerConfig {
        private boolean ativo = true;
        private String titulo = "QUIZ";
        private String subtitulo = "Novo desafio no chat!";
        
        public BannerConfig() {}
        
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
}
