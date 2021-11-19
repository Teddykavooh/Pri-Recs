package com.edit.pri_recs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class StartupReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Boot completed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Boot done or Error occurred", Toast.LENGTH_LONG).show();
        }
        Intent myIntent = new Intent(context, FileSystemObserverService.class);
        context.startService(myIntent);
        Log.e("onReceive", "Is my service starting");
        Log.d("RECEIVER", "ENTERED");
    }
}
