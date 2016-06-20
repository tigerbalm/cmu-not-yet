package com.lge.notyet.attendant.manager;

public class Slot {

    private final int mId;
    private final int mNumber;
    private final boolean mOccupied;
    private final boolean mReserved;
    private final long mOccupiedTimeStamp;
    private final long mReservedTimeStamp;
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
    public boolean isReserved() {
        return mReserved;
    }
    public int getReservationId() {
        return mReservationId;
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
    public int getControllerId() {
        return mControllerId;
    }
    public String getControllerPhysicalId() {
        return mPhysicalId;
    }

}
