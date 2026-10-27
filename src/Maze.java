import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        int n = w.cells();
        DisjointSet dsu = new DisjointSet(n);
        ArrayList<Integer> broken = new ArrayList<>();

        for (int i = 0; i < w.count(); i++) {
            int cellA = w.a()[i];
            int cellB = w.b()[i];
            if (dsu.union(cellA, cellB)) {
                broken.add(i);
            }
        }

        return broken.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        int n = w.cells();
        Integer[] indices = new Integer[w.count()];
        for (int i = 0; i < w.count(); i++) {
            indices[i] = i;
        }

        // Sort wall indices by their cost
        Arrays.sort(indices, Comparator.comparingInt(i -> w.cost()[i]));

        DisjointSet dsu = new DisjointSet(n);
        ArrayList<Integer> broken = new ArrayList<>();

        for (int idx : indices) {
            int cellA = w.a()[idx];
            int cellB = w.b()[idx];
            if (dsu.union(cellA, cellB)) {
                broken.add(idx);
            }
        }

        return broken.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();

        // Build adjacency list for Prim's algorithm
        @SuppressWarnings("unchecked")
        ArrayList<int[]>[] adj = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>();
        }

        for (int i = 0; i < w.count(); i++) {
            int u = w.a()[i];
            int v = w.b()[i];
            int cost = w.cost()[i];
            adj[u].add(new int[]{v, cost});
            adj[v].add(new int[]{u, cost});
        }

        boolean[] visited = new boolean[n];
        // Priority queue stores edges as {cost, targetNode}
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        long totalCost = 0;
        int visitedCount = 0;

        // Start from cell 0
        visited[0] = true;
        visitedCount++;
        for (int[] edge : adj[0]) {
            pq.add(new int[]{edge[1], edge[0]});
        }

        while (!pq.isEmpty() && visitedCount < n) {
            int[] current = pq.poll();
            int cost = current[0];
            int u = current[1];

            if (visited[u]) {
                continue;
            }

            visited[u] = true;
            visitedCount++;
            totalCost += cost;

            for (int[] edge : adj[u]) {
                int v = edge[0];
                int c = edge[1];
                if (!visited[v]) {
                    pq.add(new int[]{c, v});
                }
            }
        }

        return totalCost;
    }
}