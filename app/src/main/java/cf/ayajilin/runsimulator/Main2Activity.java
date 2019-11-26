package cf.ayajilin.runsimulator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import cf.ayajilin.runsimulator.File.Common;
import cf.ayajilin.runsimulator.Fragment.Main2ActivityFragment;
import cf.ayajilin.runsimulator.Fragment.RecordFragment;

public class Main2Activity extends AppCompatActivity
        implements Main2ActivityFragment.IMain2FragmentCallback, RecordFragment.IRecordFragmentCallback
{
    private Main2ActivityFragment main2ActivityFragment;
    private RecordFragment recordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        main2ActivityFragment = new Main2ActivityFragment();
        ft.add(R.id.FrameLayout, main2ActivityFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main) {
            if (main2ActivityFragment == null)
                main2ActivityFragment = new Main2ActivityFragment();

            ft.replace(R.id.FrameLayout,main2ActivityFragment);
            ft.commit();
            return true;
        }
        else if (id == R.id.action_record){
            if (recordFragment == null)
                recordFragment = new RecordFragment();

            ft.replace(R.id.FrameLayout, recordFragment);
            ft.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode & 0xffff) == Common.ACCFILE_SELECTOR_CODE &&
                resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            OnMain2Click(uri.getPath());
        } else if ((requestCode & 0xffff) == Common.GPSFILE_SELECTOR_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            OnGPSFileSelectorClick(uri.getPath());
        } else if ((requestCode & 0xffff) == Common.ACCSTOREFOLDER_SELECTOR_CODE
                && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            OnStoreFileClick(uri.getPath());
        }
    }

    @Override
    public void OnMain2Click(String filepath) {
        if (main2ActivityFragment == null)
            main2ActivityFragment = new Main2ActivityFragment();

        main2ActivityFragment.SetFilePath(filepath);
    }

    public void OnGPSFileSelectorClick(String filepath) {
        if (main2ActivityFragment == null)
            main2ActivityFragment = new Main2ActivityFragment();

        main2ActivityFragment.SetGPSFilePath(filepath);
    }

    @Override
    public void OnStoreFileClick(String filepath) {
        if (recordFragment == null)
            recordFragment = new RecordFragment();

        Log.d("", filepath);
        recordFragment.SetStoreFilePath(filepath);
    }
}
