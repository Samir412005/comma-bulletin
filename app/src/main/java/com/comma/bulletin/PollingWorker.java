package com.comma.bulletin;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
/** Kept for existing WorkManager records across upgrades; never fetch in background. */
public class PollingWorker extends Worker {
 public PollingWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);}
 @NonNull @Override public Result doWork(){return Result.success();}
 public static void schedule(Context c){WorkManager.getInstance(c).cancelUniqueWork("bulletin-poll");}
}
