package com.comma.bulletin;
import org.junit.Test;
import static org.junit.Assert.*;

public class FeedTest {
 private String item(String id,String extra){return "{\"id\":\""+id+"\",\"title\":\"عنوان\",\"body\":\"نص الإعلان\",\"published_at\":\"2026-10-20T10:00:00Z\""+extra+"}";}
 private String feed(String items){return "{\"version\":1,\"updated_at\":\"2026-10-20T11:00:00Z\",\"items\":["+items+"]}";}
 @Test public void emptyIsValid() throws Exception {assertTrue(Item.parse(feed("")).isEmpty());}
 @Test public void parsesArabic() throws Exception {assertEquals("عنوان",Item.parse(feed(item("one",""))).get(0).title);}
 @Test public void pinnedComesFirst() throws Exception {assertEquals("two",Item.parse(feed(item("one","")+","+item("two",",\"pinned\":true"))).get(0).id);}
 @Test(expected=Exception.class) public void rejectsDuplicates() throws Exception {Item.parse(feed(item("one","")+","+item("one","")));}
 @Test(expected=Exception.class) public void rejectsBadDate() throws Exception {Item.parse(feed(item("one","")).replace("2026-10-20T10:00:00Z","not-a-date"));}
 @Test(expected=Exception.class) public void rejectsHttp() throws Exception {Item.parse(feed(item("one",",\"url\":\"http://example.com\"")));}
 @Test(expected=Exception.class) public void rejectsUnknownVersion() throws Exception {Item.parse(feed("").replace("\"version\":1","\"version\":2"));}
 @Test(expected=Exception.class) public void rejectsEmptyTitle() throws Exception {Item.parse(feed(item("one","")).replace("عنوان",""));}
}
