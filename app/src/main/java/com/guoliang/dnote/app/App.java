package com.guoliang.dnote.app;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by XhinLiang on 2017/5/13.
 * xhinliang@gmail.com
 */
public class App extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "4tr8DCDJVfHMq4fzWlf4Ey5D-gzGzoHsz", "b0bK6SrjcTURP1gLHtHL4O5t");
        context = this;
    }
}
