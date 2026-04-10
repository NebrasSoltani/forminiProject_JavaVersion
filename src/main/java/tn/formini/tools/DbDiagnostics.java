package tn.formini.tools;

import java.sql.*;

public class DbDiagnostics {
    public static void main(String[] args) {
        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            System.out.println("Connection failed");
            return;
        }
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "blog", null);
            System.out.println("Columns in 'blog' table:");
            while (columns.next()) {
                System.out.println("- " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
