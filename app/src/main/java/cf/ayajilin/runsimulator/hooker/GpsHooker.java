package cf.ayajilin.runsimulator.hooker;

import cf.ayajilin.runsimulator.hooker.Interface.IHooker;
import de.robv.android.xposed.SELinuxHelper;

import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.Iterator;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.services.BaseService;
import cf.ayajilin.runsimulator.File.GPSPlayer;

public class GpsHooker implements IHooker {
    public static final double startLatitude = 40.009650;
    public static final double startLongtitude = 116.33338;
    // 每米对应的经纬度
    public static final double LtitudePerMeter = 9.01e-06;
    // 以m/s为单位
    public static final double speed = 3.0;

    private static final double speedNoiseLevel = 0.3;

    private GPSPlayer gpsPlayer;

    private long startTime = (new Date()).getTime();

    public long GetStartTime() {
        return startTime;
    }

    public GpsHooker(String filePath) throws FileNotFoundException, JSONException {
        BaseService baseService = SELinuxHelper.getAppDataFileService();
        if (!baseService.checkFileExists(filePath)){
            throw new FileNotFoundException("Cannot find the config.json!");
        }

        InputStream inputStream;
        JSONArray jsonArray = null;
        try{
            inputStream = baseService.getFileInputStream(filePath);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            jsonArray = new JSONArray(new String(bytes));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        gpsPlayer = new GPSPlayer(jsonArray, 0.1, 0.1);

    }

    // TODO:加入用户ui，自行选择地图
    public double GetNowLongitude() {
        return gpsPlayer.getLocation().longitude;
        //Date date = new Date();
        //return startLongtitude + (date.getTime() - startTime) / 1000.0 * speed * LtitudePerMeter;
        //return startLongtitude + Math.sin((date.getTime() - startTime) / 1000.0) * speed * LtitudePerMeter;
    }

    public double GetNowLatitude() {
        return gpsPlayer.getLocation().latitude;
        //Date date = new Date();
        //return startLatitude + 0.8f * (date.getTime() - startTime) / 1000.0 * speed * LtitudePerMeter;
    }

    public double GetNowAltitude() {
        return gpsPlayer.getLocation().altitude;
    }

    private LocationManager locationManager = null;
    private ArrayList<LocationListener> locationListeners = new ArrayList<LocationListener>();

    @Override
    public void Hook(ClassLoader classLoader) {
        // 设置开始时间
        Date date = new Date();
        startTime = date.getTime();
        gpsPlayer.begin();

        // 基站信息设置为Null
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getAllCellInfo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        }

        /*
        // 纬度
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getLatitude", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                super.afterHookedMethod(param);

                param.setResult(GetNowLatitude());
            }

        });

        // 经度
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getLongitude", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(startLongtitude);
            }
        });
        // 海拔
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getAltitude", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(50f);
            }
        });
        // 速度
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getSpeed", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(3.2f);
            }
        });

        // 时间
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getTime", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                super.afterHookedMethod(param);
                Date date = new Date();
                param.setResult(date.getTime());
            }
        });
        */

        /*
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });
         */

        /*
        XposedHelpers.findAndHookMethod(LocationManager.class, "requiresCell", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "requiresNetwork", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });
        */

        XposedBridge.hookAllMethods(LocationManager.class, "getProviders", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ArrayList<String> arrayList = new ArrayList<String>();
                arrayList.add("gps");
                param.setResult(arrayList);
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "getBestProvider", Criteria.class, Boolean.TYPE, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("gps");
            }
        });

        XposedHelpers.findAndHookMethod("android.location.LocationManager", classLoader,
                "getGpsStatus", GpsStatus.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GpsStatus gss = (GpsStatus) param.getResult();
                        if (gss == null)
                            return;

                        Class<?> clazz = GpsStatus.class;
                        Method m = null;
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.getName().equals("setStatus")) {
                                if (method.getParameterTypes().length > 1) {
                                    m = method;
                                    break;
                                }
                            }
                        }
                        if (m == null)
                            return;

                        //access the private setStatus function of GpsStatus
                        m.setAccessible(true);

                        //make the apps belive GPS works fine now
                        int svCount = 5;
                        int[] prns = {1, 2, 3, 4, 5};
                        float[] snrs = {0, 0, 0, 0, 0};
                        float[] elevations = {0, 0, 0, 0, 0};
                        float[] azimuths = {0, 0, 0, 0, 0};
                        int ephemerisMask = 0x1f;
                        int almanacMask = 0x1f;

                        //5 satellites are fixed
                        int usedInFixMask = 0x1f;

                        XposedHelpers.callMethod(gss, "setStatus", svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                        param.args[0] = gss;
                        param.setResult(gss);
                        try {
                            m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                            param.setResult(gss);
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("android.location.LocationManager", classLoader,
                "getLastKnownLocation", String.class, new XC_MethodHook(){
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable{
                        Location l = new Location(LocationManager.GPS_PROVIDER);
                        l.setLatitude(GetNowLatitude());
                        l.setLongitude(GetNowLongitude());
                        l.setAltitude(GetNowAltitude());
                        //l.setAltitude(50.0f);
                        l.setAccuracy(10.00f);
                        l.setSpeed(3.2f);
                        l.setTime((new Date()).getTime());
                        param.setResult(l);
                    }
                });

        // 截取locationManager和监听的listener
        XposedHelpers.findAndHookMethod("android.location.LocationManager", classLoader,
                "requestLocationUpdates",
                String.class, long.class, float.class, LocationListener.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        try{
                            if (param.thisObject != null) locationManager = (LocationManager)param.thisObject;
                            if (param.args[3] != null && !locationListeners.contains((LocationListener)param.args[3]))
                                locationListeners.add((LocationListener)param.args[3]);
                        }
                        catch (Exception e){
                            // Nothing
                        }
                    }
                });
        SpyOnLocation();

        /*
        for (Method method : LocationManager.class.getDeclaredMethods()) {
            if (method.getName().equals("requestLocationUpdates")
                    && !Modifier.isAbstract(method.getModifiers())
                    && Modifier.isPublic(method.getModifiers())) {
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args.length >= 4 && (param.args[3] instanceof LocationListener)) {

                            LocationListener ll = (LocationListener) param.args[3];

                            Class<?> clazz = LocationListener.class;
                            Method m = null;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                    m = method;
                                    break;
                                }
                            }
                            Location l = new Location(LocationManager.GPS_PROVIDER);
                            l.setLatitude(GetNowLatitude());
                            l.setLongitude(GetNowLongitude());
                            l.setAltitude(50.0f);
                            l.setAccuracy(10.00f);
                            l.setSpeed(3.2f);
                            l.setTime((new Date()).getTime());
                            //XposedHelpers.callMethod(ll, "onLocationChanged", l);

                            try {
                                if (m != null) {
                                    m.invoke(ll, l);
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }

                        }
                    }


                });
            }
         */

            /*
            if (method.getName().equals("requestSingleUpdate ")
                    && !Modifier.isAbstract(method.getModifiers())
                    && Modifier.isPublic(method.getModifiers())) {
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args.length >= 3 && (param.args[1] instanceof LocationListener)) {

                            LocationListener ll = (LocationListener) param.args[3];

                            Class<?> clazz = LocationListener.class;
                            Method m = null;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                    m = method;
                                    break;
                                }
                            }

                            try {
                                if (m != null) {
                                    Location l = new Location(LocationManager.GPS_PROVIDER);
                                    l.setLatitude(GetNowLatitude());
                                    l.setLongitude(GetNowLongitude());
                                    l.setAccuracy(100f);
                                    l.setTime(0);
                                    m.invoke(ll, l);
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }
                    }
                });
            }
            */
    }

    public void SpyOnLocation(){
        Thread SpyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1200);
                        //XposedBridge.log(Boolean.toString(locationManager == null));
                        if (locationManager != null){
                            Location l = new Location(LocationManager.GPS_PROVIDER);
                            l.setLatitude(GetNowLatitude());
                            l.setLongitude(GetNowLongitude());
                            l.setAltitude(GetNowAltitude());
                            l.setAltitude(50.0f);
                            l.setAccuracy(10.00f);
                            l.setSpeed(3.2f);
                            l.setTime((new Date()).getTime());

                            for (Iterator it = locationListeners.iterator();it.hasNext();){
                                try{
                                    LocationListener lListener = (LocationListener) it.next();
                                    lListener.onLocationChanged(l);
                                }
                                catch (Exception e){
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
        });
        SpyThread.start();
    }
}


