package com.bignerdranch.coyne.criminalintent;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Patrick Coyne on 7/10/2017.
 */

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private Date mTime;
    private boolean mSolved;
    private boolean mPoliceRequired;
    private String mSuspect;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
        mTime = new Date();
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

    public Date getTime() {
        return mTime;
    }

    public void setTime(Date time) {
        mTime = time;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getPhotoFileName(){
        return "IMG_" +getId().toString() + ".jpg";
    }
}
