package com.vishnusivadas.evictchina.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.Status;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.vishnusivadas.evictchina.BuildConfig;
import com.vishnusivadas.evictchina.R;
import com.vishnusivadas.evictchina.util.UtilsDir;
import com.vishnusivadas.evictchina.widget.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class UpdateView extends AppCompatActivity {
    TextView updateTextViewd, updateappv, updatecontact, updaterdate, devupdate, updatehead;
    String appname, version, releasedate, downloadurl, description, dirPath, contactus;
    String URLDOWNLOAD = "https://evictchina.vishnusivadas.com/apk/EvictChina.apk";

    Button buttonUpdate, buttonCancelUpdate;
    ProgressBar progressBarUpdate;
    int downloadIdUpdate;
    TextView textViewProgressUpdate;
    String updatenoteurl = "https://evictchina.vishnusivadas.com/apis/update_details.php";

    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Tools.setSystemBarColor(this, R.color.red_evict_china_light);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateTextViewd = findViewById(R.id.updateviewdetails);

        updateappv = findViewById(R.id.updateappversion);
        updaterdate = findViewById(R.id.updatereleasedate);
        updatehead = findViewById(R.id.updateheading);

        devupdate = findViewById(R.id.developerupdate);
        devupdate.setMovementMethod(LinkMovementMethod.getInstance());
        updatecontact = findViewById(R.id.updatecontactus);
        updatecontact.setMovementMethod(LinkMovementMethod.getInstance());
        mQueue = Volley.newRequestQueue(this);
        jsonParse(updatenoteurl);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancelUpdate = findViewById(R.id.buttonCancelUpdate);
        textViewProgressUpdate = findViewById(R.id.textViewProgressUpdate);
        progressBarUpdate = findViewById(R.id.progressBarUpdate);

        dirPath = UtilsDir.getRootDirPath(getApplicationContext());

        PRDownloader.initialize(getApplicationContext());
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buttonUpdate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (Status.RUNNING == PRDownloader.getStatus(downloadIdUpdate)) {
                                        PRDownloader.pause(downloadIdUpdate);
                                        return;
                                    }
                                    buttonUpdate.setEnabled(false);
                                    progressBarUpdate.setIndeterminate(true);
                                    progressBarUpdate.getIndeterminateDrawable().setColorFilter(
                                            Color.BLUE, PorterDuff.Mode.SRC_IN);

                                    if (Status.PAUSED == PRDownloader.getStatus(downloadIdUpdate)) {
                                        PRDownloader.resume(downloadIdUpdate);
                                        return;
                                    }
                                    Uri u = Uri.parse(URLDOWNLOAD);
                                    final File f = new File("" + u);
                                    final String filename = f.getName();
                                    downloadIdUpdate = PRDownloader.download(URLDOWNLOAD, dirPath, filename)
                                            .build()
                                            .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                                @Override
                                                public void onStartOrResume() {
                                                    progressBarUpdate.setIndeterminate(false);
                                                    buttonUpdate.setEnabled(true);
                                                    buttonUpdate.setText(R.string.pause);
                                                    buttonCancelUpdate.setEnabled(true);
                                                    buttonCancelUpdate.setText(R.string.cancel);
                                                }
                                            })
                                            .setOnPauseListener(new OnPauseListener() {
                                                @Override
                                                public void onPause() {
                                                    buttonUpdate.setText(R.string.resume);
                                                }
                                            })
                                            .setOnCancelListener(new OnCancelListener() {
                                                @Override
                                                public void onCancel() {
                                                    downloadIdUpdate = 0;
                                                    buttonUpdate.setText(R.string.start);
                                                    buttonCancelUpdate.setEnabled(false);
                                                    progressBarUpdate.setProgress(0);
                                                    textViewProgressUpdate.setText("");
                                                    progressBarUpdate.setIndeterminate(false);
                                                }
                                            })
                                            .setOnProgressListener(new OnProgressListener() {
                                                @Override
                                                public void onProgress(Progress progress) {
                                                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                                    progressBarUpdate.setProgress((int) progressPercent);
                                                    textViewProgressUpdate.setText(UtilsDir.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                                }
                                            })
                                            .start(new OnDownloadListener() {
                                                @Override
                                                public void onDownloadComplete() {
                                                    buttonUpdate.setEnabled(true);
                                                    buttonCancelUpdate.setEnabled(false);
                                                    buttonUpdate.setText(R.string.redownload);
                                                    File file = new File(dirPath + "/" + filename);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                        Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, file);
                                                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        intent.setData(apkUri);
                                                        getApplication().startActivity(intent);

                                                    } else {
                                                        Uri apkUri = Uri.fromFile(file);
                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        getApplication().startActivity(intent);
                                                    }

                                                }

                                                @Override
                                                public void onError(Error error) {
                                                    buttonUpdate.setText(R.string.start);
                                                    Toast.makeText(getApplicationContext(), getString(R.string.some_error_occurred), Toast.LENGTH_SHORT).show();
                                                    textViewProgressUpdate.setText("");
                                                    progressBarUpdate.setProgress(0);
                                                    downloadIdUpdate = 0;
                                                    buttonCancelUpdate.setEnabled(false);
                                                    progressBarUpdate.setIndeterminate(false);
                                                    buttonUpdate.setEnabled(true);
                                                }
                                            });
                                }
                            });
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toast.makeText(getApplicationContext(), "Permission is required", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();


        buttonCancelUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadIdUpdate);
            }
        });
    }

    private void jsonParse(String url) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("updatearray");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject videodetails = jsonArray.getJSONObject(i);

                                appname = videodetails.getString("appname");
                                version = videodetails.getString("version");
                                contactus = videodetails.getString("contact");
                                releasedate = videodetails.getString("releasedate");
                                downloadurl = videodetails.getString("downloadurl");
                                description = videodetails.getString("description");
                                updateTextViewd.setText(description);
                                updateappv.append(version);
                                updatecontact.setText(contactus);
                                updaterdate.append(releasedate);
                                updatehead.setText(appname + " " + version);
                                if (downloadurl != null && !downloadurl.equals("")) {
                                    URLDOWNLOAD = downloadurl;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
//        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
//        rootRef.collection("appDetails").document("appInfo")
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isComplete()) {
//                    if (task.getResult().getData() != null) {
//                        Log.e("E: ", String.valueOf(task.getResult().getData().get("appname")));
//                        appname = String.valueOf(task.getResult().getData().get("appname"));
//                        version = String.valueOf(task.getResult().getData().get("version"));
//                        contactus = String.valueOf(task.getResult().getData().get("contact"));
//                        releasedate = String.valueOf(task.getResult().getData().get("releasedate"));
//                        downloadurl = String.valueOf(task.getResult().getData().get("downloadurl"));
//                        description = String.valueOf(task.getResult().getData().get("description"));
//                        updateTextViewd.setText(description);
//                        updateappv.append(version);
//                        updatecontact.setText(contactus);
//                        updaterdate.append(releasedate);
//                        updatehead.setText(appname + " " + version);
//                        if (downloadurl != null && !downloadurl.equals("")) {
//                            URLDOWNLOAD = downloadurl;
//                        }
//                    }
//                }
//            }
//        });
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

