package com.comma.bulletin;
import android.content.*;
import java.util.*;
import org.json.*;

public final class Repository {
 private static final Object LOCK=new Object();
 private final Context context;
 private final SharedPreferences prefs;
 private final FeedSource source;
 public Repository(Context c){this(c,new HttpsFeedSource(BuildConfig.FEED_URL));}
 public Repository(Context c,FeedSource s){context=c.getApplicationContext();prefs=context.getSharedPreferences("bulletin",0);source=s;}
 public List<Item> cached(){try{return Item.parse(prefs.getString("feed",""));}catch(Exception e){return new ArrayList<>();}}
 public boolean isRead(String id){return prefs.getStringSet("read",Collections.emptySet()).contains(id);}
 public int unread(){int n=0;for(Item i:cached())if(!isRead(i.id))n++;return n;}
 public void markRead(String id){synchronized(LOCK){Set<String> ids=new HashSet<>(prefs.getStringSet("read",Collections.emptySet()));ids.add(id);prefs.edit().putStringSet("read",ids).apply();}}
 public List<Item> favorites(){try{return Item.parse(prefs.getString("favorites","{\"items\":[]}"));}catch(Exception e){return new ArrayList<>();}}
 public boolean isFavorite(String id){for(Item i:favorites())if(i.id.equals(id))return true;return false;}
 public void toggleFavorite(Item item){synchronized(LOCK){List<Item> list=favorites();boolean removed=list.removeIf(i->i.id.equals(item.id));if(!removed)list.add(item);saveFavorites(list);}}
 private void saveFavorites(List<Item> list){JSONArray a=new JSONArray();for(Item i:list)a.put(i.json());try{prefs.edit().putString("favorites",new JSONObject().put("version",1).put("items",a).toString()).apply();}catch(JSONException ignored){}}
 public Item find(String id){for(Item i:cached())if(i.id.equals(id))return i;for(Item i:favorites())if(i.id.equals(id))return i;return null;}
 public List<Item> sync() throws Exception {
  synchronized(LOCK){String raw=source.fetch();List<Item> list=Item.parse(raw);
   Set<String> seen=new HashSet<>(prefs.getStringSet("seen",Collections.emptySet()));boolean initialized=prefs.getBoolean("initialized",false);List<Item> fresh=new ArrayList<>();
   for(Item i:list){if(initialized&&!seen.contains(i.id))fresh.add(i);seen.add(i.id);}
   if(seen.size()>5000){seen.clear();for(Item i:list)seen.add(i.id);}
   if(!prefs.edit().putString("feed",raw).putStringSet("seen",seen).putBoolean("initialized",true).putLong("last_sync",System.currentTimeMillis()).commit())throw new Exception("storage");
   List<Item> favorites=favorites();for(int j=0;j<favorites.size();j++)for(Item current:list)if(current.id.equals(favorites.get(j).id)){favorites.set(j,current);break;}saveFavorites(favorites);
   if(!fresh.isEmpty())Notifications.show(context,fresh);return list;
  }
 }
 public long lastSync(){return prefs.getLong("last_sync",0);}
}
