package com.lge.notyet.driver.manager;

import com.lge.notyet.channels.UpdateControllerStatusSubscribeChannel;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.IOnNotify;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SessionManager {

    private final static String EMAIL = "email";
    private final static String KEY = "key";
    private final static String CREDIT_CARD_NUMBER = "credit_card_number";
    private final static String CREDIT_CARD_EXPIRE = "credit_card_expire";

    private final HashMap<String, String> mSession;

    private final HashMap<Integer, String> mFacilityList;

    // Reservation Information
    private long mReservedTime = 0L;
    private int mReservedConfirmationNumber = 0;
    private int mReservedFacilityId = 0;
    private int mReservedId = 0;
    private String mControllerPhysicalId = null;

    private static SessionManager sSessionManager = null;

    private SessionManager () {
        mSession = new HashMap<>();
        mFacilityList = new HashMap<>();
    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (sSessionManager == null) {
                sSessionManager = new SessionManager();
            }
        }
        return sSessionManager;
    }

    public String getUserEmail() {
        return mSession.get(EMAIL);
    }
    public String getKey() {
        return mSession.get(KEY);
    }
    public String getCreditCardNumber() {
        return mSession.get(CREDIT_CARD_NUMBER);
    }
    public String getCreditCardExpireDate() {
        return mSession.get(CREDIT_CARD_EXPIRE);
    }

    public void setUserEmail(String email) {
        mSession.put(EMAIL, email);
    }
    public void setKey(String key) {
        mSession.put(KEY, key);
    }
    public void setCreditCardNumber(String credit_card_number) {
        mSession.put(CREDIT_CARD_NUMBER, credit_card_number);
    }
    public void setCreditCardExpireDate(String credit_card_expire) {
        mSession.put(CREDIT_CARD_EXPIRE, credit_card_expire);
    }

    public void setReservationInformation(long reservedTime, int reservedConfirmationNumber, int reservedFacilityId, int reservationId, String controllerPhysicalId) {
        mReservedTime = reservedTime;
        mReservedConfirmationNumber = reservedConfirmationNumber;
        mReservedFacilityId = reservedFacilityId;
        mReservedId = reservationId;
        mControllerPhysicalId = controllerPhysicalId;

        startListenFacilityStatus();
    }

    public long getReservationTime() {
        return mReservedTime;
    }
    public int getReservationConfirmationNumber() {
        return mReservedConfirmationNumber;
    }
    public int getReservationFacilityId() {
        return mReservedFacilityId;
    }
    public int getReservationId() {
        return mReservedId;
    }
    public String getControllerPhysicalId() {
        return mControllerPhysicalId;
    }

    public void clearReservationInformation() {
        mReservedTime = 0L;
        mReservedConfirmationNumber = 0;
        mReservedFacilityId = 0;
        mReservedId = 0;
    }

    public void addFacility(int id, String name) {
        mFacilityList.put(id, name);
    }

    public String getFacilityName(int id) {
        return mFacilityList.get(id);
    }

    public int getFacilityId(String name) {
        for (int id : mFacilityList.keySet()) {
            if (mFacilityList.get(id).equals(name)) return id;
        }

        return -1;
    }

    public String getFacilityByIndex(int index) {
        int i = 0;
        for (int id : mFacilityList.keySet()) {
            if (i == index) return mFacilityList.get(id);
            i++;
        }
        return "";
    }

    public int getFacilitySize() {
        return mFacilityList.size();
    }

    public void clearFacility() {
        mFacilityList.clear();
    }

    public void clear() {
        mSession.clear();
        mFacilityList.clear();
        clearReservationInformation();
        stopListenFacilityStatus();
    }

    private boolean mControllerAvailable = true;
    private UpdateControllerStatusSubscribeChannel mUpdateControllerStatusSubscribeChannel = null;

    private void startListenFacilityStatus() {

        if (mUpdateControllerStatusSubscribeChannel == null) {
            mUpdateControllerStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateControllerStatusChannel();
            mUpdateControllerStatusSubscribeChannel.addObserver(mControllerStatusChanged);
            mUpdateControllerStatusSubscribeChannel.listen();
        }
    }

    private void stopListenFacilityStatus() {

        if (mUpdateControllerStatusSubscribeChannel != null) {
            mUpdateControllerStatusSubscribeChannel.unlisten();
            mUpdateControllerStatusSubscribeChannel.removeObserver(mControllerStatusChanged);
            mUpdateControllerStatusSubscribeChannel = null;
        }
    }

    private final IOnNotify mControllerStatusChanged = (networkChannel, uri, message) -> {

        String LOG_TAG = "mControllerStatusChanged";
        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "mControllerStatusChanged Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

            if (topicTokenizer.countTokens() == 2) {

                try {

                    topicTokenizer.nextToken(); // skip "controller"
                    String physicalId = topicTokenizer.nextToken();

                    if (physicalId != null && physicalId.equals(mControllerPhysicalId)) {

                        boolean available = true;
                        if (notificationMessage.getMessage().get("available") != null &&
                                !notificationMessage.getMessage().get("available").isNull()) {
                            available = notificationMessage.getMessage().get("available").asInt() == 1;
                        }

                        if (mControllerAvailable && !available) {
                            // TODO : If time is enough, we will change it to modeless Dialog later to show only 1 dialog.
                            new Thread(() -> {
                                JOptionPane.showMessageDialog(ScreenManager.getInstance().getCurrentScreen().getRootPanel(),
                                        Strings.CONTROLLER_ERROR + ":" + Strings.CONTACT_ATTENDANT,
                                        Strings.APPLICATION_NAME,
                                        JOptionPane.ERROR_MESSAGE);
                            }).start();
                        }

                        mControllerAvailable = available;

                    } else {
                        Log.logv(LOG_TAG, "No such controller in session, physicalId=" + physicalId);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.logd(LOG_TAG, "Wrong topic name on ControllerStatusChangedChannel, topic=" + uri.getLocation());
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to parse notification message, exception occurred");
            e.printStackTrace();
        }
    };
}
