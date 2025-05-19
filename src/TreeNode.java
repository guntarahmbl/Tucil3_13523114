
public class TreeNode implements Comparable<TreeNode> {
    private Board board;
    private TreeNode parent;
    private int cost;
    private int heuristic;
    

    public TreeNode(Board board, TreeNode parent, int cost, int heuristic){
        this.board = board;
        this.parent = parent;
        this.cost = cost;
        this.heuristic = heuristic;
    }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public TreeNode getParent() { return parent; }
    public void setParent(TreeNode parent) { this.parent = parent; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getHeuristic() { return heuristic; }
    public void setHeuristic(int heuristic) { this.heuristic = heuristic; }


    @Override
    public int compareTo(TreeNode other) {
        return Integer.compare(this.cost, other.cost); // UCS: g(n)
    }


}