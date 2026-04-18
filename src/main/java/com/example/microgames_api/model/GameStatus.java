package com.example.microgames_api.model;

/**
 * Estado del ciclo de vida de una sala de juego.
 */
public enum GameStatus {
    WAITING,      // Esperando al segundo jugador
    PLACEMENT,    // Fase de colocación de barcos (solo Battleship)
    IN_PROGRESS,  // Partida en curso
    FINISHED      // Partida terminada
}
