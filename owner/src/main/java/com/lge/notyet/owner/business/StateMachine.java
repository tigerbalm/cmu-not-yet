package com.lge.notyet.owner.business;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class StateMachine {
    public static ArrayList<AbstractMap.SimpleEntry<String, String>> QUERY_LIST= new ArrayList();
    {
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Average Occupancy (in hours)", "select sum(end_ts-begin_ts) from transaction"));
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Peak Usage Hours", "select custId from custTable"));
        QUERY_LIST.add(new AbstractMap.SimpleEntry("How much time cars were parked in each slot", "select custId from custTable"));
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Revenue based on facility", "select custId from custTable"));
        QUERY_LIST.add(new AbstractMap.SimpleEntry("Show transaction DB", "SELECT * FROM transaction"));
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
