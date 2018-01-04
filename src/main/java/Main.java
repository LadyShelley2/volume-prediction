import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        DataPicker dp =new DataPicker();
        dp.generateSnapshots();
    }
}


class DataPicker {
    private String base_url = "E:\\data\\";

    private List<String> nodes = new ArrayList<String>();
    private double[][] weights;
    private static Map<String, Integer> segments = new HashMap<String, Integer>() {
        {
//			put("G15", 741);
//			put("S35", 118);
//			put("G25", 305);
//			put("G70", 213);
//			put("G72", 225);
            put("G76", 234);
            put("G1501", 68);

            //另外两条高速数据异常
        }
    };

    private List<String> roadsNames;
    private List<Integer> roadsSegmentCounts;
    private int totalCount;

    private int timeSlot = 5 * 60;
    private int beginDate = 20161001, endDate = 20161002;
    private String beginTimeString = " 00:05:00", endTimeString = " 00:10:00" /*endTimeString=" 24:00:00"*/;
    private DateFormat df; // date format of txt datas;
    private DateFormat dfFilename; // generate name of output file by time


    DataPicker() {

        // put road names and segment counts in two lists;
        roadsNames = segments.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        roadsSegmentCounts = segments.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

        totalCount = roadsSegmentCounts.stream().reduce(0, (acc, a) -> acc + a);

        df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dfFilename = new SimpleDateFormat("yyyyMMddHHmmss");

    }

    public void generateSnapshots() {

        double[][] graph = new double[totalCount][totalCount];
        //按照日期循环访问
        for (int i = beginDate; i < endDate; i++) {

            long beginTime = TimeFormat.parse(i + beginTimeString).getTime() / 1000;
            long endTime = TimeFormat.parse(i + endTimeString).getTime() / 1000;

            //按照时间循环访问
            for (long t = beginTime; t < endTime; t += timeSlot) {

                String filename = dfFilename.format(new Date(t * 1000));

                for (int index = 0; index < totalCount; index++)
                    Arrays.fill(graph[index], 0.0);

                FileUtil fin = new FileUtil(base_url + filename + ".txt");
                generateSnapshot(fin, graph);

                System.out.print("Start outputing");

                FileUtil fout = new FileUtil(base_url + filename + "out.txt");
                output(graph, fout);
            }
        }
    }

    /**
     * generate a snapshot(one timeslot for road network)
     * @param fin
     * @param graph
     */
    private void generateSnapshot(FileUtil fin, double[][] graph) {
        String line = fin.readLine();
        System.out.print("Reading....");

        while (line != null) {
            String[] strs = line.split("\t");
            int index = findIndex(strs[0], strs[2]);
            if (index != -1)
                graph[index][index - 1] = Integer.parseInt(strs[5]);
            line = fin.readLine();
        }

        fin.close();
    }

    /**
     * write graph adjacency matrix into txt file
     *
     * @param graph
     * @param fout
     */
    private void output(double[][] graph, FileUtil fout) {
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