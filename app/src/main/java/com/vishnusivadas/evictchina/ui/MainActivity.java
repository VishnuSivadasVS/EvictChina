package com.vishnusivadas.evictchina.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vishnusivadas.evictchina.BuildConfig;
import com.vishnusivadas.evictchina.R;
import com.vishnusivadas.evictchina.adapter.ItemlistAdapter;
import com.vishnusivadas.evictchina.databinding.ActivityMainBinding;
import com.vishnusivadas.evictchina.listner.OnItemClickListener;
import com.vishnusivadas.evictchina.model.AppInfo;
import com.vishnusivadas.evictchina.util.UtilsDir;
import com.vishnusivadas.evictchina.widget.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final List<String> myAppsList = new ArrayList<>();
    private final List<AppInfo> appInfos = new ArrayList();
    private ItemlistAdapter itemlistAdapter;
    private int selectedPos;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Tools.setSystemBarColor(this, R.color.red_evict_china_light);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setClickListner();

        binding.btnShareAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApplication();
            }
        });
        setData();
    }

    private void uninstallIntent(int pos) {
        Intent intent;
        intent = new Intent("android.intent.action.UNINSTALL_PACKAGE");
        intent.setData(Uri.fromParts("package", appInfos.get(pos).packageName, (String) null));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setData() {
        itemlistAdapter = new ItemlistAdapter(appInfos, new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectedPos = position;
                isDeleteClick = true;
                uninstallIntent(selectedPos);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(itemlistAdapter);
        getListItems();
    }

    private void setClickListner() {
        binding.btnShare.setOnClickListener(new View.OnClickListener() {
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

    private void getListItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference questionsRef1 = rootRef.collection("appList").document("apps");
        questionsRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete()) {
                    myAppsList.addAll((Collection<? extends String>) task.getResult().getData().get("china"));
                    getApps();
                }
            }
        });
    }


    private boolean isDeleteClick = false;

    public void onResume() {
        super.onResume();
        if (isDeleteClick) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    getApps();
                }
            }, 1500);
        }
        isDeleteClick = false;
    }


    private boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & 1) != 0;
    }

    private ArrayList getInstalledApps() {
        ArrayList arrayList = new ArrayList();

        Log.e("myappslistsize", "" + myAppsList.size());
        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            if (!isSystemPackage(packageInfo)) {
                if (myAppsList.contains(packageInfo.packageName)) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                    appInfo.packageName = packageInfo.packageName;
                    appInfo.versionName = packageInfo.versionName;
                    appInfo.versionCode = packageInfo.versionCode;
                    appInfo.icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                    appInfo.size = (new File(packageInfo.applicationInfo.publicSourceDir).length() / 1048576) + " MB";
                    arrayList.add(appInfo);
                }
            }
        }
        return arrayList;
    }


    @SuppressLint("StaticFieldLeak")
    class GetAppsAsync extends AsyncTask<Void, Void, List<AppInfo>> {
        GetAppsAsync() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        public List<AppInfo> doInBackground(Void... voidArr) {
            return getInstalledApps();
        }

        public void onPostExecute(List<AppInfo> list) {
            super.onPostExecute(list);
            appInfos.clear();
            appInfos.addAll(list);
            binding.progressBar.setVisibility(View.GONE);
            if (list.size() > 0) {
                binding.appFoundCount.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.conNoApps.setVisibility(View.GONE);
                binding.appFoundCount.setText(Html.fromHtml(getResources().getString(R.string.app_found_count, new Object[]{Integer.valueOf(list.size())})));
            } else {
                binding.appFoundCount.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.conNoApps.setVisibility(View.VISIBLE);
            }
            itemlistAdapter.notifyDataSetChanged();
            //if (list.size() == 0) {
            Collections.sort(list, new Comparator<AppInfo>() {
                public int compare(AppInfo appInfo, AppInfo appInfo2) {
                    return appInfo.appName.compareToIgnoreCase(appInfo2.appName);
                }
            });
        }
    }

    private void getApps() {
        new GetAppsAsync().execute();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainupdate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.closeupdate) {
            finish();
        }
        return true;
    }

}
