import java.util.*;

public  class Board {
    private int rows;
    private int cols;
    private Position exit;
    private String[][] grid;
    private HashMap<String, Piece> pieces;
    private Movement movement;

    public Board(int rows, int cols, Position exit, String[][] grid, HashMap<String, Piece> pieces, Movement movement){
        this.rows = rows;
        this.cols = cols;
        this.exit = exit;
        this.grid = grid;
        this.pieces = pieces;
        this.movement = movement;
    }

    public Position getExit() { return exit; }
    public String[][] getGrid() { return grid; }
    public Movement getMovement() { return movement; }

    public void setMovement(String pieceValue, int dir){
        this.movement = new Movement(pieceValue, dir);
    }

    public boolean inBounds(Position check){
        return (check.col >= 0) && (check.col < cols) && (check.row >= 0) &&(check.row < rows);
    }

    public boolean isEmpty(Position check){
        if (!inBounds(check)) {return false;}
        return grid[check.row][check.col].equals(".");
    }

    public boolean isGoal() {
        Piece primary = pieces.get("P");
        if (primary == null) return false;
    
        int r = primary.getRowPos();
        int c = primary.getColPos();
        
        

        for (int i = 0; i < primary.getSize(); i++) {
            if (!primary.getOrientation()) { // horizontal
                // System.out.println("Primary pos: " + r + " " + c);
                if (exit.row == r && exit.col == c+i) return true;
            } else { // vertical
                if (exit.row == r+i && exit.col == c) return true;
            }
        }
        return false;
    }

    public List<Board> generateSuccessors() {
        List<Board> successors = new ArrayList<>();
    
        for (Piece piece : pieces.values()) {
            
            for (int dir = 0; dir < 4; dir++) {
                List<Piece> movedVariants = piece.getAllPossibleMoves(dir, this);
                for (Piece moved : movedVariants) {
                    Board newBoard = this.copy();
                    newBoard.setMovement(moved.getValue(), dir);
                    newBoard.applyMove(piece, moved);
                    successors.add(newBoard);
                }
            }
        }
    
        return successors;
    }
    
    private void applyMove(Piece oldPiece, Piece newPiece) {
        // delete old
        List<Position> oldPos = getOccupiedPositions(oldPiece);
        for (Position pos : oldPos) grid[pos.row][pos.col] = ".";
    
        // add new
        List<Position> newPos = getOccupiedPositions(newPiece);
        for (Position pos : newPos) grid[pos.row][pos.col] = newPiece.getValue();
    
        // update in map
        pieces.put(String.valueOf(newPiece.getValue()), newPiece);
    }

    private List<Position> getOccupiedPositions(Piece piece) {
        List<Position> positions = new ArrayList<>();
        int r = piece.getRowPos();
        int c = piece.getColPos();
    
        for (int i = 0; i < piece.getSize(); i++) {
            if (piece.getOrientation()) {
                positions.add(new Position(r + i, c));
            } else {
                positions.add(new Position(r, c + i));
            }
        }
    
        return positions;
    }
    
    
    public Board copy() {
        Board b = new Board(this.rows, this.cols, new Position(this.exit.row, this.exit.col), new String[rows][cols], new HashMap<>(), this.movement);

        for (Piece p : this.pieces.values()) {
            Piece clone = new Piece(p.getValue(), p.getRowPos(), p.getColPos(), p.getSize(), p.getOrientation());
            b.pieces.put(String.valueOf(p.getValue()), clone);
            for (Position pos : getOccupiedPositions(clone)) {
                b.grid[pos.row][pos.col] = clone.getValue();
            }
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (b.grid[i][j] == null){
                    b.grid[i][j] = ".";
                }
            }
        }
    
        return b;
    }

    public void printColored() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j].equals(".")) {
                    if (exit.equals(new Position(i, j))) {
                        System.out.print("\u001B[32mK\u001B[0m");
                    } else {
                        System.out.print(".");
                    }
                } else {
                    String symbol = grid[i][j];
                    if ("P".equals(symbol)) {
                        System.out.print("\u001B[31mP\u001B[0m");
                    } else {
                        System.out.print("\u001B[34m" + symbol + "\u001B[0m");
                    }
                }
            }
            System.out.println();
        }
    }

    public String[][] getGridRepresentation() {
        String[][] gridRep = new String[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                gridRep[y][x] = !".".equals(grid[y][x]) ? grid[y][x] : ".";
            }
        }
        return gridRep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == "." || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Arrays.deepEquals(this.getGridRepresentation(), board.getGridRepresentation());
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.getGridRepresentation());
    }
    
                
    

}