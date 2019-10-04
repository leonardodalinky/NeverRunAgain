package cf.ayajilin.runsimulator.hooker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import cf.ayajilin.runsimulator.File.AccReader;
import cf.ayajilin.runsimulator.MainHooker;
import cf.ayajilin.runsimulator.hooker.Interface.IHooker;
import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.services.BaseService;

public class MovementHooker implements IHooker {
    private static long startTime = (new Date()).getTime();
    public static long GetStartTime() {
        return startTime;
    }

    public static float GetCurrentYAccelerate() {
        Date date = new Date();
        float passTime = (date.getTime() - startTime) / 1000f;
        return -3.0f + 7.0f * (float)Math.sin((2 * Math.PI) * passTime);
    }

    public static float GetCurrentXZAccelerate(){
        Date date = new Date();
        float passTime = (date.getTime() - startTime) / 1000f;
        return 0.5f * (float)Math.sin((2 * Math.PI) * passTime);
    }

    public static float GetCurrentStep(){
        Date date = new Date();
        float passTime = (date.getTime() - startTime) / 1000f;
        return passTime * 4f;
    }

    private Context mContext;
    private PackageManager packageManager = null;
    private SensorManager sensorManager = null;
    private Sensor stepSensor = null;
    private ArrayList<SensorEventListener> stepListeners = new ArrayList<>();

    private AccReader mAccReader;

    public MovementHooker(String accFilePath)
            throws FileNotFoundException, JSONException
    {
        BaseService baseService = SELinuxHelper.getAppDataFileService();
        if (!baseService.checkFileExists(accFilePath)){
            throw new FileNotFoundException("Cannot find the config.json!");
        }

        InputStream inputStream;
        JSONObject jsonObject = null;
        try{
            inputStream = baseService.getFileInputStream(accFilePath);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            jsonObject = new JSONObject(new String(bytes));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (!jsonObject.has("filepath")){
            throw new JSONException("Wrong Config Format!");
        }
        else{
            mAccReader = new AccReader(jsonObject.getString("filepath"));
        }
    }

    @Override
    public void Hook(ClassLoader classLoader) {

        // 欺骗系统，假装有StepCountSensor
        XposedHelpers.findAndHookMethod("android.content.ContextWrapper", classLoader,
                "getPackageManager", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (param.getResult() instanceof PackageManager){
                            Class<?> clazz = param.getResult().getClass();
                            Method packageMethod = clazz.getDeclaredMethod("hasSystemFeature", String.class);

                            XposedBridge.hookMethod(packageMethod, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    if (param.args.length == 1){
                                        if (((String)param.args[0]).equals("android.hardware.sensor.stepcounter")){
                                            param.setResult(true);
                                            //XposedBridge.log("stepcounter cheating");
                                        }
                                    }
                                }
                            });

                        }
                    }
                });


        // 通过hook手动添加传感器
        XposedHelpers.findAndHookMethod("android.hardware.SensorManager", classLoader,
                "getDefaultSensor",
                int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if ((int)param.args[0] == Sensor.TYPE_STEP_COUNTER){
                            if (stepSensor == null){
                                Constructor con = Sensor.class.getDeclaredConstructor();
                                con.setAccessible(true);

                                // 伪造一个StepSensor
                                stepSensor = (Sensor)con.newInstance();
                                Field field = Sensor.class.getDeclaredField("mType");
                                field.setAccessible(true);
                                field.set(stepSensor, Sensor.TYPE_STEP_COUNTER);
                            }
                            param.setResult(stepSensor);
                        }
                    }
                });

        /*
        // 更改步数
        XposedHelpers.findAndHookMethod("cn.runagain.run.gear.GearUtil", classLoader,
                "sendRunningData",
                float.class, float.class, int.class, int.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.args[3] = GetCurrentStep();
                        XposedBridge.log("" + param.args[3]);
                    }
                });

         */

        // hook注册器
        XposedHelpers.findAndHookMethod("android.hardware.SensorManager", classLoader,
                "registerListener",
                SensorEventListener.class, Sensor.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        if (param.thisObject != null)
                            sensorManager = (SensorManager) param.thisObject;


                        if (param.args.length >= 3 && (param.args[1] instanceof Sensor)) {
                            // 加速度模拟器
                            if (((Sensor) param.args[1]).getType() == Sensor.TYPE_ACCELEROMETER
                                    && param.args[0] != null) {
                                SensorEventListener sensorEventListener = (SensorEventListener) param.args[0];
                                //XposedBridge.log(Integer.toString(Sensor.TYPE_ACCELEROMETER));
                                Method sensorChangedMethod = sensorEventListener.getClass().getDeclaredMethod("onSensorChanged", SensorEvent.class);
                                XposedBridge.hookMethod(sensorChangedMethod, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        super.beforeHookedMethod(param);
                                        try {
                                            SensorEvent sensorEvent = (SensorEvent) param.args[0];
                                            float[] accs = mAccReader.GetAbsTimeAcc((new Date()).getTime());
                                            for (int i = 0;i < 3;i++){
                                                sensorEvent.values[i] = accs[i];
                                            }
                                            XposedBridge.log("fir:" + accs[0] + ", sec:" +
                                                    accs[1] + ", trd:" + accs[2]);
                                        } catch (Exception ex) {
                                            // Nothing
                                        }
                                    }
                                });
                            } else if (((Sensor) param.args[1]).getType() == Sensor.TYPE_STEP_COUNTER &&
                                    param.args[0] != null) {
                                // 拦截伪造的Sensor
                                param.args[1] = null;
                                // 加入消息队列
                                if (!stepListeners.contains((SensorEventListener) (param.args[0])))
                                    stepListeners.add((SensorEventListener) (param.args[0]));
                            }

                            Run();
                        }
                    }
                });
    }

    public void Run(){
        Thread stepCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(500);
                        if (!stepListeners.isEmpty()){
                            Constructor con = SensorEvent.class.getDeclaredConstructor(int.class);
                            con.setAccessible(true);

                            SensorEvent e = (SensorEvent)con.newInstance(1);
                            e.accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
                            e.sensor = stepSensor;
                            e.timestamp = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.NANOSECONDS);
                            e.values[0] = GetCurrentStep();

                            for (Iterator it = stepListeners.iterator();it.hasNext();){
                                try{
                                    SensorEventListener sListener = (SensorEventListener) it.next();
                                    sListener.onSensorChanged(e);
                                    //XposedBridge.log("Now Step: " + e.values[0]);
                                }
                                catch (Exception ex){
                                    it.remove();
                                }
                            }

                        }
                    }
                    catch (Exception e){
                        continue;
                    }
                }
            }
        }, "StepCheck");
        stepCheckThread.start();
    }
}
