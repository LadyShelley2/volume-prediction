import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LSM_RN_ALL {
    private int[][] L;
    private List<int[][]> snapshots;
    private int n;
    private int k;

    private static double labmda = 0.4;

    LSM_RN_ALL(List<int[][]> snapshots, int[][] L) {
        this.snapshots = snapshots;
        this.L = L;
        n = snapshots.get(0).length;
        k = 10;
    }

    public List<int[][]> getYMatrixs(List<int[][]> snapshots) {
        return snapshots.stream().map(s -> getYMatrix(s)).collect(Collectors.toList());
    }

    private int[][] getYMatrix(int[][] snapshot) {
        return Arrays.stream(snapshot).map(s ->
                Arrays.stream(s).map(e -> e > 0 ? 1 : 0).toArray()
        ).toArray(int[][]::new);
    }

    public void globalLearning() {
        List<int[][]> Y = getYMatrixs(snapshots);

        List<double[][]> U = new ArrayList<>();
        int T = Y.size();
        for (int t = 0; t < T; t++)
            U.add(initMatrix(n, k));

        double[][] B = initMatrix(k, k);
        double[][] A = initMatrix(k, k);


    }

    private double[][] initMatrix(int m, int n) {
        return new double[m][n];
    }

    private double getGoal(List<int[][]> Y,
                           List<double[][]> snapshots,
                           List<double[][]> U,
                           double[][] B, int[][] L, double[][] A) {
        int T = Y.size();

        double res = 0.0;
        for(int t=1;t<T;t++){

        }

        return res;
    }
}
