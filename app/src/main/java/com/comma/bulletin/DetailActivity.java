package com.comma.bulletin;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
 @Override public void onCreate(Bundle state){super.onCreate(state);Repository repo=new Repository(this);String id=getIntent().getStringExtra("id");Item found=null;for(Item i:repo.cached())if(i.id.equals(id)){found=i;break;}
  LinearLayout root=Ui.column(this);Ui.page(this,root);int p=Ui.dp(this,22);root.setPadding(p,p,p,p);Button back=new Button(this);back.setText("العودة إلى الإعلانات");back.setOnClickListener(v->finish());root.addView(back);ScrollView scroll=new ScrollView(this);LinearLayout content=Ui.column(this);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(found==null)content.addView(Ui.text(this,"لم يعد الإعلان متاحاً. ارجع وحدّث القائمة.",20,false));else{final Item item=found;repo.markRead(item.id);content.addView(Ui.text(this,item.category,14,true));content.addView(Ui.text(this,item.title,30,true));content.addView(Ui.text(this,item.displayDate(),14,false));TextView body=Ui.text(this,item.body,19,false);body.setTextIsSelectable(true);body.setLineSpacing(Ui.dp(this,5),1f);content.addView(body);
   if(!item.url.isEmpty()){Button link=new Button(this);link.setText("فتح الرابط الخارجي");link.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(item.url)));}catch(android.content.ActivityNotFoundException e){Toast.makeText(this,"لا يوجد متصفح لفتح الرابط",Toast.LENGTH_LONG).show();}});content.addView(link);}
  }setContentView(root);
 }
}
