package com.example.microgames_api.service;

import com.example.microgames_api.dto.CreateRoomRequest;
import com.example.microgames_api.dto.JoinRoomRequest;
import com.example.microgames_api.model.GameRoom;
import com.example.microgames_api.model.GameStatus;
import com.example.microgames_api.model.GameType;
import com.example.microgames_api.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRoomRepository roomRepository;
    private final Connect4Service connect4Service;
    private final YangService yangService;
    private final BattleshipService battleshipService;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Crea una nueva sala de juego.
     * Inicializa el tablero según el tipo de juego.
     */
    public GameRoom createRoom(CreateRoomRequest request) {
        GameRoom room = new GameRoom();
        room.setRoomCode(generateUniqueCode());
        room.setGameType(request.getGameType());
        room.setStatus(GameStatus.WAITING);
        room.setPlayer1Id(request.getPlayerId());
        room.setPlayer1Name(request.getPlayerName());

        // Inicializar tablero según el tipo de juego
        switch (request.getGameType()) {
            case CONNECT4 -> room.setBoardState(
                    connect4Service.serialize(connect4Service.initialBoard()));
            case YANG -> room.setBoardState(
                    yangService.serialize(yangService.initialBoard()));
            case BATTLESHIP -> {
                // Battleship: tableros vacíos, la colocación se hace en PLACEMENT
                room.setBoardState(battleshipService.serialize(battleshipService.emptyBoard()));
                room.setPlayer1PrivateBoard(battleshipService.serialize(battleshipService.emptyBoard()));
                room.setPlayer2PrivateBoard(battleshipService.serialize(battleshipService.emptyBoard()));
                room.setPlayer2Attacks(battleshipService.serialize(battleshipService.emptyBoard()));
            }
        }

        return roomRepository.save(room);
    }

    /**
     * Une al segundo jugador a una sala existente.
     * Cambia el estado a IN_PROGRESS (o PLACEMENT para Battleship).
     */
    public Optional<GameRoom> joinRoom(JoinRoomRequest request) {
        return roomRepository.findByRoomCode(request.getRoomCode().toUpperCase())
                .filter(room -> room.getStatus() == GameStatus.WAITING)
                .filter(room -> !room.getPlayer1Id().equals(request.getPlayerId()))
                .map(room -> {
                    room.setPlayer2Id(request.getPlayerId());
                    room.setPlayer2Name(request.getPlayerName());

                    if (room.getGameType() == GameType.BATTLESHIP) {
                        room.setStatus(GameStatus.PLACEMENT);
                    } else {
                        room.setStatus(GameStatus.IN_PROGRESS);
                    }

                    return roomRepository.save(room);
                });
    }

    /** Obtiene una sala por su código */
    public Optional<GameRoom> findByCode(String code) {
        return roomRepository.findByRoomCode(code.toUpperCase());
    }

    /** Guarda cambios en una sala */
    public GameRoom save(GameRoom room) {
        return roomRepository.save(room);
    }

    /** Genera un código de sala único de 6 caracteres */
    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }
}
