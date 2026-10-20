package com.comma.bulletin;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class PollingWorker extends Worker {
 public PollingWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);}
 @NonNull @Override public Result doWork(){try{new Repository(getApplicationContext()).sync();return Result.success();}catch(Exception e){return getRunAttemptCount()<3?Result.retry():Result.failure();}}
 public static void schedule(Context c){
  Constraints constraints=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
  PeriodicWorkRequest request=new PeriodicWorkRequest.Builder(PollingWorker.class,15,TimeUnit.MINUTES).setConstraints(constraints).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build();
  WorkManager.getInstance(c).enqueueUniquePeriodicWork("bulletin-poll",ExistingPeriodicWorkPolicy.KEEP,request);
 }
}
