package com.lge.notyet.owner.business;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by gladvin.durai on 16-Jun-2016.
 */
public class GenericQueryHandler extends Query{


    public GenericQueryHandler(String displayString, String[] columnNames, String sqlQueryString) {
        super(displayString, columnNames, sqlQueryString);
    }

    public void handleResult(JTextPane resultArea, JsonArray columnNamesJson, JsonArray resultSetTableJson) {
        String[] columnNames= new String[columnNamesJson.size()];
        StringBuilder result= new StringBuilder("");

        int i=0;
        for(JsonValue columnNameJson: columnNamesJson){
            columnNames[i]= columnNameJson.asString();
            i++;
        }
        if(resultSetTableJson.size()==1) {
            for (JsonValue resultRow : resultSetTableJson) {
                i=0;
                JsonArray resultRow2 = (JsonArray) resultRow;
                for (JsonValue entry : resultRow2) {
                    if(i<columnNames.length)
                        result.append(columnNames[i]).append(":\t").append(entry.toString()).append("\r\n");
                    else
                        result.append(entry.toString()).append("\r\n");
                    i++;
                }
                result.append("------------\r\n");
            }
        }
        else if(resultSetTableJson.size()>1){
            for(String colName: columnNames){
                result.append(colName).append("\t");
            }
            result.append("------------\r\n");
            for (JsonValue resultRow : resultSetTableJson) {
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

    DateFormat dateTimeFormat;

    JFormattedTextField startTime;
    JFormattedTextField endTime;
    {
        dateTimeFormat= new SimpleDateFormat("yyyy-MM-dd h:m:s a");//DateFormat.getDateTimeInstance();
         startTime= new JFormattedTextField(dateTimeFormat);
         endTime= new JFormattedTextField(dateTimeFormat);

    }
//    //DateFormat dateTimeFormat;
//    //DateTimeFormatter dateTimeFormatter;
//    DateFormatter dateTimeFormatter;
//    JFormattedTextField startTime;
//    JFormattedTextField endTime;
//    {
//        //dateTimeFormat= new SimpleDateFormat("yyyy-MM-dd h:m:s a");//DateFormat.getDateTimeInstance();
//        //dateTimeFormatter= new DateTimeFormatter(dateTimeFormat);
//        dateTimeFormatter= new DateFormatter(new SimpleDateFormat("yyyy-MM-dd h:m:s a"));
//        dateTimeFormatter.setValueClass(Calendar.class);
//        dateTimeFormatter.setAllowsInvalid(false);
//        dateTimeFormatter.setCommitsOnValidEdit(false);
//
//        JFormattedTextField startTime= new JFormattedTextField(dateTimeFormatter);
//        JFormattedTextField endTime= new JFormattedTextField(dateTimeFormatter);
//
//    }

    public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
        chooseMoreSettingsPanel.removeAll();
        chooseMoreSettingsPanel.setLayout(new GridLayout(0,2));
        chooseMoreSettingsPanel.add(new JLabel("Start date/time:"));
        startTime.setValue(Calendar.getInstance().getTime());
        ;
        chooseMoreSettingsPanel.add(startTime);
        chooseMoreSettingsPanel.add(new JLabel("End date/time:"));
        endTime.setValue(Calendar.getInstance().getTime());
        chooseMoreSettingsPanel.add(endTime);
        chooseMoreSettingsPanel.setEnabled(true);
        chooseMoreSettingsPanel.revalidate();
    }
}
