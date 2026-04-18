package com.example.microgames_api.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica del juego Yang (Reversi / Othello).
 * Tablero: 8x8
 * 0 = vacío, 1 = jugador 1 (negro), 2 = jugador 2 (blanco)
 */
@Service
public class YangService {

    private static final int SIZE = 8;

    private static final int[][] DIRECTIONS = {
        {-1,-1},{-1,0},{-1,1},
        { 0,-1},        { 0,1},
        { 1,-1},{ 1,0},{ 1,1}
    };

    /** Tablero inicial de Reversi */
    public int[][] initialBoard() {
        int[][] board = new int[SIZE][SIZE];
        board[3][3] = 2;
        board[3][4] = 1;
        board[4][3] = 1;
        board[4][4] = 2;
        return board;
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

    /** Comprueba si una casilla es un movimiento válido para el jugador */
    public boolean isValidMove(int[][] board, int row, int col, int player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return false;
        if (board[row][col] != 0) return false;
        int opponent = (player == 1) ? 2 : 1;
        for (int[] dir : DIRECTIONS) {
            int r = row + dir[0];
            int c = col + dir[1];
            int count = 0;
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == opponent) {
                r += dir[0];
                c += dir[1];
                count++;
            }
            if (count > 0 && r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aplica el movimiento y voltea las fichas capturadas.
     * @return true si el movimiento era válido
     */
    public boolean applyMove(int[][] board, int row, int col, int player) {
        if (!isValidMove(board, row, col, player)) return false;
        int opponent = (player == 1) ? 2 : 1;
        board[row][col] = player;
        for (int[] dir : DIRECTIONS) {
            int r = row + dir[0];
            int c = col + dir[1];
            List<int[]> toFlip = new ArrayList<>();
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == opponent) {
                toFlip.add(new int[]{r, c});
                r += dir[0];
                c += dir[1];
            }
            if (!toFlip.isEmpty() && r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == player) {
                for (int[] pos : toFlip) {
                    board[pos[0]][pos[1]] = player;
                }
            }
        }
        return true;
    }

    /** Comprueba si el jugador tiene al menos un movimiento válido */
    public boolean hasValidMoves(int[][] board, int player) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isValidMove(board, r, c, player)) return true;
            }
        }
        return false;
    }

    /**
     * Determina quién gana contando fichas.
     * @return 1, 2 o 0 (empate)
     */
    public int getWinner(int[][] board) {
        int p1 = 0, p2 = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 1) p1++;
                else if (cell == 2) p2++;
            }
        }
        if (p1 > p2) return 1;
        if (p2 > p1) return 2;
        return 0;
    }
}
