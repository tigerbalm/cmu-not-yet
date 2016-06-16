package com.lge.notyet.owner.business;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import javax.swing.*;

/**
 * Created by gladvin.durai on 16-Jun-2016.
 */
public class GenericTextResultHandler {
    private static GenericTextResultHandler ourInstance = new GenericTextResultHandler();

    public static GenericTextResultHandler getInstance() {
        return ourInstance;
    }

    private GenericTextResultHandler() {
    }
    public static void handleResult(JTextArea resultArea, JsonValue resultSetTable) {
        String[] colNames= StateMachine.getInstance().getColumnNames();
        JsonArray resultTable = (JsonArray) resultSetTable;
        StringBuilder result= new StringBuilder("");
        if(resultTable.size()==1) {
            for (JsonValue resultRow : resultTable) {
                int i=0;
                JsonArray resultRow2 = (JsonArray) resultRow;
                for (JsonValue entry : resultRow2) {
                    if(i<colNames.length)
                        result.append(colNames[i]).append(":\t").append(entry.toString()).append("\r\n");
                    else
                        result.append(entry.toString()).append("\r\n");
                    i++;
                }
                result.append("------------\r\n");
            }
        }
        else if(resultTable.size()>1){
            for(String colName: colNames){
                result.append(colName).append("\t");
            }
            result.append("------------\r\n");
            for (JsonValue resultRow : resultTable) {
                JsonArray resultRow2 = (JsonArray) resultRow;
                for (JsonValue entry : resultRow2) {
                    result.append(entry.toString()).append("\t");
                }
                result.append("\r\n");
            }
            result.append("------------\r\n");
        }
        else{
            result.append("No result found. Sorry\r\n");
        }

        resultArea.setText(result.toString());
    }
}
