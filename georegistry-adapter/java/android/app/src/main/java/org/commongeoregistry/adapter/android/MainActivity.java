package org.commongeoregistry.adapter.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.commongeoregistry.client.android.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
