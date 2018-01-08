import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LSM_RN_ALL {
    private List<DoubleMatrix> G;
    private DoubleMatrix W;
    private DoubleMatrix D;
    private DoubleMatrix L;
    private int n;

    private static int k=10;
    private static double labmda = 0.4;
    private static double gamma = 0.2;
    private static double threshold = 1;

    LSM_RN_ALL(List<double[][]> snapshots, double[][] W, double[][] D) {

        this.G = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());
        this.W = new DoubleMatrix(W);
        this.D = new DoubleMatrix(D);
        this.L = this.D.sub(this.W);

        n = snapshots.get(0).length;
    }

    public List<DoubleMatrix> getYMatrixs(List<DoubleMatrix> G) {
        return G.stream().map(s -> getYMatrix(s)).collect(Collectors.toList());
    }

    private DoubleMatrix getYMatrix(DoubleMatrix G) {
        return new DoubleMatrix(Arrays.stream(G.toArray2()).map(g ->
                Arrays.stream(g).map(e -> e > 0 ? 1 : 0).toArray()
        ).toArray(double[][]::new));
    }

    public void globalLearning() {
        List<DoubleMatrix> Y = getYMatrixs(G);//指示矩阵

        List<DoubleMatrix> U = new ArrayList<>();
        int T = Y.size();
        for (int t = 0; t < T; t++)
            U.add(DoubleMatrix.rand(n, k));

        DoubleMatrix A = DoubleMatrix.rand(k, k);
        DoubleMatrix B = DoubleMatrix.rand(k, k);

        double goalValue = Double.MAX_VALUE;
        while (goalValue > threshold) {
            U = caculateU(B,W,D,A,U,G,Y);
            B = caculateB(B,A,G,Y,U);
            A = caculateA(U,A);
            goalValue=getGoal(Y,G,U,B,L,A);
        }

    }

    private double getGoal(List<DoubleMatrix> Y,
                           List<DoubleMatrix> G,
                           List<DoubleMatrix> U,
                           DoubleMatrix B, DoubleMatrix L, DoubleMatrix A) {
        int T = Y.size();

        double res = 0.0;
        for (int t = 0; t < T; t++) {
            res += Y.get(t).mul(G.get(t).sub(U.get(t).mul(B).mul(U.get(t).transpose()))).norm2();
            res += labmda * getTrace(U.get(t).mmul(L).mmul(U.get(t).transpose()));
        }
        for (int t = 1; t < T; t++) {
            res += gamma * U.get(t).sub(U.get(t - 1).mmul(A)).norm2();
        }
        return res;
    }

    private List<DoubleMatrix> caculateU(DoubleMatrix B,
                                   DoubleMatrix W,
                                   DoubleMatrix D,
                                   DoubleMatrix A,
                                   List<DoubleMatrix> U,
                                   List<DoubleMatrix> G,
                                   List<DoubleMatrix> Y) {
        int T = U.size();
        DoubleMatrix numerator;
        DoubleMatrix denominator;

        //公式中涉及对t-1和t+1元素的访问，因此t的范围设定为1~T-2
        for(int t=1;t<T-1;t++){

            numerator = ((Y.get(t).mul(G.get(t))).mmul(U.get(t).mmul(B)))
                    .add((Y.get(t).transpose().mul(G.get(t).transpose())).mmul(U.get(t)).mmul(B))
                    .add(W.mmul(U.get(t)).mmul(labmda))
                    .add((U.get(t-1).mmul(A).add(U.get(t+1).mmul(A.transpose()))).mmul(gamma));

            denominator = (Y.get(t).mul(U.get(t).mmul(B).mmul(U.get(t).transpose())))
                    .mmul(U.get(t).mmul(B.transpose()).add(U.get(t).mmul(B)))
                    .add(D.mmul(U.get(t)).mmul(labmda))
                    .add(U.get(t).add(U.get(t).mmul(A).mmul(A.transpose())).mmul(gamma));

            U.set(t,U.get(t).mul(MatrixFunctions.pow(numerator.div(denominator),0.25)));
        }
        return  U;
    }
    private DoubleMatrix caculateB(DoubleMatrix B,
                                   DoubleMatrix A,
                                   List<DoubleMatrix> G,
                                   List<DoubleMatrix> Y,
                                   List<DoubleMatrix> U) {
        int T = U.size();
        DoubleMatrix numerator = DoubleMatrix.zeros(k,k);
        DoubleMatrix denominator = DoubleMatrix.zeros(k,k);

        for(int t=0;t<T;t++){
            numerator.add(U.get(t).mmul(Y.get(t).mul(G.get(t))).mul(U.get(t).transpose()));
            denominator.add(U.get(t).mmul(Y.get(t).mul(U.get(t).mmul(B).mmul(U.get(t).transpose()))));
        }
        return B.mul(numerator.div(denominator));
    }

    private DoubleMatrix caculateA(List<DoubleMatrix> U, DoubleMatrix A) {
       int T = U.size();
       DoubleMatrix numerator = DoubleMatrix.zeros(k,k);
       DoubleMatrix denominator = DoubleMatrix.zeros(k,k);
       for(int t=2;t<T;t++){
           numerator.add(U.get(t-1).transpose().mmul(U.get(t)));
           denominator.add(U.get(t-1).transpose().mmul(U.get(t-1)).mmul(A));
       }

       return A.mul(numerator.div(denominator));
    }

    public DoubleMatrix completion(DoubleMatrix U, DoubleMatrix B) {
        return U.mmul(B).mmul(U.transpose());
    }
    public DoubleMatrix prediction(DoubleMatrix B, DoubleMatrix A, DoubleMatrix U, int h){
        return U.mmul(MatrixFunctions.pow(A,h)).mmul(B).mmul(U.mmul(MatrixFunctions.pow(A,h)).transpose());
    }

    public double getMAPE(DoubleMatrix base, DoubleMatrix estimate){

        System.out.println(base);
        System.out.println(estimate);
        System.out.println(base.sub(estimate));
        System.out.println(MatrixFunctions.abs(base.sub(estimate)));
        return MatrixFunctions.abs(base.sub(estimate)).divi(base).sum()/base.length;
    }
    public double getRMSE(DoubleMatrix base, DoubleMatrix estimate){
        return MatrixFunctions.sqrt(MatrixFunctions.pow(base.sub(estimate),2).sum()/base.length);
    }

    private double getTrace(DoubleMatrix matrix) {
        double res = 0.0;
        for (int i = 0; i < matrix.length; i++)
            res += matrix.get(i, i);
        return res;
    }

    public static void main(String[] args){
        DoubleMatrix U = new DoubleMatrix(new double[][]{{112.3,108.4,148.9,117.4}});
        DoubleMatrix E = new DoubleMatrix(new double[][]{{124.7,103.7,116.6,78.5}});

//        System.out.println(LSM_RN_ALL.getMAPE(U,E));
//        System.out.println(LSM_RN_ALL.getRMSE(U,E));
        E.add(U.transpose().mmul(U));
    }
}
