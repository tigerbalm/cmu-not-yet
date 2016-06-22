package com.lge.notyet.attendant.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.attendant.business.GetSlotListTask;
import com.lge.notyet.attendant.business.ITaskDoneCallback;
import com.lge.notyet.attendant.business.RequestManualExitTask;
import com.lge.notyet.attendant.manager.*;
import com.lge.notyet.attendant.resource.Strings;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.ControllerErrorReportSubscribeChannel;
import com.lge.notyet.channels.ControllerStatusSubscribeChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private JLabel mLabelLogout;

    private ControllerStatusSubscribeChannel mControllerStatusSubscribeChannel = null;
    private ControllerErrorReportSubscribeChannel mControllerErrorReportSubscribeChannel = null;

    private final AtomicBoolean mSlotStatusUpdateThreadStarted = new AtomicBoolean(false);
    private ScheduledFuture<?> mSlotStatusUpdateThread = null;

    public FacilityMonitorPanel() {
        mLabelLogout.addMouseListener(new MouseAdapter() {

            // Logout
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                disposeScreen();
                SessionManager.getInstance().clear(); // Log-out
                // NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });
    }

    @Override
    public void initScreen() {

        if (mControllerStatusSubscribeChannel == null) {
            mControllerStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateControllerStatusSubscribeChannel();
            mControllerStatusSubscribeChannel.listen();
            mControllerStatusSubscribeChannel.addObserver(mControllerStatusChanged);
        }

        if (mControllerErrorReportSubscribeChannel == null) {
            mControllerErrorReportSubscribeChannel = NetworkConnectionManager.getInstance().createControllerErrorReportSubscribeChannel();
            mControllerErrorReportSubscribeChannel.listen();
            mControllerErrorReportSubscribeChannel.addObserver(mControllerErrorReported);
        }

        mLabelFacilityName.setText(SessionManager.getInstance().getFacilityName());

        Set<Integer> slotIds = SessionManager.getInstance().getSlotIds();
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 4, 5, 5));
        for (int slotId : slotIds) {

            Slot slot = SessionManager.getInstance().getSlot(slotId);
            Controller controller = SessionManager.getInstance().getController(slot.getControllerId());

            JLabel slotNumber = new JLabel(slot.getControllerId() + "-" + slot.getNumber());
            slotNumber.setHorizontalAlignment(SwingConstants.CENTER);
            slotNumber.setFont(new Font(null, 0, 16));

            boolean isControllerAvailable = controller != null && controller.isAvailable();
            boolean isOccupied = slot.isOccupied();
            boolean isReserved = slot.isReserved();

            JLabel availableStatus;
            if (!isControllerAvailable) {
                availableStatus = new JLabel("UNAVAILABLE");
                availableStatus.setForeground(Color.red);
                availableStatus.setHorizontalAlignment(SwingConstants.CENTER);
                availableStatus.setFont(new Font(null, Font.BOLD, 14));
            } else {
                availableStatus = new JLabel("");
            }

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
            labelStatus.setFont(new Font(null, 0, 16));

            JLabel labelTime;
            if (isOccupied) {
                long now = Calendar.getInstance().getTimeInMillis()/1000;
                long occupiedTimeSec = now - slot.getOccupiedTimeStamp();
                labelTime = new JLabel(occupiedTimeSec / 60 + " min(s)");
                labelTime.setForeground(Color.white);
            } else if (isReserved) {
                Calendar reservedTime = Calendar.getInstance();
                reservedTime.setTimeInMillis(slot.getReservedTimeStamp() * 1000);
                reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                SimpleDateFormat dataFormat = new SimpleDateFormat("hh:mm a, MM/dd/yy");
                dataFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                labelTime = new JLabel(dataFormat.format(reservedTime.getTime()));
                labelTime.setForeground(new Color(24, 27, 143));
            } else {
                labelTime = new JLabel();
            }
            labelTime.setHorizontalAlignment(SwingConstants.CENTER);
            labelTime.setFont(new Font(null, 0, 16));

            JLabel labelName;
            if (isOccupied) {
                labelName = new JLabel(slot.getReservedUserEmail());
                labelName.setForeground(Color.white);
            } else if (isReserved) {
                labelName = new JLabel(slot.getReservedUserEmail());
                labelName.setForeground(new Color(24, 27, 143));
            } else {
                labelName = new JLabel();
            }
            labelName.setHorizontalAlignment(SwingConstants.CENTER);
            labelName.setFont(new Font(null, 0, 16));

            JPanel slotPanel = new JPanel(new GridLayout(0, 1));
            slotPanel.add(slotNumber);
            slotPanel.add(availableStatus);
            slotPanel.add(new JSeparator());
            slotPanel.add(labelStatus);
            slotPanel.add(labelTime);
            slotPanel.add(labelName);
            slotPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            if(isOccupied) {
                slotPanel.setBackground(new Color(24, 27, 143));
                slotNumber.setForeground(Color.white);
            } else if(isReserved) {
                slotPanel.setBackground(new Color(255, 191, 245));
                slotNumber.setForeground(new Color(24, 27, 143));
            }

            if (isOccupied) {
                ManualExitEventListener manualExitEventListener = new ManualExitEventListener(slotId);
                slotPanel.addKeyListener(manualExitEventListener);
                slotPanel.addMouseListener(manualExitEventListener);
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

        if (mControllerStatusSubscribeChannel != null) {
            mControllerStatusSubscribeChannel.unlisten();
            mControllerStatusSubscribeChannel = null;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    private class ManualExitEventListener extends MouseAdapter implements KeyListener {

        final int mSlotId;
        ManualExitEventListener(int slotId) {
            mSlotId = slotId;
        }

        private void requestManualExit() {

            int chosen = JOptionPane.showConfirmDialog(getRootPanel(),
                    Strings.MANUAL_PAYMENT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.OK_CANCEL_OPTION);

            if (chosen == JOptionPane.OK_OPTION) {

                Slot slot = SessionManager.getInstance().getSlot(mSlotId);
                if (slot == null) {
                    Log.logd(LOG_TAG, "No such Slot, slot id=" + mSlotId);
                    return;
                }
                Controller controller = SessionManager.getInstance().getController(slot.getControllerId());
                if (controller == null) {
                    Log.logd(LOG_TAG, "No such Controller, controller id=" + slot.getControllerId());
                    return;
                }

                TaskManager.getInstance().runTask(RequestManualExitTask.getTask(
                        SessionManager.getInstance().getKey(),
                        controller.getPhysicalId(),
                        slot.getNumber(),
                        mManualExitTaskCallback));
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            requestManualExit();
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                requestManualExit();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private final ITaskDoneCallback mGetSlotListCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to get slot list due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_SLOT_LIST_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();

            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to GetSlotListRequest, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                SessionManager.getInstance().clearSlots();
                JsonArray slots = resMsg.getMessage().get("slots").asArray();

                for (JsonValue aSlot : slots.values()) {

                    JsonObject slot = aSlot.asObject();
                    Log.logv(LOG_TAG, "Slot=" + slot);

                    // mandatory fields
                    int id = slot.get("id").asInt();  // Slot's Unique ID
                    int number = slot.get("number").asInt();
                    int occupied = slot.get("parked").isNull() ? 0 : slot.get("parked").asInt();
                    long occupied_ts = slot.get("parked_ts").isNull() ? 0L : slot.get("parked_ts").asLong();
                    int reserved = slot.get("reserved").isNull() ? 0 : slot.get("reserved").asInt();
                    int controller_id = slot.get("controller_id").asInt();
                    String physical_id = slot.get("controller_physical_id").asString();
                    // optional fields
                    int reservation_id = slot.get("reservation_id").isNull() ? -1 : slot.get("reservation_id").asInt();
                    String user_email = slot.get("email").isNull() ? null : slot.get("email").asString();
                    long reservation_ts = slot.get("reservation_ts").isNull() ? -1 : slot.get("reservation_ts").asLong();
                    boolean is_controller_activated = slot.get("available").isNull() || (slot.get("available").asInt() != 0);

                    SessionManager.getInstance().addSlot(id, number, occupied == 1, reserved==1, occupied_ts, controller_id, physical_id, reservation_id, user_email, reservation_ts, is_controller_activated);
                }

                initScreen();

            } else if (success == 0) {
                Log.log(LOG_TAG, "Failed to get slot list, fail cause is " + resMsg.getMessage().get("cause").asString());

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.GET_SLOT_LIST_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();

            } else {

                Log.logd(LOG_TAG, "Failed to get slot list, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.GET_SLOT_LIST_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to get slot list, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_SLOT_LIST_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();

            SessionManager.getInstance().clear();
            ScreenManager.getInstance().showLoginScreen();
        }
    };


    private final ITaskDoneCallback mManualExitTaskCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to open exit gate manually due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MANUAL_EXIT_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();

            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to ManualExiRequest, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                TaskManager.getInstance().runTask(GetSlotListTask.getTask(
                        SessionManager.getInstance().getKey(),
                        SessionManager.getInstance().getFacilityId(),
                        mGetSlotListCallback));

            } else if (success == 0) {
                Log.log(LOG_TAG, "Failed to open exit gate manually, fail cause is " + resMsg.getMessage().get("cause").asString());

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.MANUAL_EXIT_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();

            } else {

                Log.logd(LOG_TAG, "Failed to open exit gate manually, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.MANUAL_EXIT_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to open exit gate manually, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MANUAL_EXIT_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();
        }
    };

    private final IOnNotify mControllerStatusChanged = (networkChannel, uri, message) -> {

        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "mControllerStatusChanged Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

            if (topicTokenizer.countTokens() == 2) {

                try {

                    topicTokenizer.nextToken(); // skip "controller"
                    String physicalId = topicTokenizer.nextToken();

                    Controller controller = SessionManager.getInstance().getController(physicalId);

                    if (controller != null) {

                        boolean isChanged = false;
                        if (notificationMessage.getMessage().get("available") != null &&
                                !notificationMessage.getMessage().get("available").isNull()) {
                            boolean available = notificationMessage.getMessage().get("available").asInt() == 1;
                            if (controller.isAvailable() != available) {
                                controller.setAvailable(available);
                                isChanged = true;
                            }
                        }

                        if (!isChanged &&
                                notificationMessage.getMessage().get("updated") != null &&
                                !notificationMessage.getMessage().get("updated").isNull()) {
                            isChanged = notificationMessage.getMessage().get("updated").asInt() == 1;
                        }

                        if (isChanged) {
                            TaskManager.getInstance().runTask(GetSlotListTask.getTask(
                                    SessionManager.getInstance().getKey(),
                                    SessionManager.getInstance().getFacilityId(),
                                    mGetSlotListCallback));

                            if (!controller.isAvailable()) {
                                // TODO : If time is enough, we will change it to modeless Dialog later to show only 1 dialog.
                                new Thread(() -> {
                                    JOptionPane.showMessageDialog(getRootPanel(),
                                            Strings.CONTROLLER_UNAVAILABLE + controller.getPhysicalId(),
                                            Strings.APPLICATION_NAME,
                                            JOptionPane.ERROR_MESSAGE);
                                }).start();
                            }
                        }
                    } else {
                        Log.logv(LOG_TAG, "No such controller in session, physicalId=" + physicalId);
                    }

                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }
            } else {
                Log.logd(LOG_TAG, "Wrong topic name on ControllerStatusChangedChannel, topic=" + uri.getLocation());
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to parse notification message, exception occurred");
            e.printStackTrace();
        }
    };


    private final IOnNotify mControllerErrorReported = (networkChannel, uri, message) -> {

        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "mControllerErrorReported Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

            if (topicTokenizer.countTokens() == 2) {

                try {

                    topicTokenizer.nextToken(); // skip "controller"
                    String physicalId = topicTokenizer.nextToken();

                    Controller controller = SessionManager.getInstance().getController(physicalId);

                    final String errorMessage;
                    if (notificationMessage.getMessage().get("message") != null &&
                            !notificationMessage.getMessage().get("message").isNull()) {
                        errorMessage = notificationMessage.getMessage().get("message").asString();
                    } else {
                        errorMessage = "";
                    }

                    if (controller != null) {
                        if (!controller.isAvailable()) {
                            // TODO : If time is enough, we will change it to modeless Dialog later to show only 1 dialog.
                            new Thread(() -> {
                                JOptionPane.showMessageDialog(getRootPanel(),
                                        Strings.CONTROLLER_ERROR_REPORT + controller.getPhysicalId() + "\n" + errorMessage,
                                        Strings.APPLICATION_NAME,
                                        JOptionPane.ERROR_MESSAGE);
                            }).start();

                        }
                    } else {
                        Log.logv(LOG_TAG, "No such controller in session, physicalId=" + physicalId);
                    }

                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }
            } else {
                Log.logd(LOG_TAG, "Wrong topic name on ControllerStatusChangedChannel, topic=" + uri.getLocation());
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
