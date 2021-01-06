package de.nkilders.astar;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AStar {
    // ====== SETTINGS ====== //
    final int nodeSize = 15; // for rendering
    final int gridWidth = 50;
    final int gridHeight = 50;
    final long sleepTime = 5L;
    final float barriers = 0.25F;
    final boolean allowDiagonals = true;
    // ====================== //

    JFrame frame;
    JPanel cont;

    Node[][] grid;

    List<Node> nodes;
    List<Node> openList;
    List<Node> closedList;
    List<Node> path;

    Node start;
    Node end;

    Node lastChecked;

    long started;
    Timer t = new Timer();

    public AStar() {
        System.err.println("Loading...");
        System.err.println();
        System.err.println("== SETTINGS ==");
        System.err.println("Size: " + gridWidth + "x" + gridHeight);
        System.err.println("Barriers: " + (int) (barriers * 100) + "%");
        System.err.println("Allow diagonals: " + allowDiagonals);
        System.err.println();

        // create nodes
        grid = new Node[gridWidth][gridHeight];
        nodes = new ArrayList<>();
        openList = new LinkedList<>();
        closedList = new ArrayList<>();
        path = new ArrayList<>();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Node n = new Node(x, y);
                nodes.add(n);
                grid[x][y] = n;
            }
        }

        // set start + end
        Random r = new Random();
        start = grid[r.nextInt(gridWidth)][r.nextInt(gridHeight)];
        end = grid[r.nextInt(gridWidth)][r.nextInt(gridHeight)];
        start = grid[0][0];
        end = grid[gridWidth - 1][gridHeight - 1];

        start.wall = false;
        end.wall = false;

        // find neighbors
        for (Node n : nodes)
            n.findNeighbors();

        // create window
        frame = new JFrame("A*");
        frame.setDefaultCloseOperation(3);
        frame.setSize(gridWidth * nodeSize + 7, gridHeight * nodeSize + 30);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        cont = new JPanel();
        cont.setLayout(null);
        frame.setContentPane(cont);

        C c = new C();
        c.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        cont.add(c);

        frame.setVisible(true);

        // start algorithm
        startAlgorithm();
    }

    void startAlgorithm() {
        System.err.println("Finding path...");
        started = System.currentTimeMillis();

        openList.add(start);

        start.g = 0;
        start.f = start.h = heuristic(start, end);

        // find path
        while (!openList.isEmpty()) {
            if (!t.hasReached(sleepTime))
                continue;

            t.reset();

            Node current = lastChecked = minF();

            openList.remove(current);
            closedList.add(current);

            if (current == end) {
                long l = System.currentTimeMillis() - started;
                System.err.println("Done in " + l + "ms (" + (l / 1000.0F) + "sec)");
                findPath();
                System.err.println("Path length: " + path.size());
                return;
            }

            for (Node n : current.neighbors) {
                if (closedList.contains(n))
                    continue;

                if (n.wall)
                    continue;

                if (!openList.contains(n))
                    openList.add(n);

                double v = current.g + heuristic(current, n);
                if (v >= n.g)
                    continue;

                n.parent = current;
                n.g = v;
                n.f = n.g + (n.h = heuristic(n, end));
            }
        }

        findPath();
        System.err.println("No path found!");
    }

    // get node with min f value
    Node minF() {
        Node m = null;

        for (Node n : openList) {
            if (m == null)
                m = n;
            else if (n.f < m.f)
                m = n;
        }

        return m;
    }

    // get distance a->b
    double heuristic(Node a, Node b) {
        if (allowDiagonals) {
            int x = a.x - b.x;
            int y = a.y - b.y;

            return Math.sqrt(x * x + y * y);
        } else {
            return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        }
    }

    // save found path
    void findPath() {
        if (lastChecked == null)
            return;

        path.clear();

        Node t = lastChecked;
        path.add(t);
        while (t.parent != null) {
            path.add(t.parent);
            t = t.parent;
        }
    }

    class C extends JLabel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D g = (Graphics2D) graphics;

            for (Node n : nodes)
                n.draw(g);

            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(2));
            g.translate(nodeSize / 2, nodeSize / 2);

            if (path != null) {
                Node t = null;
                for (Node n : path) {
                    if (t != null && n != null)
                        g.drawLine(t.x * nodeSize, t.y * nodeSize, n.x * nodeSize, n.y * nodeSize);

                    t = n;
                }
            }

            repaint();
        }
    }

    class Node {
        // pos
        final int x;
        final int y;

        // full cost
        double f = Integer.MAX_VALUE;
        // cost start->this
        double g = Integer.MAX_VALUE;
        // cost this->end
        double h = Integer.MAX_VALUE;

        // stuff
        Node parent = null;
        boolean wall;
        List<Node> neighbors = new ArrayList<>();

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.wall = Math.random() < barriers;
        }

        void draw(Graphics2D g) {
            g.setColor(wall ? Color.BLACK : Color.WHITE);

            if (closedList.contains(this))
                g.setColor(new Color(0, 0, 0, 100));

            if (openList.contains(this))
                g.setColor(new Color(0, 0, 0, 150));

            if (lastChecked == this)
                g.setColor(Color.MAGENTA);

            if (this == start || this == end)
                g.setColor(Color.GREEN);

            g.fillRect(x * nodeSize, y * nodeSize, nodeSize, nodeSize);

            g.setColor(Color.BLACK);
            g.drawRect(x * nodeSize, y * nodeSize, nodeSize, nodeSize);
        }

        void findNeighbors() {
            if (x - 1 >= 0)
                if (!grid[x - 1][y].wall)
                    neighbors.add(grid[x - 1][y]);
            if (x + 1 < gridWidth)
                if (!grid[x + 1][y].wall)
                    neighbors.add(grid[x + 1][y]);
            if (y - 1 >= 0)
                if (!grid[x][y - 1].wall)
                    neighbors.add(grid[x][y - 1]);
            if (y + 1 < gridWidth)
                if (!grid[x][y + 1].wall)
                    neighbors.add(grid[x][y + 1]);

            if (allowDiagonals) {
                if (x > 0 && y > 0)
                    if (!(grid[x - 1][y].wall || grid[x][y - 1].wall))
                        neighbors.add(grid[x - 1][y - 1]);
                if (x < gridWidth - 1 && y > 0)
                    if (!(grid[x + 1][y].wall || grid[x][y - 1].wall))
                        neighbors.add(grid[x + 1][y - 1]);
                if (x < gridWidth - 1 && y < gridHeight - 1)
                    if (!(grid[x + 1][y].wall || grid[x][y + 1].wall))
                        neighbors.add(grid[x + 1][y + 1]);
                if (x > 0 && y < gridHeight - 1)
                    if (!(grid[x - 1][y].wall || grid[x][y + 1].wall))
                        neighbors.add(grid[x - 1][y + 1]);
            }
        }
    }

}