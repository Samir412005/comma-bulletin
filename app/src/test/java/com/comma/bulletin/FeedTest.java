package com.comma.bulletin;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class FeedTest {
 private String item(String id,String extra){return "{\"id\":\""+id+"\",\"title\":\"عنوان\",\"body\":\"نص الإعلان Hello\",\"published_at\":\"2026-10-20T10:00:00Z\""+extra+"}";}
 private String feed(String items){return "{\"version\":1,\"updated_at\":\"2026-10-20T11:00:00Z\",\"items\":["+items+"]}";}
 @Test public void emptyIsValid() throws Exception {assertTrue(Item.parse(feed("")).isEmpty());}
 @Test public void parsesArabic() throws Exception {assertEquals("عنوان",Item.parse(feed(item("one",""))).get(0).title);}
 @Test public void pinnedComesFirst() throws Exception {assertEquals("two",Item.parse(feed(item("one","")+","+item("two",",\"pinned\":true"))).get(0).id);}
 @Test public void duplicateIsIgnored() throws Exception {assertEquals(1,Item.parse(feed(item("one","")+","+item("one",""))).size());}
 @Test public void badDateFallsBack() throws Exception {assertEquals("تاريخ غير محدد",Item.parse(feed(item("one","")).replace("2026-10-20T10:00:00Z","bad")).get(0).displayDate());}
 @Test public void missingAndNullFieldsAreSafe() throws Exception {Item i=Item.parse("{\"items\":[{\"body\":null,\"category\":null,\"extra\":123}]}").get(0);assertEquals("",i.body);assertEquals("",i.url);assertFalse(i.id.isEmpty());assertEquals("إعلان بدون عنوان",i.title);}
 @Test public void generatedIdIsStable() throws Exception {assertEquals(Item.parse("{\"items\":[{}]}").get(0).id,Item.parse("{\"items\":[{}]}").get(0).id);}
 @Test public void emptyTitleIsSafe() throws Exception {assertEquals("إعلان بدون عنوان",Item.parse(feed(item("one","")).replace("عنوان","")).get(0).title);}
 @Test public void unknownFieldsAreIgnored() throws Exception {assertEquals(1,Item.parse(feed(item("one",",\"extra\":{\"x\":1}"))).size());}
 @Test public void invalidEntriesSkipped() throws Exception {assertEquals(1,Item.parse(feed("null,42,"+item("one",""))).size());}
 @Test public void searchesTitleAndBody() throws Exception {Item i=Item.parse(feed(item("one",""))).get(0);assertTrue(i.matches("عنوان"));assertTrue(i.matches("الإعلان"));assertTrue(i.matches("HELLO"));assertTrue(i.matches(" "));assertFalse(i.matches("ZZZZ"));}
 @Test public void sharesUrlOnlyWhenPresent() throws Exception {Item i=Item.parse(feed(item("one",",\"url\":\"https://example.com/a\""))).get(0);assertTrue(i.shareText().contains("عنوان"));assertTrue(i.shareText().endsWith(i.url));assertFalse(Item.parse(feed(item("one",""))).get(0).shareText().contains("http"));}
 @Test public void unsafeLinksIgnored() throws Exception {assertEquals("",Item.parse(feed(item("one",",\"url\":\"javascript:alert(1)\""))).get(0).url);assertFalse(Item.safeUrl("https://user:secret@example.com"));assertFalse(Item.safeUrl("intent://bad"));assertTrue(Item.safeUrl("http://example.com"));}
 @Test public void relativeMinutesHoursDays() throws Exception {Item i=Item.parse(feed(item("one",""))).get(0);assertEquals("قبل 5 دقيقة",i.relativeDate(i.timestamp+300000));assertEquals("قبل 2 ساعة",i.relativeDate(i.timestamp+7200000));assertEquals("قبل 2 يوم",i.relativeDate(i.timestamp+172800000));assertEquals("الآن",i.relativeDate(i.timestamp-1000));}
 @Test public void datesAcceptOffsetsAndMilliseconds() throws Exception {assertEquals(Item.parseDate("2026-10-20T10:00:00Z"),Item.parseDate("2026-10-20T11:00:00+01:00"));assertEquals(Item.parseDate("2026-10-20T10:00:00Z")+123,Item.parseDate("2026-10-20T10:00:00.123Z"));}
 @Test public void roundTripPreservesFields() throws Exception {Item i=Item.parse(feed(item("one",",\"url\":\"https://example.com\",\"pinned\":true,\"category\":\"خبر\""))).get(0);Item j=Item.parse(feed(i.json().toString())).get(0);assertEquals(i.shareText(),j.shareText());assertEquals(i.category,j.category);assertTrue(j.pinned);}
 @Test(expected=Exception.class) public void rejectsUnknownVersion() throws Exception {Item.parse(feed("").replace("\"version\":1","\"version\":2"));}
 @Test(expected=Exception.class) public void rejectsInvalidJson() throws Exception {Item.parse("not json");}
 @Test(expected=Exception.class) public void rejectsInvalidItemsType() throws Exception {Item.parse("{\"items\":{}}");}
}
