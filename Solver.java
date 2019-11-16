import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

public class Solver {

    private MinPQ<SearchNode> pq = new MinPQ<>();
    private MinPQ<SearchNode> pqTwin = new MinPQ<>();
    private Board goal;
    private final boolean isSolvableBool;
    private int movesSolution;

    private Stack<Board> solution = new Stack<>();

    private class SearchNode implements Comparable<SearchNode> {
        private final Board board;
        private final SearchNode prev;
        private final int moves;

        private final int hammingPriority;
        private final int manhattanPriority;

        public SearchNode(Board board, int moves, SearchNode prev) {
            this.board = board;
            this.moves = moves;
            this.prev = prev;

            hammingPriority = board.hamming() + moves;
            manhattanPriority = board.manhattan() + moves;
        }

        public int compareTo(SearchNode s) {
            // which ever has smaller hamming or manhattan priority, is less than
            if (this.manhattanPriority < s.manhattanPriority) {
                return -1;
            }
            else if (this.manhattanPriority > s.manhattanPriority) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException("initial cannot be null");
        }

        // set up goal
        makeGoal(initial.dimension());

        // 1. insert initial board into priority queues
        SearchNode s = new SearchNode(initial, 0, null);
        pq.insert(s);

        // set up twin
        Board twin = initial.twin();
        SearchNode sTwin = new SearchNode(twin, 0, null);
        pqTwin.insert(sTwin);

        // 2. delete from PQ the searchNode with minimum priority as min
        SearchNode min = pq.delMin();

        // 3. repeat until searchNode = goalBoard
        int alternate = 0;
        while (!min.board.equals(goal) && min != null) {
            // StdOut.println("\nAlte : " + alternate);
            // StdOut.println("Goal : " + goal.toString());
            // StdOut.println("Board: " + min.board.toString());
            // ... and insert into the PQ all neighboring search nodes that can
            // be reached in 1 move from this dequeued min searchNode:
            //      ...access all neighbors from min.board
            for (Board b : min.board.neighbors()) {
                //      ...create search nodes from neighbors
                SearchNode sneighbor = new SearchNode(b, min.moves + 1, min);
                //      ...add all neighbors to PQ
                // optimisation : don't add boards if it matches current searchNode
                // if (!sneighbor.board.equals(min.board)) {
                if (alternate == 0) {
                    // StdOut.println("neighbor inerted actual PQ");
                    pq.insert(sneighbor);
                }
                else {
                    // StdOut.println("neighbor inerted twin PQ");
                    pqTwin.insert(sneighbor);
                }
                // }
            }

            // alternate between twin and actual
            if (alternate == 0) {
                alternate = 1;
            }
            else {
                alternate = 0;
            }

            if (alternate == 0) {
                if (!pq.isEmpty()) {
                    min = pq.delMin();
                }
                else {
                    min = null;
                }
            }
            else {
                if (!pq.isEmpty()) {
                    min = pqTwin.delMin();
                }
                else {
                    min = null;
                }
            }
        }

        // use alternate to determine if twin or actual board was solved
        // ... if twin, -> set NOT solvable
        // ... else, solvable TODO - test
        if (alternate == 0) {
            isSolvableBool = true;
        }
        else {
            isSolvableBool = false;
        }

        // set number of moves required to solve
        // movesSolution = pq.size(); // TODO is this correct? What if unsolvable?

        // fill in solution -> loop over PQ  TODO what if unsolvable?
        // TODO - is this the correct board? no, i start from goal!
        SearchNode solutionSequence = min;
        // chase pointers 'prev' to build solution
        while (solutionSequence.prev != null) {
            solution.push(solutionSequence.board);
            solutionSequence = solutionSequence.prev;
        }

        movesSolution = solution.size();
    }

    private void makeGoal(int size) {
        int[][] goalTile = new int[size][size];
        int gt = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                goalTile[i][j] = gt++;
            }
        }
        // set final place as 0
        goalTile[size - 1][size - 1] = 0;

        goal = new Board(goalTile);
    }

    // is the initial board solvable? (see below)
    public boolean isSolvable() {
        return isSolvableBool;
    }

    // min number of moves to solve initial board
    public int moves() {
        return movesSolution;
    }

    // sequence of boards in a shortest solution
    public Iterable<Board> solution() {
        return solution;
    }

    public static void main(String[] args) {

        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);

        // solve the puzzle
        Solver solver = new Solver(initial);

        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
