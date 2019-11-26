package cf.ayajilin.runsimulator.Fragment;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Arrays;

import cf.ayajilin.runsimulator.File.Common;
import cf.ayajilin.runsimulator.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class Main2ActivityFragment extends Fragment {
    private View selfView;
    private boolean isOn;
    private String accFilePath;
    private String gpsFilePath;

    private TextView textFile;
    private TextView gpsFile;
    private Switch switchOn;
    private LinearLayout accFileLayout;
    private LinearLayout gpsFileLayout;

    public Main2ActivityFragment() {
    }

    public interface IMain2FragmentCallback{
        void OnMain2Click(String filepath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main2, container, false);
        selfView = v;
        Initialize(v);
        return v;
    }

    private void Initialize(View v) {
        textFile = v.findViewById(R.id.textFile);
        gpsFile = v.findViewById(R.id.gpsFile);
        switchOn = v.findViewById(R.id.switchOn);
        accFileLayout = v.findViewById(R.id.AccFileLayout);
        gpsFileLayout = v.findViewById(R.id.GPSFileLayout);

        try{
            if (!Common.ExistConfig() || Common.ConfigEmpty()){
                Common.CreateConfig();
            }

            isOn = false;
            accFilePath = "";
            gpsFilePath = "";
            JSONObject jsonConfig = Common.GetJson();
            if (jsonConfig.has("enabled")){
                isOn = jsonConfig.getBoolean("enabled");
            }
            if (jsonConfig.has("filepath")){
                accFilePath = jsonConfig.getString("filepath");
            }
            if (jsonConfig.has("gps_file_path")){
                gpsFilePath = jsonConfig.getString("gps_file_path");
            }

            switchOn.setChecked(isOn);
            textFile.setText(accFilePath);
            gpsFile.setText(gpsFilePath);

            // 开关的监听事件
            switchOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try{
                        isOn = isChecked;
                        Log.d("", "" + isOn);
                        SaveToConfig();
                    }
                    catch (Exception e){
                        Toast.makeText(buttonView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }
                }
            });

            accFileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenAccFileSelector();
                }
            });

            gpsFileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenGpsFileSelector();
                }
            });

        }
        catch (Exception e){
            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

    public void SetFilePath(String str){
        if (textFile != null)
            textFile.setText(str);

        accFilePath = str;
        SaveToConfig();
    }

    public void SetGPSFilePath(String str){
        if (gpsFile != null)
            gpsFile.setText(str);

        gpsFilePath = str;
        SaveToConfig();
    }

    private void OpenAccFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose"), Common.ACCFILE_SELECTOR_CODE);
    }

    private void OpenGpsFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose"), Common.GPSFILE_SELECTOR_CODE);
    }

    private void SaveToConfig(){
        try{
            Common.Put("filepath", accFilePath);
            Common.Put("gps_file_path", gpsFilePath);
            Common.Put("enabled", isOn);
        }
        catch (Exception e){
            Toast.makeText(selfView.getContext(), Arrays.toString(e.getStackTrace()), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

    // end of Main2ActivityFragment.class
}
