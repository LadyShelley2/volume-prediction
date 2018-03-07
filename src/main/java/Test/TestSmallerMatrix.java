package Test;

import Algrithom.DataPicker;
import Algrithom.LSM_RN_ALL;
import Util.FileUtil;
import org.jblas.DoubleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class TestSmallerMatrix
{
    private static int n =5;
    private static int num = 20;//number of snapshots
    private static String base_url = "D:\\data\\testSmallerMatrix\\";
    private static DataPicker dp = new DataPicker();
    public static void main(String[] args){
        List<double[][]> snapshots = new ArrayList<>();
        for(int i=0;i<num;i++){
            snapshots.add(DoubleMatrix.rand(n,n).mul(100).toArray2());
        }

        //generate W

        double[][] random = DoubleMatrix.rand(n,n).toArray2();
        double[][] matrixW = Arrays.stream(random).map(r-> Arrays.stream(r).map(rr->rr>0.7?1:0).toArray()).toArray(double[][]::new);
        double[][] matrixD = dp.getD(matrixW);

        LSM_RN_ALL lsmRnAll = new LSM_RN_ALL(snapshots,matrixW,matrixD);
        lsmRnAll.globalLearning();

        List<DoubleMatrix> completedSnapshots = lsmRnAll.completion(lsmRnAll.trainedU,lsmRnAll.trainedB);
        List<DoubleMatrix> baseSnapshots = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());

        FileUtil fout1 = new FileUtil(base_url+"base.txt");
        FileUtil fout2 = new FileUtil(base_url+"completion.txt");
        dp.output(baseSnapshots.get(2).toArray2(),fout1);
        dp.output(completedSnapshots.get(2).toArray2(),fout2);

        double mape= lsmRnAll.getListMAPE(baseSnapshots,completedSnapshots);
        double rmse = lsmRnAll.getListRMSE(baseSnapshots,completedSnapshots);

        System.out.println(LSM_RN_ALL.getYMatrixs(baseSnapshots));

        System.out.println("RMSE 为：" + rmse);
        System.out.println("MAPE 为：" + mape);

    }

}
