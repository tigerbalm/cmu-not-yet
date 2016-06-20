package com.lge.notyet.owner.business;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class Query {
    public static final String CUSTOM_QUERY= "Custom (Additional developer query)";

    private String displayString;
    protected String[] columnNames;
    protected String sqlQueryString;
    private static ArrayList<GenericQueryHandler> queryList= new ArrayList<GenericQueryHandler>();
    private static GenericQueryHandler customQuery= null;


   public Query(String displayString, String[] columnNames, String sqlQueryString) {
        this.displayString= displayString;
        this.columnNames= columnNames;
        this.sqlQueryString= sqlQueryString;
    }

    static
    {
        queryList.add(new GenericQueryHandler(
                "Average Occupancy (in hours)",
                new String[]{"First Day", "Last Day", "Hours Occupied", "Occupancy in hours per day"},
                "select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day (Oops the slot count is not considered!!!)' from transaction"));//select datediff(from_unixtime(1465963300), from_unixtime(1465963199)), from_unixtime(1465963300),from_unixtime(1465963199)
        queryList.add(new GenericQueryHandler(
                "Peak Usage Hours",
                new String[]{},
                "select @NUM2+1 as 'From time of the day', @NUM2:=t2 as 'To time of the day', truncate(c3, 0) as Usages from (\n" +
                    "select t1 as t2, truncate(@FULLDAYS + (@NUM:= (c2+@NUM)), 0) as c3 from (\n" +
                    "select t1, sum(c1) as c2 from (\n" +
                    "SELECT (hour(from_unixtime(begin_ts))-1) as t1, (-count(*)) as c1  FROM transaction group by hour(from_unixtime(begin_ts)) \n" +
                    "union all\n" +
                    "SELECT hour(from_unixtime(end_ts)), (count(*))  FROM transaction group by hour(from_unixtime(end_ts)) \n" +
                    ") as combinedTable, (SELECT @NUM := 0, @FULLDAYS := sum(datediff(from_unixtime(end_ts), from_unixtime(begin_ts))) from transaction) r\n" +
                    "group by t1\n" +
                    "order by t1 desc\n" +
                    ")as doubleCombined\n" +
                    "union\n" +
                    "select 23, @FULLDAYS\n" +
                    ")as fourthCombined, (select @NUM2 :=-1) as r2\n" +
                    "order by t2\n"));//SELECT @row := @row + 1 as row, t.* FROM transaction t, (SELECT @row := 0) r         //SELECT @NUM:=@NUM+1, id FROM transaction
        queryList.add(new GenericQueryHandler(
                "How much time cars were parked in each slot",
                new String[]{"slot_id", "Hours parked in the particular slot"},
                "SELECT slot_id, round(sum(TIMESTAMPDIFF(MINUTE, from_unixtime(begin_ts), from_unixtime(end_ts)))/60, 1)  FROM transaction, reservation where reservation.id=transaction.reservation_id group by slot_id"));
        queryList.add(new GenericQueryHandler(
                "Revenue based on facility",
                new String[]{"Revenue in dollars"},
                "SELECT sum(revenue) 'Revenue in Dollars' FROM transaction;"));
        customQuery = new GenericQueryHandler(
                CUSTOM_QUERY,
                new String[]{"It is invalid, need to fetch from db query"},
                "It is invalid now, will be filled by user in text area") {
            JTextArea customQuery = new JTextArea();
            JScrollPane textScrollPane = new JScrollPane(customQuery);
            {
                //customQuery.set
            }

            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                customQuery.setText(sqlQueryString);
                chooseMoreSettingsPanel.add(textScrollPane);
                chooseMoreSettingsPanel.revalidate();
            }

            @Override
            public String getSqlQuery() {
                sqlQueryString= customQuery.getText();
                return super.getSqlQuery();
            }
        };
    }

    public static GenericQueryHandler getInstance(String queryId, boolean isCustomSQLQuery){
        if(isCustomSQLQuery)
            return customQuery;
        else
            return queryList.get(Integer.parseInt(queryId));

    }

    public static String[] getQueryIdList() {
        String[] returnValue= new String[queryList.size()];
        for (int i=0; i< queryList.size(); i++ ) {
            returnValue[i]= ""+i;
        }
        return returnValue;
    }

    public static String getDefaultQueryId() {
        return "0";
    }

    public String getDisplayString() {
        return displayString;
    }

    public String[] getColumnNames() {
        return columnNames;
    }
    public String getSqlQuery() {
        return sqlQueryString;
    }

    public void setSQLQuery(String sqlQueryString) {
        this.sqlQueryString= sqlQueryString;
    }
}
