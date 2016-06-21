package com.lge.notyet.attendant.manager;

import java.util.HashMap;
import java.util.Set;

public class SessionManager {

    private String mSessionKey;
    private int mFacilityId;
    private String mFacilityName;
    private final HashMap<Integer, Controller> mControllers;
    private final HashMap<Integer, Slot> mSlots;

    private static SessionManager sSessionManager = null;

    private SessionManager () {
        mSessionKey = null;
        mFacilityId = -1;
        mFacilityName = null;
        mSlots = new HashMap<>();
        mControllers = new HashMap<>();
    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (sSessionManager == null) {
                sSessionManager = new SessionManager();
            }
        }
        return sSessionManager;
    }

    public void setKey(String sessionKey) { mSessionKey = sessionKey; }
    public String getKey() { return mSessionKey; }

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

    public void addSlot(int id, int number, boolean occupied, boolean reserved, long occupiedTimeStamp, int controller_id, String physical_id, int reservation_id, String user_email, long reservation_ts, boolean is_controller_activated) {
        mSlots.put(id, new Slot(id, number, occupied, reserved, occupiedTimeStamp, controller_id, physical_id, reservation_id, user_email, reservation_ts));
        if (!mControllers.containsKey(controller_id)) {
            Controller controller = new Controller(controller_id, physical_id);
            controller.setAvailable(is_controller_activated);
            mControllers.put(controller_id, controller);
        }
    }
    public Set<Integer> getSlotIds() {
        return mSlots.keySet();
    }
    public Slot getSlot(int id) {
        return mSlots.get(id);
    }

    public void clearSlots() {
        mSlots.clear();
    }

    public Controller getController(int id) {
        return mControllers.get(id);
    }
    public Controller getController(String physicalId) {
        for (int id : mControllers.keySet()) {
            if (mControllers.get(id).getPhysicalId().equals(physicalId)) return mControllers.get(id);
        }
        return null;
    }

    public void clear() {
        mSessionKey = null;
        clearFacilityInformation();
        mControllers.clear();
        clearSlots();
    }
}
