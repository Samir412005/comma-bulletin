package com.comma.bulletin;

import org.json.*;
import java.text.*;
import java.util.*;

public final class Item {
 public final String id,title,body,date,category,url;
 public final boolean pinned;
 public final long timestamp;
 private Item(JSONObject o) throws Exception {
  id=required(o,"id",128); title=required(o,"title",200); body=required(o,"body",20000);
  date=required(o,"published_at",32); timestamp=parseDate(date);
  category=o.optString("category",""); if(category.length()>80) throw new Exception("category");
  pinned=o.optBoolean("pinned",false); url=o.optString("url","");
  if(url.length()>2048 || (!url.isEmpty() && (!url.startsWith("https://") || new java.net.URI(url).getHost()==null))) throw new Exception("url");
 }
 private static String required(JSONObject o,String key,int max) throws Exception {
  Object v=o.get(key); if(!(v instanceof String) || ((String)v).trim().isEmpty() || ((String)v).length()>max) throw new Exception(key);
  return (String)v;
 }
 static long parseDate(String s) throws Exception {
  SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US); f.setTimeZone(TimeZone.getTimeZone("UTC")); f.setLenient(false);
  ParsePosition p=new ParsePosition(0); Date d=f.parse(s,p); if(d==null || p.getIndex()!=s.length()) throw new Exception("date"); return d.getTime();
 }
 public static List<Item> parse(String raw) throws Exception {
  if(raw.length()>1048576) throw new Exception("size");
  JSONObject root=new JSONObject(raw); if(root.getInt("version")!=1) throw new Exception("version"); parseDate(root.getString("updated_at"));
  JSONArray a=root.getJSONArray("items"); if(a.length()>500) throw new Exception("count");
  List<Item> out=new ArrayList<>(); Set<String> ids=new HashSet<>();
  for(int i=0;i<a.length();i++){Item item=new Item(a.getJSONObject(i)); if(!ids.add(item.id)) throw new Exception("duplicate"); out.add(item);}
  out.sort((a1,b)->a1.pinned!=b.pinned?(a1.pinned?-1:1):Long.compare(b.timestamp,a1.timestamp)); return out;
 }
 public String displayDate(){return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,new Locale("ar")).format(new Date(timestamp));}
}
