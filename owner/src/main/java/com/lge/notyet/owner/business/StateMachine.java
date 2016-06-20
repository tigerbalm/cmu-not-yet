package com.lge.notyet.owner.business;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class StateMachine {

    private static StateMachine ourInstance = new StateMachine();

    public States getInternalState() {
        return internalState;
    }

    public void setInternalState(States internalState) {
        this.internalState = internalState;
    }

    private States internalState= States.MAINUI;

    private String queryId=Query.getDefaultQueryId();

    private boolean isCustomSQLQuery= false;

    public static StateMachine getInstance() {
        return ourInstance;
    }

    private StateMachine() {
    }

    public void setQuery(String queryId, boolean customSelected) {
        setInternalState(States.SUBUI);
        this.queryId= queryId;
        this.isCustomSQLQuery= customSelected;
        if(this.isCustomSQLQuery==true){
            getQueryInstance().setSQLQuery(Query.getInstance(queryId, false).getSqlQuery());
        }
    }

    public GenericQueryHandler getQueryInstance(){
        return Query.getInstance(queryId, isCustomSQLQuery);
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
