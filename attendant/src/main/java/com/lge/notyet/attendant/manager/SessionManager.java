package com.lge.notyet.attendant.manager;

import java.util.HashMap;
import java.util.Set;

public class SessionManager {

    private final static String EMAIL = "email";
    private final static String KEY = "key";

    private final HashMap<String, String> mSession;
    private final HashMap<Integer, Slot> mSlots;

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

    public void addSlot(int id, int number, boolean occupied, boolean reserved, long occupiedTimeStamp, int controller_id, String physical_id, int reservation_id, String user_email, long reservation_ts) {
        mSlots.put(id, new Slot(id, number, occupied, reserved, occupiedTimeStamp, controller_id, physical_id, reservation_id, user_email, reservation_ts));
    }

    public Set<Integer> getSlotIds() {
        return mSlots.keySet();
    }

    public Slot getSlot(int id) {
        return mSlots.get(id);
    }

    public Slot getSlot(String physical_id, int slot_number) {
        for (Slot slot : mSlots.values()) {

            if (slot.getPhysicalId().equals(physical_id) && slot.getNumber() == slot_number) {
                return slot;
            }
        }
        return null;
    }

    public int getSlotSize() {
        return mSlots.size();
    }

    public void clear() {
        mSession.clear();
        clearFacilityInformation();
    }
}
