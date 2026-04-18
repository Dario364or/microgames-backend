package com.example.microgames_api.controller;

import com.example.microgames_api.dto.GameMoveMessage;
import com.example.microgames_api.dto.GameStateMessage;
import com.example.microgames_api.model.GameRoom;
import com.example.microgames_api.model.GameStatus;
import com.example.microgames_api.model.GameType;
import com.example.microgames_api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * Controller WebSocket (STOMP) que maneja todos los movimientos de los juegos.
 *
 * El cliente envía mensajes a:
 *   /app/game/{roomCode}/move       → movimiento de juego
 *   /app/game/{roomCode}/placement  → colocación de barcos (Battleship)
 *
 * El servidor publica el nuevo estado en:
 *   /topic/room/{roomCode}  → todos los jugadores de esa sala
 */
@Controller
@RequiredArgsConstructor
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameRoomService gameRoomService;
    private final Connect4Service connect4Service;
    private final YangService yangService;
    private final BattleshipService battleshipService;

    // =========================================================
    // MOVIMIENTO GENÉRICO (Connect4 y Yang)
    // =========================================================

    @MessageMapping("/game/{roomCode}/move")
    public void handleMove(
            @DestinationVariable String roomCode,
            GameMoveMessage move
    ) {
        Optional<GameRoom> optRoom = gameRoomService.findByCode(roomCode);
        if (optRoom.isEmpty()) return;

        GameRoom room = optRoom.get();

        // Verificar que la partida está en curso
        if (room.getStatus() != GameStatus.IN_PROGRESS) {
            broadcast(roomCode, buildState(room, "GAME_NOT_ACTIVE"));
            return;
        }

        // Verificar que es el turno del jugador que envía el movimiento
        String expectedPlayerId = room.getCurrentTurn() == 1
                ? room.getPlayer1Id()
                : room.getPlayer2Id();

        if (!move.getPlayerId().equals(expectedPlayerId)) {
            broadcast(roomCode, buildState(room, "NOT_YOUR_TURN"));
            return;
        }

        int player = room.getCurrentTurn();
        GameStateMessage state;

        if (room.getGameType() == GameType.CONNECT4) {
            state = handleConnect4Move(room, move, player);
        } else if (room.getGameType() == GameType.YANG) {
            state = handleYangMove(room, move, player);
        } else {
            return; // Battleship usa su propio endpoint
        }

        gameRoomService.save(room);
        broadcast(roomCode, state);
    }

    // =========================================================
    // CONNECT 4
    // =========================================================

    private GameStateMessage handleConnect4Move(GameRoom room, GameMoveMessage move, int player) {
        int[][] board = connect4Service.deserialize(room.getBoardState());
        boolean valid = connect4Service.applyMove(board, move.getCol(), player);

        if (!valid) {
            return buildState(room, "INVALID_MOVE");
        }

        room.setBoardState(connect4Service.serialize(board));
        room.setMoveCount(room.getMoveCount() + 1);

        if (connect4Service.checkWin(board, player)) {
            room.setStatus(GameStatus.FINISHED);
            String winnerId = player == 1 ? room.getPlayer1Id() : room.getPlayer2Id();
            String winnerName = player == 1 ? room.getPlayer1Name() : room.getPlayer2Name();
            room.setWinnerId(winnerId);
            GameStateMessage state = buildState(room, "GAME_OVER");
            state.setWinnerId(winnerId);
            state.setWinnerName(winnerName);
            return state;
        }

        if (connect4Service.isBoardFull(board)) {
            room.setStatus(GameStatus.FINISHED);
            return buildState(room, "DRAW");
        }

        // Cambiar turno
        room.setCurrentTurn(player == 1 ? 2 : 1);
        return buildState(room, "MOVE_OK");
    }

    // =========================================================
    // YANG (Reversi)
    // =========================================================

    private GameStateMessage handleYangMove(GameRoom room, GameMoveMessage move, int player) {
        int[][] board = yangService.deserialize(room.getBoardState());
        boolean valid = yangService.applyMove(board, move.getRow(), move.getCol(), player);

        if (!valid) {
            return buildState(room, "INVALID_MOVE");
        }

        room.setBoardState(yangService.serialize(board));
        room.setMoveCount(room.getMoveCount() + 1);

        int nextPlayer = player == 1 ? 2 : 1;

        // Comprobar si el siguiente jugador puede mover
        if (!yangService.hasValidMoves(board, nextPlayer)) {
            // Si tampoco puede mover el jugador actual, fin de partida
            if (!yangService.hasValidMoves(board, player)) {
                room.setStatus(GameStatus.FINISHED);
                int winnerNum = yangService.getWinner(board);
                GameStateMessage state = buildState(room, "GAME_OVER");
                if (winnerNum != 0) {
                    String winnerId   = winnerNum == 1 ? room.getPlayer1Id()   : room.getPlayer2Id();
                    String winnerName = winnerNum == 1 ? room.getPlayer1Name() : room.getPlayer2Name();
                    state.setWinnerId(winnerId);
                    state.setWinnerName(winnerName);
                    room.setWinnerId(winnerId);
                } else {
                    state.setEvent("DRAW");
                }
                return state;
            }
            // El siguiente no puede mover, pero el actual sí → pasar turno
            // Se mantiene el turno actual y se notifica
            GameStateMessage state = buildState(room, "TURN_SKIPPED");
            state.setCurrentTurn(player); // misma persona sigue
            return state;
        }

        room.setCurrentTurn(nextPlayer);
        return buildState(room, "MOVE_OK");
    }

    // =========================================================
    // BATTLESHIP — Fase colocación de barcos
    // =========================================================

    @MessageMapping("/game/{roomCode}/placement")
    public void handlePlacement(
            @DestinationVariable String roomCode,
            GameMoveMessage move
    ) {
        Optional<GameRoom> optRoom = gameRoomService.findByCode(roomCode);
        if (optRoom.isEmpty()) return;

        GameRoom room = optRoom.get();
        if (room.getStatus() != GameStatus.PLACEMENT) return;

        int[][] shipBoard = battleshipService.deserialize(move.getShipData());
        if (!battleshipService.isValidPlacement(shipBoard)) {
            broadcast(roomCode, buildState(room, "INVALID_PLACEMENT"));
            return;
        }

        boolean isPlayer1 = move.getPlayerId().equals(room.getPlayer1Id());
        if (isPlayer1) {
            room.setPlayer1PrivateBoard(battleshipService.serialize(shipBoard));
            room.setPlayer1Ready(true);
        } else {
            room.setPlayer2PrivateBoard(battleshipService.serialize(shipBoard));
            room.setPlayer2Ready(true);
        }

        // Si los dos están listos → empezar partida
        if (room.isPlayer1Ready() && room.isPlayer2Ready()) {
            room.setStatus(GameStatus.IN_PROGRESS);
            gameRoomService.save(room);
            broadcast(roomCode, buildState(room, "GAME_START"));
        } else {
            gameRoomService.save(room);
            broadcast(roomCode, buildState(room, "PLAYER_READY"));
        }
    }

    // =========================================================
    // BATTLESHIP — Ataque
    // =========================================================

    @MessageMapping("/game/{roomCode}/attack")
    public void handleAttack(
            @DestinationVariable String roomCode,
            GameMoveMessage move
    ) {
        Optional<GameRoom> optRoom = gameRoomService.findByCode(roomCode);
        if (optRoom.isEmpty()) return;

        GameRoom room = optRoom.get();
        if (room.getStatus() != GameStatus.IN_PROGRESS
                || room.getGameType() != GameType.BATTLESHIP) return;

        // Verificar turno
        String expectedPlayerId = room.getCurrentTurn() == 1
                ? room.getPlayer1Id()
                : room.getPlayer2Id();
        if (!move.getPlayerId().equals(expectedPlayerId)) {
            broadcast(roomCode, buildState(room, "NOT_YOUR_TURN"));
            return;
        }

        int player = room.getCurrentTurn();
        boolean isPlayer1Attacking = player == 1;

        // El atacante ataca el tablero privado del defensor
        int[][] defenderPrivate = isPlayer1Attacking
                ? battleshipService.deserialize(room.getPlayer2PrivateBoard())
                : battleshipService.deserialize(room.getPlayer1PrivateBoard());

        // Tablero de ataques del atacante (registro de H y M)
        int[][] attackerBoard = isPlayer1Attacking
                ? battleshipService.deserialize(room.getBoardState())  // reutilizamos boardState para p1Attacks
                : battleshipService.deserialize(room.getPlayer2PrivateBoard()); // temp

        // Para Battleship usamos boardState como ataques de player1 y player2PrivateBoard como ataques de player2
        // Mejor separarlo con player1Attacks / player2Attacks usando los campos player1PrivateBoard y boardState
        // Arquitectura simplificada: player1 ataca → actualiza player2PrivateBoard (oculto) y boardState (visible)
        //                            player2 ataca → actualiza player1PrivateBoard (oculto) y se usa player2Attacks

        String result = processBattleshipAttack(room, move.getRow(), move.getCol(), isPlayer1Attacking);
        room.setMoveCount(room.getMoveCount() + 1);

        GameStateMessage state = buildState(room, result);

        // Detectar victoria
        if ("HIT".equals(result)) {
            int[][] targetPrivate = isPlayer1Attacking
                    ? battleshipService.deserialize(room.getPlayer2PrivateBoard())
                    : battleshipService.deserialize(room.getPlayer1PrivateBoard());

            if (battleshipService.allShipsSunk(targetPrivate)) {
                room.setStatus(GameStatus.FINISHED);
                String winnerId   = isPlayer1Attacking ? room.getPlayer1Id()   : room.getPlayer2Id();
                String winnerName = isPlayer1Attacking ? room.getPlayer1Name() : room.getPlayer2Name();
                room.setWinnerId(winnerId);
                state = buildState(room, "GAME_OVER");
                state.setWinnerId(winnerId);
                state.setWinnerName(winnerName);
                gameRoomService.save(room);
                broadcast(roomCode, state);
                return;
            }
            // Acierto → el mismo jugador repite turno en Battleship estándar
        } else if ("MISS".equals(result)) {
            // Fallo → cambiar turno
            room.setCurrentTurn(player == 1 ? 2 : 1);
        }

        gameRoomService.save(room);
        broadcast(roomCode, state);
    }

    /**
     * Procesa el ataque de Battleship y actualiza los tableros en la sala.
     * Arquitectura de almacenamiento:
     *   boardState          → tablero de ataques visible del player1 (lo que ha acertado/fallado atacando)
     *   player2PrivateBoard → tablero privado del player2 CON los hits marcados
     *   player1PrivateBoard → tablero privado del player1 CON los hits marcados
     *   boardState se reutiliza para player1Attacks; se añade player2Attacks en el DTO
     */
    private String processBattleshipAttack(GameRoom room, int row, int col, boolean isPlayer1Attacking) {
        if (isPlayer1Attacking) {
            // Player1 ataca el tablero privado de player2
            int[][] p2Private = battleshipService.deserialize(room.getPlayer2PrivateBoard());
            int[][] p1Attacks = battleshipService.deserialize(room.getBoardState());
            String result = battleshipService.applyAttack(p2Private, p1Attacks, row, col);
            room.setPlayer2PrivateBoard(battleshipService.serialize(p2Private));
            room.setBoardState(battleshipService.serialize(p1Attacks));
            return result;
        } else {
            // Player2 ataca el tablero privado de player1
            int[][] p1Private = battleshipService.deserialize(room.getPlayer1PrivateBoard());
            int[][] p2Attacks = battleshipService.deserialize(room.getPlayer2Attacks());
            String result = battleshipService.applyAttack(p1Private, p2Attacks, row, col);
            room.setPlayer1PrivateBoard(battleshipService.serialize(p1Private));
            room.setPlayer2Attacks(battleshipService.serialize(p2Attacks));
            return result;
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private void broadcast(String roomCode, GameStateMessage state) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, state);
    }

    private GameStateMessage buildState(GameRoom room, String event) {
        GameStateMessage state = new GameStateMessage();
        state.setRoomCode(room.getRoomCode());
        state.setGameType(room.getGameType());
        state.setStatus(room.getStatus());
        state.setBoardState(room.getBoardState());
        state.setCurrentTurn(room.getCurrentTurn());
        state.setWinnerId(room.getWinnerId());
        state.setEvent(event);
        state.setMoveCount(room.getMoveCount());
        state.setPlayer1Ready(room.isPlayer1Ready());
        state.setPlayer2Ready(room.isPlayer2Ready());
        state.setPlayer1Attacks(room.getBoardState());
        state.setPlayer2Attacks(room.getPlayer2Attacks());
        return state;
    }
}
