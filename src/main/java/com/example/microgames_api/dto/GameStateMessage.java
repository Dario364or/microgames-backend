package com.example.microgames_api.dto;

import com.example.microgames_api.model.GameStatus;
import com.example.microgames_api.model.GameType;
import lombok.Data;

/**
 * Mensaje broadcast enviado a todos los clientes de una sala
 * después de cada movimiento válido.
 *
 * El cliente React Native recibe este objeto y actualiza su UI.
 */
@Data
public class GameStateMessage {

    private String roomCode;
    private GameType gameType;
    private GameStatus status;

    /** Estado del tablero serializado (int[][] → JSON string) */
    private String boardState;

    /** Turno actual: 1 = player1, 2 = player2 */
    private int currentTurn;

    /** ID del jugador que acaba de moverse */
    private String lastMovedPlayerId;

    /** ID del ganador, null si la partida sigue */
    private String winnerId;

    /** Nombre del ganador, null si la partida sigue */
    private String winnerName;

    /** Mensaje de evento (ej: "INVALID_MOVE", "DRAW", "PLAYER_JOINED") */
    private String event;

    /** Número de movimiento */
    private int moveCount;

    /* ---- Solo para Battleship ---- */

    /** Ataques realizados por player1 (reutiliza boardState en el Controller por compatibilidad) */
    private String player1Attacks;

    /** Ataques realizados por player2 */
    private String player2Attacks;

    /** Si player1 ya colocó sus barcos */
    private boolean player1Ready;

    /** Si player2 ya colocó sus barcos */
    private boolean player2Ready;
}
