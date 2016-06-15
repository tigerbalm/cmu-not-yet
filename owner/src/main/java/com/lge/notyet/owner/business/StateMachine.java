package com.lge.notyet.owner.business;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class StateMachine {
    private static final String[] QUERY_LIST= {
            "select custId from custTable",
            "select custId from custTable",
            "select custId from custTable",
            "select custId from custTable"
    } ;
    private static StateMachine ourInstance = new StateMachine();

    public static States getInternalState() {
        return internalState;
    }

    public static void setInternalState(States internalState) {
        StateMachine.internalState = internalState;
    }

    private static States internalState= States.MAINUI;

    public String getQuery() {
        return query;
    }

    private String query= null;
    private static boolean customQuery= false;

    public static StateMachine getInstance() {
        return ourInstance;
    }

    private StateMachine() {
    }

    public void setQuery(int selectedQuery, boolean customSelected) {
        setInternalState(StateMachine.States.MAINUI);
        query= QUERY_LIST[selectedQuery];
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
