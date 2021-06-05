import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;


class main {
  public static void main(String args[]) {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection conn = null;
      conn = DriverManager.getConnection("jdbc:mysql://localhost/Project?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=EST","root", "");
      System.out.print("Database is connected !");
      conn.close();
    }
    catch(Exception e) {
      System.out.print("Do not connect to DB - Error:"+e);
    }
  }
}

