package com.comma.bulletin;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
public class FallbackFeedTest {
 private static final String FEED="{\"version\":1,\"items\":[]}";
 @Test public void primaryWins() throws Exception {assertEquals(FEED,new FallbackFeedSource(()->FEED,()->{throw new AssertionError("fallback called");}).fetch());}
 @Test public void httpFailureFallsBack() throws Exception {assertEquals(FEED,new FallbackFeedSource(()->{throw new IOException("404");},()->FEED).fetch());}
 @Test public void invalidPrimaryFallsBack() throws Exception {assertEquals(FEED,new FallbackFeedSource(()->"bad json",()->FEED).fetch());}
 @Test(expected=Exception.class) public void bothFail() throws Exception {new FallbackFeedSource(()->{throw new IOException("offline");},()->"bad json").fetch();}
 @Test public void primaryRetriedEverySync() throws Exception {int[] calls={0};FallbackFeedSource s=new FallbackFeedSource(()->{calls[0]++;if(calls[0]==1)throw new IOException();return FEED;},()->FEED);s.fetch();s.fetch();assertEquals(2,calls[0]);}
 @Test public void growthAndDateOrder() throws Exception {org.json.JSONArray a=new org.json.JSONArray();for(int i=0;i<1200;i++)a.put(new org.json.JSONObject().put("id","tg-"+i).put("title","x").put("published_at",i==1199?"2026-10-21T10:00:00Z":"2026-10-20T10:00:00Z"));java.util.List<Item> items=Item.parse(new org.json.JSONObject().put("items",a).toString());assertEquals(1200,items.size());assertEquals("tg-1199",items.get(0).id);}
}
