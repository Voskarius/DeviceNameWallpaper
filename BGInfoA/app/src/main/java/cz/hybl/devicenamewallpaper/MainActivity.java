package cz.hybl.devicenamewallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayUpdater.updateDisplay(getBaseContext());

        IntentFilter restrictionsFilter =
                new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);

        registerReceiver(new RestrictionsReceiver(), restrictionsFilter);
        startService(new Intent(this, TimeService.class));
    }

}