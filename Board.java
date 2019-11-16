/* a board class for the 8x8 puzzle */

import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class Board {
    private boolean isGoal = true;
    private final int n;
    private int hammingDistance;
    private int manhattanDistance;

    private final int[][] board;
    private final int[][] groundTruth;
    private int[][] twinBoard = null;

    // use queue for neighbor boards
    private int emptySquarei;
    private int emptySquarej;
    private Stack<Board> neighbors = new Stack<>();
    private boolean neighborsGen = false;

    private class Tile {
        private int i = -1;
        private int j = -1;
    }

    // create a board from an n-by-n array of tiles,
    // where tiles[row][col] = tile at (row, col)
    public Board(int[][] tiles) {
        if (tiles == null) {
            throw new IllegalArgumentException("tiles cannot be null");
        }

        n = tiles.length;
        board = new int[n][n];
        groundTruth = new int[n][n];

        // fill board & set up groundTruth board
        int gt = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = tiles[i][j];

                if (i == n - 1 && j == n - 1) {
                    groundTruth[i][j] = 0;
                }
                else {
                    groundTruth[i][j] = gt++;
                }

                // store empty position
                if (tiles[i][j] == 0) {
                    emptySquarei = i;
                    emptySquarej = j;
                }

                // is this board solved?
                if (tiles[i][j] != groundTruth[i][j]) {
                    isGoal = false;
                }
            }
        }

        // cache manhattan & hamming distance
        hammingDistance = getHamming(board, groundTruth, n);
        manhattanDistance = getManhattan();
    }

    // string representation of this board
    public String toString() {
        // base formatting on max number length (digits)
        int spacing = ("" + n * n).length() + 1;
        StdOut.println("n = " + n + " spacing: " + spacing);
        String formatting = "%" + spacing + "d";

        StringBuilder s = new StringBuilder();
        s.append(n + "\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                s.append(String.format(formatting, board[i][j]));
            }
            s.append("\n");
        }
        return s.toString();
    }

    // board dimension n
    public int dimension() {
        return n;
    }

    private int getHamming(int[][] a, int[][] b, int size) {
        // generate correct array (- to A - n)
        // count number of indices that do not match
        int distance = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // don't count 0 square
                if (a[i][j] != b[i][j] && a[i][j] != 0) {
                    distance++;
                }
            }
        }
        return distance;
    }

    // number of tiles out of place
    public int hamming() {
        return hammingDistance;
    }

    private int getManhattan() {
        // each tile is sum of distance from correct location (vert + horiz)
        int distance = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // skip empty square
                int tile = board[i][j];
                if (tile != 0) {
                    if (tile != groundTruth[i][j]) {
                        // calculate manhattan distance
                        // calculate correct i & j for tile
                        Tile correct = getCorrectCoord(tile);
                        distance += (Math.abs(i - correct.i) + Math.abs(j - correct.j));
                    }
                }
            }
        }
        return distance;
    }

    private Tile getCorrectCoord(int tile) {
        Tile t = new Tile();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (groundTruth[i][j] == tile) {
                    t.i = i;
                    t.j = j;
                    return t;
                }
            }
        }
        return t;
    }

    // sum of Manhattan distances between tiles and goal
    public int manhattan() {
        return manhattanDistance;
    }

    // is this board the goal board?
    public boolean isGoal() {
        return isGoal;
    }

    // does this board equal y?
    public boolean equals(Object y) {
        // TODO - test this (is this correct interpretation?)
        if (y == null || y.getClass() != this.getClass()) {
            return false;
        }

        // compare boards
        Board yb = (Board) y;
        if (yb.dimension() != n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (yb.board[i][j] != board[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        if (!neighborsGen) {
            generateNeighborBoards();
            neighborsGen = true;
        }
        return neighbors;
    }

    /*
    private class NeighborIterator implements Iterator<Board> {
        public boolean hasNext() {
            return !neighbors.isEmpty();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        public Board next() {
            if (!hasNext()) {
                throw new NoSuchElementException("cannot get next, no more items to return");
            }
            return neighbors.pop();
        }
    } */

    private void generateNeighborBoards() {
        // generate & store neighbor boards
        // gather valid moves (switch with 0 & other tile)
        if (emptySquarei - 1 >= 0) {
            int[][] up = deepCopy();
            // switch 0 and up
            switchTiles(emptySquarei - 1, emptySquarej, emptySquarei, emptySquarej, up);
            // store board
            neighbors.push(new Board(up));
        }
        if (emptySquarei + 1 < n) {
            int[][] down = deepCopy();
            // switch 0 and down
            switchTiles(emptySquarei + 1, emptySquarej, emptySquarei, emptySquarej, down);
            // store board
            neighbors.push(new Board(down));
        }
        if (emptySquarej - 1 >= 0) {
            int[][] right = deepCopy();
            // switch 0 and down
            switchTiles(emptySquarei, emptySquarej - 1, emptySquarei, emptySquarej, right);
            // store board
            neighbors.push(new Board(right));
        }
        if (emptySquarej + 1 < n) {
            int[][] left = deepCopy();
            // switch 0 and down
            switchTiles(emptySquarei, emptySquarej + 1, emptySquarei, emptySquarej, left);
            // store board
            neighbors.push(new Board(left));
        }
    }

    private int[][] deepCopy() {
        int[][] c = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                c[i][j] = board[i][j];
            }
        }
        return c;
    }

    // a board that is obtained by exchanging any pair of tiles
    public Board twin() {
        if (twinBoard == null) {
            twinBoard = deepCopy();
            makeTwin(twinBoard);
            Board twinB = new Board(twinBoard);
            return twinB;
        }
        else {
            return new Board(twinBoard);
        }

    }

    // -------------------------------------- *
    //          HELPER                        *
    // -------------------------------------- *

    private void makeTwin(int[][] twin) {

        // pick random tile 1 - old.length - 1
        int tileIndex = StdRandom.uniform(0, n * n - 1);
        int i = tileIndex / n;
        int j = tileIndex % n;

        // switch it with random neighbor
        /*
        if (i - 1 >= 0) { // switch up
            switchTiles(i - 1, j, i, j, twin);
        }
        else if (i + 1 < n) { // switch down
            switchTiles(i + 1, j, i, j, twin);
        }*/
        if (j - 1 >= 0) { // switch left
            switchTiles(i, j - 1, i, j, twin);
        }
        else { // switch right
            switchTiles(i, j + 1, i, j, twin);
        }
    }

    private void switchTiles(int i, int j, int oldi, int oldj, int[][] b) {
        int temp;
        temp = b[i][j];
        b[i][j] = b[oldi][oldj];
        b[oldi][oldj] = temp;
    }

    // -------------------------------------- *
    //          TESTS                         *
    // -------------------------------------- *
    private void testAll() {
        int nt = 3;
        int[][] tiles = new int[nt][nt];
        int cnt = 1;
        for (int i = 0; i < nt; i++) {
            for (int j = 0; j < nt; j++) {
                if (i == nt - 1 && j == nt - 1) {
                    tiles[i][j] = 0;
                }
                else {
                    tiles[i][j] = cnt++;
                }
            }
        }
        Board b = new Board(tiles);
        StdOut.println(b.toString());

        // test hamming & manhattan distance
        assert (b.hamming() == 0);
        assert (b.manhattan() == 0);

        int[][] tiles2 = { { 4, 2, 1 }, { 0, 5, 6 }, { 8, 7, 1 } };
        Board b2 = new Board(tiles2);
        StdOut.println(b2.toString());
        assert (b2.hamming() == 8);
        assert (b2.manhattan() == 15);

        for (Board neighbor : b2.neighbors()) {
            StdOut.println(neighbor.toString());
        }

        // test twin
        int[][] twin = b2.deepCopy();
        makeTwin(twin); // switches 2 adjacent tiles
        assert (b2.getHamming(b2.board, twin, 3) == 2);
        // StdOut.println("\nB2:\n" + b2.toString());
        // Board b2Twin = new Board(twin);
        // StdOut.println("Twin of B2:\n" + b2Twin.toString());
        // Board b2GrounTruth = new Board(b2.groundTruth);
        // StdOut.println("\nb2 groundtruth:\n" + b2GrounTruth.toString());

        // distance tests - goal board
        int[][] tiles3 = { { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 10, 11, 12 }, { 13, 14, 15, 0 } };
        Board b3 = new Board(tiles3);
        assert (b3.hamming() == 0);
        assert (b3.manhattan() == 0);
        // test twin
        int[][] twin3 = b3.deepCopy();
        makeTwin(twin3); // switches 2 adjacent tiles
        assert (b3.getHamming(b3.board, twin3, 4) == 2);

        // distance tests 3
        int[][] tiles4 = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
        Board b4 = new Board(tiles4);
        assert (b4.hamming() == 9);
        assert (b4.manhattan() == 16);
        // test twin
        int[][] twin4 = b4.deepCopy();
        makeTwin(twin4); // switches 2 adjacent tiles
        assert (b4.getHamming(b4.board, twin4, 3) == 2);
        assert (b4.getHamming(b4.board, groundTruth, 3) == 9);

        // Further twin testing...
        StdOut.println("\nTwin testing:\n");
        StdOut.println("Board4: " + b4.toString());
        StdOut.println("Twin4 : " + b4.twin().toString());

        // toString tests with 3 digit nums
        int[][] tiles5 = new int[20][20];
        int cnt5 = 1;
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (i == 20 - 1 && j == 20 - 1) {
                    tiles5[i][j] = 0;
                }
                else {
                    tiles5[i][j] = cnt5++;
                }
            }
        }
        Board b5 = new Board(tiles5);
        StdOut.println("\n" + b5.toString());

    }

    // unit testing (not graded)
    public static void main(String[] args) {
        int[][] t = new int[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                t[i][j] = 1;
            }
        }
        Board testBoard = new Board(t);
        testBoard.testAll();

        StdOut.println("\nBoard tests passed");
    }
}
