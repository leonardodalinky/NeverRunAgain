package cf.ayajilin.runsimulator.hooker.Interface;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public interface IHooker {
    void Hook(ClassLoader classLoader);
}
