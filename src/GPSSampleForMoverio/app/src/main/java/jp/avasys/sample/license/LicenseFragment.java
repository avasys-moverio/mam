package jp.avasys.sample.license;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import jp.avasys.sample.locationsample.BuildConfig;
import jp.avasys.sample.locationsample.R;


/**
 * Copyright(C) EPSON AVASYS CORPORATION 2018. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * ライセンスの文言を動的に追加し表示するフラグメント
 * layoutフォルダにlicense_main.xml、valuesにlicense.xmlを追加すること
 */
public class LicenseFragment extends Fragment {
    /**
     * ライセンス表示画面用のクリックリスナー
     */
    public interface CloseButtonClickListener {
        void onClick();
    }

    private Context mContext;

    private CloseButtonClickListener mCloseListener;
    private boolean mAutoScroll = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CloseButtonClickListener) {
            mCloseListener = (CloseButtonClickListener)activity;
        } else {
            mCloseListener = new CloseButtonClickListener() {
                @Override
                public void onClick() {
                    //空動作
                }
            };
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mContext = inflater.getContext();
        /*表示するViewの取得*/
        View view = inflater.inflate(R.layout.license_main, container, false);
        TextView appName = (TextView)view.findViewById(R.id.appName);
        ColorStateList currentBaseTextColor = appName.getTextColors();
        appName.setText(getString(R.string.app_name));

        TextView version = (TextView)view.findViewById(R.id.versionCode);
        String versionCode = "Version " + BuildConfig.VERSION_NAME;
        version.setText(versionCode);

        /*ボタンイベント追加*/
        Button finishButton = (Button)view.findViewById(R.id.finishButton);
        finishButton.requestFocus();
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseListener.onClick();
            }
        });

        /*自アプリのライセンスについて追加する*/
        TextView textView = (TextView) view.findViewById(R.id.thisAppLicense);
        textView.setText(getText(R.string.app_license));
        textView.setTextColor(currentBaseTextColor);

        /*利用しているOSSのライセンス一覧を追加する*/
        TextView textView1 = (TextView) view.findViewById(R.id.ossList);
        textView1.setText(getOSSList());
        textView1.setTextColor(currentBaseTextColor);

        /*OSSのライセンス条項を追加する*/
        TextView textView2 = (TextView) view.findViewById(R.id.otherLicense);
        textView2.setText(getOSSLicenseWords());
        textView2.setTextColor(currentBaseTextColor);

        setAutoScroll(view);

        return view;
    }

    /**
     * 自動スクロールを有効化する
     */
    public void setAutoScrollFlag() {
        mAutoScroll = true;
    }

    /**
     * 使用しているOSSの一覧を取得し、表示に利用できる形で出力するためのクラス
     * @return StringのOSSリスト
     */
    private String getOSSList() {
        String[] ossList = getResources().getStringArray(R.array.comprise_lib_array);
        String usedOSSList = "";
        if (ossList.length != 0) {
            usedOSSList = getString(R.string.open_source_usage_word) + " " + getString(R.string.app_name) + ".\n\n";

            for (int i = 0; i < ossList.length; i++) {
                usedOSSList = usedOSSList + Integer.toString(i + 1) + ".    " + ossList[i] + "\n";
            }
        }

        return usedOSSList;
    }

    /**
     * 使用しているOSSライセンスの文言をresourceから検索する
     * @return
     */
    private String getOSSLicenseWords() {
        final Resources resources = getResources();
        final String packageName = mContext.getPackageName();

        String[] ossList = resources.getStringArray(R.array.comprise_lib_array);
        String ossWord = "";

        for (int i = 0; i < ossList.length; i++) {
            int resourceID = resources.getIdentifier("item_" + Integer.toString(i + 1), "string", packageName);
            if (resourceID != 0) {
                ossWord = ossWord + Integer.toString(i + 1) + ". " + ossList[i] + "\n";
                ossWord = ossWord + resources.getString(resourceID) + "\n";
            }
        }

        return ossWord;
    }

    /**
     * 自動スクロール設定用メソッド
     * @param view 自動スクロールを追加するScrollView
     */
    private void setAutoScroll(View view) {
        if (mAutoScroll) {
            view.findViewById(R.id.finishButton).setVisibility(View.INVISIBLE);
            final ScrollView scrollView = (ScrollView)view.findViewById(R.id.scrollmain);

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0, scrollView.getScrollY() + 5);
                    if (scrollView.canScrollVertically(1)) {
                        scrollView.postDelayed(this, 100);
                    } else {
                        mCloseListener.onClick();
                    }
                }
            }, 1000);
        }
    }
}
