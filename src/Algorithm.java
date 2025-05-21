import java.util.*;

public class Algorithm {
    public void UCS() {
        solve(Evaluator.UCS, 0);
    }

    public void GreedyBestFirstSearch(int optionHeuristic) {
        solve(Evaluator.GBFS, optionHeuristic);
    }

    public void AStar(int optionHeuristic) {
        solve(Evaluator.AStar, optionHeuristic);
    }

    private void solve(Comparator<TreeNode> comparator, int optionHeuristic) {
        long startTime = System.nanoTime();

        Board rootBoard = IO.readInputTXT();
        TreeNode root = new TreeNode(rootBoard, null, 0, 0);

        PriorityQueue<TreeNode> q = new PriorityQueue<>(comparator);
        Set<Board> visited = new HashSet<>();
        q.add(root);

        int cnt = 0;

        while (!q.isEmpty()) {
            TreeNode current = q.poll();
            Board board = current.getBoard();

            if (visited.contains(board)) continue;
            visited.add(board);

            if (board.isGoal()) {
                printSolution(current);
                long endTime = System.nanoTime();
                System.out.printf("Jumlah node yang diproses: %d%n", cnt);
                System.out.printf("Waktu eksekusi: %.4f ms%n", (endTime - startTime) / 1_000_000.0);
                return;
            }

            for (Board successor : board.generateSuccessors()) {
                if (!visited.contains(successor)) {
                    TreeNode child = new TreeNode(successor, current, calculateCost(current), calculateHeuristic(successor, optionHeuristic));
                    q.add(child);
                }
            }

            cnt++;
        }

        long endTime = System.nanoTime();
        System.out.println("Tujuan tidak ditemukan.");
        System.out.printf("Jumlah node yang diproses: %d%n", cnt);
        System.out.printf("Waktu eksekusi: %.4f ms%n", (endTime - startTime) / 1_000_000.0);
    }

    private void printSolution(TreeNode node) {
        Stack<TreeNode> path = new Stack<>();
        while (node != null) {
            path.push(node);
            node = node.getParent();
        }

        if (!path.isEmpty()) path.pop(); // skip root

        int step = 1;
        while (!path.isEmpty()) {
            TreeNode current = path.pop();
            System.out.println("Gerakan " + step++ + ": " +
                current.getBoard().getMovement().pieceValue + "-" +
                current.getBoard().getMovement().direction);
            current.getBoard().printColored();
            System.out.println();
        }
    }

    public int calculateCost(TreeNode node) {
        return node.getCost() + 1;
    }
  
    public TreeNode solveAndReturn(TreeNode root, Comparator<TreeNode> comparator, int optionHeuristic) {
        long startTime = System.nanoTime();
        PriorityQueue<TreeNode> q = new PriorityQueue<>(comparator);
        Set<Board> visited = new HashSet<>();
        q.add(root);
    
        while (!q.isEmpty()) {
            TreeNode current = q.poll();
            Board board = current.getBoard();
    
            if (visited.contains(board)) continue;
            visited.add(board);
    
            if (board.isGoal()) return current;
    
            for (Board successor : board.generateSuccessors()) {
                if (!visited.contains(successor)) {
                    TreeNode child = new TreeNode(successor, current, calculateCost(current), calculateHeuristic(successor, optionHeuristic));
                    q.add(child);
                }
            }
        }
    
        return null;
    }
    



    public int calculateManhattanDistance(Board b) {
        Piece primary = b.getPieces().get("P");
        int row = primary.getRowPos();
        int col = primary.getColPos();
        int size = primary.getSize();
        int endRow = row + size - 1;
        int endCol = col + size - 1;
    
        Position exit = b.getExit();
        int totalManhattan = 0;
    
        if (exit.col == b.getCols()) { // Right exit
            totalManhattan += sumBlockerDistances(b, row, endCol + 1, exit.col, true, true);
        } else if (exit.col == -1) { // Left exit
            totalManhattan += sumBlockerDistances(b, row, col - 1, exit.col, false, true);
        } else if (exit.row == b.getRows()) { // Down exit
            totalManhattan += sumBlockerDistances(b, col, endRow + 1, exit.row, true, false);
        } else if (exit.row == -1) { // Up exit
            totalManhattan += sumBlockerDistances(b, col, row - 1, exit.row, false, false);
        }
    
        return totalManhattan;
    }

