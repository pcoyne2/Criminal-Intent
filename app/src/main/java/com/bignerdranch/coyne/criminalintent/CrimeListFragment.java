package com.bignerdranch.coyne.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Patrick Coyne on 7/10/2017.
 */

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisisble;
    private TextView mTextView;

    //Override Functions
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        if(savedInstanceState != null){
            mSubtitleVisisble = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        ButterKnife.bind(this, view);

        mCrimeRecyclerView = (RecyclerView)view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTextView = (TextView)view.findViewById(R.id.recycler_text_view);
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subTitleItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisisble){
            subTitleItem.setTitle(R.string.hide_subtitle);
        }else{
            subTitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity
                        .newIntent(getActivity(), crime.getId());
                updateUI();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisisble = !mSubtitleVisisble;
                getActivity().invalidateOptionsMenu();
                updateSubTitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisisble);
    }

    //Reusable Code
    private void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if(crimes.isEmpty()){
            mTextView.setVisibility(View.VISIBLE);
            mCrimeRecyclerView.setVisibility(View.INVISIBLE);
        }else{
            mTextView.setVisibility(View.INVISIBLE);
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
        }

        if(mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }

        updateSubTitle();
    }

    private void updateSubTitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if(!mSubtitleVisisble){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    //RecyclerView functions
    private abstract class AbstractCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public AbstractCrimeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView)itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            SimpleDateFormat dateFormat = new SimpleDateFormat(CrimeFragment.DATE_FORMAT);
            SimpleDateFormat timeFormat = new SimpleDateFormat(CrimeFragment.TIME_FORMAT);
            mDateTextView.setText(dateFormat.format(mCrime.getDate()) + " " + timeFormat.format(mCrime.getTime()));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        public Crime getCrime() {
            return mCrime;
        }

        @Override
        public void onClick(View v) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeHolder extends AbstractCrimeHolder {
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime);
        }
    }

    private class PoliceCrimeHolder extends AbstractCrimeHolder {
        public PoliceCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime_police);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int LIST_ITEM_CRIME = 0;
        private static final int LIST_ITEM_CRIME_POLICE = 1;

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == LIST_ITEM_CRIME) {
                return new CrimeHolder(layoutInflater, parent);
            } else if (viewType == LIST_ITEM_CRIME_POLICE) {
                return new PoliceCrimeHolder(layoutInflater, parent);
            } else {
                return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            if (holder instanceof CrimeHolder) {
                ((CrimeHolder) holder).bind(crime);
            } else if (holder instanceof PoliceCrimeHolder) {
                ((PoliceCrimeHolder) holder).bind(crime);
            }

        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType (int position) {
            boolean requiresPolice = mCrimes.get(position).isPoliceRequired();
            if (requiresPolice) {
                return LIST_ITEM_CRIME_POLICE;
            } else {
                return LIST_ITEM_CRIME;
            }
        }
    }
}