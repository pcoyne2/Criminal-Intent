package com.bignerdranch.coyne.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.CompoundButton.*;

/**
 * Created by Patrick Coyne on 7/10/2017.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    public static final String DATE_FORMAT = "EEE MMM dd yyyy";
    public static final String TIME_FORMAT = "hh:mm a z";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private Crime mCrime;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    //Views
    @BindView(R.id.crime_title) EditText mTitleField;
    @BindView(R.id.crime_date) Button mDateButton;
    @BindView(R.id.crime_solved) CheckBox mSolvedCheckBox;
    @BindView(R.id.crime_report)Button mReportButton;
    @BindView(R.id.crime_suspect)Button mSuspectButton;
    @BindView(R.id.crime_photo)ImageView mPhotoView;
    @BindView(R.id.crime_camera)ImageButton mPhotoButton;

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);
        ButterKnife.bind(this, view);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTitle(charSequence.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
//        updateTime();
//        mTimeButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager manager = getFragmentManager();
//                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
//                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
//                dialog.show(manager, DIALOG_TIME);
//            }
//        });

        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));
//                startActivity(i);
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report)
                        .startChooser();
            }
        });
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        //Check to see if user has a contacts app
        PackageManager pm = getActivity().getPackageManager();
        if(pm.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(pm) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.coyne.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        updatePhotoView();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        }

        if (requestCode == REQUEST_TIME){
            Date time = (Date)data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(time);
            updateCrime();
//            updateTime();
        }

        if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            //Specify the fields you want your query to return values for
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            //Perform your query - the contactsUri is like a "where" clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try{
                //Double-check that you actually got results
                if(c.getCount() ==0){
                    return;
                }

                //Pull out the first column of the first row of data -
                //that is your suspect's name
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                updateCrime();
            }finally {
                c.close();
            }
        }

        if(requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.coyne.criminalintent.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
            updateCrime();
        }
    }

    private void updateDate(){
        mDateButton.setText(DateFormat.format(DATE_FORMAT, mCrime.getDate()).toString());
    }

    private void updateTime(){
//        mTimeButton.setText(DateFormat.format(DATE_FORMAT, mCrime.getDate()).toString());
    }

    private String getCrimeReport(){
        String solvedCrime = null;
        if(mCrime.isSolved()){
            solvedCrime = getString(R.string.crime_report_solved);
        }else{
            solvedCrime = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.format(DATE_FORMAT, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedCrime, suspect);

        return report;
    }

    private void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
