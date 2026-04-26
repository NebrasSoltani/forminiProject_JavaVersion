package tn.formini.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    String url ="jdbc:mysql://localhost:3306/formini_db";
    String user="root";
    String pwd="";
    private Connection cnx;
    static MyDataBase  MyDB;
    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(url, user, pwd);
            if (cnx != null) {
                System.out.println("cnx etablie !!!");
            } else {
                System.out.println("Erreur : La connexion a retourné null.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Driver non trouvé : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de la connexion : " + e.getMessage());
        }
    }
    public static MyDataBase getInstance(){

        if(MyDB==null){
            MyDB=new MyDataBase();
        }
        return MyDB;
    }

    public Connection getCnx() {
        return cnx;
    }

    public void setCnx(Connection cnx) {
        this.cnx = cnx;
    }
}
