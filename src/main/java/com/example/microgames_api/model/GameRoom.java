package com.example.microgames_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad principal que representa una sala de juego online.
 * Almacena el estado serializado del juego como JSON en un campo de texto.
 */
@Entity
@Table(name = "game_rooms")
@Data
@NoArgsConstructor
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Código corto de 6 letras que los jugadores comparten para unirse */
    @Column(unique = true, nullable = false, length = 6)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.WAITING;

    /** ID del jugador 1 (host) */
    @Column(nullable = false)
    private String player1Id;

    /** Nombre del jugador 1 */
    @Column(nullable = false)
    private String player1Name;

    /** ID del jugador 2 (se une a la sala) */
    private String player2Id;

    /** Nombre del jugador 2 */
    private String player2Name;

    /**
     * Estado del tablero serializado como JSON.
     * Connect4:   "[[0,0,...],[...]]"  (int[6][7])
     * Yang:       "[[0,0,...],[...]]"  (int[8][8])
     * Battleship: gestionado en BattleshipState (columna separada por privacidad)
     */
    @Column(columnDefinition = "TEXT")
    private String boardState;

    /**
     * Solo para Battleship: tablero PRIVADO del jugador 1 (posición de barcos).
     * El jugador 2 nunca recibe este dato directamente.
     */
    @Column(columnDefinition = "TEXT")
    private String player1PrivateBoard;

    /**
     * Solo para Battleship: tablero PRIVADO del jugador 2.
     */
    @Column(columnDefinition = "TEXT")
    private String player2PrivateBoard;

    /**
     * Solo para Battleship: indica si el jugador 1 ya colocó sus barcos.
     */
    private boolean player1Ready = false;

    /**
     * Solo para Battleship: indica si el jugador 2 ya colocó sus barcos.
     */
    private boolean player2Ready = false;

    /**
     * Solo para Battleship: ataques realizados por el jugador 2.
     */
    @Column(columnDefinition = "TEXT")
    private String player2Attacks;

    /** Turno actual: 1 = player1, 2 = player2 */
    private int currentTurn = 1;

    /** ID del ganador (null si no hay ganador todavía) */
    private String winnerId;

    /** Contador de movimientos realizados */
    private int moveCount = 0;
}
