package com.resea.androtelebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BotService extends Service {
	
	private final IBinder mBinder = new MyBinder();
	static Context context;
	ArrayList<String> list = new ArrayList<>();
	String[] load ={"Kata_Row"};
	/**
	 *  
	 * Calendar cal = Calendar.getInstance();
	 * Intent intent = new Intent(ActionbarPage.this,NotificationServis.class);
	 * PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
	 * AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	 * alarm.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),10*1000,pintent);
	 * 
	 * */
	
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODOs Auto-generated method stub
		return mBinder;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODOs Auto-generated method stub
		context = BotService.this;
		new GetUpdates().execute("latian");
		return (START_STICKY);
	}
	
	private class GetUpdates extends AsyncTask <String, Void, String>{

		@Override
		protected String doInBackground(String... arg0) {
			// TODOs Auto-generated method stub
			int updates_id=0;
			String res = "";
			Log.d("MU", "load this");
			try {
				Log.d("MU", "load this");
				res = Utils.getUpdates(updates_id++);
				Log.d("MU", res);
				JSONObject obj = new JSONObject(res);
				String arrayString = obj.getString("result");
				JSONArray jArray = new JSONArray(arrayString);
				Log.d("MU", "array String "+arrayString);
				for(int i=0;i<jArray.length();i++){
					JSONObject message= jArray.getJSONObject(i).getJSONObject("message");
					String message_string = jArray.getJSONObject(i).getString("message");
					int chat_id = message.getJSONObject("chat").getInt("id");
					Log.d("MU", "chat id "+chat_id);
					String username = message.getJSONObject("chat").getString("username");
					Log.d("MU","username "+username);
					Log.d("MU", "get the text messages");
					
					 String text = "kosong";
					if(message_string.contains("\"text\"")){
					    text = message.getString("text");
					    Log.d("MU", "the text is "+text);
					}else{
						String sticker =message.getString("sticker");
						Log.d("Mu",sticker);
					}
					int message_id = message.getInt("message_id");
					if(text.contains("siapa lo")){
						String text_replay = "aku adalah seorang muslim jika aku sendirian dan komunis jika aku dalam kerumunan karena Allah berfirman setan ada dalam kerumunan (Tan Malaka)";
						Log.d("MU","sending "+text_replay);
						Utils.sendMessage(chat_id, message_id, text_replay);
					}else if(text.contains("mulai")){
						Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.resea.androtelebot.databaseprovider/element"), load, null, null, null);
						cursor.moveToFirst();
						do{
							String kata = cursor.getString(cursor.getColumnIndex("Kata_Row"));
							list.add(kata);
						}while(cursor.moveToNext());
						cursor.close();
						Random rnd = new Random();
						int indexrand = rnd.nextInt(list.size());
						String text_replay = list.get(indexrand);
						indexrand = rnd.nextInt(list.size());
						text_replay += list.get(indexrand);
						indexrand = rnd.nextInt(list.size());
						text_replay += list.get(indexrand);
						Log.d("MU", "sending");
						Utils.sendMessage(chat_id, message_id, text_replay);
					}else if(text.contains("you")){
	
						Utils.sendMessage(chat_id, message_id, username);
					}
				}
			} catch (ClientProtocolException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			}
			//Log.d("MU", res);
			return null;
		}

		
		
	}

	public class MyBinder extends Binder {
		BotService getService() {
			return BotService.this;
		}
	}

}
