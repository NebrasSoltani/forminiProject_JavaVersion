package tn.formini.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    String url ="jdbc:mysql://localhost:3306/formation_db";
    String user="root";
    String pwd="";
    private Connection cnx;
    static MyDataBase  MyDB;
    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(url, user, pwd);
            System.out.println("cnx etablie !!!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
