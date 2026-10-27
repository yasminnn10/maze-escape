import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Escape {

    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int rows = w.rows();
        int cols = w.cols();
        int totalCells = rows * cols;
        int exitCell = totalCells - 1;

        int[][] openNeighbors = Graphs.open(w, broken);

        boolean[][] isOpen = new boolean[totalCells][4]; // 0: up, 1: right, 2: down, 3: left
        int[] candidateCells = new int[]{-cols, 1, cols, -1};

        for (int u = 0; u < totalCells; u++) {
            for (int v : openNeighbors[u]) {
                for (int i = 0; i < 4; i++) {
                    if (candidateCells[i] + u == v) {
                        isOpen[u][i] = true;
                    }
                }
            }
        }

        int deadEnds = 0;
        for (int i = 0; i < totalCells; i++) {
            if (openNeighbors[i].length == 1) {
                deadEnds++;
            }
        }

        int[] parent = new int[totalCells];
        for (int i = 0; i < totalCells; i++) {
            parent[i] = -1;
        }

        boolean[] visited = new boolean[totalCells];
        int visitedCount = 0;
        int exitVisitedCount = 0;

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        visited[0] = true;
        visitedCount++;

        boolean exitFound = false;

        if (0 == exitCell) {
            exitFound = true;
            exitVisitedCount = visitedCount;
        }

        while (!stack.isEmpty()) {
            int curr = stack.peek();

            if (curr == exitCell && !exitFound) {
                exitFound = true;
                exitVisitedCount = visitedCount;
            }

            int nextNode = -1;
            // Try neighbors in order: up (0), right (1), down (2), left (3)
            for (int i = 0; i < 4; i++) {
                if (isOpen[curr][i]) {
                    int neighbor = curr + candidateCells[i];
                    if (!visited[neighbor]) {
                        nextNode = neighbor;
                        break;
                    }
                }
            }

            if (nextNode != -1) {
                visited[nextNode] = true;
                visitedCount++;
                parent[nextNode] = curr;

                if (nextNode == exitCell && !exitFound) {
                    exitFound = true;
                    exitVisitedCount = visitedCount;
                }

                stack.push(nextNode);
            } else {
                stack.pop();
            }
        }

        List<Integer> pathList = new ArrayList<>();
        int curr = exitCell;
        while (curr != -1) {
            pathList.add(curr);
            curr = parent[curr];
        }
        Collections.reverse(pathList);

        int[] path = pathList.stream().mapToInt(i -> i).toArray();

        return new Result(path, exitVisitedCount, deadEnds);
    }
}