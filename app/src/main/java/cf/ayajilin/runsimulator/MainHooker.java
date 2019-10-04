package cf.ayajilin.runsimulator;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonReader;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cf.ayajilin.runsimulator.hooker.MovementHooker;
import cf.ayajilin.runsimulator.hooker.GpsHooker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.services.BaseService;

public class MainHooker implements IXposedHookLoadPackage{
    private final static String FILE_STORE_PATH = Environment.getExternalStorageDirectory().getPath() + "/runsimulator/config.json";
    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable{
        if (loadPackageParam.packageName.equals("cn.runagain.run")){
            if (!CheckEnable()){
                return;
            }

            GpsHooker gpsHooker = new GpsHooker();
            MovementHooker accelerateHooker = null;
            try{
                accelerateHooker = new MovementHooker(FILE_STORE_PATH);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            gpsHooker.Hook(loadPackageParam.classLoader);
            accelerateHooker.Hook(loadPackageParam.classLoader);
        }
    }

    public boolean CheckEnable(){
        BaseService baseService = SELinuxHelper.getAppDataFileService();
        if (!baseService.checkFileExists(FILE_STORE_PATH)){
            return false;
        }

        JSONObject jsonObject = null;
        try{
            InputStream inputStream = baseService.getFileInputStream(FILE_STORE_PATH);
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
