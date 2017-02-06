package com.tokinonagare.fakewechatlocation;

/**
 * Created by harry on 1/25/17.
 */

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

class HookUtils {
    static void HookAndChange(ClassLoader classLoader, final double latitude, final double longitude) {

        findAndHookMethod("android.location.GpsStatus", classLoader,
                "setStatus", int.class, int[].class, float[].class,
                float[].class, float[].class, int.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
                            throws Throwable {
                        Log.e("debug", "hook GpsStatus.setStatus success!");

                        int svCount = (Integer) param.args[0];
                        Log.e("debug", "svCount = " + svCount);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                    }
                });

        findAndHookMethod("android.net.wifi.WifiManager", classLoader,
                "getScanResults", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        findAndHookMethod("android.telephony.TelephonyManager",
                classLoader, "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        hook_methods("android.location.LocationManager", "requestLocationUpdates",
                new XC_MethodHook() {
                    //		findAndHookMethod("android.location.LocationManager",
//				classLoader, "requestLocationUpdates",
//				new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        XposedBridge.log("hook requestLocationUpdates success");

                        if (((param.args.length == 4) || param.args.length == 5)
                                && (param.args[0] instanceof String)) {
                            LocationListener ll = (LocationListener) param.args[3];

                            Class<?> clazz = LocationListener.class;
                            Method m = null;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName()
                                        .equals("onLocationChanged")) {
                                    m = method;

                                    XposedBridge
                                            .log("#####hook onLocationChanged success");
                                    break;
                                }
                            }

                            try {
                                if (m != null) {
                                    Object[] args = new Object[1];
                                    Location location = new Location(
                                            LocationManager.GPS_PROVIDER);

                                    location.setLatitude(latitude);
                                    location.setLongitude(longitude);

                                    args[0] = location;

                                    m.invoke(ll, args);
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }
                    }
                });

        hook_methods("android.location.LocationManager", "getGpsStatus", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                XposedBridge.log("hook getGpsStatus success");

                GpsStatus gss = (GpsStatus) param.getResult();

                if (gss == null) {
                    XposedBridge.log("enter if (gss == null)");

                    return;
                }

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

                m.setAccessible(true);

                // make the apps belive GPS works fine now
                int svCount = 5;
                int[] prns = { 1, 2, 3, 4, 5 };
                float[] snrs = { 0, 0, 0, 0, 0 };
                float[] elevations = { 0, 0, 0, 0, 0 };
                float[] azimuths = { 0, 0, 0, 0, 0 };
                int ephemerisMask = 0x1f;
                int almanacMask = 0x1f;

                int usedInFixMask = 0x1f;

                try {
                    if (m != null) {
                        m.invoke(gss, svCount, prns, snrs, elevations,
                                azimuths, ephemerisMask, almanacMask,
                                usedInFixMask);
                        param.setResult(gss);
                    }
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }
        });
    }

    private static void hook_methods(String className, String methodName, XC_MethodHook xmh)
    {
        try {
            Class<?> clazz = Class.forName(className);

            for (Method method : clazz.getDeclaredMethods())
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, xmh);
                }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }
}

