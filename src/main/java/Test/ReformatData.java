package Test;

import Util.FileUtil;
import Util.TimeFormat;

import java.util.Arrays;

public class ReformatData {
    private String baseurl = "D:\\Github\\matrix-fac\\lsm-rn\\visulization\\";
    private String baseurlOut = "D:\\Github\\matrix-fac\\lsm-rn\\visulization\\";
    private int nrow = 741*2;
    private int ncolumn = 12* 24;
    private String startTimeStr = "2017/09/10 00:05:00";

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

        int currSegmentId = 1;
        boolean direction = false; // direction 为0
        for (int i = 0; i < nrow; i++) {
            Arrays.fill(data[i], 0);
            int index;

            while (line != null) {
                String[] strs = line.split("\t");
                if(Integer.parseInt(strs[2]) != currSegmentId ){
                    currSegmentId++;
                    break;
                }
                if(Boolean.parseBoolean(strs[3])!=direction){
                    direction=!direction;
                    break;
                }
                index = getIndex(strs[1]);
                System.out.println(index);
                data[i][index] = Integer.parseInt(strs[5]);
                line = fin.readLine();
            }
        }
        fin.close();
        return data;
    }

    public int getIndex(String time) {
        return (int) (TimeFormat.parse(time).getTime() - TimeFormat.parse(startTimeStr).getTime()) / (1000 * 60 * 5);
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
//        System.out.println(rd.getIndex("2017/9/11 00:00:00"));
        double[][] matrix = rd.serializeByTime("reformatedData.txt");
        rd.output(matrix,"output.txt");
    }
}
