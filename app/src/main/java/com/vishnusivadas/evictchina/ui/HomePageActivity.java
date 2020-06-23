package com.vishnusivadas.evictchina.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.vishnusivadas.evictchina.BuildConfig;
import com.vishnusivadas.evictchina.R;
import com.vishnusivadas.evictchina.databinding.ActivityHomePageBinding;
import com.vishnusivadas.evictchina.util.SharedPrefsUtils;
import com.vishnusivadas.evictchina.util.Utility;
import com.vishnusivadas.evictchina.util.UtilsDir;
import com.vishnusivadas.evictchina.widget.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityHomePageBinding homePageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homePageBinding = DataBindingUtil.setContentView(this, R.layout.activity_home_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
        setSupportActionBar(toolbar);
        Tools.setSystemBarColor(this, R.color.red_evict_china_light);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setClickLIstenr();
    }


    void setClickLIstenr() {
        homePageBinding.tvUlternativeApp.setOnClickListener(this);
        homePageBinding.tvUninstallApps.setOnClickListener(this);
        homePageBinding.ShareAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApplication();
            }
        });
        homePageBinding.ShareURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });

    }
    private void shareApp() {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", getResources().getString(R.string.app_name));
            intent.putExtra("android.intent.extra.TEXT", "\nFind and remove chinese apps from your device and also find alternative apps. Download the EvictChina APP from \n\n https://evictchina.vishnusivadas.com/ \n\n");
            startActivity(Intent.createChooser(intent, "Choose One"));
        } catch (Exception ignored) {
        }
    }


    public void showInternetAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(HomePageActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage("You don't have internet connection. Kindly check and try again.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        alertDialog.show();
    }


    void checkUlterNativeAndGoNext() {
        if (Utility.isNetworkConnected(HomePageActivity.this)) {
            if (SharedPrefsUtils.getBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_ALTERNATIVE, false)) {
                startActivitys(AlternativeAppsActivity.class);

            } else {
                showInternetAlert();
            }

        } else {
            SharedPrefsUtils.setBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_ALTERNATIVE, true);
            startActivitys(AlternativeAppsActivity.class);

        }

    }

    void checkRemoverAndGoNext() {
        if (Utility.isNetworkConnected(HomePageActivity.this)) {
            if (SharedPrefsUtils.getBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_REMOVER, false)) {
                startActivitys(MainActivity.class);
            } else {
                showInternetAlert();
            }

        } else {
            SharedPrefsUtils.setBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_REMOVER, true);
            startActivitys(MainActivity.class);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvUlternativeApp:
                checkUlterNativeAndGoNext();
                break;
            case R.id.tvUninstallApps:
                checkRemoverAndGoNext();
                break;
        }
    }

    public void startActivitys(Class c) {
        Intent i = new Intent(HomePageActivity.this, c);
        startActivity(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private void shareApplication() {
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        File originalApk = new File(filePath);
        try {
            String dirPath = UtilsDir.getRootDirPath(getApplicationContext());
            File tempFile = new File(dirPath + "/ExtractedApk");
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            tempFile = new File(tempFile.getPath() + "/" + getString(app.labelRes).replace(" ", "").toUpperCase() + ".apk");
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, tempFile);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Social Downloader");
            intent.putExtra(Intent.EXTRA_TEXT, "Install Social Downloader - Best Social media downloader");
            intent.putExtra(Intent.EXTRA_STREAM, apkUri);
            startActivity(Intent.createChooser(intent, "Share app via"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share_apk_file) {
            shareApplication();
        }
        return true;
    }


}
