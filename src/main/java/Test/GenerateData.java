package Test;

import Util.FileUtil;
import org.jblas.DoubleMatrix;

public class GenerateData {
    private String base_url = "D:\\Github\\matrix-fac\\lsm-rn\\test_data\\";
    private double threshold = 0.6;

    public void increasing(int size, int start, int interval, int num, String url){
        DoubleMatrix random = DoubleMatrix.rand(size,size);
//
//        DoubleMatrix W  = new DoubleMatrix(Arrays.stream(random.toArray2())
//                .map(r-> Arrays.stream(r).map(rr->rr>threshold?1:0))
//                .toArray(double[][]::new)
//        );

        DoubleMatrix W = genWSeq(size);

        output(W,url+"W.txt");

        int value = start;
        for(int i=0;i<num;i++){
            DoubleMatrix snapshot = DoubleMatrix.ones(size,size).mul(value).mul(W);
            output(snapshot,url+Integer.toString(i)+".txt");
            value=value+interval;
        }
    }

    public void steady(int size, int value, int num, String url){
        DoubleMatrix W = genWSeq(size);
        output(W,url+"W.txt");
        DoubleMatrix snapshot = DoubleMatrix.ones(size,size).mul(value).mul(W);
        for(int i=0;i<num;i++){
            output(snapshot,url+Integer.toString(i)+".txt");
        }
    }

    public DoubleMatrix genWSeq(int size){
        DoubleMatrix W = DoubleMatrix.zeros(size,size);
        for(int i=0;i<size-1;i++){
            W.put(i,i+1,1);
            W.put(i+1,i,1);
        }
        return W;
    }

    public DoubleMatrix genW(int size){
        DoubleMatrix random = DoubleMatrix.rand(size,size);
        DoubleMatrix W = new DoubleMatrix(size,size);

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(random.get(i,j)>threshold)
                    W.put(i,j,1);
                else W.put(i,j,0);
            }
        }
        return W;
    }

    public void output(DoubleMatrix matrix, String url){
        FileUtil fout = new FileUtil(url);
        int rowCount = matrix.rows;
        int columnCount = matrix.columns;

        String line = "" ;
        for(int i=0;i<rowCount;i++){
            line ="";
            for(int j =0;j<columnCount;j++){
                line+=String.valueOf(matrix.get(i,j));
                line+="\t";
            }
            fout.writeLine(line);
        }
        fout.close();
    }

    public static void main(String[] args){
        GenerateData gd = new GenerateData();
        gd.increasing(6,30,5,10,gd.base_url+"test_increasing\\");
        gd.steady(6,30,10,gd.base_url+"test_steady\\");
}

}
