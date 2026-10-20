package com.comma.bulletin;
import android.content.*;
import java.util.*;

public final class Repository {
 private static final Object LOCK=new Object();
 private final Context context;
 private final android.content.SharedPreferences prefs;
 private final FeedSource source;
 public Repository(Context c){this(c,new HttpsFeedSource(BuildConfig.FEED_URL));}
 public Repository(Context c, FeedSource s){context=c.getApplicationContext(); prefs=context.getSharedPreferences("bulletin",0);source=s;}
 public List<Item> cached(){try{return Item.parse(prefs.getString("feed",""));}catch(Exception e){return new ArrayList<>();}}
 public boolean isRead(String id){return prefs.getStringSet("read",Collections.emptySet()).contains(id);}
 public void markRead(String id){synchronized(LOCK){Set<String> ids=new HashSet<>(prefs.getStringSet("read",Collections.emptySet())); ids.add(id);prefs.edit().putStringSet("read",ids).apply();}}
 public List<Item> sync() throws Exception {
  synchronized(LOCK){
   String raw=source.fetch(); List<Item> list=Item.parse(raw);
   Set<String> seen=new HashSet<>(prefs.getStringSet("seen",Collections.emptySet()));
   boolean initialized=prefs.getBoolean("initialized",false); List<Item> fresh=new ArrayList<>();
   for(Item i:list){if(initialized&&!seen.contains(i.id))fresh.add(i); seen.add(i.id);}
   if(seen.size()>5000){seen.clear();for(Item i:list)seen.add(i.id);}
   if(!prefs.edit().putString("feed",raw).putStringSet("seen",seen).putBoolean("initialized",true).putLong("last_sync",System.currentTimeMillis()).commit()) throw new Exception("storage");
   if(!fresh.isEmpty()) Notifications.show(context,fresh);
   return list;
  }
 }
 public long lastSync(){return prefs.getLong("last_sync",0);}
}
