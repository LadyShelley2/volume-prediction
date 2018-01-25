package Test;

import java.sql.*;

public class MySQLDemo {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    static final String USER = "root";
    static final String PASS = "888888";

    public static void main(String[] args){
        Connection con = null;
        Statement stmt = null;

        try{
            Class.forName("com.mysql.jdbc.Driver");

            System.out.println("打开链接");
            con=DriverManager.getConnection(DB_URL,USER,PASS);

            System.out.println("实例化Statement对");
            stmt=con.createStatement();

            String sql ="SELECT * from way";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                // 通过字段检索
                String roadid = rs.getString("roadid");


                // 输出数据
                System.out.print("road id: " + roadid);
                System.out.print("\n");
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        catch (Exception se){
            se.printStackTrace();
        }
        finally {
            System.out.println("Goodbye");
        }
    }
}
