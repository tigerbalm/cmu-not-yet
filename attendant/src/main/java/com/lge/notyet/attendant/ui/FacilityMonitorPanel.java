package com.lge.notyet.attendant.ui;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FacilityMonitorPanel implements Screen {

    private static final String LOG_TAG = "FacilityMonitorPanel";

    private JPanel mForm;
    private JLabel mLabelFacilityName;
    private JScrollPane mSpSlotStatus;

    private UpdateSlotStatusSubscribeChannel mUpdateSlotStatusSubscribeChannel = null;

    private final AtomicBoolean mSlotStatusUpdateThreadStarted = new AtomicBoolean(false);
    private ScheduledFuture<?> mSlotStatusUpdateThread = null;

    @Override
    public void initScreen() {

        if (mUpdateSlotStatusSubscribeChannel == null) {
            mUpdateSlotStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateSlotStatusSubscribeChannel();
            mUpdateSlotStatusSubscribeChannel.listen();
            mUpdateSlotStatusSubscribeChannel.addObserver(mSlotStatusChanged);
        }

        mLabelFacilityName.setText(SessionManager.getInstance().getFacilityName());

        Set<Integer> slotIds = SessionManager.getInstance().getSlotIds();
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 4, 5, 5));
        for (int slotId : slotIds) {

            Slot slot = SessionManager.getInstance().getSlot(slotId);

            JLabel slotNumber = new JLabel(slot.getControllerId() + "-" + slot.getNumber());
            slotNumber.setHorizontalAlignment(SwingConstants.CENTER);

            boolean isOccupied = slot.isOccupied();
            boolean isReserved = slot.isReserved();

            JLabel labelStatus;
            if (isOccupied) {
                labelStatus = new JLabel("Occupied");
                labelStatus.setForeground(Color.white);
            } else if (isReserved) {
                labelStatus = new JLabel("Reserved");
                labelStatus.setForeground(new Color(24, 27, 143));
            } else {
                labelStatus = new JLabel("Empty");
            }
            labelStatus.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel labelTime;
            if (isOccupied) {
                long now = Calendar.getInstance().getTimeInMillis()/1000;
                long occupiedTimeSec = now - slot.getOccupiedTimeStamp();
                labelTime = new JLabel(occupiedTimeSec / 60 + " min(s)");
                labelTime.setForeground(Color.white);
            } else if (isReserved) {
                // TODO: ADD this information
                /*
                Calendar reservedTime = Calendar.getInstance();
                reservedTime.setTimeInMillis(1466091160L * 1000);
                // reservedTime.setTimeInMillis(slot.getReservedTimeStamp() * 1000);
                reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                SimpleDateFormat dataFormat = new SimpleDateFormat("hh:mm, MM/dd/yy");
                dataFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                labelTime = new JLabel(dataFormat.format(reservedTime.getTime()));
                */
                labelTime = new JLabel();
                labelTime.setForeground(new Color(24, 27, 143));
            } else {
                labelTime = new JLabel();
            }
            labelTime.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel slotPanel = new JPanel(new GridLayout(0, 1));
            slotPanel.add(slotNumber);
            slotPanel.add(new JSeparator());
            slotPanel.add(labelStatus);
            slotPanel.add(labelTime);
            slotPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            if(isOccupied) {
                slotPanel.setBackground(new Color(24, 27, 143));
                slotNumber.setForeground(Color.white);
            } else if(isReserved) {
                slotPanel.setBackground(new Color(255, 191, 245));
                slotNumber.setForeground(new Color(24, 27, 143));
            }

            center.add(slotPanel);
        }

        mSpSlotStatus.getViewport().removeAll();
        mSpSlotStatus.getViewport().add(center, null);

        if (!mSlotStatusUpdateThreadStarted.get()) {
            mSlotStatusUpdateThreadStarted.set(true);
            scheduleSlotStatusUpdate();
        }
    }

    @Override
    public void disposeScreen() {

        if (mUpdateSlotStatusSubscribeChannel != null) {
            mUpdateSlotStatusSubscribeChannel.unlisten();
            mUpdateSlotStatusSubscribeChannel = null;
        }

        if (mSlotStatusUpdateThreadStarted.get()) {
            if (mSlotStatusUpdateThread != null) {
                mSlotStatusUpdateThread.cancel(true);
            }
            mSlotStatusUpdateThreadStarted.set(false);
        }
    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "FacilityMonitorPanel";
    }

    private void setUserInputEnabled(boolean enabled) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    private final IOnNotify mSlotStatusChanged = (networkChannel, uri, message) -> {

        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "mSlotStatusChanged Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

            if (topicTokenizer.countTokens() == 4) {

                try {

                    topicTokenizer.nextToken(); // skip "controller"
                    int physicalId = Integer.parseInt(topicTokenizer.nextToken());
                    topicTokenizer.nextToken(); // "slot"
                    int slotNumber = Integer.parseInt(topicTokenizer.nextToken());
                    int occupied = notificationMessage.getMessage().get("occupied").asInt();
                    Slot slot = SessionManager.getInstance().getSlot(physicalId, slotNumber);
                    if (slot != null) {
                        slot.setOccupied(occupied == 1);
                    }
                    initScreen();

                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }
            }
        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to parse notification message, exception occurred");
            e.printStackTrace();
        }
    };

    private static final int SLOT_STATUS_UPDATE_PERIOD = 10;
    private static final int SLOT_STATUS_UPDATE_MESSAGE_MAX = 3;
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(SLOT_STATUS_UPDATE_MESSAGE_MAX);

    private class SlotStatusUpdateThread implements Runnable {

        public void run() {
            initScreen();
        }
    }

    private void scheduleSlotStatusUpdate() {
        // TODO: Need to check Maximum Pended Requests?
        mSlotStatusUpdateThread = mScheduler.scheduleAtFixedRate(new SlotStatusUpdateThread(), SLOT_STATUS_UPDATE_PERIOD, SLOT_STATUS_UPDATE_PERIOD, SECONDS);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        mSpSlotStatus = new JScrollPane(new JPanel());

    }
}
