package Algrithom;

import Util.FileUtil;
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

    private static int K=20;
    private static double LAMBDA = Math.pow(2,3);
    private static double GAMMA = Math.pow(2,-5);
    private static double THRESHOLD= 1;
    private static int MAX_ITERATION = 1000;
    private static double EPSILON =  1e-10;

    public List<DoubleMatrix> trainedU;
    public DoubleMatrix trainedB;
    public DoubleMatrix trainedA;

    LSM_RN_ALL(List<double[][]> snapshots, double[][] W, double[][] D) {

        this.G = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());
        this.W = new DoubleMatrix(W);
        this.D = new DoubleMatrix(D);
        this.L = this.D.sub(this.W);

        n = snapshots.get(0).length;
    }

    public static List<DoubleMatrix> getYMatrixs(List<DoubleMatrix> G) {
        return G.stream().map(s -> getYMatrix(s)).collect(Collectors.toList());
    }

    public static DoubleMatrix getYMatrix(DoubleMatrix G) {
        return new DoubleMatrix(Arrays.stream(G.toArray2()).map(g ->
                Arrays.stream(g).map(e -> e > 0 ? 1 : 0).toArray()
        ).toArray(double[][]::new));
    }

    public void globalLearning() {
        List<DoubleMatrix> Y = getYMatrixs(G);//指示矩阵

        List<DoubleMatrix> U = new ArrayList<>();
        int T = Y.size();
        for (int t = 0; t < T; t++)
            U.add(DoubleMatrix.rand(n, K));

        DoubleMatrix A = DoubleMatrix.rand(K, K);
        DoubleMatrix B = DoubleMatrix.rand(K, K);

        double goalValue = Double.MAX_VALUE;
        double preGoalValue = 0.0;
        int counter = 0;

        while (Math.abs(goalValue-preGoalValue) > THRESHOLD && counter++ < MAX_ITERATION) {

            System.out.println("第"+counter+"次迭代, 目标值 为：" + goalValue);
            U = caculateU(B,W,D,A,U,G,Y);
            B = caculateB(B,A,G,Y,U);
            A = caculateA(U,A);

            FileUtil foutU = new FileUtil("D:\\data\\U"+counter+".txt");
            new DataPicker().output(U.get(2).toArray2(),foutU);
            FileUtil foutB = new FileUtil("D:\\data\\B"+counter+".txt");
            new DataPicker().output(B.toArray2(),foutB);
            FileUtil foutA = new FileUtil("D:\\data\\A"+counter+".txt");
            new DataPicker().output(A.toArray2(),foutA);

            preGoalValue = goalValue;
            goalValue=getGoal(Y,G,U,B,L,A);
        }

        this.trainedU = U;
        this.trainedA = A;
        this.trainedB = B;
    }

    private double getGoal(List<DoubleMatrix> Y,
                           List<DoubleMatrix> G,
                           List<DoubleMatrix> U,
                           DoubleMatrix B, DoubleMatrix L, DoubleMatrix A) {
        int T = Y.size();

        double res = 0.0;
        double item1 = 0.0;
        double item2 = 0.0;
        double item3 = 0.0;
        for (int t = 0; t < T; t++) {
            item1 += Y.get(t).mul(G.get(t).sub(U.get(t).mmul(B).mmul(U.get(t).transpose()))).norm2();
//            res += Y.get(t).mul(G.get(t).sub(U.get(t).mmul(B).mmul(U.get(t).transpose()))).norm2();
            item2 += LAMBDA * getTrace(U.get(t).transpose().mmul(L).mmul(U.get(t)));
//            res += LAMBDA * getTrace(U.get(t).transpose().mmul(L).mmul(U.get(t)));
        }
        for (int t = 1; t < T; t++) {
            item3 += GAMMA * U.get(t).sub(U.get(t - 1).mmul(A)).norm2();
        }
        System.out.println("************************");
        System.out.println("item1: "+item1);
        System.out.println("item2: "+item2);
        System.out.println("item3: "+item3);
        return item1+item2+item3;
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

            numerator = ((Y.get(t).mul(G.get(t))).mmul(U.get(t)).mmul(B).transpose())
                    .add((Y.get(t).transpose().mul(G.get(t).transpose())).mmul(U.get(t)).mmul(B))
                    .add(W.mmul(U.get(t)).mmul(LAMBDA))
                    .add((U.get(t-1).mmul(A).add(U.get(t+1).mmul(A.transpose()))).mmul(GAMMA));

            denominator = (Y.get(t).mul(U.get(t).mmul(B).mmul(U.get(t).transpose())))
                    .mmul(U.get(t).mmul(B.transpose()).add(U.get(t).mmul(B)))
                    .add(D.mmul(U.get(t)).mmul(LAMBDA))
                    .add(U.get(t).add(U.get(t).mmul(A).mmul(A.transpose())).mmul(GAMMA));
//                    .add(DoubleMatrix.ones(n,K).mul(EPSILON));

            U.set(t,U.get(t).mul(MatrixFunctions.powi(diviReplace(numerator,denominator),0.25)));
        }
        return  U;
    }

    private DoubleMatrix caculateB(DoubleMatrix B,
                                   DoubleMatrix A,
                                   List<DoubleMatrix> G,
                                   List<DoubleMatrix> Y,
                                   List<DoubleMatrix> U) {
        int T = U.size();
        DoubleMatrix numerator = DoubleMatrix.zeros(K,K);
        DoubleMatrix denominator = DoubleMatrix.zeros(K,K);

        for(int t=0;t<T;t++){
//            System.out.println("U's numerator sum: "+ U.get(t).transpose().mmul(Y.get(t).mul(G.get(t))).mmul(U.get(t)).sum());
//            System.out.println("Y.get(t).sum(): "+Y.get(t).sum());
//            System.out.println("G.get(t).sum(): "+G.get(t).sum());
//            System.out.println("Y.get(t).mul(G.get(t)): "+ Y.get(t).mul(G.get(t)).sum());
            numerator.addi(U.get(t).transpose().mmul(Y.get(t).mul(G.get(t))).mmul(U.get(t)));
            denominator.addi(U.get(t).transpose().mmul(Y.get(t).mul(U.get(t).mmul(B).mmul(U.get(t).transpose()))).mmul(U.get(t)));
        }

//        denominator.addi(DoubleMatrix.ones(K,K).mmul(EPSILON));
        return B.mul(diviReplace(numerator,denominator));
    }

    private DoubleMatrix caculateA(List<DoubleMatrix> U, DoubleMatrix A) {
       int T = U.size();
       DoubleMatrix numerator = DoubleMatrix.zeros(K,K);
       DoubleMatrix denominator = DoubleMatrix.zeros(K,K);
       for(int t=2;t<T;t++){
           numerator.addi(U.get(t-1).transpose().mmul(U.get(t)));
           denominator.addi(U.get(t-1).transpose().mmul(U.get(t-1)).mmul(A));
       }
//       denominator.addi(DoubleMatrix.ones(K,K).mmul(EPSILON));

       return A.mul(diviReplace(numerator,denominator));
    }

    public static DoubleMatrix diviReplace(DoubleMatrix numerator, DoubleMatrix denominator){
        DoubleMatrix division = numerator.divi(denominator);

        return new DoubleMatrix(Arrays.stream(division.toArray2())
                .map(d-> Arrays.stream(d)
                        .map(dd->(Double.isInfinite(dd)||Double.isNaN(dd))?0:dd)
                        .toArray())
                .toArray(double[][]::new)
        );
    }


    public List<DoubleMatrix> completion(List<DoubleMatrix> U, DoubleMatrix B) {

        //record
        FileUtil fout = new FileUtil("D:\\data\\"+"U.txt");
        FileUtil fout2 = new FileUtil("D:\\data\\"+"B.txt");
        new DataPicker().output(U.get(0).toArray2(),fout);
        new DataPicker().output(B.toArray2(),fout2);

        return U.stream().map(u->u.mmul(B).mmul(u.transpose())).collect(Collectors.toList());
    }



    public DoubleMatrix prediction(DoubleMatrix B, DoubleMatrix A, DoubleMatrix U, int h){
        return U.mmul(MatrixFunctions.pow(A,h)).mmul(B).mmul(U.mmul(MatrixFunctions.pow(A,h)).transpose());
    }

    public double getListMAPE(List<DoubleMatrix> base, List<DoubleMatrix> estimate){
        int length = base.size();
        double res = 0.0;
        for(int i=0;i<length;i++)
            res+=getMAPE(base.get(i),estimate.get(i));
        return res;
    }

    public  double getListRMSE(List<DoubleMatrix> base, List<DoubleMatrix> estimate){
        int length = base.size();
        double res =0.0;
        for(int i=0;i<length;i++)
            res+=getRMSE(base.get(i),estimate.get(i));
        return res;
    }

    public double getMAPE(DoubleMatrix base, DoubleMatrix estimate){

//        System.out.println(base.sub(estimate));
        System.out.println(MatrixFunctions.abs(base.sub(estimate)).sum());
        System.out.println(MatrixFunctions.abs(base.sub(estimate)).divi(base.add(DoubleMatrix.ones(n,n).mmul(EPSILON))).sum());

        return diviReplace(MatrixFunctions.abs(base.sub(estimate)),base).sum()/base.length;
//        return MatrixFunctions.abs(base.sub(estimate)).divi(base.add(DoubleMatrix.ones(n,n).mmul(EPSILON))).sum()/base.length;
    }

    public double getRMSE(DoubleMatrix base, DoubleMatrix estimate){
//        System.out.println(base.sub(estimate));
        System.out.println(MatrixFunctions.pow(base.sub(estimate),2).sum());
        return MatrixFunctions.sqrt(MatrixFunctions.pow(base.sub(estimate),2).sum()/base.length);
    }

    private double getTrace(DoubleMatrix matrix) {
        double res = 0.0;
        for (int i = 0; i < matrix.rows; i++)
            res += matrix.get(i, i);
        return res;
    }


    public static void main(String[] args){
        DoubleMatrix U = new DoubleMatrix(new double[][]{{112.3,108.4,148.9,117.4},{112.3,0,0,0}});

        DoubleMatrix E = new DoubleMatrix(new double[][]{{112.3,108.4,0,0},{112.3,0,0,0}});

        System.out.println(LSM_RN_ALL.diviReplace(U,E));

//        System.out.println(LSM_RN_ALL.getMAPE(U,E));
//        System.out.println(LSM_RN_ALL.getRMSE(U,E));
//        E.add(U.transpose().mmul(U));
    }
}
