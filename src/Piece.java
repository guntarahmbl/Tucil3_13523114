import java.util.*;

public class Piece {
    private String value;
    private int rowPos;
    private int colPos;
    private int size;
    private boolean orientation; //0 if horizontal and 1 if vertical

    public Piece(String value, int rowPos, int colPos, int size, boolean orientation){
        this.value = value;
        this.rowPos = rowPos;
        this.colPos = colPos;
        this.size = size;
        this.orientation = orientation;
    }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public int getRowPos() { return rowPos; }
    public void setRowPos(int rowPos) { this.rowPos = rowPos; }

    public int getColPos() { return colPos; }
    public void setColPos(int colPos) { this.colPos = colPos; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean getOrientation() { return orientation; }
    public void setOrientation(boolean orientation) { this.orientation = orientation; }


    public boolean canMove(int dir, Board board) {
        if (orientation == false) {
            if (dir == 0) { // left
                Position check = new Position(rowPos, colPos - 1);
                return board.inBounds(check) && board.isEmpty(check);
            } else if (dir == 2) {// right
                Position check = new Position(rowPos, colPos + size);
                return board.inBounds(check) && board.isEmpty(check);
            }
        } else { // VERTICAL
            if (dir == 1) { // up
                Position check = new Position(rowPos - 1, colPos);
                return board.inBounds(check) && board.isEmpty(check);
            } else if (dir == 3) { // down
                Position check = new Position(rowPos + size, colPos);
                return board.inBounds(check) && board.isEmpty(check);
            }
        }
        return false;
    }

    public Piece move(int dir) {
        int newRow = rowPos;
        int newCol = colPos;
        switch (dir) {
            case 0 -> newCol--; // left
            case 2 -> newCol++; // right
            case 1 -> newRow--; // up
            case 3 -> newRow++; // down
        }
    
        return new Piece(value, newRow, newCol, size, orientation);
    }

    public List<Piece> getAllPossibleMoves(int dir, Board board) {
        List<Piece> moves = new ArrayList<>();
    
        int deltaRow = 0;
        int deltaCol = 0;
    
        switch (dir) {
            case 0 -> deltaCol = -1; // left
            case 2 -> deltaCol = 1;  // right
            case 1 -> deltaRow = -1; // up
            case 3 -> deltaRow = 1;  // down
            default -> {
                return moves; // arah tidak valid
            }
        }
    
        // Cek validitas arah terhadap orientasi
        if ((!orientation && (dir == 1 || dir == 3)) || // horizontal tapi arah vertical
            (orientation && (dir == 0 || dir == 2))) { // vertical tapi arah horizontal
            return moves;
        }
    
        int steps = 1;
        while (true) {
            Position check;
            if (!orientation) { // horizontal
                check = (dir == 0)
                    ? new Position(rowPos, colPos - steps)
                    : new Position(rowPos, colPos + size - 1 + steps);
            } else { // vertical
                check = (dir == 1)
                    ? new Position(rowPos - steps, colPos)
                    : new Position(rowPos + size - 1 + steps, colPos);
            }
    
            if (!board.inBounds(check) || !board.isEmpty(check)) {
                steps--;
                break;
            }
    
            steps++;
        }
    
        if (steps > 0) {
            int newRow = rowPos + deltaRow * steps;
            int newCol = colPos + deltaCol * steps;
    
            if (board.inBounds(new Position(newRow, newCol))) {
                moves.add(new Piece(value, newRow, newCol, size, orientation));
            }
        }
    
        return moves;
    }
    
    
    
    
}