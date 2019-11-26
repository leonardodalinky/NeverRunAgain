package cf.ayajilin.runsimulator;


import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import cf.ayajilin.runsimulator.hooker.MovementHooker;
import cf.ayajilin.runsimulator.hooker.GpsHooker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.services.BaseService;

public class MainHooker implements IXposedHookLoadPackage{
    private final static String CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/runsimulator/config.json";
    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable{
        XposedBridge.log("PACKAGE: " + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals("cn.runagain.run")){
            XposedBridge.log("runagin.");
            if (!CheckEnable()){
                return;
            }
            XposedBridge.log("enabled.");

            GpsHooker gpsHooker = null;
            MovementHooker accelerateHooker = null;
            String accFilePath = "";
            String gpsFilePath = "";

            try {
                try {
                    BaseService baseService = SELinuxHelper.getAppDataFileService();
                    if (!baseService.checkFileExists(CONFIG_FILE_PATH)){
                        throw new FileNotFoundException("Cannot find the config.json!");
                    }

                    InputStream inputStream = baseService.getFileInputStream(CONFIG_FILE_PATH);
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    if (!jsonObject.has("filepath") || !jsonObject.has("gps_file_path")){
                        throw new JSONException("Wrong Config Format!");
                    }
                    else{
                        accFilePath = jsonObject.getString("filepath");
                        gpsFilePath = jsonObject.getString("gps_file_path");
                    }
                }
                catch (Exception e){
                    XposedBridge.log(Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                }

                accelerateHooker = new MovementHooker(accFilePath);
                accelerateHooker.Hook(loadPackageParam.classLoader);

                XposedBridge.log("ACC hooked.");

                gpsHooker = new GpsHooker(gpsFilePath);
                gpsHooker.Hook(loadPackageParam.classLoader);

                XposedBridge.log("GPS hooked.");
            }
            catch (IOException e){
                e.printStackTrace();
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public boolean CheckEnable(){
        BaseService baseService = SELinuxHelper.getAppDataFileService();
        if (!baseService.checkFileExists(CONFIG_FILE_PATH)){
            return false;
        }

        JSONObject jsonObject = null;
        try{
            InputStream inputStream = baseService.getFileInputStream(CONFIG_FILE_PATH);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            jsonObject = new JSONObject(new String(bytes));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{
            if (!jsonObject.has("enabled")){
                return false;
            }
            else{
                return jsonObject.getBoolean("enabled");
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        return false;
    }

}
