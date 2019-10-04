package cf.ayajilin.runsimulator.File;
// AccReader.class: 用于读取加速度曲线

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.services.BaseService;

public class AccReader {
    private AccReaderHelper mAccReaderHelper = new AccReaderHelper();
    private long mStartTime = -1;
    private String mAccFilePath;

    public AccReader(String path){
        mAccFilePath = path;
        try{
            LoadAccFile();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void SetAbsStartTime(long time){
        if (time >= 0)
            mStartTime = time;
    }

    public float[] GetAbsTimeAcc(long time){
        return mAccReaderHelper.GetAcc(time - mStartTime);
    }

    public float[] GetRelTimeAcc(long time){
        return mAccReaderHelper.GetAcc(time);
    }

    private void LoadAccFile() throws IOException{
        String content = null;
        try{
            BaseService baseService = SELinuxHelper.getAppDataFileService();

            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = baseService.getFileInputStream(mAccFilePath);

            byte[] buf = new byte[1024];
            int rlen = -1;
            while((rlen = inputStream.read(buf)) != -1) {
                stringBuilder.append(new String(buf, 0, rlen));
            }
            content = stringBuilder.toString();
            inputStream.close();
        }
        catch (IOException err){
            throw err;
        }

        try{
            JSONArray jsonArray = new JSONArray(content);
            mAccReaderHelper.SetJson(jsonArray);
        }
        catch (JSONException err){
            err.printStackTrace();
        }
    }

    // 用于读取从json的数据中读取对应点的数据
    private class AccReaderHelper{
        private ArrayList<Long> mTimeList;
        private ArrayList<float[]> mAccList;
        private long mMaxTime;

        public AccReaderHelper(){
            mTimeList = new ArrayList<>();
            mAccList = new ArrayList<>();
        }

        public void Clear(){
            mTimeList.clear();
            mAccList.clear();
            mMaxTime = Long.MIN_VALUE;
        }

        public void SetJson(JSONArray jsonArray){
            Clear();
            try{
                for (int i = 0;i < jsonArray.length();i++){
                    JSONArray js = jsonArray.getJSONArray(i);
                    Long newlong = js.getLong(0);
                    if (newlong > mMaxTime)
                        mMaxTime = newlong;

                    float[] newfloats = new float[3];
                    for (int j = 0;j < 3;j++){
                        newfloats[j] = (float)js.getDouble(j + 1);
                    }
                    mTimeList.add(newlong);
                    mAccList.add(newfloats);
                }
            }
            catch (JSONException err){
                err.printStackTrace();
            }
        }

        // 获得某时刻的加速度（可周期循环,并线性拟合）
        public float[] GetAcc(long time){
            long cTime = GetTimeCirculated(time);
            int[] indexs = FindNeareastIndexs(cTime);
            float[] ans = new float[3];

            long ltime = mTimeList.get(indexs[0]);
            long rtime = mTimeList.get(indexs[1]);
            float[] laccs = mAccList.get(indexs[0]);
            float[] raccs = mAccList.get(indexs[1]);
            float ratio = (cTime - ltime) / (rtime - ltime);
            ans[0] = laccs[0] * ratio + raccs[0] * (1 - ratio);
            ans[1] = laccs[1] * ratio + raccs[1] * (1 - ratio);
            ans[2] = laccs[2] * ratio + raccs[2] * (1 - ratio);
            return ans;
        }

        // 将时间改到周期内
        private long GetTimeCirculated(long time){
            long ans = time % mMaxTime;
            if (ans < 0)
                ans += mMaxTime;

            return ans;
        }

        // 找寻与时间time最相近的时间的序号
        private int FindNeareastIndex(long time){
            return _FindNearestIndex(time, 0, mTimeList.size() - 1);
        }

        private int _FindNearestIndex(long time, int lindex, int rindex){
            if (rindex < lindex)
                return -1;
            else if (mTimeList.get(lindex) == time)
                return lindex;
            else if (mTimeList.get(rindex) == time)
                return rindex;
            // 继续缩小区间
            else if (lindex != rindex - 1){
                int nextIndex = (lindex + rindex) / 2;
                long nextTime = mTimeList.get(nextIndex);
                if (nextTime <= time){
                    return _FindNearestIndex(time, nextIndex, rindex);
                }
                else{
                    return _FindNearestIndex(time, lindex, nextIndex);
                }
            }
            else{
                long dleft = time - mTimeList.get(lindex);
                long dright = mTimeList.get(rindex) - time;
                return (dleft > dright)? rindex : lindex;
            }
        }

        // 找寻与时间time最相近的时间段的始末序号
        private int[] FindNeareastIndexs(long time){
            return _FindNearestIndexs(time, 0, mTimeList.size() - 1);
        }

        private int[] _FindNearestIndexs(long time, int lindex, int rindex){
            if (rindex < lindex){
                return null;
            }
            else if (mTimeList.get(lindex) == time){
                int[] ans = new int[2];
                ans[0] = lindex;
                ans[1] = lindex + 1;
                return ans;
            }
            else if (mTimeList.get(rindex) == time){
                int[] ans = new int[2];
                ans[0] = rindex - 1;
                ans[1] = rindex;
                return ans;
            }
                // 继续缩小区间
            else if (lindex != rindex - 1){
                int nextIndex = (lindex + rindex) / 2;
                long nextTime = mTimeList.get(nextIndex);
                if (nextTime <= time){
                    return _FindNearestIndexs(time, nextIndex, rindex);
                }
                else{
                    return _FindNearestIndexs(time, lindex, nextIndex);
                }
            }
            else{
                int[] ans = new int[2];
                ans[0] = lindex;
                ans[1] = rindex;
                return ans;
            }
        }

        // end of AccReadHelper
    }
    // end of AccReader
}
