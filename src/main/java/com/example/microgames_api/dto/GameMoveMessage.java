package com.example.microgames_api.dto;

import lombok.Data;

/**
 * Mensaje WebSocket enviado por el cliente para realizar un movimiento.
 *
 * Connect4:   col = columna (0-6),  row = -1 (ignorado)
 * Yang:       col = columna (0-7),  row = fila (0-7)
 * Battleship: col = columna (0-9),  row = fila (0-9)  [ataque]
 *             para la fase de colocación se usa shipData (JSON serializado)
 */
@Data
public class GameMoveMessage {
    private String playerId;
    private int row;
    private int col;

    /** Solo para Battleship fase colocación: JSON con posiciones de barcos */
    private String shipData;
}
