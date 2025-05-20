import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class IO {
    public static Board readInputTXT() {
        String[][] grid = null;
        HashMap<String, Piece> pieces = new HashMap<>();
        Position exit = new Position(-1, -1);
        int rows = 0, cols = 0;

        try {
            File f = new File("./data/tc2-22.txt");
            Scanner s = new Scanner(f);

            rows = s.nextInt();
            cols = s.nextInt();
            int nPieces = s.nextInt();
            s.nextLine(); 

            ArrayList<String> lines = new ArrayList<>();
            for (int i = 0; i <= rows && s.hasNextLine(); i++) {
                lines.add(s.nextLine());
            }

            int lowerBound = rows - 1, upperBound = 0, leftBound = 0, rightBound = cols - 1;

            exit = detectExit(lines, rows, cols);

            if (exit.col == -1) { // exit di kiri
                leftBound = 1;
                rightBound = cols;
            } else if (exit.col == cols) { // exit di kanan
                leftBound = 0;
                rightBound = cols - 1;
            } else if (exit.row == -1) { // exit di atas
                upperBound = 1;
                lowerBound = rows;
            } else  { // exit di bawah if (exit.row == rows)
                upperBound = 0;
                lowerBound = rows - 1;
            }

        

            grid = new String[rows][cols];
            String[][] charMatrix = new String[rows][cols];
            System.err.println("Exit row: " + exit.row + " col: " + exit.col);
            System.err.println("Left: " + leftBound + " Right: " + rightBound);
            System.err.println("Lower: " + lowerBound + " Upper: " + upperBound);

            int row = 0;
            for (int i = upperBound; i <= lowerBound; i++) {
                if (i >= lines.size()) break;
                String rawLine = lines.get(i);
                int col = 0;
                for (int j = leftBound; j <= rightBound; j++) {
                    char ch = rawLine.charAt(j);
                    if (ch == 'K') continue;
                    String sCh = String.valueOf(ch);
                    grid[row][col] = sCh;
                    charMatrix[row][col] = sCh;
                    col++;
                }

                while (col < cols) {
                    grid[row][col] = ".";
                    charMatrix[row][col] = ".";
                    col++;
                }
                row++;
            }

            // detect pieces
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    String ch = charMatrix[i][j];
                    if (ch == null || ch.equals(".") || pieces.containsKey(ch)) continue;

                    int size = 1;
                    boolean isVertical = false;

                    if (j + 1 < cols && ch.equals(charMatrix[i][j + 1])) {
                        isVertical = false;
                        int tempJ = j + 1;
                        while (tempJ < cols && ch.equals(charMatrix[i][tempJ])) {
                            size++;
                            tempJ++;
                        }
                    } else if (i + 1 < rows && ch.equals(charMatrix[i + 1][j])) {
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

        System.out.println("Exit: " + exit.row + " " + exit.col);

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }

        return new Board(rows, cols, exit, grid, pieces, null);
    }

    private static Position detectExit(ArrayList<String> lines, int rows, int cols) {
        for (int i = 0; i < lines.size(); i++) {
            String rawLine = lines.get(i);
            int rawLen = rawLine.length();
            int startIdx = Math.max(0, rawLen - cols);
            int kIndex = rawLine.indexOf('K');

            if (kIndex != -1) {
                // K di kiri
                if (kIndex < startIdx && i < rows) {
                    return new Position(i, -1);
                }
                // K di kanan
                else if (kIndex >= cols && i < rows) {
                    return new Position(i, cols);
                }
                // K di atas
                else if (i == 0 && kIndex >= startIdx && kIndex < startIdx + cols) {
                    return new Position(-1, kIndex - startIdx);
                }
                // K di bawah
                else if (i == rows && kIndex >= startIdx && kIndex < startIdx + cols) {
                    return new Position(rows, kIndex - startIdx);
                }
            }
        }
        return new Position(-1, -1);
    }
}
