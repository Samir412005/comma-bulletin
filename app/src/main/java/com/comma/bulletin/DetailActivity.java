package com.comma.bulletin;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class DetailActivity extends AppCompatActivity {
 @Override public void onCreate(Bundle state){Ui.applyTheme(this);super.onCreate(state);Repository repo=new Repository(this);Item found=repo.find(getIntent().getStringExtra("id"));
  LinearLayout root=Ui.column(this);Ui.page(this,root);Ui.padded(this,root,20);MaterialButton back=Ui.button(this,"العودة إلى الإعلانات");back.setOnClickListener(v->finish());root.addView(back);ScrollView scroll=new ScrollView(this);LinearLayout content=Ui.column(this);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  if(found==null){content.addView(Ui.text(this,"لم يعد الإعلان متاحاً. ارجع وحدّث القائمة.",20,false));return;}
  final Item item=found;repo.markRead(item.id);content.addView(Ui.text(this,(item.pinned?"مثبّت · ":"")+item.category,14,true));content.addView(Ui.text(this,item.title,28,true));content.addView(Ui.text(this,item.displayDate(),14,false));TextView body=Ui.text(this,item.body,19,false);body.setTextIsSelectable(true);body.setLineSpacing(Ui.dp(this,5),1f);content.addView(body);
  MaterialButton favorite=Ui.button(this,repo.isFavorite(item.id)?"★ إزالة من المفضلة":"☆ إضافة إلى المفضلة");favorite.setOnClickListener(v->{repo.toggleFavorite(item);favorite.setText(repo.isFavorite(item.id)?"★ إزالة من المفضلة":"☆ إضافة إلى المفضلة");});content.addView(favorite);
  MaterialButton share=Ui.button(this,"مشاركة الإعلان");share.setOnClickListener(v->{Intent send=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT,item.title).putExtra(Intent.EXTRA_TEXT,item.shareText());try{startActivity(Intent.createChooser(send,"مشاركة الإعلان"));}catch(ActivityNotFoundException e){Snackbar.make(root,"لا يوجد تطبيق للمشاركة",Snackbar.LENGTH_LONG).show();}});content.addView(share);
  MaterialButton copy=Ui.button(this,"نسخ النص");copy.setOnClickListener(v->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(clipboard!=null){clipboard.setPrimaryClip(ClipData.newPlainText(item.title,item.shareText()));Snackbar.make(root,"تم نسخ النص",Snackbar.LENGTH_SHORT).show();}});content.addView(copy);
  if(!item.url.isEmpty()){MaterialButton link=Ui.button(this,"فتح الرابط");link.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(item.url)).addCategory(Intent.CATEGORY_BROWSABLE));}catch(ActivityNotFoundException|SecurityException e){Snackbar.make(root,"لا يوجد متصفح متاح لفتح الرابط",Snackbar.LENGTH_LONG).show();}});content.addView(link);}
 }
}
