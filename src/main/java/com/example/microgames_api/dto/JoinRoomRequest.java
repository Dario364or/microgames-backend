package com.example.microgames_api.dto;

import lombok.Data;

/**
 * Petición REST para unirse a una sala existente mediante código.
 */
@Data
public class JoinRoomRequest {
    private String roomCode;
    private String playerId;
    private String playerName;
}
