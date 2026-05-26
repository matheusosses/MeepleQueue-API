CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_jogador VARCHAR(255) NOT NULL,
    nome_jogo VARCHAR(255) NOT NULL,
    data_partida DATE NOT NULL,
    quantidade_jogadores INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);
