import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class IO {
    public static Board readInputTXT() {
        String[][] grid = null;
        HashMap<String, Piece> pieces = new HashMap<>();
        Position exit = new Position(-1, -1);
        int rows = 0, cols = 0;
    
        try {
            File f = new File("./data/tc5-44.txt");
            Scanner s = new Scanner(f);
    
            rows = s.nextInt();
            cols = s.nextInt();
            int nPieces = s.nextInt(); 
            s.nextLine(); // consume newline
    
            grid = new String[rows][cols];
            String[][] charMatrix = new String[rows][cols];
    
            for (int i = 0; i < rows; i++) {
                if (!s.hasNextLine()) break;
                String rawLine = s.nextLine();
    
                int kIndex = rawLine.indexOf('K');
                int rawLen = rawLine.length();
    
                int startIdx = Math.max(0, rawLen - cols);
                String visible = rawLine.substring(startIdx);
    
                //  exit
                if (kIndex != -1 && kIndex < startIdx) {
                    // right
                    exit = new Position(i, 0);
                } else if (kIndex != -1 && kIndex >= cols) {
                    // left
                    exit = new Position(i, cols - 1);
                }
    
                int col = 0;
                for (int j = 0; j < visible.length() && col < cols; j++) {
                    char ch = visible.charAt(j);
                    if (ch == 'K') continue; 
    
                    String sCh = String.valueOf(ch);
                    grid[i][col] = sCh;
                    charMatrix[i][col] = sCh;
                    col++;
                }
    
                
                while (col < cols) {
                    grid[i][col] = ".";
                    charMatrix[i][col] = ".";
                    col++;
                }
            }
    
            // detect pieces
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    String ch = charMatrix[i][j];
                    if (ch == null || ch.equals(".") || pieces.containsKey(ch)) continue;
    
                    int size = 1;
                    boolean isVertical = false;
    
                    // Check right
                    if (j + 1 < cols && ch.equals(charMatrix[i][j + 1])) {
                        isVertical = false;
                        int tempJ = j + 1;
                        while (tempJ < cols && ch.equals(charMatrix[i][tempJ])) {
                            size++;
                            tempJ++;
                        }
                    }
                    // Check down
                    else if (i + 1 < rows && ch.equals(charMatrix[i + 1][j])) {
                        isVertical = true;
                        int tempI = i + 1;
                        while (tempI < rows && ch.equals(charMatrix[tempI][j])) {
                            size++;
                            tempI++;
                        }
                    }
    
                    pieces.put(ch, new Piece(ch, i, j, size, isVertical));
                }
            }
    
            s.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
            e.printStackTrace();
        }
    
        return new Board(rows, cols, exit, grid, pieces, null);
    }
}