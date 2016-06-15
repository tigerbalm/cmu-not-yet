package com.lge.notyet.owner.business;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class Networking {
    private static Networking ourInstance = new Networking();

    public static Networking getInstance() {
        return ourInstance;
    }

    private Networking() {
    }

    public void queryServer(JTextArea callingUI, String query) {
      //  JOptionPane.showMessageDialog(callingUI, query);
        String result= DatabaseStub.getInstance().processQuery(query);
        callingUI.append(result);
    }


    public static class DatabaseStub {
        private static DatabaseStub ourInstance = new DatabaseStub();
        MysqlDataSource dataSource = new MysqlDataSource();

        public static DatabaseStub getInstance() {
            return ourInstance;
        }

        private DatabaseStub() {
            dataSource.setUser("admin");
            dataSource.setPassword("admin");
            dataSource.setServerName("si-gladvinc1");
            dataSource.setDatabaseName("surepark");
        }

        public String processQuery(String query) {
            StringBuilder result= new StringBuilder();
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData columns= rs.getMetaData();
                int columnCount= columns.getColumnCount();
                String columnNames[]= new String[columnCount];
                for(int i=1; i<= columnCount; i++){
                    columnNames[i-1]= columns.getColumnName(i);
                }
                while (rs.next()) {
                    result.append("________________\r\n");
                    for (int i=1;i<=columnCount;i++) {
                        result.append(columnNames[i-1]).append("\t").append(rs.getString(i)).append("\r\n");
                    }
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
    }
}
