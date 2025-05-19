import java.util.*;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime(); 

        Board rootBoard = IO.readInputTXT();
        TreeNode root = new TreeNode(rootBoard, null, 0, 0);

        PriorityQueue<TreeNode> q = new PriorityQueue<>();
        Set<Board> visited = new HashSet<>();

        q.add(root);

        int cnt = 0; 
        while (!q.isEmpty()) {
            TreeNode processedNode = q.poll();
            Board processedBoard = processedNode.getBoard();

            if (visited.contains(processedBoard)) continue;
            visited.add(processedBoard);

            if (processedBoard.isGoal()) {
                printSolution(processedNode);

                long endTime = System.nanoTime(); 
                double durationMs = (endTime - startTime) / 1_000_000.0;

                System.out.println("Jumlah node yang diproses: " + cnt);
                System.out.printf("Waktu eksekusi: %.4f ms%n", durationMs);

                return;
            }

            List<Board> successors = processedBoard.generateSuccessors();
            for (Board b : successors) {
                if (!visited.contains(b)) {
                    TreeNode newNode = new TreeNode(b, processedNode, processedNode.getCost() + 1, 0);
                    q.add(newNode);
                }
            }

            cnt++;
        }

        
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("Tujuan tidak ditemukan.");
        System.out.println("Jumlah node yang diproses: " + cnt);
        System.out.printf("Waktu eksekusi: %.4f ms%n", durationMs);
    }

    private static void printSolution(TreeNode node) {
        Stack<TreeNode> path = new Stack<>();
        while (node != null) {
            path.push(node);
            node = node.getParent();
        }

        TreeNode test = path.pop();  // skip root
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
}
