import org.jblas.DoubleMatrix;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        DataPicker dp =new DataPicker();

        List<double[][]> snapshots = dp.getSnapshots(false);

        double[][] matrixW = dp.getW();
//        double[][] matrixD = dp.getD(matrixW);
//
//
//        LSM_RN_ALL lsmRnAll = new LSM_RN_ALL(snapshots,matrixW,matrixD);
//        lsmRnAll.globalLearning();
//
//        List<DoubleMatrix> completedSnapshots = lsmRnAll.completion(lsmRnAll.trainedU,lsmRnAll.trainedB);
//        List<DoubleMatrix> baseSnapshots = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());
//
//        double mape= lsmRnAll.getListMAPE(baseSnapshots,completedSnapshots);
//        double rmse = lsmRnAll.getListRMSE(baseSnapshots,completedSnapshots);
//
//        System.out.println(LSM_RN_ALL.getYMatrixs(baseSnapshots));
//
//        System.out.println("MAPE 为：" + mape);
//        System.out.println("RMSE 为：" + rmse);

        // permutated test

        DoubleMatrix permutation = new Main().randomPermutation(snapshots.get(0).length);

        List<double[][]> snapshotsPermutated = snapshots.stream().map(s->(new DoubleMatrix(s).mmul(permutation).toArray2())).collect(Collectors.toList());
        double[][] matrixWPermutated = (new DoubleMatrix(matrixW).mmul(permutation)).toArray2();
        double[][] matrixDPermutated = dp.getD(matrixWPermutated);

        LSM_RN_ALL lsmRnAllPermutated = new LSM_RN_ALL(snapshotsPermutated,matrixWPermutated,matrixDPermutated);
        lsmRnAllPermutated.globalLearning();

        List<DoubleMatrix> completedSnapshotsPermutated = lsmRnAllPermutated.completion(lsmRnAllPermutated.trainedU,lsmRnAllPermutated.trainedB);
        List<DoubleMatrix> baseSnapshotsPermutated = snapshotsPermutated.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());

        double mapePermutated = lsmRnAllPermutated.getListMAPE(baseSnapshotsPermutated,completedSnapshotsPermutated);
        double rmsePermutated = lsmRnAllPermutated.getListRMSE(baseSnapshotsPermutated,completedSnapshotsPermutated);

        System.out.println("MAPE 为：" + mapePermutated);
        System.out.println("RMSE 为：" + rmsePermutated);

    }

    public DoubleMatrix randomPermutation(int length){
        DoubleMatrix res = DoubleMatrix.eye(length);
        Random random = new Random(Long.parseLong(new SimpleDateFormat("HHmmss").format(new Date())));

        for(int i=0;i<1000;i++){
            int first =random.nextInt(length);
            int seconed = random.nextInt(length);
            res.swapRows(first,seconed);
        }
        return  res;
    }
}
