package com.example.microgames_api.service;

import org.springframework.stereotype.Service;

/**
 * Lógica del juego Hundir la Flota (Battleship).
 *
 * Tablero: 10x10
 * 0 = agua, 1 = barco (privado), 2 = impacto (hit), 3 = fallo (miss)
 *
 * Flota estándar: 1x5 + 1x4 + 1x3 + 1x3 + 1x2 = 17 casillas
 */
@Service
public class BattleshipService {

    public static final int SIZE = 10;
    public static final int SHIP_CELLS = 17;

    public static final int WATER = 0;
    public static final int SHIP  = 1;
    public static final int HIT   = 2;
    public static final int MISS  = 3;

    /** Tablero vacío inicial */
    public int[][] emptyBoard() {
        return new int[SIZE][SIZE];
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

    /**
     * Valida que un tablero de colocación sea correcto.
     * Comprueba que tenga exactamente SHIP_CELLS casillas marcadas.
     */
    public boolean isValidPlacement(int[][] board) {
        if (board.length != SIZE) return false;
        int shipCount = 0;
        for (int r = 0; r < SIZE; r++) {
            if (board[r].length != SIZE) return false;
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != WATER && board[r][c] != SHIP) return false;
                if (board[r][c] == SHIP) shipCount++;
            }
        }
        return shipCount == SHIP_CELLS;
    }

    /**
     * Procesa un ataque.
     * @param privateBoard tablero privado del defensor (con barcos)
     * @param attackBoard  tablero de ataques del atacante (H y M)
     * @return "HIT", "MISS" o "ALREADY_ATTACKED"
     */
    public String applyAttack(int[][] privateBoard, int[][] attackBoard, int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return "INVALID";
        if (attackBoard[row][col] == HIT || attackBoard[row][col] == MISS) return "ALREADY_ATTACKED";

        if (privateBoard[row][col] == SHIP) {
            privateBoard[row][col] = HIT;
            attackBoard[row][col] = HIT;
            return "HIT";
        } else {
            attackBoard[row][col] = MISS;
            return "MISS";
        }
    }

    /** Comprueba si todos los barcos del defensor han sido hundidos */
    public boolean allShipsSunk(int[][] privateBoard) {
        for (int[] row : privateBoard) {
            for (int cell : row) {
                if (cell == SHIP) return false;
            }
        }
        return true;
    }
}
