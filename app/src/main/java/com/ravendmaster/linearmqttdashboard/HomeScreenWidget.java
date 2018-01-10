package com.ravendmaster.linearmqttdashboard;

import java.util.Arrays;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.ravendmaster.linearmqttdashboard.activity.HomeScreenWidgetConfigActivity;
import com.ravendmaster.linearmqttdashboard.service.MQTTService;

public class HomeScreenWidget extends AppWidgetProvider {

    final static String LOG_TAG = "myLogs";
    private PendingIntent service = null;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        final Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        final Intent i = new Intent(context, MQTTWidgetUpdateService.class);
        if (service == null)
        {
            service = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        manager.setRepeating(AlarmManager.RTC_WAKEUP,startTime.getTime().getTime(),15000,service);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));

        SharedPreferences sp = context.getSharedPreferences(HomeScreenWidgetConfigActivity.Companion.getWIDGET_PREF(), Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));

        // Удаляем Preferences
        Editor editor = context.getSharedPreferences(
                HomeScreenWidgetConfigActivity.Companion.getWIDGET_PREF(), Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(HomeScreenWidgetConfigActivity.Companion.getWIDGET_TEXT() + widgetID);
            editor.remove(HomeScreenWidgetConfigActivity.Companion.getWIDGET_COLOR() + widgetID);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int widgetID) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);

        // Читаем параметры Preferences
        String topicName = sp.getString(HomeScreenWidgetConfigActivity.Companion.getWIDGET_TEXT() + widgetID, null);

        //Calendar cal = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        //System.out.println( sdf.format(cal.getTime()) );


        //String widgetText = sdf.format(cal.getTime());
        String widgetText = MQTTService.Companion.getInstance().getMQTTCurrentValue(topicName);



        if (widgetText == null) return;
        int widgetColor = sp.getInt(HomeScreenWidgetConfigActivity.Companion.getWIDGET_COLOR() + widgetID, 0);

        // Настраиваем внешний вид виджета
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.home_screen_widget);
        widgetView.setTextViewText(R.id.tv, widgetText);
        widgetView.setInt(R.id.tv, "setBackgroundColor", widgetColor);

        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    public class MQTTWidgetUpdateService extends Service {
        public MQTTWidgetUpdateService() {
        }
        @Override
        public void onCreate()
        {
            super.onCreate();
        }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
        {
            updateInfoWidget();
            return super.onStartCommand(intent, flags, startId);
        }
        private void updateInfoWidget()
        {//Обновление виджета
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(this.getApplicationContext().getPackageName(), HomeScreenWidget.class.getName()));
            SharedPreferences sp = this.getApplicationContext().getSharedPreferences(HomeScreenWidgetConfigActivity.Companion.getWIDGET_PREF(), Context.MODE_PRIVATE);
            HomeScreenWidget.updateWidget(this.getApplicationContext(), appWidgetManager, sp, ids[0]);
        }
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}

