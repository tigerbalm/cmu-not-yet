package com.lge.notyet.attendant.manager;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by beney.kim on 2016-06-15.
 */
public class SessionManager {

    private final static String EMAIL = "email";
    private final static String KEY = "key";

    private HashMap<String, String> mSession;
    private HashMap<Integer, Slot> mSlots;

    private int mFacilityId = -1;
    private String mFacilityName = null;

    private static SessionManager sSessionManager = null;

    private SessionManager () {
        mSession = new HashMap<>();
        mSlots = new HashMap<>();
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

    public void setUserEmail(String email) {
        mSession.put(EMAIL, email);
    }

    public void setKey(String key) {
        mSession.put(KEY, key);
    }

    public void setFacilityInformation(int id, String name) {
        mFacilityId = id;
        mFacilityName = name;
    }

    public int getFacilityId() {
        return mFacilityId;
    }

    public String getFacilityName() {
        return mFacilityName;
    }

    public void clearFacilityInformation() {
        mFacilityId = -1;
        mFacilityName = null;
    }

    public void addSlot(int id, int number, boolean occupied, boolean reserved, long occupiedTimeStamp, int controller_id, int physical_id) {

        mSlots.put(id, new Slot(id, number, occupied, reserved, occupiedTimeStamp, controller_id, physical_id));
    }

    public Set<Integer> getSlotIds() {
        return mSlots.keySet();
    }

    public Slot getSlot(int id) {
        return mSlots.get(id);
    }

    public int getSlotSize() {
        return mSlots.size();
    }

    public void clear() {
        clearFacilityInformation();
        mSession.clear();
    }
}
