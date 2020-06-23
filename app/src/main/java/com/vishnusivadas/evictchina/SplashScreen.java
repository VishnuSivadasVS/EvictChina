package com.vishnusivadas.evictchina;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.vishnusivadas.evictchina.ui.HomePageActivity;
import com.vishnusivadas.evictchina.widget.Tools;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setSystemBarColor(this, R.color.red_evict_china);

                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent);
                finish();

    }
}
