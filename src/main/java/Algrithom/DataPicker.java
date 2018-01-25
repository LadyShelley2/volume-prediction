package Algrithom;

import Util.FileUtil;
import Util.TimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据格式说明：路网中所有路段每个时间片存入一个文件中
 * 字段：RoadId,Date,Time,SegmentId,Direction,Speed,Volume(relative)
 */
class DataPicker {
    private String base_url = "D:\\data\\";

    private List<String> nodes = new ArrayList<String>();
    private double[][] weights;
    private static Map<String, Integer> segments = new HashMap<String, Integer>() {
        {
			put("G15", 741);
//			put("S35", 118);
//			put("G25", 305);
//			put("G70", 213);
//			put("G72", 225);
//            put("G76", 234);
//            put("G1501", 68);

            //另外两条高速数据异常
        }
    };

    private List<String> roadsNames;
    private List<Integer> roadsSegmentCounts;
    private int totalCount;
    private List<double[][]> snapshots;

    private int timeSlot = 5 * 60;
    private int beginDate = 20161001, endDate = 20161002;
    private String beginTimeString = " 08:35:00", endTimeString = " 09:00:00" /*endTimeString=" 24:00:00"*/;
    private DateFormat df; // date format of txt datas;
    private DateFormat dfFilename; // generate name of output file by time



    DataPicker() {

        // put road names and segment counts in two lists;
        roadsNames = segments.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        roadsSegmentCounts = segments.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

        totalCount = roadsSegmentCounts.stream().reduce(0, (acc, a) -> acc + a);

        df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dfFilename = new SimpleDateFormat("yyyyMMddHHmm");

        snapshots = new ArrayList<>();

    }

    public double[][] getW(){
        double[][] res = new double[totalCount][totalCount];
        for(int i=0;i<totalCount-1;i++)
            res[i][i+1]=1;
        return res;
    }

    public double[][] getD(double[][] W){
        double[][] res = new double[W.length][W[0].length];
        for(int i=0;i<W.length;i++){
            res[i][i]=Arrays.stream(W[i]).reduce(0,(a,b)->a+b);
        }
        return res;
    }

    public List<double[][]> getSnapshots(boolean output){
        generateSnapshots(output);
        return this.snapshots;
    }

    /**
     * 数据格式：每个文件为一天的数据，数据内部按照由早到晚排序，每一时间片包含所有的道路信息。
     * @param output
     */
    private void generateSnapshots(boolean output){
        double[][] snapshot = new double[totalCount][totalCount];

        for(int i=beginDate;i<endDate;i++){
            long beginTime = TimeFormat.parse(i+beginTimeString).getTime()/1000;
            long endTime = TimeFormat.parse(i+endTimeString).getTime()/1000;
            FileUtil fin = new FileUtil(base_url+i+".txt");

            //读取该时间片的所有数据，并生成矩阵。
            for(long t=beginTime;t<endTime;t+=timeSlot){

                for(int index = 0;index<totalCount;index++)
                    Arrays.fill(snapshot[index],0.0);

                //生成一个时间片的网络快照，并添加至list中
                generateSnapshot(fin, df.format(new Date(t*1000)),snapshot);
                snapshots.add(snapshot);

                if(output) {
                    System.out.print("Start outputing");
                    FileUtil fout = new FileUtil(base_url + dfFilename.format(new Date(t*1000)) + "out.txt");
                    output(snapshot, fout);
                }
            }
            fin.close();
        }
    }

    private void generateSnapshot(FileUtil fin, String time,double[][] graph){
        String line = fin.readLine();
        System.out.println("Reading");

        while(line!=null&&!line.contains(time)){
            line = fin.readLine();
        }

        while(line!=null&&line.contains(time)){
            String[] strs = line.split("\t");
            int index = findIndex(strs[0],strs[2]);
            if(index!=-1){
                graph[index][index-1] = Integer.parseInt(strs[5]);//strs[5]:relative volume;
            }
            line = fin.readLine();
        }
    }


    /**
     * write graph adjacency matrix into txt file
     *
     * @param graph
     * @param fout
     */
    public void output(double[][] graph, FileUtil fout) {
        int rowCount = graph.length;
        int columnCount = graph[0].length;

        String line = "";
        for (int i = 0; i < rowCount; i++) {
            line = "";
            for (int j = 0; j < columnCount; j++) {
                line += String.valueOf(graph[i][j]);
                line += "\t";
            }
            fout.writeLine(line);
        }
        fout.close();
    }

    /**
     * find node index in graph adjacency matrix by road id and segment id
     *
     * @param roadId
     * @param segmentId
     * @return node index. insert weight in G[index][index-1]
     */
    private int findIndex(String roadId, String segmentId) {
        int roadIndex = roadsNames.indexOf(roadId);
        // if segmentId is larger than its segment count, invalid data;
        if (Integer.parseInt(segmentId) > roadsSegmentCounts.get(roadIndex))
            return -1;

        int index = 0;
        for (int i = 0; i < roadIndex; i++) {
            index += roadsSegmentCounts.get(i);
        }
        index += Integer.parseInt(segmentId);
        return index;
    }

}