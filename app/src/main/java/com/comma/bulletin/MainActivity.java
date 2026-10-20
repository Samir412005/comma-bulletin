package com.comma.bulletin;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {
 private Repository repo;private LinearLayout items;private SwipeRefreshLayout swipe;private TextView status,unread;private MaterialButton refresh,allTab,favTab;private ChipGroup chips;
 private final Handler pollHandler=new Handler(Looper.getMainLooper());
 private boolean resumed=false;
 private final Runnable poll=new Runnable(){@Override public void run(){if(resumed){load();pollHandler.postDelayed(this,90000);}}};
 private final ExecutorService executor=Executors.newSingleThreadExecutor();private boolean busy=false,favorites=false;private String query="",category="",error=null;
 @Override public void onCreate(Bundle state){Ui.applyTheme(this);super.onCreate(state);repo=new Repository(this);Notifications.create(this);PollingWorker.schedule(this);
  if(state!=null){query=state.getString("query","");category=state.getString("category","");favorites=state.getBoolean("favorites");}
  LinearLayout root=Ui.column(this);Ui.page(this,root);root.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),0);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(Ui.text(this,"إعلامي",30,true),new LinearLayout.LayoutParams(0,-2,1));MaterialButton theme=Ui.button(this,"المظهر");theme.setContentDescription("اختيار الوضع الليلي أو النهاري");theme.setOnClickListener(v->chooseTheme());header.addView(theme);root.addView(header);
  unread=Ui.text(this,"",14,true);unread.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);root.addView(unread);
  TextInputLayout searchBox=new TextInputLayout(this);searchBox.setHint("ابحث في العنوان أو النص");TextInputEditText search=new TextInputEditText(searchBox.getContext());search.setSingleLine(true);search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);search.setText(query);search.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);searchBox.addView(search,new LinearLayout.LayoutParams(-1,-2));searchBox.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);root.addView(searchBox);
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();render();}public void afterTextChanged(Editable e){}});
  LinearLayout tabs=new LinearLayout(this);allTab=Ui.button(this,"الإعلانات");favTab=Ui.button(this,"المفضلة");tabs.addView(allTab,new LinearLayout.LayoutParams(0,-2,1));tabs.addView(favTab,new LinearLayout.LayoutParams(0,-2,1));allTab.setOnClickListener(v->{favorites=false;render();});favTab.setOnClickListener(v->{favorites=true;render();});root.addView(tabs);
  HorizontalScrollView horizontal=new HorizontalScrollView(this);horizontal.setHorizontalScrollBarEnabled(false);chips=new ChipGroup(this);chips.setSingleLine(true);horizontal.addView(chips);root.addView(horizontal);buildCategories();
  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);status=Ui.text(this,"",12,false);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);status.setOnClickListener(v->load());status.setContentDescription("آخر تحديث، اضغط للمزامنة");bar.addView(status,new LinearLayout.LayoutParams(0,-2,1));refresh=Ui.button(this,"تحديث");refresh.setOnClickListener(v->load());bar.addView(refresh);root.addView(bar);
  swipe=new SwipeRefreshLayout(this);swipe.setColorSchemeColors(Ui.color(this,com.google.android.material.R.attr.colorPrimary));ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);items=Ui.column(this);scroll.addView(items);swipe.addView(scroll);swipe.setOnRefreshListener(this::load);root.addView(swipe,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);render();
  if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED&&!getPreferences(0).getBoolean("asked",false)){getPreferences(0).edit().putBoolean("asked",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);}
 }
 private void chooseTheme(){int[] modes={AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,AppCompatDelegate.MODE_NIGHT_NO,AppCompatDelegate.MODE_NIGHT_YES};int current=getSharedPreferences("bulletin",0).getInt("theme",modes[0]),selected=0;for(int i=0;i<modes.length;i++)if(modes[i]==current)selected=i;
  new MaterialAlertDialogBuilder(this).setTitle("مظهر التطبيق").setSingleChoiceItems(new String[]{"حسب النظام","نهاري","ليلي"},selected,(dialog,which)->{getSharedPreferences("bulletin",0).edit().putInt("theme",modes[which]).apply();dialog.dismiss();AppCompatDelegate.setDefaultNightMode(modes[which]);}).setNegativeButton("إلغاء",null).show();
 }
 private void buildCategories(){TreeSet<String> categories=new TreeSet<>();for(Item i:repo.cached())if(!i.category.isEmpty())categories.add(i.category);if(!categories.contains(category))category="";chips.removeAllViews();addChip("الكل","");for(String name:categories)addChip(name,name);}
 private void addChip(String title,String value){Chip chip=new Chip(this);chip.setText(title);chip.setCheckable(true);chip.setChecked(value.equals(category));chip.setOnClickListener(v->{category=value;for(int i=0;i<chips.getChildCount();i++){Chip c=(Chip)chips.getChildAt(i);c.setChecked(c==chip);}render();});chips.addView(chip);}
 @Override protected void onResume(){super.onResume();resumed=true;if(repo!=null&&items!=null){buildCategories();render();load();}pollHandler.removeCallbacks(poll);pollHandler.postDelayed(poll,90000);}
 @Override protected void onPause(){resumed=false;pollHandler.removeCallbacks(poll);super.onPause();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);load();}
 @Override protected void onSaveInstanceState(Bundle b){b.putString("query",query);b.putString("category",category);b.putBoolean("favorites",favorites);super.onSaveInstanceState(b);}
 private void load(){PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);if(!resumed||!power.isInteractive()||busy)return;busy=true;error=null;refresh.setEnabled(false);swipe.setRefreshing(true);showStatus();
  executor.execute(()->{String failure=null;try{repo.sync();}catch(Exception e){failure="تعذّر الاتصال. تحقق من الإنترنت.";}final String result=failure;runOnUiThread(()->{if(isDestroyed())return;busy=false;error=result;refresh.setEnabled(true);swipe.setRefreshing(false);buildCategories();render();});});
 }
 private void showStatus(){if(status==null)return;if(busy){status.setText("جارٍ تحميل الإعلانات…");return;}refresh.setText(error==null?"تحديث":"إعادة المحاولة");if(error!=null){status.setText(error+(repo.cached().isEmpty()?"":"\nتُعرض آخر نسخة محفوظة."));return;}long time=repo.lastSync();status.setText(time==0?"اسحب للأسفل للتحديث":"آخر تحديث: "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,new Locale("ar")).format(new Date(time)));}
 private void render(){if(items==null)return;items.removeAllViews();unread.setText("غير المقروء: "+repo.unread());allTab.setText((favorites?"":"✓ ")+"الإعلانات");favTab.setText((favorites?"✓ ":"")+"المفضلة");allTab.setSelected(!favorites);favTab.setSelected(favorites);showStatus();
  List<Item> source=favorites?repo.favorites():repo.cached();Set<String> favoriteIds=new HashSet<>();for(Item i:repo.favorites())favoriteIds.add(i.id);int shown=0;
  for(Item item:source){if(!item.matches(query)||(!category.isEmpty()&&!category.equals(item.category)))continue;shown++;
   MaterialCardView card=new MaterialCardView(this);card.setRadius(Ui.dp(this,20));LinearLayout content=Ui.column(this);Ui.padded(this,content,16);card.addView(content);
   LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView badge=Ui.text(this,(item.pinned?"مثبّت · ":"")+item.category+(!repo.isRead(item.id)?" · جديد":""),13,true);badge.setTextColor(Ui.color(this,com.google.android.material.R.attr.colorPrimary));top.addView(badge,new LinearLayout.LayoutParams(0,-2,1));MaterialButton star=Ui.button(this,favoriteIds.contains(item.id)?"★":"☆");star.setMinWidth(Ui.dp(this,48));star.setMinimumWidth(Ui.dp(this,48));star.setContentDescription((favoriteIds.contains(item.id)?"إزالة من المفضلة: ":"إضافة إلى المفضلة: ")+item.title);star.setOnClickListener(v->{repo.toggleFavorite(item);render();});top.addView(star,new LinearLayout.LayoutParams(Ui.dp(this,64),-2));content.addView(top);
   content.addView(Ui.text(this,item.title,22,true));TextView excerpt=Ui.text(this,item.body,16,false);excerpt.setMaxLines(3);excerpt.setEllipsize(TextUtils.TruncateAt.END);content.addView(excerpt);content.addView(Ui.text(this,item.displayDate(),12,false));
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=Ui.dp(this,12);items.addView(card,lp);card.setFocusable(true);card.setClickable(true);card.setContentDescription("فتح الإعلان: "+item.title);card.setOnClickListener(v->startActivity(new Intent(this,DetailActivity.class).putExtra("id",item.id)));
  }
  if(shown==0){String text=!query.trim().isEmpty()||!category.isEmpty()?"لا نتائج":favorites?"لا توجد إعلانات في المفضلة":"لا توجد إعلانات بعد";items.addView(Ui.text(this,text,23,true));items.addView(Ui.text(this,favorites?"اضغط النجمة على أي إعلان لحفظه هنا.":"جرّب تغيير البحث أو التصنيف، أو اسحب للتحديث.",16,false));}
 }
 @Override protected void onDestroy(){executor.shutdown();super.onDestroy();}
}
