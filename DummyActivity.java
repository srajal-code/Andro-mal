package com.example.payload;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DummyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start service and die immediately
        startService(new Intent(this, MainActivity.class));
        finish(); // Older Android compatible closure
    }
}
