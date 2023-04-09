package cz.hybl.devicenamewallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;

public class DisplayUpdater {
    private static final String DEVICE_NAME_RESTRICTION_NAME = "device_name";
    private static final String SIGNATURE_RESTRICTION_NAME = "signature";

    private static final String NAME_METHOD = "https://prod-160.westeurope.logic.azure.com/workflows/91494e5ca9b14883af6c135fd407518a/triggers/manual/paths/invoke/device/";

    private static final String ENCODING = "UTF-8";

    public static void updateDisplay(Context context) {
        // Check current configuration settings, change your app's UI and
        // functionality as necessary.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getSystemService(WindowManager.class).getDefaultDisplay().getMetrics(displayMetrics);
        // run on another thread
        new Handler().post(() -> {
            // display toast
            handleStartBG(context, displayMetrics.widthPixels, displayMetrics.heightPixels);
        });
    }

    private static Bitmap textAsBitmap(String text, float textSize, int width, int height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(Color.RED);
        paint.setTextAlign(Paint.Align.LEFT);


        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 100, textSize + 200, paint);

        return image;
    }

    private static void handleStartBG(Context context, int width, int height) {
        RestrictionsManager restrictionsManager = context.getSystemService(RestrictionsManager.class);
        // Get the current configuration bundle
        // String deviceName = "05b225ee-2985-47a0-af32-de43d86e8c00";
        Bundle appRestrictions = restrictionsManager.getApplicationRestrictions();
        String deviceName = appRestrictions.getString(DEVICE_NAME_RESTRICTION_NAME);
        String signature = appRestrictions.getString(SIGNATURE_RESTRICTION_NAME);

        if (deviceName == null) {
            Toast.makeText(context, "Cannot update wallpaper - device name not known",
                    Toast.LENGTH_SHORT).show();
        } else {
            new GetNameTask(context, width, height).execute(deviceName, signature);
        }
    }

    private static class GetNameTask extends AsyncTask<String, Integer, String> {
        WeakReference<Context> context;
        int width;
        int height;

        GetNameTask(Context context, int width, int height) {
            this.context = new WeakReference<>(context);
            this.width = width;
            this.height = height;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(NAME_METHOD + strings[0]
                        + "?api-version=" + URLEncoder.encode("2016-10-01", ENCODING)
                        + "&sp=" + URLEncoder.encode("/triggers/manual/run", ENCODING)
                        + "&sv=" + URLEncoder.encode("1.0", ENCODING)
                        + "&sig=" + URLEncoder.encode(strings[1], ENCODING));

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "<unknown>";
                }

                return new BufferedReader(new InputStreamReader((con.getInputStream()))).readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String name) {
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context.get());

            try {
                wallpaperManager.setBitmap(DisplayUpdater.textAsBitmap(name, 70, width, height));
            } catch (IOException e) {
                throw new RuntimeException("Cannot update wallpaper");
            }

            Collection states = new HashSet<>();
            states.add(KeyedAppState.builder()
                    .setKey("Serial number")
                    .setSeverity(KeyedAppState.SEVERITY_INFO)
                    .setMessage("Set as background")
                    .build());

            KeyedAppStatesReporter keyedAppStatesReporter = KeyedAppStatesReporter.create(context.get());
            keyedAppStatesReporter.setStates(states);

            Toast.makeText(context.get(), "Wallpaper updated", Toast.LENGTH_SHORT).show();
        }
    }
}
