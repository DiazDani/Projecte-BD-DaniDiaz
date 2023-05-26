package Model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class connection {
    public static Connection getConnection() throws SQLException {

        Connection conn = null;

        try (FileInputStream f = new FileInputStream("C:\\Users\\ddiaz\\IdeaProjects\\proyectoMagdonal\\src\\Model\\connection.properties")) {


            Properties pros = new Properties();
            pros.load(f);


            String url = pros.getProperty("url");
            String user = pros.getProperty("user");
            String password = pros.getProperty("password");


            conn = DriverManager.getConnection(url, user, password);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
