package com.example.microgames_api.controller;

import com.example.microgames_api.dto.CreateRoomRequest;
import com.example.microgames_api.dto.JoinRoomRequest;
import com.example.microgames_api.dto.RoomResponse;
import com.example.microgames_api.model.GameRoom;
import com.example.microgames_api.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    /**
     * POST /api/rooms
     * Crea una nueva sala de juego.
     * Body: { "gameType": "CONNECT4", "playerId": "user123", "playerName": "Darío" }
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        GameRoom room = gameRoomService.createRoom(request);
        return ResponseEntity.ok(RoomResponse.of(room, 1));
    }

    /**
     * POST /api/rooms/join
     * Únete a una sala existente con su código.
     * Body: { "roomCode": "ABC123", "playerId": "user456", "playerName": "Ana" }
     */
    @PostMapping("/join")
    public ResponseEntity<RoomResponse> joinRoom(@RequestBody JoinRoomRequest request) {
        return gameRoomService.joinRoom(request)
                .map(room -> ResponseEntity.ok(RoomResponse.of(room, 2)))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/rooms/{code}
     * Obtiene el estado actual de una sala (útil para reconexión).
     */
    @GetMapping("/{code}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String code,
                                                 @RequestParam String playerId) {
        return gameRoomService.findByCode(code)
                .map(room -> {
                    int playerNum = room.getPlayer1Id().equals(playerId) ? 1 : 2;
                    return ResponseEntity.ok(RoomResponse.of(room, playerNum));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
