# ChatQuiz Plugin para Hytale

Sistema de quiz automático baseado em chat para servidores Hytale.

## 📋 Funcionalidades

- **Quiz Automático**: Inicia quizzes periodicamente em intervalos configuráveis
- **Respostas via Chat**: Os jogadores respondem diretamente no chat global
- **Mensagens Visíveis**: Todas as tentativas de resposta ficam visíveis para todos
- **Sistema de Recompensas**: Execute comandos automaticamente para o vencedor
- **Banners**: Exibe banners de início e vitória (opcional)
- **Configuração Flexível**: Personalize perguntas, respostas, mensagens e comandos
- **Comando Admin**: `/chatquiz reload` para recarregar configuração

## 🚀 Instalação

1. Compile o plugin usando Gradle:
   ```bash
   ./gradlew build
   ```

2. Copie o JAR gerado para a pasta de plugins do servidor Hytale

3. Coloque o arquivo `ChatQuizConfig.json` na pasta do plugin

4. Inicie o servidor

## ⚙️ Configuração

O arquivo `ChatQuizConfig.json` deve ser colocado na pasta de dados do plugin (`plugins/ChatQuiz/`):

```json
{
  "IntervaloInicioQuizSegundos": 300,
  "DuracaoQuizSegundos": 60,
  "MensagemAntesQuizGlobal": [
    "&eQUIZ",
    "&fPergunta: &7{pergunta}",
    "&8Digite sua resposta no chat"
  ],
  "MensagemGlobalAoAcertar": [
    "&eQUIZ",
    "&a{player} &facertou a pergunta!"
  ],
  "MensagemPrivadaAoAcertar": [
    "&eQUIZ",
    "&fParabéns &a{player}&f!",
    "&fVocê acertou a pergunta corretamente."
  ],
  "BannerIniciarQuiz": {
    "Ativo": true,
    "Titulo": "&6&lQUIZ",
    "Subtitulo": "&fResponda a pergunta no chat!"
  },
  "BannerGanhadorQuiz": {
    "Ativo": true,
    "Titulo": "&6&lQUIZ",
    "Subtitulo": "&fParabéns &a{player}&f! Você acertou!"
  },
  "ComandosGlobaisAoAcertar": [
    "give {player} diamond 1"
  ],
  "Perguntas": [
    {
      "Pergunta": "Qual é a capital do Brasil?",
      "Respostas": [
        "brasilia",
        "brasília"
      ],
      "ComandosAoAcertar": [
        "give {player} gold_ingot 5"
      ]
    }
  ]
}
```

### Campos de Configuração

| Campo | Descrição | Padrão |
|-------|-----------|--------|
| `IntervaloInicioQuizSegundos` | Tempo entre quizzes (segundos) | 300 |
| `DuracaoQuizSegundos` | Duração máxima de cada quiz (segundos) | 60 |
| `MensagemAntesQuizGlobal` | Mensagens enviadas no início do quiz | - |
| `MensagemGlobalAoAcertar` | Mensagens enviadas quando alguém acerta | - |
| `MensagemPrivadaAoAcertar` | Mensagens privadas ao vencedor | - |
| `BannerIniciarQuiz` | Configuração do banner de início | - |
| `BannerGanhadorQuiz` | Configuração do banner de vitória | - |
| `ComandosGlobaisAoAcertar` | Comandos executados para todo vencedor | - |
| `Perguntas` | Lista de perguntas e respostas | - |

### Placeholders

- `{player}` - Nome do jogador
- `{pergunta}` - Texto da pergunta

### Cores

Use `&` seguido do código de cor:
- `&a` - Verde
- `&e` - Amarelo
- `&f` - Branco
- `&7` - Cinza
- `&6` - Dourado
- `&c` - Vermelho

## 🔧 Comandos

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/chatquiz reload` | `chatquiz.reload` | Recarrega a configuração do plugin |

## 📁 Estrutura do Projeto

```
src/main/java/com/hytale/server/plugin/chatquiz/
├── ChatQuizPlugin.java          # Classe principal
├── config/
│   └── ChatQuizConfig.java      # Configuração com CODEC
├── manager/
│   └── QuizManager.java         # Lógica do quiz
├── commands/
│   └── ChatQuizCommand.java     # Comando /chatquiz
└── listener/
    └── ChatQuizListener.java    # Listener de chat
```

## 🎮 Como Jogar

1. Quando um quiz iniciar, uma pergunta aparecerá no chat e/ou como banner
2. Digite sua resposta no chat normalmente
3. Todas as mensagens são visíveis para todos os jogadores
4. O primeiro a acertar vence e recebe as recompensas
5. Se ninguém acertar dentro do tempo limite, o quiz termina sem vencedor

## 📝 Notas

- As respostas são normalizadas (ignoram maiúsculas/minúsculas e acentos)
- Todas as tentativas ficam visíveis no chat global
- Não há limite de tentativas
- Apenas o primeiro acerto vence

## 📜 Licença

Este plugin é um fork do template de plugin Hytale. Consulte a licença original para mais detalhes.
