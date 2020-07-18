package com.example.doran;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Implementation of App Widget functionality.
 */
public class MyAppWidget extends AppWidgetProvider {
    TextView appwidget_text;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        /*위젯의 크기 및 옵션이 변경될때마다 호출되는 함수*/

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_app_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        //위젯 누르면 메인화면으로 이동
        Intent intent=new Intent(context, MainActivity.class);
        PendingIntent pe = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pe);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        /*앱 위젯이 설치될 때마다 호출되는 함수*/
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        /*앱 위젯이 최초로 실행되는 순간 호출되는 함수*/
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        /*앱 위젯이 제거되는 순간 호출되는 함수*/
        // Enter relevant functionality for when the last widget is disabled
    }
}

