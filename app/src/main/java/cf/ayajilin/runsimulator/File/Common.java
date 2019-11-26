package cf.ayajilin.runsimulator.File;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Common {
    public final static int ACCFILE_SELECTOR_CODE = 1919;
    public final static int ACCSTOREFOLDER_SELECTOR_CODE = 810;
    public final static int REQUEST_PERMISSION_CODE = 114;
    public final static int GPSFILE_SELECTOR_CODE = 514;
    public final static String[] PERMISSION_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public final static String FILE_STORE_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/runsimulator/";
    public final static String FILE_STORE_PATH = Environment.getExternalStorageDirectory().getPath() + "/runsimulator/config.json";
    public static boolean ExistConfig(){
        File file = new File(FILE_STORE_PATH);
        return file.exists();
    }

    public static boolean ConfigEmpty(){
        if (!ExistConfig())
            return true;
        try{
            FileInputStream fileInputStream = new FileInputStream(FILE_STORE_PATH);
            return (fileInputStream.available() == 0);
        }
        catch (IOException e){
            return false;
        }

    }

    public static void CreateConfig() throws IOException {
        try{
            if (!ExistConfig()){
                File folder = new File(FILE_STORE_FOLDER);
                if (!folder.exists())
                    folder.mkdirs();
                File file = new File(FILE_STORE_PATH);
                file.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(file);
                String str = "{\"enabled\":false,\"filepath\":\"\"}";
                outputStream.write(str.getBytes());
            }
        }
        catch (IOException e){
            throw new IOException("Create Config Error", e);
        }
    }

    public static void Put(String key, boolean value) throws IOException, JSONException {
        if (!ExistConfig())
            throw new IOException("Cannot Find the Config.json");

        JSONObject jsonObject = GetJson();
        jsonObject.put(key, value);
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_STORE_PATH);
        fileOutputStream.write(jsonObject.toString().getBytes());

    }

    public static void Put(String key, String value) throws IOException, JSONException {
        if (!ExistConfig())
            throw new IOException("Cannot Find the Config.json");

        JSONObject jsonObject = GetJson();
        jsonObject.put(key, value);
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_STORE_PATH);
        fileOutputStream.write(jsonObject.toString().getBytes());

    }

    public static JSONObject GetJson() throws IOException, JSONException {
        try{
            if (!ExistConfig())
                CreateConfig();
            FileInputStream inputStream = new FileInputStream(FILE_STORE_PATH);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            JSONObject jsonObject = new JSONObject(new String(bytes));

            if (jsonObject.length() == 0) throw new JSONException("Invalid Config.json");

            return jsonObject;
        }
        catch (IOException e){
            throw new IOException("Read or Create Config Error", e);
        }
        catch (JSONException e){
            throw e;
        }
    }

    public static Object GetValue(String key) throws IOException, JSONException {
        if (!ExistConfig())
            throw new IOException("Cannot Find the Config.json");

        try{
            JSONObject jsonObject = GetJson();
            if (jsonObject.has(key)){
                return jsonObject.get(key);
            }
            else
                throw new JSONException("Key Error");
        }
        catch (JSONException e){
            throw e;
        }
        catch (IOException e){
            throw e;
        }
    }

    public static void AskForPermission(Activity activity){

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }
}
