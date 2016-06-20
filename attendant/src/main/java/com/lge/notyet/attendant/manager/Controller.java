package com.lge.notyet.attendant.manager;

public class Controller {

    private final int mId;
    private final String mPhysicalId;
    private boolean mIsAvailable;

    public Controller(int id, String physicalId) {
        mId = id;
        mPhysicalId = physicalId;
        mIsAvailable = true;
    }

    public int getId() { return mId; }
    public String getPhysicalId() { return mPhysicalId; }
    public boolean isAvailable() { return mIsAvailable; }
    public void setAvailable(boolean available) { mIsAvailable = available; }

    @Override
    public String toString() {
        return "ID=" + mId + ", Physical ID=" + mPhysicalId + ", IsAvailable=" + mIsAvailable;
    }
}
