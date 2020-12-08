package com.nankai.flutter_nearby_connections.wifip2p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class WifiDirectAutoAccept {
    public static final String TAG = "WifiDirectAutoAccept";

    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final Object dialogListener;
    private Class<?> dialogInterface = null;
    private Method dialogListenerMethod = null;

    @SuppressLint("PrivateApi")
    private WifiDirectAutoAccept(Context context, WifiP2pManager m, WifiP2pManager.Channel c) {
        if (context != null && m == null) {
            manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        } else {
            manager = m;
        }
        if (context != null && c == null) {
            channel = manager.initialize(context, context.getMainLooper(), null);
        } else {
            channel = c;
        }

        Log.d(TAG, "manager: " + manager.hashCode());
        Log.d(TAG, "channel: " + channel.hashCode());

        try {
            dialogInterface = Class.forName("android.net.wifi.p2p.WifiP2pManager$DialogListener");
            dialogListenerMethod = manager.getClass().getMethod("setDialogListener",
                    WifiP2pManager.Channel.class,
                    dialogInterface);
        } catch (NoSuchMethodException ex) {
            // Ignore
            Log.d(TAG, "NoSuchMethod");
        } catch (ClassNotFoundException ex) {
            // Ignore
            Log.d(TAG, "ClassNotFound");
        }

        dialogListener = newDialogListener();
    }

    public WifiDirectAutoAccept(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this(null, manager, channel);
    }

    public void intercept(boolean enable) {
        if (enable) {
            setDialogListener(dialogListener);
        } else {
            setDialogListener(null);
        }
    }

    private Object newDialogListener() {
        Object dialogListener;

        if (manager == null || channel == null || dialogInterface == null) {
            return null;
        }

        final Object object = new DialogListenerProxy(manager, channel);

        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Method proxyMethod = null;
                for (Method m : DialogListenerProxy.class.getMethods()) {
                    if (m.getName().equals(method.getName())) {
                        proxyMethod = m;
                        break;
                    }
                }

                if (proxyMethod != null) {
                    try {
                        proxyMethod.invoke(object, args);
                    } catch (IllegalArgumentException ex) {
                        Log.d(TAG, ex.getClass().getName());
                    }
                } else {
                    Log.d(TAG, "No method found: " + method.getName());
                }
                return null;
            }
        };

        dialogListener = Proxy.newProxyInstance(DialogListenerProxy.class.getClassLoader(),
                new Class[]{dialogInterface}, handler);

        return dialogListener;
    }

    private void setDialogListener(Object listener) {
        if (dialogListenerMethod == null) {
            return;
        }

        try {
            dialogListenerMethod.invoke(manager, channel, listener);
        } catch (IllegalAccessException ex) {
            // Ignore
            Log.d(TAG, ex.getClass().getName());
        } catch (InvocationTargetException ex) {
            // Ignore
            Log.d(TAG, ex.getClass().getName());
        }
    }

    private static class DialogListenerProxy {

        private final WifiP2pManager manager;
        private final WifiP2pManager.Channel channel;

        public DialogListenerProxy(WifiP2pManager m, WifiP2pManager.Channel c) {
            manager = m;
            channel = c;
        }

        @SuppressWarnings("unused")
        public void onShowPinRequested(String pin) {
            Log.d(TAG, "onShowPinRequested: " + pin);
        }

        @SuppressWarnings("unused")
        public void onConnectionRequested(WifiP2pDevice device, WifiP2pConfig config) {
            Log.d(TAG, "onConnectionRequested");
            Log.d(TAG, "    device: " + device.deviceAddress + " " + device.deviceName);
            Log.d(TAG, "    config: " + config.wps.setup + " " + config.wps.pin);
            manager.connect(channel, config, new ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Log.i(TAG, "Connect success");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Connect failed");
                }
            });
        }

        @SuppressWarnings("unused")
        public void onAttached() {
            Log.d(TAG, "onAttached");
        }

        @SuppressWarnings("unused")
        public void onDetached(int reason) {
            Log.d(TAG, "onDetached: " + reason);
        }
    }
}
