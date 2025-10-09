#package pt.isel.reversi

## Descrição
Este módulo contém toda a implementação do jogo Reversi, organizado de forma modular e limpa. Inclui lógica de jogo, interface de linha de comando e integração com bibliotecas externas.

## Pacotes Principais

- **pt.isel.reversi.core**  
  Lógica principal do jogo, regras e manipulação do tabuleiro.

- **pt.isel.reversi.cli**  
  Interface de linha de comando para interação com o utilizador.

- **pt.isel.reversi.utils**  
  Utilitários e helpers usados em todo o projeto.

## Como executar

Para executar o projeto via linha de comando:
```sh
java -jar reversi-fat.jar
```

## Documentação

A documentação gerada por Dokka encontra-se no diretório `build/dokka/html` ou pode ser distribuída via o artefacto `javadoc.jar`.

## Autores

Grupo 1 - TDS 2025
# Commands
Groups discrete user operations (start, join, move, pass, help, quit) under cohesive command objects. Emphasizes clear feedback, argument validation, and future extensibility (adding commands without modifying existing logic). Avoids embedding persistence or rendering concerns directly.

#package pt.isel.reversi
# Core Entry
Provides the application bootstrap: wiring dependencies, selecting configured implementations (e.g., local data access), and delegating control to the CLI loop. Serves as the integration point for embedding this module into alternate front‑ends (GUI, web service) or automated test harnesses.

---

## Design Notes
- Immutability: Core structures (Board, Piece) return new instances, simplifying reasoning and potential concurrency.
- Small Surface: Interfaces (GameImpl, GameLogicImpl, GameDataAccessImpl) concentrate on intent, enabling alternative implementations (AI strategies, remote persistence) without refactoring callers.
- Explicit Outcomes: Data access uses structured result codes rather than exceptions for expected domain states (missing data, invalid format), aiding CLI feedback & tooling.
- Progressive Enhancement: The current feature set is a foundation; logic expansion (legal move generation, scoring, endgame detection) is intentionally modular.
