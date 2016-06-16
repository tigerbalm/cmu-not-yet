package com.lge.notyet.owner.business;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class StateMachine {

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
        if(customSQLQuery!= null){
            return customSQLQuery;
        }
        else{
            return Query.getSqlQuery(queryId);
        }
    }

    private String queryId=Query.getDefaultQueryId();
    public String getSqlQuery() {
        return Query.getSqlQuery(queryId);
    }

    private static String customSQLQuery= null;

    public static StateMachine getInstance() {
        return ourInstance;
    }

    private StateMachine() {
    }

    public void setQuery(String queryId, boolean customSelected) {
        setInternalState(States.SUBUI);
        this.queryId= queryId;
        if(customSelected)
            customSQLQuery= Query.getSqlQuery(queryId);
        else
            customSQLQuery= null;
    }

    public String[] getColumnNames() {
        return Query.getColumnNames(queryId);
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
