package Test;

import Util.FileUtil;
import Util.TimeFormat;
import org.jblas.DoubleMatrix;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReformatData {
    private String baseurl = "D:\\Github\\matrix-fac\\lsm-rn\\visulization\\";
    private String baseurlOut = "D:\\Github\\matrix-fac\\lsm-rn\\visulization\\";
    private int nrow = 740*2;
    private int ncolumn = 12* 24;
    private String startTimeStr = "2017/09/10 00:05:00";
    private int timeInterval = 1000*60*5;
    private DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private String roadId = "G15";


    /**
     * 将数据转换成一个2m*n矩阵，m 为路段数，2m表示为同一路段不同方向分别用一行来表示。n为时间片数。
     * 一行代表一个路段不同时间片下的数值，列代表一个时间片下不同路段的数值，
     *
     * @param filename
     * @return
     */
    public double[][] serializeByTime(String filename) {
        FileUtil fin = new FileUtil(baseurl + filename);
        double[][] data = new double[nrow][ncolumn];
        String line = fin.readLine();

        for(int i=0;i<nrow;i++){
            Arrays.fill(data[i],0);
        }

        while (line != null) {
            String[] strs = line.split("\t");
            int segmentId = Integer.parseInt(strs[2]);
            int rowNum = (segmentId-1)*2+Integer.parseInt(strs[3]);
            int index = getIndex(strs[1]);
            data[rowNum][index] = Integer.parseInt(strs[5]);
            line = fin.readLine();
        }
        fin.close();
        return data;
    }

    /**
     * 将生成的矩阵重新恢复成源格式(  G15	2017/09/10 21:05:00	466	0	83.7	139)
     * @param matrix
     */
    public void regenerate(double[][] matrix,String filename){

        // 转置变成行是某个时间片下所有路段的情况，
        double[][] matrixTrans = (new DoubleMatrix(matrix).transpose()).toArray2();
        int nSnapshot= matrixTrans.length;
        int nSegment = matrixTrans[0].length;

        FileUtil fout = new FileUtil(baseurlOut+filename);

        for(int i=0;i<nSnapshot;i++){
            String time = getTime(i);
            for(int j=0;j<nSegment;j++){
                if(matrixTrans[i][j]==0)
                    continue;
                String str = "";
                int direction = j%2;
                int segmentId = (j-direction)/2+1;
                str=str+roadId+"\t"+time+"\t"+segmentId+"\t"+direction+"\t"+matrixTrans[i][j];
                fout.writeLine(str);
            }
        }
        fout.close();
    }


    public String getTime(int index){
        return df.format(TimeFormat.parse(startTimeStr).getTime()+timeInterval*index);
    }

    /**
     * 根据时间的String获得处于第几个时间片
     * @param time
     * @return
     */
    public int getIndex(String time) {
        return (int) (TimeFormat.parse(time).getTime() - TimeFormat.parse(startTimeStr).getTime()) / (1000 * 60 * 5);
    }

    public double[][] input(String filename){
        FileUtil fin = new FileUtil(baseurlOut+filename);
        String line = fin.readLine();
        List<List<Double>> list = new ArrayList<>();
        while(line!=null){
            String[] strs = line.split("\t");
            List<Double> tmp = new ArrayList();
            for(int i=0;i<strs.length;i++){
                tmp.add(Double.parseDouble(strs[i]));
            }
            list.add(tmp);
            line = fin.readLine();
        }

        //将list手动转化成matrix
        int m = list.size();
        int n = list.get(0).size();
        double[][] res = new double[m][n];
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++)
                res[i][j]=list.get(i).get(j);
        }
        return res;

    }

    public void output(double[][] matrix, String filename) {
        FileUtil fout = new FileUtil(baseurlOut + filename);
        String line = "";
        int m = matrix.length;
        int n = matrix[0].length;
        for(int i=0;i<m;i++){
            line="";
            for(int j=0;j<n;j++){
                line+= Double.toString(matrix[i][j]);
                line+="\t";
            }
            fout.writeLine(line);
        }
        fout.close();
    }

    public static void main(String[] args) {
        ReformatData rd = new ReformatData();
        double[][] matrix=rd.input("output_smooth.txt");
        rd.regenerate(matrix,"smooth_data.txt");
//        double[][] matrix2 = rd.serializeByTime("G15_20170910.txt");
//        rd.output(matrix2,"output.txt");
    }
}
