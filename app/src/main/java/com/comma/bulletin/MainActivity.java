package com.comma.bulletin;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {
 private Repository repo; private LinearLayout items; private SwipeRefreshLayout swipe; private TextView status; private Button refresh;
 private final ExecutorService executor=Executors.newSingleThreadExecutor(); private boolean busy=false;
 @Override public void onCreate(Bundle state){super.onCreate(state);repo=new Repository(this);Notifications.create(this);PollingWorker.schedule(this);
  LinearLayout root=Ui.column(this);Ui.page(this,root);root.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),0);
  TextView brand=Ui.text(this,"إعلامي",34,true);root.addView(brand);root.addView(Ui.text(this,"كل جديد، في مكان واحد",16,false));
  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);status=Ui.text(this,"",13,false);bar.addView(status,new LinearLayout.LayoutParams(0,-2,1));refresh=new Button(this);refresh.setText("تحديث");refresh.setOnClickListener(v->load());bar.addView(refresh);root.addView(bar);
  swipe=new SwipeRefreshLayout(this);swipe.setColorSchemeColors(0xff078777);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);items=Ui.column(this);scroll.addView(items);swipe.addView(scroll);swipe.setOnRefreshListener(this::load);root.addView(swipe,new LinearLayout.LayoutParams(-1,0,1));
  setContentView(root);render(repo.cached());
  if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED&&!getPreferences(0).getBoolean("asked",false)){
   getPreferences(0).edit().putBoolean("asked",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
  }
  load();
 }
 @Override protected void onResume(){super.onResume();if(repo!=null)render(repo.cached());}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);load();}
 private void load(){if(busy)return;busy=true;refresh.setEnabled(false);swipe.setRefreshing(true);status.setText("جارٍ تحديث الإعلانات…");
  executor.execute(()->{String error=null;try{repo.sync();}catch(Exception e){error="تعذّر التحديث. تحقق من الإنترنت ثم حاول مجدداً.";}final String message=error;
   runOnUiThread(()->{if(isDestroyed())return;busy=false;refresh.setEnabled(true);swipe.setRefreshing(false);render(repo.cached());
    if(message!=null){status.setText(repo.cached().isEmpty()?message:message+"\nتُعرض آخر نسخة محفوظة.");}else{status.setText("آخر تحديث: "+java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,new Locale("ar")).format(new Date(repo.lastSync())));}
   });
  });
 }
 private void render(List<Item> list){items.removeAllViews();
  if(list.isEmpty()){LinearLayout empty=Ui.column(this);Ui.card(this,empty);empty.addView(Ui.text(this,"لا توجد إعلانات بعد",23,true));empty.addView(Ui.text(this,"اسحب إلى الأسفل أو اضغط تحديث. ستظهر الإعلانات هنا عند نشرها.",16,false));items.addView(empty);return;}
  for(Item item:list){LinearLayout card=Ui.column(this);Ui.card(this,card);TextView badge=Ui.text(this,(item.pinned?"مثبّت · ":"")+item.category+(!repo.isRead(item.id)?"  • جديد":""),13,true);badge.setTextColor(Ui.dark(this)?0xff6edccc:0xff00695c);card.addView(badge);card.addView(Ui.text(this,item.title,22,true));TextView excerpt=Ui.text(this,item.body,16,false);excerpt.setMaxLines(3);excerpt.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(excerpt);card.addView(Ui.text(this,item.displayDate(),12,false));
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=Ui.dp(this,14);items.addView(card,lp);card.setFocusable(true);card.setOnClickListener(v->{repo.markRead(item.id);startActivity(new Intent(this,DetailActivity.class).putExtra("id",item.id));});
  }
 }
 @Override protected void onDestroy(){executor.shutdown();super.onDestroy();}
}
