package com.lge.notyet.owner.business;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class StateMachine {
    public static ArrayList<AbstractMap.SimpleEntry<String, String>> QUERY_LIST= new ArrayList();
    {
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Average Occupancy (in hours)", "select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day (Oops the slot count is not considered!!!)' from transaction"));//select datediff(from_unixtime(1465963300), from_unixtime(1465963199)), from_unixtime(1465963300),from_unixtime(1465963199)
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Peak Usage Hours", "select @NUM2+1 as 'From time of the day', @NUM2:=t2 as 'To time of the day', truncate(c3, 0) as Usages from (\n" +
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
        QUERY_LIST.add(new AbstractMap.SimpleEntry("How much time cars were parked in each slot", "SELECT slot_id, round(sum(TIMESTAMPDIFF(MINUTE, from_unixtime(begin_ts), from_unixtime(end_ts)))/60, 1)  FROM transaction, reservation where reservation.id=transaction.reservation_id group by slot_id"));
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Revenue based on facility", "SELECT sum(revenue) 'Revenue in Dollars' FROM transaction;"));
    }

    private static StateMachine ourInstance = new StateMachine();

    public static States getInternalState() {
        return internalState;
    }

    public static void setInternalState(States internalState) {
        StateMachine.internalState = internalState;
    }

    private static States internalState= States.MAINUI;

    private String query= null;
    public String getQuery() {
        if(customQuery){
            return query+" custom query selected";
        }
        else{
            return query;
        }
    }

    private String sqlQuery= null;
    public String getSqlQuery() {
        return sqlQuery;
    }

    private static boolean customQuery= false;

    public static StateMachine getInstance() {
        return ourInstance;
    }

    private StateMachine() {
    }

    public void setQuery(String selectedQuery, boolean customSelected) {
        setInternalState(States.SUBUI);
        query= selectedQuery;
        for (AbstractMap.SimpleEntry queryMapping:QUERY_LIST) {
            if(queryMapping.getKey().equals(selectedQuery)){
                sqlQuery= queryMapping.getValue().toString();
            }
        }
    }


    /**
     * Created by gladvin.durai on 15-Jun-2016.
     */
    public static enum States {
        MAINUI,
        SUBUI,
        QUERY_SERVER
    }
}
