package com.example.microgames_api.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Lógica del juego Conecta 4.
 * Tablero: 6 filas x 7 columnas
 * 0 = vacío, 1 = jugador 1, 2 = jugador 2
 */
@Service
public class Connect4Service {

    private static final int ROWS = 6;
    private static final int COLS = 7;

    /** Genera un tablero vacío inicial */
    public int[][] initialBoard() {
        return new int[ROWS][COLS];
    }

    /** Serializa el tablero a JSON String */
    public String serialize(int[][] board) {
        StringBuilder sb = new StringBuilder("[");
        for (int r = 0; r < board.length; r++) {
            sb.append("[");
            for (int c = 0; c < board[r].length; c++) {
                sb.append(board[r][c]);
                if (c < board[r].length - 1) sb.append(",");
            }
            sb.append("]");
            if (r < board.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Deserializa JSON String a tablero int[][] */
    public int[][] deserialize(String json) {
        String trimmed = json.trim().replaceAll("\\s", "");
        // Eliminar corchetes exteriores [[...]]
        String inner = trimmed.substring(1, trimmed.length() - 1);
        String[] rows = inner.split("\\],\\[");
        int[][] board = new int[rows.length][];
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r].replace("[", "").replace("]", "");
            String[] cells = row.split(",");
            board[r] = new int[cells.length];
            for (int c = 0; c < cells.length; c++) {
                board[r][c] = Integer.parseInt(cells[c].trim());
            }
        }
        return board;
    }

    /**
     * Aplica un movimiento (columna) al tablero.
     * @return true si el movimiento es válido, false si la columna está llena
     */
    public boolean applyMove(int[][] board, int col, int player) {
        if (col < 0 || col >= COLS) return false;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = player;
                return true;
            }
        }
        return false; // Columna llena
    }

    /** Comprueba si el jugador dado ha ganado */
    public boolean checkWin(int[][] board, int player) {
        // Horizontal
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player && board[r][c+1] == player
                        && board[r][c+2] == player && board[r][c+3] == player) return true;
            }
        }
        // Vertical
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == player && board[r+1][c] == player
                        && board[r+2][c] == player && board[r+3][c] == player) return true;
            }
        }
        // Diagonal ↘
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player && board[r+1][c+1] == player
                        && board[r+2][c+2] == player && board[r+3][c+3] == player) return true;
            }
        }
        // Diagonal ↙
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 3; c < COLS; c++) {
                if (board[r][c] == player && board[r+1][c-1] == player
                        && board[r+2][c-2] == player && board[r+3][c-3] == player) return true;
            }
        }
        return false;
    }

    /** Comprueba si el tablero está lleno (empate) */
    public boolean isBoardFull(int[][] board) {
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] == 0) return false;
        }
        return true;
    }
}
