package jp.avasys.sample.locationsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.GpsSatellite;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Copyright(C) EPSON AVASYS CORPORATION 2018. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */
public class GPSDialogManager {

    public static void resultDialog(Activity activity, GpsSatellite satellite, Map<Integer, Integer> strengthMap) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View inflate = inflater.inflate(R.layout.alart_main, (ViewGroup) activity.findViewById(R.id.alertMain));

        String prn = Integer.toString(satellite.getPrn());
        TextView prnTextView = (TextView)inflate.findViewById(R.id.prn_message);
        prnTextView.setText(prn);

        String azimuth = Float.toString(satellite.getAzimuth());
        TextView azimuthTextView = (TextView)inflate.findViewById(R.id.azimuth_message);
        azimuthTextView.setText(azimuth);

        String elevation = Float.toString(satellite.getElevation());
        TextView elevationTextView = (TextView)inflate.findViewById(R.id.elevation_massage);
        elevationTextView.setText(elevation);

        String snr = Float.toString(satellite.getSnr());
        TextView snrTextView = (TextView)inflate.findViewById(R.id.snr_message);
        snrTextView.setText(snr);

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(inflate);
        alert.setTitle(activity.getString(R.string.detail_info));

        alert.setPositiveButton(activity.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                boolean result = false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        result = true;
                        break;
                    case KeyEvent.KEYCODE_F1:
                        result = true;
                        break;
                }
                return result;
            }
        });
        alert.create().show();
    }
}
