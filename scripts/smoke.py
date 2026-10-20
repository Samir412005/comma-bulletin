import subprocess, time, re, xml.etree.ElementTree as ET, json, urllib.request, os

def adb(*args):
    return subprocess.check_output(['adb', *args], text=True)
def dump():
    for _ in range(4):
        try:
            adb('shell','uiautomator','dump','/sdcard/window.xml')
            raw=adb('shell','cat','/sdcard/window.xml')
            return ET.fromstring(raw)
        except Exception:
            time.sleep(1)
    raise AssertionError('UI dump failed')
def find(predicate, wait=15):
    for _ in range(wait):
        root=dump()
        for n in root.iter('node'):
            if predicate(n.attrib): return n.attrib
        time.sleep(1)
    raise AssertionError('UI element not found: '+ET.tostring(root,encoding='unicode'))
def tap(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(1)
def text(value): return find(lambda a:a.get('text')==value)
def launch():
    adb('shell','am','start','-W','-n','com.comma.bulletin/.MainActivity');time.sleep(3)
def close(): adb('shell','am','force-stop','com.comma.bulletin')

# Install the previous public release, then perform a real same-signature upgrade.
adb('install','-r','previous.apk');launch();close()
adb('install','-r','iilami-1.1.0.apk');adb('logcat','-c');launch()
text('إعلامي');text('المظهر')
# Wait until at least one live announcement has arrived.
card=find(lambda a:a.get('content-desc','').startswith('فتح الإعلان:'),wait=30)
# Search must filter title AND body immediately and display the empty state.
edit=find(lambda a:a.get('class')=='android.widget.EditText')
tap(edit);adb('shell','input','text','ZZZnonexistent123');time.sleep(1);text('لا نتائج')
adb('shell','input','keyevent','4')
# Clear the search via Material's accessible clear icon (localized label may vary).
clear=find(lambda a:'clear' in a.get('content-desc','').lower() or 'محو' in a.get('content-desc','') or 'مسح' in a.get('content-desc',''))
tap(clear)
# Save an announcement, open it, copy its text and observe read state.
star=find(lambda a:a.get('content-desc','').startswith('إضافة إلى المفضلة:'))
tap(star);tap(text('المفضلة'))
card=find(lambda a:a.get('content-desc','').startswith('فتح الإعلان:'))
tap(card);text('مشاركة الإعلان');tap(text('نسخ النص'));text('تم نسخ النص')
text('★ إزالة من المفضلة');tap(text('العودة إلى الإعلانات'))
find(lambda a:a.get('text','').startswith('غير المقروء:'))
# The favorite and the chosen night mode must survive process death.
close();launch();tap(text('المفضلة'));find(lambda a:a.get('content-desc','').startswith('فتح الإعلان:'))
tap(text('المظهر'));tap(text('ليلي'));time.sleep(3);close();launch()
tap(text('المظهر'));night=text('ليلي');assert night.get('checked')=='true',night
adb('shell','input','keyevent','4')
# Force offline, restart, and confirm the last successful feed is still shown.
adb('shell','svc','wifi','disable');adb('shell','svc','data','disable');close();launch()
find(lambda a:a.get('content-desc','').startswith('فتح الإعلان:'))
find(lambda a:a.get('text')=='إعادة المحاولة',wait=40)
adb('shell','svc','wifi','enable');adb('shell','svc','data','enable')
log=adb('logcat','-d','-s','AndroidRuntime:E')
assert 'FATAL EXCEPTION' not in log,log
adb('shell','screencap','-p','/sdcard/smoke.png');adb('pull','/sdcard/smoke.png','smoke.png')
print('PASS: signed upgrade, live feed, instant search, favorites persistence, read counter, copy snackbar, saved night mode, offline cache, retry and no crash')
