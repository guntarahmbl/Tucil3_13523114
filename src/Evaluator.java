import java.util.Comparator;

public class Evaluator {
    public static final Comparator<TreeNode> UCS = Comparator.comparingInt(n -> n.getCost());

    public static final Comparator<TreeNode> GBFS = Comparator.comparingInt(n -> n.getHeuristic());

    public static final Comparator<TreeNode> AStar = Comparator.comparingInt(n -> n.getCost() + n.getHeuristic());
}