    private int sumBlockerDistances(Board b, int fixed, int start, int end, boolean increasing, boolean horizontal) {
        int sum = 0;
        int step = increasing ? 1 : -1;
    
        for (int i = start; increasing ? i < end : i > end; i += step) {
            String cell = horizontal ? b.getGrid()[fixed][i] : b.getGrid()[i][fixed];
            if (!cell.equals(".") && !cell.equals("P")) {
                Piece blocker = b.getPieces().get(cell);
                int minMove = calculateMinMoveToClear(b, blocker);
                sum += minMove;
            }
        }
    
        return sum;
    }
    
    private int calculateMinMoveToClear(Board b, Piece piece) {
        int row = piece.getRowPos();
        int col = piece.getColPos();
        int size = piece.getSize();
        boolean isHorizontal = piece.getOrientation();
    
        int minDist = Integer.MAX_VALUE;
    
        //  left/up
        for (int offset = 1; offset <= 5; offset++) {
            int r = isHorizontal ? row : row - offset;
            int c = isHorizontal ? col - offset : col;
            if (isValidMove(b, r, c, size, isHorizontal)) {
                minDist = Math.min(minDist, offset);
                break;
            }
        }
    
        // right/down
        for (int offset = 1; offset <= 5; offset++) {
            int r = isHorizontal ? row : row + offset;
            int c = isHorizontal ? col + offset : col;
            if (isValidMove(b, r, c, size, isHorizontal)) {
                minDist = Math.min(minDist, offset);
                break;
            }
        }
    
        return (minDist == Integer.MAX_VALUE) ? 5 : minDist;
    }
    
    private boolean isValidMove(Board b, int newRow, int newCol, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = horizontal ? newRow : newRow + i;
            int c = horizontal ? newCol + i : newCol;
            Position pos = new Position(r, c);
    
            if (!b.inBounds(pos)) return false;
    
            String cell = b.getGrid()[r][c];
            if (!cell.equals(".")) return false;
        }
        return true;
    }



    public int calculateHeuristic(Board b, int option) {
        return switch (option) {
            case 1 -> calculateBlockingPiece(b);
            case 2 -> calculateManhattanDistance(b);
            default -> -10000;
        };
    }
    

    public int calculateBlockingPiece(Board b) {
        Piece primary = b.getPieces().get("P");
        int row = primary.getRowPos();
        int col = primary.getColPos();
        int size = primary.getSize();
        int endRow = row + size - 1;
        int endCol = col + size - 1;
    
        int blockingCount = 0;
        Position exit = b.getExit();
    
        if (exit.col == b.getCols()) { // Right exit
            blockingCount += countBlockers(
                b, row, endCol + 1, exit.col, true, true
            );
        } else if (exit.col == -1) { // Left exit
            blockingCount += countBlockers(
                b, row, col - 1, exit.col, false, true
            );
        } else if (exit.row == b.getRows()) { // Down exit
            blockingCount += countBlockers(
                b, col, endRow + 1, exit.row, true, false
            );
        } else if (exit.row == -1) { // Up exit
            blockingCount += countBlockers(
                b, col, row - 1, exit.row, false, false
            );
        }
    
        return blockingCount;
    }
    private int countBlockers(Board b, int fixed, int start, int end, boolean increasing, boolean horizontal) {
        int count = 0;
        int step = increasing ? 1 : -1;
    
        for (int i = start; increasing ? i < end : i > end; i += step) {
            String cell = horizontal ? b.getGrid()[fixed][i] : b.getGrid()[i][fixed];
            if (!cell.equals(".") && !cell.equals("P")) {
                Piece blocker = b.getPieces().get(cell);
                if (blocker.getOrientation() == horizontal) {
                    count++;
                }
            }
        }
    
        return count;
    }
        
    
}
