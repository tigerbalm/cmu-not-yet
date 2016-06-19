package com.lge.notyet.attendant.manager;

public class Slot {

    private final int mId;
    private final int mNumber;
    private boolean mOccupied;
    private boolean mReserved;
    private long mOccupiedTimeStamp;
    private long mReservedTimeStamp;
    private final int mControllerId;
    private final String mPhysicalId;
    private final int mReservationId;
    private final String mUserEmail;

    public Slot(int id, int number, boolean occupied, boolean reserved, long occupiedTimeStamp, int controller_id, String physical_id, int reservation_id, String user_email, long reservation_ts) {

        mId = id;
        mNumber = number;
        mOccupied = occupied;
        mReserved = reserved;
        mOccupiedTimeStamp = occupiedTimeStamp;
        mReservedTimeStamp = reservation_ts;
        mControllerId = controller_id;
        mPhysicalId = physical_id;
        mReservationId = reservation_id;
        mUserEmail = user_email;
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
    public int getReservationId() {
        return mReservationId;
    }
    public void setReserved(boolean reserved) {
        mReserved = reserved;
    }
    public long getReservedTimeStamp() {
        return mReservedTimeStamp;
    }
    public String getReservedUserEmail() {
        return mUserEmail;
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
    public String getPhysicalId() {
        return mPhysicalId;
    }
}
