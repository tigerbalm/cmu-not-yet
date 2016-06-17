package com.lge.notyet.attendant.manager;

public class Slot {

    private final int mId;
    private final int mNumber;
    private boolean mOccupied;
    private boolean mReserved;
    private long mOccupiedTimeStamp;
    private long mReservedTimeStamp;
    private final int mControllerId;
    private final int mPhysicalId;

    public Slot(int id, int number, boolean occupied, boolean reserved, long occupiedTimeStamp, int controller_id, int physical_id) {

        mId = id;
        mNumber = number;
        mOccupied = occupied;
        mReserved = reserved;
        mOccupiedTimeStamp = occupiedTimeStamp;
        mReservedTimeStamp = 0L;
        mControllerId = controller_id;
        mPhysicalId = physical_id;
    }

    public int getId() {
        return mId;
    }

    public int getNumber() {
        return mNumber;
    }

    public boolean isOccupied() {
        return mOccupied;
    }
    public void setOccupied(boolean occupied) {
        mOccupied = occupied;
    }

    public boolean isReserved() {
        return mReserved;
    }
    public void setReserved(boolean reserved) {
        mReserved = reserved;
    }
    public long getReservedTimeStamp() {
        return mReservedTimeStamp;
    }


    public long getOccupiedTimeStamp() {
        return mOccupiedTimeStamp;
    }
    public void setOccupiedTimeStamp(long occupiedTimeStamp) {
        mOccupiedTimeStamp =occupiedTimeStamp;
    }

    public int getControllerId() {
        return mControllerId;
    }
    public int getPhysicalId() {
        return mPhysicalId;
    }
}
