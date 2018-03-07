package Algrithom;

import org.jblas.DoubleMatrix;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String base_url = "D:\\data\\test_20170910\\";

        DataPicker dp =new DataPicker();

        List<double[][]> snapshots = dp.getSnapshots(true);
        double[][] matrixW = dp.getW();
        double[][] matrixD = dp.getD(matrixW);

        System.out.println(new DoubleMatrix(matrixW).sum());

//        snapshots.stream().map(s->{
//            System.out.println(new DoubleMatrix(s).sum());
//            return 1;
//        }).collect(Collectors.toList());

        LSM_RN_ALL lsmRnAll = new LSM_RN_ALL(snapshots,matrixW,matrixD);
//        lsmRnAll.globalLearning();
//
//        List<DoubleMatrix> completedSnapshots = lsmRnAll.completion(lsmRnAll.trainedU,lsmRnAll.trainedB);
//        List<DoubleMatrix> baseSnapshots = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());
//
//        FileUtil fout1 = new FileUtil(base_url+"base.txt");
//        FileUtil fout2 = new FileUtil(base_url+"completion.txt");
//        dp.output(baseSnapshots.get(2).toArray2(),fout2);
//        dp.output(completedSnapshots.get(2).toArray2(),fout1);
//
//        double mape= lsmRnAll.getListMAPE(baseSnapshots,completedSnapshots);
//        double rmse = lsmRnAll.getListRMSE(baseSnapshots,completedSnapshots);
//
//        System.out.println(LSM_RN_ALL.getYMatrixs(baseSnapshots));
//
//        System.out.println("RMSE 为：" + rmse);
//        System.out.println("MAPE 为：" + mape);

    }
}
