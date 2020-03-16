package com.piaopiao.ymhbutterknife;

import android.app.Activity;

public class YmhButterKnife {

    public static void binder(Activity activity) {
        String name = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<?> aClass = Class.forName(name);
            IBinder iBinder = (IBinder) aClass.newInstance();
            iBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
