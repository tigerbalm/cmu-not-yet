package com.lge.notyet.attendant.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.manager.ScreenManager;
import com.lge.notyet.attendant.manager.SessionManager;
import com.lge.notyet.attendant.manager.Slot;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.UpdateSlotStatusSubscribeChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class FacilityMonitorPanel {
    private JPanel mForm;
    private JLabel mLabelFacilityName;
    private JScrollPane mSpSlotStatus;

    UpdateSlotStatusSubscribeChannel mUpdateSlotStatusSubscribeChannel = null;

    public void init() {

        if (mUpdateSlotStatusSubscribeChannel == null) {
            // TODO: NEED TO USE CONTROLLER ID HERE
            mUpdateSlotStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateSlotStatusSubscribeChannel(1 /* TEST*/);
            mUpdateSlotStatusSubscribeChannel.listen();
            mUpdateSlotStatusSubscribeChannel.addObserver(mSlotStatusChanged);
        }

        mLabelFacilityName.setText(SessionManager.getInstance().getFacilityName());


        Set<Integer> slotIds = SessionManager.getInstance().getSlotIds();
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 2, 5, 5));
        for (int slotId : slotIds) {

            Slot slot = SessionManager.getInstance().getSlot(slotId);

            JLabel slotNumber = new JLabel(slot.getNumber() + "");
            slotNumber.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel strReservedFrom = new JLabel("Reserved From");
            strReservedFrom.setHorizontalAlignment(SwingConstants.CENTER);

            Calendar reservedTime = Calendar.getInstance();
            reservedTime.setTimeInMillis(slot.getOccupiedTimeStamp()*1000);
            reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm, MM/dd/yy");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            String reservedTimeString = sdf.format(reservedTime.getTime());

            JLabel reservedTimeL = new JLabel(reservedTimeString);
            reservedTimeL.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel slotPanel = new JPanel(new GridLayout(0, 1));
            slotPanel.add(slotNumber);
            slotPanel.add(new JSeparator());
            slotPanel.add(strReservedFrom);
            slotPanel.add(reservedTimeL);

            slotPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            if(slot.isOccupied()) {
                slotPanel.setBackground(new Color(24, 27, 143));
                slotNumber.setForeground(Color.white);
                strReservedFrom.setForeground(Color.white);
                reservedTimeL.setForeground(Color.white);
            }
            center.add(slotPanel);
        }

        mSpSlotStatus.getViewport().removeAll();
        mSpSlotStatus.getViewport().add(center, null);
    }


    // Business Logic here, we have no time :(
    private IOnNotify mSlotStatusChanged = new IOnNotify() {

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

            System.out.println("mSlotStatusChanged Result=" + notificationMessage.getMessage());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");
            if (topicTokenizer.countTokens() == 4) {
                topicTokenizer.nextToken();
                topicTokenizer.nextToken();
                topicTokenizer.nextToken();
                try {
                    int slotId = Integer.parseInt(topicTokenizer.nextToken());
                    int occupied = notificationMessage.getMessage().get("occupied").asInt();
                    Slot slot = SessionManager.getInstance().getSlot(slotId);
                    if (slot != null) {
                        slot.setOccupied(occupied == 1);
                    }
                    init();
                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }
            }
        }
    };
    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "FacilityMonitorPanel";
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        mSpSlotStatus = new JScrollPane(new JPanel());

    }
}
