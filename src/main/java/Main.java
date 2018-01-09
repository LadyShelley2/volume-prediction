import org.jblas.DoubleMatrix;

import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        DataPicker dp =new DataPicker();

        List<double[][]> snapshots = dp.getSnapshots(false);
        double[][] matrixW = dp.getW();
        double[][] matrixD = dp.getD(matrixW);

        LSM_RN_ALL lsmRnAll = new LSM_RN_ALL(snapshots,matrixW,matrixD);
        lsmRnAll.globalLearning();

        List<DoubleMatrix> completedSnapshots = lsmRnAll.completion(lsmRnAll.trainedU,lsmRnAll.trainedB);
        List<DoubleMatrix> baseSnapshots = snapshots.stream().map(s->new DoubleMatrix(s)).collect(Collectors.toList());

        double mape= lsmRnAll.getListMAPE(baseSnapshots,completedSnapshots);
        double rmse = lsmRnAll.getListRMSE(baseSnapshots,completedSnapshots);

        System.out.println(LSM_RN_ALL.getYMatrixs(baseSnapshots));

        System.out.println("MAPE 为：" + mape);
        System.out.println("RMSE 为：" + rmse);

    }
}
