package de.nkilders.astar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AStar {
    // ====== SETTINGS ====== //
    final int nodeSize = 15; // for rendering
    final int gridWidth = 50;
    final int gridHeight = 50;
    final long sleepTime = 5L; // delay between the algorithm's steps
    final float barriers = 0.25F;
    final boolean allowDiagonals = true;
    final boolean randomStartAndEnd = false;
    // ====================== //

    JFrame frame;
    JPanel content;

    Node[][] grid;

    List<Node> nodes;
    List<Node> openList;
    List<Node> closedList;
    List<Node> path;

    Node start;
    Node end;

    Node lastChecked;

    long startTime;
    Timer timer = new Timer();

    public AStar() {
        System.out.println();
        System.out.println("== SETTINGS ==");
        System.out.printf("%-11s %dx%d%n", "Size:", gridWidth, gridHeight);
        System.out.printf("%-11s %d%%%n", "Barriers:", (int) (barriers * 100));
        System.out.printf("%-11s %s%n", "Diagonals:", allowDiagonals ? "Allowed" : "Not allowed");
        System.out.println();

        grid = new Node[gridWidth][gridHeight];
        nodes = new ArrayList<>();
        openList = new LinkedList<>();
        closedList = new ArrayList<>();
        path = new ArrayList<>();

        // create nodes
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Node n = new Node(x, y);
                nodes.add(n);
                grid[x][y] = n;
            }
        }

        // set start- and end-node
        if (randomStartAndEnd) {
            Random r = new Random();
            start = grid[r.nextInt(gridWidth)][r.nextInt(gridHeight)];
            end = grid[r.nextInt(gridWidth)][r.nextInt(gridHeight)];
        } else {
            start = grid[0][0];
            end = grid[gridWidth - 1][gridHeight - 1];
        }

        start.wall = false;
        end.wall = false;

        // find neighbors
        for (Node n : nodes) {
            n.findNeighbors();
        }

        // create window
        frame = new JFrame("A* Algorithm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(gridWidth * nodeSize + 20, gridHeight * nodeSize + 40);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        content = new JPanel();
        content.setLayout(null);
        frame.setContentPane(content);

        C c = new C();
        c.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        content.add(c);

        frame.setVisible(true);

        // start algorithm
        startAlgorithm();
    }

    void startAlgorithm() {
        startTime = System.currentTimeMillis();

        openList.add(start);

        start.g = 0;
        start.f = start.h = heuristic(start, end);

        // find path
        while (!openList.isEmpty()) {
            if (!timer.hasReached(sleepTime)) {
                continue;
            }

            timer.reset();

            Node current = lastChecked = minF();

            openList.remove(current);
            closedList.add(current);

            if (current == end) {
                long time = System.currentTimeMillis() - startTime;
                System.out.printf("Done in %dms (%.3fsec)%n", time, (time / 1000F));

                findPath();
                System.out.printf("Path length: %d%n", path.size());

                return;
            }

            for (Node node : current.neighbors) {
                if (closedList.contains(node)) {
                    continue;
                }

                if (node.wall) {
                    continue;
                }

                if (!openList.contains(node)) {
                    openList.add(node);
                }

                double v = current.g + heuristic(current, node);
                if (v >= node.g) {
                    continue;
                }

                node.parent = current;
                node.g = v;
                node.f = node.g + (node.h = heuristic(node, end));
            }
        }

        findPath();
        System.out.println("No path found!");
    }

    // get node with min f value
    Node minF() {
        Node min = null;

        for (Node node : openList) {
            if (min == null) {
                min = node;
            } else if (node.f < min.f) {
                min = node;
            }
        }

        return min;
    }

    // get distance a->b
    double heuristic(Node a, Node b) {
        if (allowDiagonals) {
            int xDiff = a.x - b.x;
            int yDiff = a.y - b.y;

            return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        } else {
            return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        }
    }

    // save found path
    void findPath() {
        if (lastChecked == null) {
            return;
        }

        path.clear();

        Node node = lastChecked;
        path.add(node);

        while (node.parent != null) {
            path.add(node.parent);
            node = node.parent;
        }
    }

    class C extends JLabel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D g = (Graphics2D) graphics;

            for (Node node : nodes) {
                node.draw(g);
            }

            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(2));
            g.translate(nodeSize / 2, nodeSize / 2);

            if (path != null) {
                Node t = null;

                for (Node node : path) {
                    if (t != null && node != null) {
                        g.drawLine(t.x * nodeSize, t.y * nodeSize, node.x * nodeSize, node.y * nodeSize);
                    }

                    t = node;
                }
            }

            repaint();
        }
    }

    class Node {
        // position
        final int x;
        final int y;

        // full cost
        double f = Integer.MAX_VALUE;
        // cost start->this
        double g = Integer.MAX_VALUE;
        // cost this->end
        double h = Integer.MAX_VALUE;

        Node parent = null;
        boolean wall;
        List<Node> neighbors;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.wall = Math.random() < barriers;
            this.neighbors = new ArrayList<>();
        }

        void draw(Graphics2D g) {
            if (closedList.contains(this)) {
                g.setColor(new Color(0, 0, 0, 100));
            } else if (openList.contains(this)) {
                g.setColor(new Color(0, 0, 0, 150));
            } else if (lastChecked == this) {
                g.setColor(Color.MAGENTA);
            } else if (this == start || this == end) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(wall ? Color.BLACK : Color.WHITE);
            }

            g.fillRect(x * nodeSize, y * nodeSize, nodeSize, nodeSize);

            g.setColor(Color.BLACK);
            g.drawRect(x * nodeSize, y * nodeSize, nodeSize, nodeSize);
        }

        void findNeighbors() {
            // left
            if (x > 0 && !grid[x - 1][y].wall) {
                neighbors.add(grid[x - 1][y]);
            }

            // right
            if (x + 1 < gridWidth && !grid[x + 1][y].wall) {
                neighbors.add(grid[x + 1][y]);
            }

            // above
            if (y > 0 && !grid[x][y - 1].wall) {
                neighbors.add(grid[x][y - 1]);
            }

            // below
            if (y + 1 < gridHeight && !grid[x][y + 1].wall) {
                neighbors.add(grid[x][y + 1]);
            }

            if (allowDiagonals) {
                // left above
                if (x > 0 && y > 0) {
                    if (!grid[x - 1][y].wall && !grid[x][y - 1].wall) {
                        neighbors.add(grid[x - 1][y - 1]);
                    }
                }

                // right above
                if (x < gridWidth - 1 && y > 0) {
                    if (!grid[x + 1][y].wall && !grid[x][y - 1].wall) {
                        neighbors.add(grid[x + 1][y - 1]);
                    }
                }

                // right below
                if (x < gridWidth - 1 && y < gridHeight - 1) {
                    if (!grid[x + 1][y].wall && !grid[x][y + 1].wall) {
                        neighbors.add(grid[x + 1][y + 1]);
                    }
                }

                // left below
                if (x > 0 && y < gridHeight - 1) {
                    if (!grid[x - 1][y].wall && !grid[x][y + 1].wall) {
                        neighbors.add(grid[x - 1][y + 1]);
                    }
                }
            }
        }
    }

}