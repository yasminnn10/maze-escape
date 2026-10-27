import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1.
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);

        int[][] slidesFrom = Graphs.slidesFrom(s);
        int[][] slidesInto = Graphs.slidesInto(s);

        // Pass 1: Order cells by finish time using iterative DFS
        boolean[] visited = new boolean[n];
        ArrayDeque<Integer> finishOrder = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                ArrayDeque<Integer> stack = new ArrayDeque<>();
                ArrayDeque<Integer> edgeIdxStack = new ArrayDeque<>();

                stack.push(i);
                edgeIdxStack.push(0);
                visited[i] = true;

                while (!stack.isEmpty()) {
                    int u = stack.peek();
                    int edgeIdx = edgeIdxStack.pop();

                    if (edgeIdx < slidesFrom[u].length) {
                        edgeIdxStack.push(edgeIdx + 1);
                        int slideIdx = slidesFrom[u][edgeIdx];
                        int v = s.to()[slideIdx];

                        if (!visited[v]) {
                            visited[v] = true;
                            stack.push(v);
                            edgeIdxStack.push(0);
                        }
                    } else {
                        stack.pop();
                        finishOrder.push(u);
                    }
                }
            }
        }

        // Pass 2: Assign zone IDs in order of decreasing finish time using reversed graph (slidesInto)
        int currentZone = 0;
        for (int root : finishOrder) {
            if (zoneOf[root] == -1) {
                ArrayDeque<Integer> stack = new ArrayDeque<>();
                stack.push(root);
                zoneOf[root] = currentZone;

                while (!stack.isEmpty()) {
                    int u = stack.pop();

                    for (int slideIdx : slidesInto[u]) {
                        int v = s.from()[slideIdx];
                        if (zoneOf[v] == -1) {
                            zoneOf[v] = currentZone;
                            stack.push(v);
                        }
                    }
                }
                currentZone++;
            }
        }

        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones and dynamic programming over topological order.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        int startZone = zoneOf[0];
        int exitZone = zoneOf[s.n() - 1];

        // Find minimum edge cost between zones
        // Min edge weights map/structure
        List<List<int[]>> zoneAdj = new ArrayList<>();
        for (int i = 0; i < zoneCount; i++) {
            zoneAdj.add(new ArrayList<>());
        }

        for (int i = 0; i < s.count(); i++) {
            int u = s.from()[i];
            int v = s.to()[i];
            int weight = s.time()[i];

            int zU = zoneOf[u];
            int zV = zoneOf[v];

            if (zU != zV) {
                zoneAdj.get(zU).add(new int[]{zV, weight});
            }
        }

        // DP for longest chain in zones (Flow stages)
        int[] longestChain = new int[zoneCount];
        Arrays.fill(longestChain, 1);

        for (int u = 0; u < zoneCount; u++) {
            for (int[] edge : zoneAdj.get(u)) {
                int v = edge[0];
                if (longestChain[u] + 1 > longestChain[v]) {
                    longestChain[v] = longestChain[u] + 1;
                }
            }
        }

        int maxStages = 0;
        for (int stages : longestChain) {
            if (stages > maxStages) {
                maxStages = stages;
            }
        }

        // DP for cheapest route from startZone to exitZone
        long[] dist = new long[zoneCount];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[startZone] = 0;

        for (int u = startZone; u < zoneCount; u++) {
            if (dist[u] != Long.MAX_VALUE) {
                for (int[] edge : zoneAdj.get(u)) {
                    int v = edge[0];
                    int cost = edge[1];
                    if (dist[u] + cost < dist[v]) {
                        dist[v] = dist[u] + cost;
                    }
                }
            }
        }

        long fastestCost = dist[exitZone] == Long.MAX_VALUE ? 0 : dist[exitZone];

        return new long[]{maxStages, fastestCost};
    }
}