package com.resea.androtelebot;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	String[] projection = {"Kata_Row"};
	String[] kata_kata = {"aku","kamu","dia","dirinya","waktu","hidup adalah menunggu mati","sekali tenar hilang dan berganti","menurutku","aku memilihmu karena kamu beda dengan yang lainnya","sayang makan yuk dari tadi ngoding ngga selesai-selesai","mau dimasakin apa say?","say, apbnnya saya yang buat ya.","say jangan lupa sholat!","say kok kamu kalo ngoding, bisa lupa segalanya sih.","apaan coba -_-","apaan forward2 -_-","kalian alay","gabut banget pokoknya","apaan sih -_-","aku ngga suka dengan yang dibahas","terlalu banyak ngomong porno","kalian kalo ngga bahas ngoding pasti bahas tuhan atau porno mboseni."};
	
	DatabaseBot dbBot;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODOs Auto-generated method stub
		super.onCreate(savedInstanceState);
		dbBot = new DatabaseBot(this);
		TextView vie = new TextView(this);
		vie.setText("bot sedang berjalan");
		if(!exists()){
			for (String item : kata_kata) {

				ContentValues values = new ContentValues();
				values.put("Kata_Row", item);

				MainActivity.this
						.getContentResolver()
						.insert(Uri
								.parse("content://com.resea.androtelebot.databaseprovider/element"),
								values);

			}
		}
		Calendar cal = Calendar.getInstance();
		Intent intent = new Intent(MainActivity.this,BotService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),2*1000,pintent);
		setContentView(vie);
	}
	
	private boolean exists() {
		SQLiteDatabase db = dbBot.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from Kata_Random", null);
		if (cursor != null && cursor.getCount() > 0) {
			return true;
		} else {
			return false;
		}
	}

	
	
}
