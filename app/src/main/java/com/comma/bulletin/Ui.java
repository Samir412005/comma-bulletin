package com.comma.bulletin;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.button.MaterialButton;

final class Ui {
 static void applyTheme(Context c){int mode=c.getSharedPreferences("bulletin",0).getInt("theme",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);AppCompatDelegate.setDefaultNightMode(mode);}
 static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
 static boolean dark(Context c){return (c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;}
 static int color(Context c,int attr){return MaterialColors.getColor(c,attr,"إعلامي");}
 static TextView text(Context c,String s,int size,boolean bold){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(color(c,com.google.android.material.R.attr.colorOnSurface));t.setGravity(Gravity.START);t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(c,4),0,dp(c,4));return t;}
 static MaterialButton button(Context c,String label){MaterialButton b=new MaterialButton(c,null,com.google.android.material.R.attr.materialButtonOutlinedStyle);b.setText(label);return b;}
 static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 static void page(Context c,View v){v.setBackgroundColor(color(c,com.google.android.material.R.attr.colorSurface));}
 static void padded(Context c,View v,int n){int p=dp(c,n);v.setPadding(p,p,p,p);}
}
