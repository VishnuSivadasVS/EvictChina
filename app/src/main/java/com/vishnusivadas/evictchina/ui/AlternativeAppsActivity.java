package com.vishnusivadas.evictchina.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vishnusivadas.evictchina.R;
import com.vishnusivadas.evictchina.adapter.AlterNativeAdapter;
import com.vishnusivadas.evictchina.model.ModelAlternative;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vishnusivadas.evictchina.widget.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlternativeAppsActivity extends AppCompatActivity {
    List<ModelAlternative> alternativeList = new ArrayList<>();
    AlterNativeAdapter alterNativeAdapter;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternative_apps);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Tools.setSystemBarColor(this, R.color.red_evict_china_light);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        pb = findViewById(R.id.progressBar);
        alterNativeAdapter = new AlterNativeAdapter(this, alternativeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(alterNativeAdapter);
        pb.setVisibility(View.VISIBLE);
        getListItems();
    }


    private void getListItems() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("appList").document("alternativeAppsList").collection("allAlternativeApps").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    Log.e("alldata", document.getId() + " => " + document.getData().get("chinaAppName"));
                    ModelAlternative m = new ModelAlternative();
                    m.setTitle(true);
                    m.setName(document.getData().get("chinaAppName").toString());
                    alternativeList.add(m);
                    /*  List<String> group =*/
                    List<Map<String, String>> l = (List<Map<String, String>>) document.getData().get("alternativeApps");
                    for (int i = 0; i < l.size(); i++) {
                        ModelAlternative model = new ModelAlternative();
                        model.setName(l.get(i).get("name"));
                        model.setUrl(l.get(i).get("url"));
                        model.setTitle(false);
                        alternativeList.add(model);
                    }
                }
                pb.setVisibility(View.GONE);
                alterNativeAdapter.notifyDataSetChanged();
            }
        });

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
