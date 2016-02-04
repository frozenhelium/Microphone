package com.fhx.microphone;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Created by fhx on 2/4/16.
 */
public class RecordingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_recordings);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.frame_content_recordings, new RecordingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
