package com.tokinonagare.fakewechatlocation;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by harry on 1/25/17.
 */

public class HookLoadPackage implements IXposedHookLoadPackage{

    private static final String TAG = "Debug";
    private SharedLocationData sharedLocationData = new SharedLocationData();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;

        sharedLocationData.reload();

        double la = Double.parseDouble(sharedLocationData.getString("la", "14.546748"));
        double lo = Double.parseDouble(sharedLocationData.getString("lo", "121.0523612"));
        int mcc = Integer.parseInt(sharedLocationData.getString("mcc", "512"));
        int mnc = Integer.parseInt(sharedLocationData.getString("mnc", "2"));

        XposedBridge.log("handleLoadPackage: ################## la =" + la);
        XposedBridge.log("handleLoadPackage: ################## lo =" + lo);

        HookUtils.HookAndChange(lpparam.classLoader, la, lo, mcc, mnc);
    }
}
