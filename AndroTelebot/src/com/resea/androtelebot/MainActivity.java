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
	String[] kata_nietze = {"When we hear the ancient bells growling on a Sunday morning we ask ourselves Is it really possible This for a jew crucified two thousand years ago who said he was God's son? The proof of such a claim is lacking Certainly the Christian religion is an antiquity projected into our times from remote prehistory and the fact that the claim is believed  whereas one is otherwise so strict in examining pretensions  is perhaps the most ancient piece of this heritage. A god who begets children with a mortal woman a sage who bids men work no more have no more courts", "but look for the signs of the impending end of the world a justice that accepts the innocent as a vicarious sacrifice; someone who orders his disciples to drink his blood; prayers for miraculous interventions; sins perpetrated against a god, atoned for by a god;" ,"fear of a beyond to which death is the portal; the form of the cross as a symbol in a time" ,"that no longer knows the function and" ,
			 "ignominy of the" ,"cross",  "how" ,"ghoulishly", "all", "this", "touches" ,"us", "as", "if", "from", "the", "tomb" ,"of" ,"a", "primeval", "past", "Can", "one", "believe", "that", "such", "things" ,"are" ,"still", "believed"};
	
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
			for (String item : kata_nietze) {

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
		alarm.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),60*1000,pintent);
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
