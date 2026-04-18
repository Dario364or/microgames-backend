package com.example.microgames_api.dto;

import lombok.Data;

/**
 * Respuesta REST al crear o unirse a una sala.
 */
@Data
public class RoomResponse {
    private String roomCode;
    private String gameType;
    private String status;
    private String player1Name;
    private String player2Name;
    private int playerNumber; // 1 o 2, indica qué jugador eres tú

    public static RoomResponse of(
            com.example.microgames_api.model.GameRoom room,
            int playerNumber
    ) {
        RoomResponse r = new RoomResponse();
        r.setRoomCode(room.getRoomCode());
        r.setGameType(room.getGameType().name());
        r.setStatus(room.getStatus().name());
        r.setPlayer1Name(room.getPlayer1Name());
        r.setPlayer2Name(room.getPlayer2Name());
        r.setPlayerNumber(playerNumber);
        return r;
    }
}
