package jp.avasys.sample.btutil;

import android.app.Activity;
import android.os.Build;

import jp.avasys.sample.locationsample.R;

/**
 * Copyright(C) EPSON AVASYS CORPORATION 2018. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */
public class BTDeviceManager {

    public static boolean isBT2PRO(Activity activity) {
        return Build.PRODUCT.equals(activity.getString(R.string.bt2pro_product));
    }

    public static boolean isBT3(Activity activity) {
        return (Build.PRODUCT.equals(activity.getString(R.string.bt3c_product)) | Build.PRODUCT.equals(activity.getString(R.string.bt3s_product)));
    }
}
