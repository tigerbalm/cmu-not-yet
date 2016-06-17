package com.lge.notyet.attendant.ui;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.manager.SessionManager;
import com.lge.notyet.attendant.manager.Slot;
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

    private AtomicBoolean mSlotStatusUpdateThreadStarted = new AtomicBoolean(false);
    ScheduledFuture<?> mSlotStatusUpdateThread = null;


    @Override
    public void initScreen() {

        if (mUpdateSlotStatusSubscribeChannel == null) {
            // TODO: NEED TO USE CONTROLLER ID HERE
            mUpdateSlotStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateSlotStatusSubscribeChannel(1 /* TEST*/);
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

            /*
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
            */

            // Reserved Time Based [START]

            JLabel strReservedFrom = new JLabel("Reserved Time");
            strReservedFrom.setHorizontalAlignment(SwingConstants.CENTER);

            Calendar reservedTime = Calendar.getInstance();
            reservedTime.setTimeInMillis(slot.getOccupiedTimeStamp()*1000);
            reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm, MM/dd/yy");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            String reservedTimeString = sdf.format(reservedTime.getTime());

            JLabel reservedTimeL = new JLabel(reservedTimeString);
            reservedTimeL.setHorizontalAlignment(SwingConstants.CENTER);





            JLabel strOccupiedFrom = new JLabel("Occupying Time");
            strOccupiedFrom.setHorizontalAlignment(SwingConstants.CENTER);

            long now = Calendar.getInstance().getTimeInMillis()/1000;
            long occupiedTimeSec = now - slot.getOccupiedTimeStamp();

            JLabel strOccupiedTime = new JLabel(occupiedTimeSec / 60 + " minute(s)");
            strOccupiedTime.setHorizontalAlignment(SwingConstants.CENTER);



            // Reserved Time Based [END]

            JPanel slotPanel = new JPanel(new GridLayout(0, 1));
            slotPanel.add(slotNumber);
            slotPanel.add(new JSeparator());

            if(slot.isOccupied()) {
                slotPanel.add(strOccupiedFrom);
                slotPanel.add(strOccupiedTime);
            } else if(slot.isReserved()) {
                slotPanel.add(strReservedFrom);
                slotPanel.add(reservedTimeL);
            } else {
                slotPanel.add(new JLabel());
                slotPanel.add(new JLabel());
            }

            slotPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            if(slot.isOccupied()) {
                slotPanel.setBackground(new Color(24, 27, 143));
                slotNumber.setForeground(Color.white);
                strOccupiedFrom.setForeground(Color.white);
                strOccupiedTime.setForeground(Color.white);
            } else if(slot.isReserved()) {
                slotPanel.setBackground(new Color(255, 191, 245));
                slotNumber.setForeground(new Color(24, 27, 143));
                strReservedFrom.setForeground(new Color(24, 27, 143));
                reservedTimeL.setForeground(new Color(24, 27, 143));
            }
            center.add(slotPanel);
        }

        mSpSlotStatus.getViewport().removeAll();
        mSpSlotStatus.getViewport().add(center, null);

        if (mSlotStatusUpdateThreadStarted.get() == false) {
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

        if (mSlotStatusUpdateThreadStarted.get() == true) {
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

    private IOnNotify mSlotStatusChanged = new IOnNotify() {

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

            System.out.println("mSlotStatusChanged Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

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
                    initScreen();
                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }
            }
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
