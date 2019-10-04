package cf.ayajilin.runsimulator.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import cf.ayajilin.runsimulator.File.AccRecorder;
import cf.ayajilin.runsimulator.File.Common;
import cf.ayajilin.runsimulator.R;


public class RecordFragment extends Fragment {
    private View selfView;

    private String textFolderPath = "";
    private boolean isRecording = false;

    private TextView textViewFolder;
    private Button buttonRecord;
    private LinearLayout accFileLayout;

    private AccRecorder mAccRecorder;

    public RecordFragment() {
        // Required empty public constructor
    }

    public interface IRecordFragmentCallback{
        void OnStoreFileClick(String filepath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_accrecord, container, false);
        selfView = v;
        Initialize(v);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAccRecorder != null)
            mAccRecorder.Stop();
    }

    public void SetStoreFilePath(String str){
        if (textFolderPath != null)
            textViewFolder.setText(str);

        textFolderPath = str;
    }

    private void Initialize(View v){
        textViewFolder = v.findViewById(R.id.textFileStore);
        buttonRecord = v.findViewById(R.id.buttonRecord);
        accFileLayout = v.findViewById(R.id.AccFileLayout);

        textFolderPath = Environment.getExternalStorageDirectory().getPath();
        isRecording = false;
        textViewFolder.setText(textFolderPath);

        mAccRecorder = new AccRecorder(v.getContext());

        accFileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFolderSelector();
            }
        });

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAccRecorder.setRecordFolderPath(textFolderPath);
                    if (!isRecording){
                        mAccRecorder.Start();
                        isRecording = true;
                        buttonRecord.setText(getResources().getString(R.string.button_stop_record));
                    }
                    else{
                        mAccRecorder.Stop();
                        isRecording = false;
                        buttonRecord.setText(getResources().getString(R.string.button_start_record));
                    }
                }
                catch (IOException e){
                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }

    private void OpenFolderSelector(){
        // TODO:改进，引用其他的选择文件夹的库
        if (Build.VERSION.SDK_INT <= 21){
            Toast.makeText(selfView.getContext(),
                    "Your Android Version didn't support to change the folder.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(intent, "Choose Application to Set Storing Folder"), Common.ACCSTOREFOLDER_SELECTOR_CODE);
    }
// end of RecordFragment.class
}
