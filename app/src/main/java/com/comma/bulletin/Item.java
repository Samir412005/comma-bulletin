package com.comma.bulletin;
import org.json.*;
import java.text.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Item {
 public final String id,title,body,date,category,url;
 public final boolean pinned;
 public final long timestamp;
 private static String text(JSONObject o,String key,int max){Object v=o.opt(key);String s=v instanceof String?(String)v:"";return s.length()>max?s.substring(0,max):s;}
 private Item(JSONObject o) throws Exception {
  String t=text(o,"title",200);title=t.trim().isEmpty()?"إعلان بدون عنوان":t;
  body=text(o,"body",20000);date=text(o,"published_at",80);
  long time=0;try{time=parseDate(date);}catch(Exception ignored){}timestamp=time;
  category=text(o,"category",80).trim();pinned=o.optBoolean("pinned",false);
  String link=text(o,"url",2048).trim();url=safeUrl(link)?link:"";
  String key=text(o,"id",128).trim();
  if(key.isEmpty()){byte[] hash=MessageDigest.getInstance("SHA-256").digest((title+"\n"+body+"\n"+date).getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder("auto-");for(byte x:hash)b.append(String.format(Locale.ROOT,"%02x",x&255));key=b.toString();}id=key;
 }
 static boolean safeUrl(String s){try{java.net.URI u=new java.net.URI(s);return ("https".equalsIgnoreCase(u.getScheme())||"http".equalsIgnoreCase(u.getScheme()))&&u.getHost()!=null&&u.getUserInfo()==null;}catch(Exception e){return false;}}
 static long parseDate(String s) throws Exception {
  String[] patterns={"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","yyyy-MM-dd'T'HH:mm:ssXXX","yyyy-MM-dd"};
  for(String pattern:patterns){SimpleDateFormat f=new SimpleDateFormat(pattern,Locale.US);f.setTimeZone(TimeZone.getTimeZone("UTC"));f.setLenient(false);ParsePosition p=new ParsePosition(0);Date d=f.parse(s,p);if(d!=null&&p.getIndex()==s.length())return d.getTime();}throw new Exception("date");
 }
 public static List<Item> parse(String raw) throws Exception {
  if(raw.length()>8388608)throw new Exception("size");JSONObject root=new JSONObject(raw);
  if(root.optInt("version",1)!=1)throw new Exception("version");
  JSONArray a=root.optJSONArray("items");if(a==null){if(root.has("items")&&!root.isNull("items"))throw new Exception("items");a=new JSONArray();}
  List<Item> out=new ArrayList<>();Set<String> ids=new HashSet<>();
  for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;Item item=new Item(o);if(ids.add(item.id))out.add(item);}
  out.sort((a1,b)->a1.pinned!=b.pinned?(a1.pinned?-1:1):Long.compare(b.timestamp,a1.timestamp));return out;
 }
 public JSONObject json(){JSONObject o=new JSONObject();try{o.put("id",id).put("title",title).put("body",body).put("published_at",date).put("category",category).put("pinned",pinned).put("url",url);}catch(JSONException ignored){}return o;}
 public boolean matches(String query){String q=query.trim().toLowerCase(Locale.ROOT);return title.toLowerCase(Locale.ROOT).contains(q)||body.toLowerCase(Locale.ROOT).contains(q);}
 public String shareText(){return title+"\n\n"+body+(url.isEmpty()?"":"\n\n"+url);}
 String relativeDate(long now){if(timestamp<=0)return "تاريخ غير محدد";long diff=Math.max(0,now-timestamp),minutes=diff/60000;if(minutes<1)return "الآن";if(minutes<60)return "قبل "+minutes+" دقيقة";long hours=minutes/60;if(hours<24)return "قبل "+hours+" ساعة";long days=hours/24;if(days<30)return "قبل "+days+" يوم";return DateFormat.getDateInstance(DateFormat.MEDIUM,new Locale("ar")).format(new Date(timestamp));}
 public String displayDate(){return relativeDate(System.currentTimeMillis());}
}
