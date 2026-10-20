package com.comma.bulletin;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

final class Ui {
 static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
 static boolean dark(Context c){return (c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;}
 static int ink(Context c){return Color.parseColor(dark(c)?"#EDF4FA":"#152F3B");}
 static TextView text(Context c,String s,int size,boolean bold){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(ink(c));t.setGravity(Gravity.START);t.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(c,4),0,dp(c,4));return t;}
 static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 static void card(Context c,View v){GradientDrawable d=new GradientDrawable();d.setColor(Color.parseColor(dark(c)?"#1D303D":"#FFFFFF"));d.setCornerRadius(dp(c,18));v.setBackground(d);int p=dp(c,18);v.setPadding(p,p,p,p);}
 static void page(Context c,View v){v.setBackgroundColor(Color.parseColor(dark(c)?"#101D27":"#F1F5F6"));}
}
