package com.example.microgames_api.dto;

import com.example.microgames_api.model.GameType;
import lombok.Data;

/**
 * Petición REST para crear una nueva sala de juego.
 */
@Data
public class CreateRoomRequest {
    private GameType gameType;
    private String playerId;
    private String playerName;
}
