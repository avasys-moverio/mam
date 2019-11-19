package jp.avasys.sample.locationsample;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.LevelListDrawable;
import android.location.GpsSatellite;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Map;

import jp.avasys.sample.btutil.BTDeviceManager;
import jp.avasys.sample.license.LicenseFragment;

/**
 * Copyright(C) EPSON AVASYS CORPORATION 2018. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */
public class MainActivity extends Activity implements LicenseFragment.CloseButtonClickListener{
    public static final String TAG = MainActivity.class.getSimpleName();

    private LocationServiceManager mLocationServiceManager;
    private ImageView[] mImageViews;
    private boolean mIsSearchingText = false;

    private TextView mSearchingText;

    private LicenseFragment mLicenseFragment;

    private ValueAnimator mAnimator;

    private LocationServiceManager.GPSCaptureListener mGpsCaptureListener = new LocationServiceManager.GPSCaptureListener() {
        @Override
        public void onCaptureListener(final List<GpsSatellite> list) {

            final Map<Integer, Integer> satelliteStrength = mLocationServiceManager.getSatelliteStrength();
            if (mIsSearchingText) {
                mAnimator.end();
                mIsSearchingText = false;
            }

            final int enableGPSs = list.size();
            int i = 0;

            int maxSatellites = 0;
            if (enableGPSs < mImageViews.length) {
                maxSatellites = enableGPSs;
            } else {
                maxSatellites = mImageViews.length;
            }
            for (; i < maxSatellites; ++i) {
                final GpsSatellite gpsSatellite = list.get(i);

                mImageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GPSDialogManager.resultDialog(MainActivity.this, gpsSatellite, satelliteStrength);
                    }
                });

                mImageViews[i].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        boolean result = false;
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            GPSDialogManager.resultDialog(MainActivity.this, gpsSatellite, satelliteStrength);
                            result = true;
                        }
                        return result;
                    }
                });

                final int snr = (int)gpsSatellite.getSnr();
                mImageViews[i].setImageLevel(snr);
                mImageViews[i].setAlpha(1.0f);
                mImageViews[i].setClickable(true);
                mImageViews[i].setFocusable(true);

            }

            for (; i < mImageViews.length; ++i) {
                resetSatelliteIcon(mImageViews[i]);
            }
        }

        @Override
        public void onSearchingListener() {

            if (!mIsSearchingText) {
                final String[] searchTexts = getResources().getStringArray(R.array.search_text);

                mAnimator = ValueAnimator.ofInt(0, searchTexts.length);
                mAnimator.setDuration(1500);
                mAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int index = (int)animation.getAnimatedValue();
                        if (index < searchTexts.length) {
                            mSearchingText.setText(searchTexts[index]);
                        }
                    }
                });

                mAnimator.start();


                //衛星アイコンのリセットを行う
                for (ImageView imageView : mImageViews) {
                    resetSatelliteIcon(imageView);
                }

                mIsSearchingText = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchingText = (TextView) findViewById(R.id.search_text);

        mLocationServiceManager = new LocationServiceManager(this);
        mLocationServiceManager.setCurrentPositionStatusListener(new LocationServiceManager.CurrentPositionStatusListener() {
            @Override
            public void onStatusChange(String utcTime, String[] longitudeLatitudes) {
                mSearchingText.setTextSize(25.0f);
                String nmeaStatus = "UTC:" + utcTime;

                if (longitudeLatitudes[0].equals("N")) {
                    nmeaStatus = nmeaStatus + "\nLongitude:" + "North" + "_" + longitudeLatitudes[1];
                } else {
                    nmeaStatus = nmeaStatus + "\nLongitude:" + "South" + "_" + longitudeLatitudes[1];
                }

                if (longitudeLatitudes[2].equals("E")) {
                    nmeaStatus = nmeaStatus + "\nLatitude:" + "East" + "_" + longitudeLatitudes[3];
                } else {
                    nmeaStatus = nmeaStatus + "\nLatitude:" + "West" + "_" + longitudeLatitudes[3];
                }
                mSearchingText.setText(nmeaStatus);
            }
        });

        mImageViews = new ImageView[10];
        for (int i = 0; i < mImageViews.length; ++i) {
            switch (i) {
                case 0:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite1);
                    break;
                case 1:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite2);
                    break;
                case 2:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite3);
                    break;
                case 3:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite4);
                    break;
                case 4:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite5);
                    break;
                case 5:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite6);
                    break;
                case 6:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite7);
                    break;
                case 7:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite8);
                    break;
                case 8:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite9);
                    break;
                case 9:
                    mImageViews[i] = (ImageView)findViewById(R.id.satellite10);
                    break;
            }
            mImageViews[i].setImageResource(R.drawable.snr_image_list);
        }

        setLicenseButtonAction((ImageView)findViewById(R.id.droid));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationServiceManager.setGPSCaptureListener(mGpsCaptureListener);
        mLocationServiceManager.locationStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationServiceManager.locationStop();
    }

    private void resetSatelliteIcon(ImageView imageView) {
        imageView.setClickable(false);
        imageView.setFocusable(false);
        imageView.setImageLevel(0);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void setLicenseButtonAction(ImageView imageView) {
        imageView.setClickable(true);
        imageView.setFocusable(true);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean result = false;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    findViewById(R.id.baseContentsLayout).setVisibility(View.INVISIBLE);
                    mLicenseFragment = new LicenseFragment();
                    getFragmentManager().beginTransaction().add(R.id.license, mLicenseFragment, "license").commit();
                    result = true;
                }
                return result;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.baseContentsLayout).setVisibility(View.INVISIBLE);
                mLicenseFragment = new LicenseFragment();
                getFragmentManager().beginTransaction().add(R.id.license, mLicenseFragment, "license").commit();
            }
        });

        if (BTDeviceManager.isBT3(this)) {
            imageView.setImageResource(R.drawable.movedroid_300);
        } else if (BTDeviceManager.isBT2PRO(this)) {
            imageView.setImageResource(R.drawable.movedroid);
        }
    }

    @Override
    public void onClick() {
        getFragmentManager().beginTransaction().remove(mLicenseFragment).commit();
        findViewById(R.id.baseContentsLayout).setVisibility(View.VISIBLE);
        mLicenseFragment = null;
    }
}
