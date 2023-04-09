package cz.hybl.devicenamewallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class RestrictionsReceiver extends BroadcastReceiver {


    public RestrictionsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DisplayUpdater.updateDisplay(context);
    }


}
