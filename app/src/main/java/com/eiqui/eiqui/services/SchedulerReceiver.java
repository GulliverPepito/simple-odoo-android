// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.eiqui.eiqui.fragments.SettingsFragment;
import com.eiqui.eiqui.utils.Constants;

/**
 * Created by uchar on 20/09/16.
 */

public class SchedulerReceiver extends BroadcastReceiver {
    static public final Integer SCHEDULER_TASK_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        ComponentName mServiceComponent = new ComponentName(context, EiquiJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(SCHEDULER_TASK_ID, mServiceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);
        builder.setPeriodic(Long.parseLong(settings.getString(SettingsFragment.KEY_PREF_SYNC_REFRESH_DELAY, "180000")));
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
        jobScheduler.schedule(builder.build());
    }

}
