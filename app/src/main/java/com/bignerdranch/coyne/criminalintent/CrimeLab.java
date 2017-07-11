package com.bignerdranch.coyne.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Patrick Coyne on 7/10/2017.
 */

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private List<Crime> mCrimes;

    public static CrimeLab get(Context context){
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
    }

    public void addCrime(Crime c){
        mCrimes.add(c);
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id){
        for(Crime crime :mCrimes){
            if(crime.getId().equals(id)){
                return crime;
            }
        }
        return null;
    }
}
