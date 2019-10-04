package cf.ayajilin.runsimulator.File;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

public class AccRecorder {
    private Context mContext;
    private SensorManager mSensorManager;
    private String mRecordFileFolderPath;

    private File mFile;
    private FileOutputStream mFileOutputStream;
    private JSONArray mJsonArray;

    private long mStartTime;
    private AccRecorderListener mAccRecorderListener;
    private MyHandler mHandler;

    private boolean isRecording = false;

    public AccRecorder(Context context){
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mHandler = new MyHandler(context, this);
        mJsonArray = new JSONArray();
    }


    public String getRecordFileFolderPath(){
        return mRecordFileFolderPath;
    }

    public void setRecordFolderPath(String path){
        mRecordFileFolderPath = path;
    }

    public void Start() throws IOException{
        if (isRecording)
            return;
        else
            isRecording = true;

        if (!IsFolderPathValid())
            throw new IOException("Invalid Folder");

        mStartTime = (new Date()).getTime();
        mAccRecorderListener = new AccRecorderListener();
        Sensor accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mAccRecorderListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // 打开并创建文件
        String newpath = mRecordFileFolderPath + "/" + (new Date()).getTime() + ".json";
        mFile = new File(newpath);
        try{
            if (!mFile.exists()){
                mFile.createNewFile();
            }
            mFileOutputStream = new FileOutputStream(mFile);
            mFileOutputStream.write("".getBytes());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void Stop(){
        if (!isRecording)
            return;
        else
            isRecording = false;

        mSensorManager.unregisterListener(mAccRecorderListener);
        try {
            mFileOutputStream.write(mJsonArray.toString().getBytes());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean IsFolderPathValid(){
        File folder = new File(mRecordFileFolderPath);
        return folder.exists();
    }

    private class AccRecorderListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)){
                Message message = Message.obtain();
                message.obj = event.values;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Nothing
        }
    }

    private static class MyHandler extends Handler{
        private WeakReference<Context> reference;

        private AccRecorder parent;
        private ArrayList<Long> timeSet = new ArrayList<>();

        MyHandler(Context context, AccRecorder p){
            reference = new WeakReference<>(context);
            parent = p;
        }

        @Override
        public void handleMessage(Message msg){
            Long nowTime = (new Date()).getTime();
            if (timeSet.contains(nowTime)){
                return;
            }
            else{
                timeSet.add(nowTime);
            }

            float[] accArray = (float[])msg.obj;
            JSONArray newJsonArray = new JSONArray();
            newJsonArray.put(nowTime - parent.mStartTime);
            for (int i = 0;i < 3;i++){
                try{
                    newJsonArray.put(accArray[i]);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
            parent.mJsonArray.put(newJsonArray);
        }
    }
}
