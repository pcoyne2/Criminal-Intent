package com.bignerdranch.coyne.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Patrick Coyne on 7/10/2017.
 */

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private boolean mPoliceRequired;

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }


    public boolean isPoliceRequired() {
        return mPoliceRequired;
    }

    public void setPoliceRequired(boolean policeRequired) {
        mPoliceRequired = policeRequired;
    }
}
