import java.util.*;

public class Algorithm {
    public void UCS() {
        solve(Evaluator.UCS);
    }

    public void GreedyBestFirstSearch() {
        solve(Evaluator.GBFS);
    }

    public void AStar() {
        solve(Evaluator.AStar);
    }

    private void solve(Comparator<TreeNode> comparator) {
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
                    TreeNode child = new TreeNode(successor, current, calculateCost(current), calculateHeuristic(successor));
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

    public int calculateHeuristic(Board b) {
        Piece primary = b.getPieces().get("P");
        int row = primary.getRowPos();
        int startCol = primary.getColPos();
        int endCol = startCol + primary.getSize() - 1;
        int blockingCount = 0;
    
        int exitCol = b.getExit().col;
    
        // right exit
        if (exitCol == b.getCols()) {
            for (int col = endCol + 1; col < exitCol; col++) {
                String cell = b.getGrid()[row][col];
                if (!cell.equals(".") && !cell.equals("P")) {
                    Piece blocker = b.getPieces().get(cell);
                    if (blocker.getOrientation()) {
                        blockingCount++;
                    }
                }
            }
        } else if (exitCol == -1) { // left exit
            for (int col = startCol - 1; col > exitCol; col--) {
                String cell = b.getGrid()[row][col];
                if (!cell.equals(".") && !cell.equals("P")) {
                    Piece blocker = b.getPieces().get(cell);
                    if (blocker.getOrientation()) {
                        blockingCount++;
                    }
                }
            }
        }
    
        return blockingCount;
    }

    public int calculateCost(TreeNode node) {
        return node.getCost() + 1;
    }

    public TreeNode solveAndReturn(TreeNode root, Comparator<TreeNode> comparator) {
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
                    TreeNode child = new TreeNode(successor, current, calculateCost(current), calculateHeuristic(successor));
                    q.add(child);
                }
            }
        }
    
        return null;
    }
    
    
}
