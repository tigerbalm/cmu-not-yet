package com.lge.notyet.owner.business;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.owner.ui.MainUI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


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
                "Usage Times",
                new String[]{"From time of the day", "To time of the day", "Usages"},
                "select combinedTable.time1, sum(combinedTable.count1) from \n" +
                        "(\n" +
                        "select b as time1, -count(b) as count1 from (SELECT hour(from_unixtime(begin_ts)) as b  FROM transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility)) as begining group by begining.b\n" +
                        "union all\n" +
                        "select e, count(e) from (SELECT hour(from_unixtime(end_ts))+1 as e  FROM transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility)) as ending group by ending.e\n" +
                        ") as combinedTable group by combinedTable.time1\n" +
                        "union\n" +
                        "(SELECT 25,  sum(datediff(from_unixtime(end_ts), from_unixtime(begin_ts))) from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility))\n" +
                        "\n")
//        select combinedTable.time1, sum(combinedTable.count1) from
//            (
//                    select b as time1, -count(b) as count1 from (SELECT hour(from_unixtime(begin_ts)) as b  FROM transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (1,2,3,4,5)) as begining group by begining.b
//                    union all
//                    select e, count(e) from (SELECT hour(from_unixtime(end_ts))+1 as e  FROM transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (1,2,3,4,5)) as ending group by ending.e
//            ) as combinedTable group by combinedTable.time1
//            union
//        (SELECT 25,  sum(datediff(from_unixtime(end_ts), from_unixtime(begin_ts))) from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (1,2,3,4,5))
//
        {
            public boolean implementation2=true;
            DateFormat dateTimeFormat;

            JFormattedTextField startTime;
            JFormattedTextField endTime;
            {
                dateTimeFormat= new SimpleDateFormat("yyyy-MM-dd h:m:s a");//DateFormat.getDateTimeInstance();
                startTime= new JFormattedTextField(dateTimeFormat);
            }

            String[] timeInterval = { "Next 24 Hour", "Next 60 Minutes", "Next 60 Seconds"};
            JComboBox timeIntervalCombo = new JComboBox(timeInterval);
            {
                timeIntervalCombo.setSelectedItem("Next 60 Seconds");//Demo defaults
            }
            String dateformat=(implementation2?"yyyy-MM-dd h a":"%d/%m/%Y %H");
            long timeToIncrementInSeconds=3600;//1 hour
            int entriesToPrint=24;//show for next 24 times (==> 24 hours from the start date)
            long startingTime;
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
            ArrayList<Map.Entry<String, Integer>> reportData= new ArrayList<Map.Entry<String, Integer>>();
            @Override
            public void handleResult(JTextPane resultArea, JsonArray columnNamesJson, JsonArray resultSetTableJson) {
                StringBuilder result= new StringBuilder("");
                SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
                HashMap<String, String> timeToResultCount= new HashMap<String, String>();
                reportData.clear();

                for(JsonValue columnNameJson: columnNamesJson){
                    result.append(columnNameJson.asString()).append("\t\t");
                }
                result.append("\r\n-------------------------------------\r\n");
                if(!implementation2) {
                    for (JsonValue resultRow : resultSetTableJson) {
                        JsonArray resultRow2 = (JsonArray) resultRow;
                        for (JsonValue entry : resultRow2) {
                            result.append(entry.toString()).append("\t");
                        }
                        result.append("\r\n");
                    }
                }
                else{
                    for (JsonValue resultRow : resultSetTableJson) {
                        JsonArray resultRow2 = (JsonArray) resultRow;
                        timeToResultCount.put(resultRow2.get(0).toString(), resultRow2.get(1).toString());
                    }
                    long timeInSeconds= startingTime;
                    for(int i=0; i< entriesToPrint; i++) {
                        String timeEntry= ""+timeInSeconds;
                        String countEntries= timeToResultCount.get(timeEntry);
                        String formattedDateString= sdf.format(new Date(Long.parseLong(timeEntry)*1000L));
                        result.append(formattedDateString).append("\t");
                        if(countEntries!=null) {
                            result.append(countEntries);
                            reportData.add(new AbstractMap.SimpleEntry<String, Integer>(formattedDateString, new Integer(countEntries)));
                        }
                        else {
                            result.append(0).append("\t");
                            reportData.add(new AbstractMap.SimpleEntry<String, Integer>(formattedDateString, new Integer(0)));
                        }
                        timeInSeconds+=timeToIncrementInSeconds;
                        result.append("\r\n");
                    }
                }
                result.append("------------\r\n");

                resultArea.setText(result.toString());

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // This method is invoked on the EDT thread
                        JFXPanel fxPanel = new JFXPanel();
                        JPanel graphicalPanel= MainUI.getInstance().getGraphicalPane();
                        graphicalPanel.removeAll();
                        graphicalPanel.setLayout(new GridLayout(1,0));
                        graphicalPanel.add(fxPanel);
                        //graphicalPanel.setSize(300, 200);
                        //graphicalPanel.setVisible(true);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //private static void initFX(JFXPanel fxPanel)
                                // This method is invoked on the JavaFX thread
                                Scene scene = createScene();
                                fxPanel.setScene(scene);
                            }
                            private Scene createScene() {
                                Group root  =  new  Group();
                                Scene  scene  =  new  Scene(root, Color.LIGHTGREY);

                                final CategoryAxis xAxis = new CategoryAxis();
                                final NumberAxis yAxis = new NumberAxis();
                                final BarChart<String,Number> bc =
                                        new BarChart<String,Number>(xAxis,yAxis);

                                bc.getData().clear();
                                xAxis.setLabel("Time");
                                yAxis.setLabel("Usages");

                                XYChart.Series series1 = new XYChart.Series();
                                //series1.setName("Run 1");
                                for(Map.Entry<String, Integer> reportValue:reportData){
                                    series1.getData().add(new XYChart.Data(reportValue.getKey(), reportValue.getValue()));
                                }
                                reportData.clear();

                                //Scene scene  = new Scene(bc,800,600);
                                bc.getData().clear();
                                bc.getData().add(0, series1);

                                root.getChildren().add(bc);
                                MainUI.getInstance().getGraphicalPane().setEnabled(true);
                                MainUI.getInstance().getGraphicalPane().setVisible(true);
                                MainUI.getInstance().getGraphicalPane().revalidate();

                                //root.getChildren().add(text);

                                return (scene);
                            }
                        });
                        //graphicalPanel.setEnabled(true);
                      //  graphicalPanel.setVisible(true);
                        //graphicalPanel.revalidate();
                    }
                });
            }

            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setLayout(new GridLayout(0,2));
                chooseMoreSettingsPanel.add(new JLabel("Start date/time:"));
                try {
                    //startTime.setValue(Calendar.getInstance().getTime());

                    Calendar defaultTime = Calendar.getInstance();
                    defaultTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                    defaultTime.set(2016, Calendar.JUNE, 22, 16, 52, 2);
                    defaultTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                    startTime.setValue(defaultTime.getTime());//Demo defaults
                } catch (Exception e) {
                    e.printStackTrace();
                }
                chooseMoreSettingsPanel.add(startTime);

                chooseMoreSettingsPanel.add(new JLabel("Time Interval:"));
                chooseMoreSettingsPanel.add(timeIntervalCombo);

                chooseMoreSettingsPanel.setEnabled(true);
                chooseMoreSettingsPanel.revalidate();

            }

            @Override
            public String getSqlQuery() {
                try {
                    String sqlQuery= "SELECT DATE_FORMAT(from_unixtime(sampleTable.sampleTime), '%dateformat') as 'At time :', count(*)  FROM transaction, reservation, slot, controller, (%searchTimeArray) as sampleTable\n" +
                            "where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility) and sampleTime >= begin_ts and sampleTime <= end_ts \n" +
                            "group by sampleTable.sampleTime\n";
                    if(implementation2) {
                        sqlQuery = "SELECT sampleTable.sampleTime as 'Time', count(*) as 'Usages' FROM transaction, reservation, slot, controller, (%searchTimeArray) as sampleTable\n" +
                                "where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility) and sampleTime >= begin_ts and sampleTime <= end_ts \n" +
                                "group by sampleTable.sampleTime\n";
                    }
                    switch(timeIntervalCombo.getSelectedIndex()){
                        case 0://hourly
                            dateformat= (implementation2?"yyyy-MM-dd hh a":"%d/%m/%Y %H");
                            timeToIncrementInSeconds= 3600;
                            entriesToPrint= 24;
                            break;
                        case 1://minutes
                            dateformat= (implementation2?"yyyy-MM-dd h:m a":"%d/%m/%Y %H:%i");
                            timeToIncrementInSeconds= 60;
                            entriesToPrint= 60;
                            break;
                        case 2://seconds
                            dateformat= (implementation2?"yyyy-MM-dd h:m:s a":"%d/%m/%Y %H:%i:%s");
                            timeToIncrementInSeconds= 1;
                            entriesToPrint= 60;
                            break;
                    }

                    if(!implementation2)
                        sqlQuery= sqlQuery.replace("%dateformat", dateformat);
                    StringBuffer searchTimeArray= new StringBuffer();
                    startingTime= (dateTimeFormat.parse(startTime.getText()).getTime()/1000);
                    long timeInSeconds= startingTime;
                    for(int i=0; i< entriesToPrint; i++) {
                        if(i==0)
                            searchTimeArray.append("(select "+timeInSeconds+" as sampleTime)");
                        else
                            searchTimeArray.append(" union (select "+timeInSeconds+")");
                        timeInSeconds+=timeToIncrementInSeconds;
                    }
                    sqlQuery= sqlQuery.replace("%searchTimeArray", searchTimeArray);
                sqlQuery= sqlQuery.replace("%facility", MainUI.getFacilityList().toString());
                return sqlQuery;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return super.getSqlQuery();
                }
            }
        });

        queryList.add(new GenericQueryHandler(
                "Average Occupancy",
                new String[]{"First Day", "Last Day", "Hours Occupied", "Occupancy in hours per day"},
                "select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day' from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility)")
                //select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day' from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (1,2,3,4,5)
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
            chooseMoreSettingsPanel.removeAll();
            chooseMoreSettingsPanel.setEnabled(false);
            chooseMoreSettingsPanel.revalidate();
            MainUI.getInstance().getGraphicalPane().setEnabled(false);
            MainUI.getInstance().getGraphicalPane().setVisible(false);
            MainUI.getInstance().getGraphicalPane().revalidate();
        }

        });
        queryList.add(new GenericQueryHandler(
                "How much time cars were parked in each slot",
                new String[]{"slot_id", "Hours parked in the particular slot"},
                "SELECT facility.name as 'Facility Name', concat(slot.controller_id,'-', slot.number) as 'Controller Identification' , round(sum(TIMESTAMPDIFF(SECOND, from_unixtime(begin_ts), from_unixtime(end_ts)))/3600, 1) as 'Hours parked in the particular slot' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (%facility) group by slot.id")
                //SELECT facility.name as 'Facility Name', concat(slot.controller_id,'-', slot.number) as 'Controller Identification' , round(sum(TIMESTAMPDIFF(SECOND, from_unixtime(begin_ts), from_unixtime(end_ts)))/3600, 1) as 'Hours parked in the particular slot' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (1,2,3,4,5) group by slot.id
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setEnabled(false);
                chooseMoreSettingsPanel.revalidate();
                MainUI.getInstance().getGraphicalPane().setEnabled(false);
                MainUI.getInstance().getGraphicalPane().setVisible(false);
                MainUI.getInstance().getGraphicalPane().revalidate();
            }

        });
        queryList.add(new GenericQueryHandler(
                "Revenue based on facility",
                new String[]{"Revenue in dollars"},
                "SELECT facility.name as 'Facility Name', sum(revenue) 'Revenue in Dollars' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (%facility) group by facility.id")
                //SELECT facility.name as 'Facility Name', sum(revenue) 'Revenue in Dollars' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (1,2,3,4,5) group by facility.id
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setEnabled(false);
                chooseMoreSettingsPanel.revalidate();
                MainUI.getInstance().getGraphicalPane().setEnabled(false);
                MainUI.getInstance().getGraphicalPane().setVisible(false);
                MainUI.getInstance().getGraphicalPane().revalidate();
        }

        });

        customQuery = new GenericQueryHandler(
                CUSTOM_QUERY,
                new String[]{"It is invalid, need to fetch from db query"},
                "It is invalid now, will be filled by user in text area") {
            JTextArea customQuery = new JTextArea();
            JScrollPane textScrollPane = new JScrollPane(customQuery);

            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setLayout(new GridLayout(1,1));
                customQuery.setText(sqlQueryString);
                chooseMoreSettingsPanel.add(textScrollPane);
                chooseMoreSettingsPanel.setEnabled(true);
                chooseMoreSettingsPanel.revalidate();
                MainUI.getInstance().getGraphicalPane().setEnabled(false);
                MainUI.getInstance().getGraphicalPane().setVisible(false);
                MainUI.getInstance().getGraphicalPane().revalidate();
                MainUI.getInstance().revalidate();
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
        //return sqlQueryString;
        return sqlQueryString.replace("%facility", MainUI.getFacilityList().toString());
    }

    public void setSQLQuery(String sqlQueryString) {
        this.sqlQueryString= sqlQueryString;
    }
}
