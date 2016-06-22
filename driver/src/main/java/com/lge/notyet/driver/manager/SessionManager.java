package com.lge.notyet.driver.manager;

import java.util.HashMap;

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
    private int mReservedId = -1;
    private boolean mIsUnderTransaction = false;
    private long mTransactionStartTimeStamp = 0L;

    private String mControllerPhysicalId = null;
    private boolean mControllerAvailable = true;

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

    public boolean isControllerAvailable() {
        return mControllerAvailable;
    }
    public void setControllerAvailable(boolean controllerAvailable) {
        mControllerAvailable = controllerAvailable;
    }

    public void setUnderTransaction(boolean underTransaction) {
        mIsUnderTransaction = underTransaction;
    }

    public boolean getUnderTransaction() {
        return mIsUnderTransaction;
    }

    public void setTransactionStartTimeStamp(long transactionStartTimeStamp) {
        mTransactionStartTimeStamp = transactionStartTimeStamp;
    }

    public long getTransactionStartTimeStamp() {
        return mTransactionStartTimeStamp;
    }

    public void clearReservationInformation() {
        mReservedTime = 0L;
        mReservedConfirmationNumber = 0;
        mReservedFacilityId = 0;
        mReservedId = -1;
        mIsUnderTransaction = false;
        mTransactionStartTimeStamp = 0L;
        mControllerPhysicalId = null;
        mControllerAvailable = true;
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
    }
}
