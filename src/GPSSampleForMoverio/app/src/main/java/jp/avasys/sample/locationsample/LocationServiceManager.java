package jp.avasys.sample.locationsample;

import android.app.Activity;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Copyright(C) EPSON AVASYS CORPORATION 2018. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */
public class LocationServiceManager implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {
    private LocationManager mLocationManager;
    private Activity mActivity;
    private GPSCaptureListener mListener;
    private CurrentPositionStatusListener mPositionListener;

    private Map<Integer, Integer> mSatelliteStrength = new HashMap<>();

    public interface GPSCaptureListener {
        void onSearchingListener();
        void onCaptureListener(List<GpsSatellite> list);
    }

    public interface CurrentPositionStatusListener {
        void onStatusChange(String utcTime, String[] longitudeLatitudes);
    }

    public LocationServiceManager(Activity activity) {
        mActivity = activity;
    }

    public void setCurrentPositionStatusListener(CurrentPositionStatusListener listener) {
        mPositionListener = listener;
    }

    public void setGPSCaptureListener(GPSCaptureListener listener) {
        mListener = listener;
    }

    /**
     * GPSの捕捉を開始するメソッド
     */
    public void locationStart() {
        // LocationManager インスタンス生成
        mLocationManager = (LocationManager) mActivity.getBaseContext().getSystemService(LOCATION_SERVICE);
        if (mListener != null && mLocationManager != null) {
            mLocationManager.addGpsStatusListener(this);
            mLocationManager.addNmeaListener(this);

            final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                // GPSを設定するように促す
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivity(settingsIntent);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
            }
        }
    }

    /**
     * GPSの捕捉を終了するメソッド
     */
    public void locationStop() {
        if (mLocationManager != null) {
            mLocationManager.removeGpsStatusListener(this);
            mLocationManager.removeNmeaListener(this);
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d(MainActivity.TAG, getClass().getSimpleName() + " LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d(MainActivity.TAG, getClass().getSimpleName() + " LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d(MainActivity.TAG, getClass().getSimpleName() + " LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        String text;

        text = "Latitude:" + location.getLatitude();
        text = text + "__longitude:" + location.getLongitude();

        Log.d(MainActivity.TAG, getClass().getSimpleName() + " LocationChange_" + text);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        try {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Iterator<GpsSatellite> iterator = mLocationManager.getGpsStatus(null).getSatellites().iterator();
                    List<GpsSatellite> gpsSatelliteList = new ArrayList<>();
                    int i = 0;
                    while (iterator.hasNext()) {

                        GpsSatellite satellite = iterator.next();
                        if (satellite.usedInFix()) {
                            gpsSatelliteList.add(satellite);
                            i++;
                        }
                    }

                    if (i == 0) {
                        mListener.onSearchingListener();
                    } else {
                        mListener.onCaptureListener(gpsSatelliteList);
                    }

                    break;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(MainActivity.TAG, getClass().getSimpleName() + " enable " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(MainActivity.TAG, getClass().getSimpleName() + " disable " + provider);
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        String[] splitNmeas = nmea.split(",", 0);

        if (splitNmeas[0].equals("$GPGSV")) {
            //4衛星分がまとめて飛んでくる
            if (splitNmeas[1].equals("1")) {
                mSatelliteStrength.clear();
            }
            //チェックサムの値を読み込まないために長さを1つ減らす
            for (int i = 3; i < splitNmeas.length - 1; i = i + 4) {
                Integer satelliteNum = null;
                Integer strength = null;
                if (!splitNmeas[i].equals("")) {
                    splitNmeas[i] = eraseZero(splitNmeas[i]);
                    satelliteNum = Integer.decode(splitNmeas[i]);
                }
                if (!splitNmeas[i + 3].equals("")) {
                    splitNmeas[i + 3] = eraseZero(splitNmeas[i + 3]);
                    strength = Integer.decode(splitNmeas[i + 3]);
                }
                if (satelliteNum != null && strength != null) {
                    mSatelliteStrength.put(satelliteNum, strength);
                }
            }
        } else if (splitNmeas[0].equals("$GPRMC")) {
            if (splitNmeas[2].equals("A") && mPositionListener != null) {
                //標準時のスプリット
                String hh = splitNmeas[1].substring(0, 2);
                String mm = splitNmeas[1].substring(2, 4);
                String ss = splitNmeas[1].substring(4, 6);

                //緯度経度の取得
                String[] longitudeLatitudes = new String[4];

                longitudeLatitudes[0] = splitNmeas[4];
                longitudeLatitudes[1] = splitNmeas[3];
                longitudeLatitudes[2] = splitNmeas[6];
                longitudeLatitudes[3] = splitNmeas[5];

                mPositionListener.onStatusChange(hh + ":" + mm + ":" + ss, longitudeLatitudes);
            }
        }
    }

    public Map<Integer, Integer> getSatelliteStrength() {
        return mSatelliteStrength;
    }

    private String eraseZero(String targetChar) {
        for (int j = 0; j < targetChar.length(); ++j) {
            if (targetChar.indexOf("0") == 0) {
                targetChar = targetChar.substring(1);
            }
        }
        return targetChar;
    }
}
