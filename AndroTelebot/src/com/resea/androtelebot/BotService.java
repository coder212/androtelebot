package com.resea.androtelebot;

import static android.os.BatteryManager.BATTERY_HEALTH_DEAD;
import static android.os.BatteryManager.BATTERY_HEALTH_GOOD;
import static android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT;
import static android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE;
import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE;
import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_DISCHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.EXTRA_HEALTH;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_PRESENT;
import static android.os.BatteryManager.EXTRA_SCALE;
import static android.os.BatteryManager.EXTRA_STATUS;
import static android.os.BatteryManager.EXTRA_TECHNOLOGY;
import static android.os.BatteryManager.EXTRA_VOLTAGE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class BotService extends Service implements LocationListener {

	private final IBinder mBinder = new MyBinder();
	static Context context;
	ArrayList<String> list = new ArrayList<>();
	String[] load = { "Kata_Row" };
	String[] loadup = { "Messages_id" };
	String userName;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGETLocation = false;
	Location location; // location
	double latitude = 0; // latitude
	double longitude = 0; // longitude
	int update_id;
	int msg_idc = 0;
	int message_id = 0;
	int chat_id = 0,user_id = 0,from_id=0;
	int from_chat_id=0,from_msg_id=0;
	String from_user_name="";
	String text = "";
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public Location getLocation() {
		try {
			locationManager = (LocationManager) context
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGETLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();

						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();

							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog
				.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
	}

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(BotService.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGETLocation;
	}

	private void setUpdate_idc() {
		Cursor cursor = context
				.getContentResolver()
				.query(Uri
						.parse("content://com.resea.androtelebot.updateprovider/element"),
						loadup, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToLast();
			String updates_id = cursor.getString(cursor
					.getColumnIndex("Messages_id"));
			msg_idc = Integer.parseInt(updates_id);
		} else {
			msg_idc = 0;
		}

		cursor.close();
	}

	private static String healthCodeToString(int health) {
		switch (health) {
		// case BATTERY_HEALTH_COLD: return "Cold"; // API level 11 only
		case BATTERY_HEALTH_DEAD:
			return "Dead";
		case BATTERY_HEALTH_GOOD:
			return "Good";
		case BATTERY_HEALTH_OVERHEAT:
			return "Overheat";
		case BATTERY_HEALTH_OVER_VOLTAGE:
			return "Over voltage";
		case BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			return "Unspecified failure";
		case BATTERY_HEALTH_UNKNOWN:
		default:
			return "Unknown";
		}
	}

	private static String pluggedCodeToString(int plugged) {
		switch (plugged) {
		case 0:
			return "Battery";
		case BATTERY_PLUGGED_AC:
			return "AC";
		case BATTERY_PLUGGED_USB:
			return "USB";
		default:
			return "Unknown";
		}

	}

	private static String statusCodeToString(int status) {
		switch (status) {
		case BATTERY_STATUS_CHARGING:
			return "Charging";
		case BATTERY_STATUS_DISCHARGING:
			return "Discharging";
		case BATTERY_STATUS_FULL:
			return "Full";
		case BATTERY_STATUS_NOT_CHARGING:
			return "Not charging";
		case BATTERY_STATUS_UNKNOWN:
		default:
			return "Unknown";
		}
	}

	private void showBatteryInfo(int chat_id, int message_id, Intent intent) {
		if (intent != null) {
			int health = intent.getIntExtra(EXTRA_HEALTH,
					BATTERY_HEALTH_UNKNOWN);
			String healthString = "Health: " + healthCodeToString(health);
			Log.i("MU", healthString);
			int level = intent.getIntExtra(EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(EXTRA_SCALE, 100);
			float percentage = (scale != 0) ? (100.f * (level / (float) scale))
					: 0.0f;
			String levelString = String.format("Level: %d/%d (%.2f%%)", level,
					scale, percentage);
			Log.i("MU", levelString);
			int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
			String pluggedString = "Power source: "
					+ pluggedCodeToString(plugged);
			Log.i("MU", pluggedString);
			boolean present = intent.getBooleanExtra(EXTRA_PRESENT, false);
			String presentString = "Present? " + (present ? "Yes" : "No");
			Log.i("MU", presentString);
			int status = intent.getIntExtra(EXTRA_STATUS,
					BATTERY_STATUS_UNKNOWN);
			String statusString = "Status: " + statusCodeToString(status);
			Log.i("MU", statusString);
			String technology = intent.getStringExtra(EXTRA_TECHNOLOGY);
			String technologyString = "Technology: " + technology;
			Log.i("MU", technologyString);
			int temperature = intent.getIntExtra(EXTRA_STATUS,
					Integer.MIN_VALUE);
			String temperatureString = "Temperature: " + temperature;
			Log.i("MU", temperatureString);
			int voltage = intent.getIntExtra(EXTRA_VOLTAGE, Integer.MIN_VALUE);
			String voltageString = "Voltage: " + voltage;
			Log.i("MU", voltageString);

			String s = getDeviceName() + "\n";
			s += getAndroidVersion() + "\n";
			s += "Models : " + Build.MODEL + "\n";
			s += "Board: " + Build.BOARD + "\n";
			s += "Hardware : " + Build.HARDWARE + "\n";
			s += "Boatloader: " + Build.BOOTLOADER + "\n";
			s += "fingerPrint : " + Build.FINGERPRINT + "\n";
			s += "other : " + Build.CPU_ABI + "\n";
			s += healthString + "\n";
			s += levelString + "\n";
			s += pluggedString + "\n";
			s += presentString + "\n";
			s += statusString + "\n";
			s += technologyString + "\n";
			s += temperatureString + "\n";
			s += voltageString;
			try {

				Log.d("MU", "string yang akan dikirim " + s);
				Utils.sendChatAction(chat_id, "typing");
				String r = Utils.sendMessage(chat_id, message_id, s);
				Log.d("MU", r);
			} catch (ClientProtocolException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			}
			// mTextView.setText(s);
			// Note: using a StringBuilder object would have been more efficient
		} else {
			String s = "No battery information";
			Log.i("MU", s);
			try {
				Utils.sendChatAction(chat_id, "typing");
				String r = Utils.sendMessage(chat_id, message_id, s);
				Log.d("MU", r);
			} catch (ClientProtocolException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODOs Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void showBatteryInfo() {
		// no receiver needed
		Intent intent = registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		showBatteryInfo(chat_id, message_id, intent);
	}

	/**
	 * 
	 * Calendar cal = Calendar.getInstance(); Intent intent = new
	 * Intent(ActionbarPage.this,NotificationServis.class); PendingIntent
	 * pintent = PendingIntent.getService(this, 0, intent, 0); AlarmManager
	 * alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	 * alarm.setRepeating
	 * (AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),10*1000,pintent);
	 * 
	 * */

	@Override
	public IBinder onBind(Intent arg0) {
		// TODOs Auto-generated method stub
		return mBinder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODOs Auto-generated method stub
		context = BotService.this;
		setUpdate_idc();
		getLocation();

		new GetUpdates().execute("latian");
		return (START_STICKY);
	}

	private class GetUpdates extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODOs Auto-generated method stub
			String res = "";
			Log.d("MU", "load this");
			try {
				Log.d("MU", "load this");
				res = Utils.getUpdates(update_id++);
				Log.d("MU", res);
				JSONObject obj = new JSONObject(res);
				String arrayString = obj.getString("result");
				JSONArray jArray = new JSONArray(arrayString);
				Log.d("MU", "array String " + arrayString);
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject message = jArray.getJSONObject(i).getJSONObject(
							"message");
					String message_string = jArray.getJSONObject(i).getString(
							"message");
					chat_id = message.getJSONObject("chat").getInt("id");
					Log.d("MU", "chat id " + chat_id);
					// String username =
					// 
					// Log.d("MU","username "+username);
					Log.d("MU", "get the text messages");
					
					message_id = message.getInt("message_id");

					update_id = jArray.getJSONObject(i).getInt("update_id");
					ContentValues values = new ContentValues();
					values.put("Messages_id", String.valueOf(message_id));

					if (message_id > msg_idc) {
						context.getContentResolver()
						.insert(Uri
								.parse("content://com.resea.androtelebot.updateprovider/element"),
								values);
						if (message_string.contains("\"text\"")) {
							text = message.getString("text");
							user_id = message.getJSONObject("from").getInt("id");
							userName = message.getJSONObject("from").getString("username");
							Log.d("MU", "the text is " + text);
						}else {
							// String sticker = message.getString("sticker");
							text = "less";
							Log.d("Mu", text);

						}
						
						if(message_string.contains("\"reply_to_message\"")){
							from_chat_id = message.getJSONObject("reply_to_message").getJSONObject("chat").getInt("id");
							from_msg_id = message.getJSONObject("reply_to_message").getInt("message_id");
							from_id = message.getJSONObject("from").getInt("id");
							from_user_name = message.getJSONObject("reply_to_message").getJSONObject("from").getString("username");
							Log.i("MU",""+from_chat_id+" "+from_msg_id+" "+from_user_name);
							if(from_user_name.toLowerCase().equalsIgnoreCase("user_name_bot")){
								Cursor cursor = context
										.getContentResolver()
										.query(Uri
												.parse("content://com.resea.androtelebot.databaseprovider/element"),
												load, null, null, null);
								cursor.moveToFirst();
								do {
									String kata = cursor.getString(cursor
											.getColumnIndex("Kata_Row"));
									list.add(kata);
								} while (cursor.moveToNext());
								cursor.close();
								Random rnd;
								String text_replay="";
								List <String> item = Arrays.asList(text.split(" "));
								if(item.size()>0&&item!=null){
									for(int j=0;j<item.size();j++){
										rnd = new Random();
										int indexrand = rnd.nextInt(list.size()-j);
										text_replay += " " + list.get(indexrand);
									}
								}else {
									rnd = new Random();
									int indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									
								}
								Log.d("MU", "sending");
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										text_replay);
								list.clear();
							}
							if((text.startsWith("forward "))&&(userName.toLowerCase().equalsIgnoreCase("your_user_name"))){
								Utils.forwardMessage(chat_id, from_chat_id, from_msg_id);
							}
							
						}
						
						Log.d("MU", "message id " + message_id);

						if (text.toLowerCase().startsWith("/echo")) {
							String msg = "";
							if(text.toLowerCase().contains("/echo@sabetan_bot")){
								msg = text.toLowerCase().replace("/echo@sabetan_bot ", "");
							}else{
								msg = text.toLowerCase().replace("/echo ", "");
							}
							Utils.sendChatAction(chat_id, "typing");
							Utils.sendMessage(chat_id, message_id, msg);
						} else if (text.toLowerCase().startsWith("/siapalo")) {
							String text_replay = "aku adalah seorang muslim jika aku sendirian dan komunis jika aku dalam kerumunan karena Allah berfirman setan ada dalam kerumunan (Tan Malaka).\ndibuat oleh kucengaerdev laboratory";
							Log.d("MU", "sending " + text_replay);
							Utils.sendChatAction(chat_id, "typing");
							String ut = Utils.sendMessage(chat_id, message_id,
									text_replay);
							Log.d("MU", ut);
						} else if (text.toLowerCase().startsWith("/bantuan")) {

							Utils.sendChatAction(chat_id, "typing");
							String s = Utils
									.sendHelp(
											user_id,
											"/siapalo - tentang bot \n/info - informasi batrei device\n/detobin <desimal> - convert desimal ke biner\n/bintode <biner> - convert biner ke desimal\n/track - tracking my location\n/echo <text> - printing the text.\n/bantuan - untuk bantuan");
							Log.d("MU", s);
						} else if (text.toLowerCase().startsWith("/info")) {
							showBatteryInfo();
						} else if (text.toLowerCase().startsWith("/detobin")) {
							String stringDes = "";
							if(text.toLowerCase().contains("/detobin@sabetan_bot")){
								stringDes = text.toLowerCase().replace("/detobin@sabetan_bot ", "");
							}else{
								stringDes = text.toLowerCase().replace("/detobin ", "");
							}
							Log.d("MU", stringDes);
							try {
								int decimal = Integer.parseInt(stringDes, 10);
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										Integer.toBinaryString(decimal));
							} catch (Exception e) {
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										"format invalid");
							}
						} else if (text.toLowerCase().startsWith("/bintode")) {
							String stringBin = "";
							if(text.toLowerCase().contains("/bintode@sabetan_bot")){
							    stringBin = text.toLowerCase().replace("/bintode@sabetan_bot ", "");
							}else{
								stringBin = text.toLowerCase().replace("/bintode ", "");
							}
							Log.d("MU", stringBin);
							try {
								int decimal = Integer.parseInt(stringBin, 2);
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										Integer.toString(decimal));
							} catch (Exception e) {
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										"format invalid");
							}
						} else if (text.toLowerCase().startsWith("/track")) {
							if (latitude != 0 && longitude != 0) {
								Log.i("MU", "send "+latitude+" and "+longitude);
								Utils.sendChatAction(chat_id, "find_location");
								Utils.sendLocation(chat_id, message_id,(float)latitude,(float)longitude);
							} else {
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										"gps dimatikan");
							}
						} else if (text.toLowerCase().equalsIgnoreCase("/poto")) {
							Utils.sendChatAction(chat_id, "typing");
							Utils.sendMessage(chat_id, message_id,
									"perintah dilaksanakan");
							context.startService(new Intent(BotService.this,
									CameraService.class));
						} else if (text.toLowerCase().contains("ganteng")) {
							Utils.sendChatAction(chat_id, "typing");
							Utils.sendMessage(chat_id, message_id,
									"di dunia ini ngga ada yang ganteng kecuali suamiku.");
						} else if (text.toLowerCase().contains("hatabomba")) {
							Utils.sendChatAction(chat_id, "typing");
							Utils.sendMessage(chat_id, message_id,
									" maksud lo ape bray, sebut-sebut dia, dia kan maho bray!");
						} else if (text.toLowerCase().contains("crot")) {
							Utils.sendChatAction(chat_id, "typing");
							Utils.sendMessage(chat_id, message_id,
									"chroot /mnt");
						} else if (text.toLowerCase().startsWith("san ")) {
							String texts = text.toLowerCase().replace("san ", "");
							if (texts.toLowerCase().contains("jam ")) {
								if(userName.equalsIgnoreCase("your_user_name")){
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(
											chat_id,
											message_id,
											"sekarang udah jam "
													+ sdf.format(new Date(System
															.currentTimeMillis()))+" ,sayang.");
								}else{
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(chat_id, message_id, "sekarang jam "+sdf.format(new Date(System.currentTimeMillis()))+" kak.");
								}
								
								
							}else if (texts.contains("rizky ")){
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id, "rizky orangnya ganteng gan");
							}else if (texts.contains("firja ")){
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id, "firja kayak orang persia atau iran ganteng gan sumpah.");
							}else if (texts.toLowerCase().contains("sms ")) {
								String textSms = texts.replace("sms ", "");
								List<String> items = Arrays.asList(textSms.split(" "));
								String phoneNum = items.get(0);
								String smsText= "";
								for(int j=1;j<items.size();j++){
								
									smsText +=" "+items.get(j);
									
								}
								
								if(userName.equalsIgnoreCase("your_user_name")){
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(chat_id, message_id, "iya sayang aku kerjakan kok nih");
									Log.d("MU", phoneNum+" "+smsText);
									kirimSms(phoneNum, smsText);
									
								}else {
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(chat_id, message_id, "kamu kan bukan suamiku jadi ngga bisa akses ini.");
								}
								items.clear();
								
							}else if(texts.toLowerCase().contains("bangun ")){
								String alarm = texts.replace("bangun ", "");
								List<String> items = Arrays.asList(alarm.split(" "));
								if(userName.equalsIgnoreCase("your_user_name")){
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(chat_id, message_id, "ok sayang aku bangunkan nanti.");
									Intent in = new Intent(AlarmClock.ACTION_SET_ALARM);
									in.putExtra(AlarmClock.EXTRA_HOUR, Integer.valueOf(items.get(0)));
									in.putExtra(AlarmClock.EXTRA_MINUTES, Integer.valueOf(items.get(1)));
									in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(in);
								}else{
									Utils.sendChatAction(chat_id, "typing");
									Utils.sendMessage(chat_id, message_id, "kamu ngga bisa akses");
								}
							} else if(texts.startsWith("play")){
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id, "akan membuka music player");
								Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								BotService.this.startActivity(intent);
							} else if(texts.contains("hitung ")){
								String texting = texts.replace("hitung ", "");
								List<String> items = Arrays.asList(texting.split(" "));
								try{
									if(items.get(0).contains("akar")){
										Log.i("MU", items.get(0));
										Log.i("MU", items.get(1));
										double dAkar = Double.valueOf(items.get(1));
										double hasil = Math.sqrt(dAkar);
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "akar dari "+dAkar+" = "+hasil);
									}else if(items.get(0).contains("luasbumi")){
										double rBumi = 6378.137;
										double lLingkaran = Math.PI *(Math.pow(rBumi, 2));
										double luasBumi = 6 * lLingkaran;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "Luas bumi adalah "+luasBumi+" km persegi");
									}else if(items.get(0).contains("luaslingkaran")){
										double rLingkaran = Double.valueOf(items.get(1));
										double luasLingkaran = Math.PI *(Math.pow(rLingkaran, 2));
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "luas lingkaranya = "+luasLingkaran+" cm persegi");
									}else if(items.get(0).contains("kelilinglingkaran")){
										double rLingkaran = Double.valueOf(items.get(1));
										double kelilingLingkaran = Math.PI * rLingkaran;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "keliling lingkaran = "+kelilingLingkaran+" cm");
									}else if(items.get(0).contains("kuatarus")){
										double tgangan = Double.valueOf(items.get(1));
										double hambatan = Double.valueOf(items.get(2));
										double kuatArus = tgangan / hambatan;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "kuat arus = "+kuatArus+" ampere");
									}else if(items.get(0).contains("tegangan")){
										double hambatan = Double.valueOf(items.get(1));
										double kuatArus = Double.valueOf(items.get(2));
										double tegangan = hambatan * kuatArus;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "tegangan = "+tegangan+" volt");
									}else if(items.get(0).contains("hambatan")){
										double tegangan = Double.valueOf(items.get(1));
										double kuatArus = Double.valueOf(items.get(2));
										double hambatan = tegangan / kuatArus;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "hambatan = "+hambatan+" ohm");
									}else if(items.get(0).contains("jarak")){
										double kecepatan = Double.valueOf(items.get(1));
										double waktu = Double.valueOf(items.get(2));
										double jarak = kecepatan * waktu;
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "jarak = "+jarak+" kilometer");
									}else if(items.get(0).contains("luaspermukaanbumi")){
										double rBumi = 6378.137;
										double lLingkaran = Math.PI *(Math.pow(rBumi, 2));
										Utils.sendChatAction(chat_id, "typing");
										Utils.sendMessage(chat_id, message_id, "luas permukaan bumi adalah "+lLingkaran+" km persegi");
									}else{
									   double d1 = Double.valueOf(items.get(0));
									   double d2 = Double.valueOf(items.get(2));
									   double hasil = 0.0;
									   Log.i("MU", items.get(1));
									   if((items.get(1).contains("bagi"))||(items.get(1).contains("/"))||(items.get(1).contains(":"))){
										   hasil = d1 / d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, ""+d1+" : "+d2+" = "+hasil);
									   }else if((items.get(1).contains("kali"))||(items.get(1).contains("x"))||(items.get(1).contains("*"))){
										   hasil = d1 * d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, ""+d1+" X "+d2+" = "+hasil);
									   }else if((items.get(1).contains("tambah"))||(items.get(1).contains("+"))){
										   hasil = d1 + d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, ""+d1+" + "+d2+" = "+hasil);
									   }else if((items.get(1).contains("kurangi"))||(items.get(1).contains("-"))){
										   hasil = d1 - d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, ""+d1+" - "+d2+" = "+hasil);
									   }else if((items.get(1).contains("modulo"))||(items.get(1).contains("mod"))||(items.get(1).contains("%"))){
										   hasil = d1 % d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, ""+d1+" % "+d2+" = "+hasil);
									   }else{
										   hasil = Math.random()*d1/d2;
										   Utils.sendChatAction(chat_id, "typing");
										   Utils.sendMessage(chat_id, message_id, "jawabannya adalah "+hasil);
									   }
									   
									}
								}catch(Exception e){
									Utils.sendMessage(chat_id, message_id, "invalid format number");								
								}
								items.clear();
							}else {
								Cursor cursor = context
										.getContentResolver()
										.query(Uri
												.parse("content://com.resea.androtelebot.databaseprovider/element"),
												load, null, null, null);
								cursor.moveToFirst();
								do {
									String kata = cursor.getString(cursor
											.getColumnIndex("Kata_Row"));
									list.add(kata);
								} while (cursor.moveToNext());
								cursor.close();
								Random rnd;
								String text_replay="";
								List <String> item = Arrays.asList(texts.split(" "));
								if(item.size()>0&&item!=null){
									for(int j=0;j<item.size();j++){
										rnd = new Random();
										int indexrand = rnd.nextInt(list.size()-j);
										text_replay += " " + list.get(indexrand);
									}
								}else {
									rnd = new Random();
									int indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									indexrand = rnd.nextInt(list.size());
									text_replay += " " + list.get(indexrand);
									
								}
								Log.d("MU", "sending");
								Utils.sendChatAction(chat_id, "typing");
								Utils.sendMessage(chat_id, message_id,
										text_replay);
								list.clear();
							}
						}
					} else {

						Log.d("MU", "message id sama dengan yang tadi hehe");
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
			// Log.d("MU", res);
			return null;
		}
	}

	private void kirimSms(String phone_text, String psn) {
		String KIRIM = "SMS_SENT";
		String TERKIRIM = "SMS_DELIVERED";
		PendingIntent kirpen = PendingIntent.getBroadcast(BotService.this, 0,
				new Intent(KIRIM), 0);
		PendingIntent terkipen = PendingIntent.getBroadcast(BotService.this, 0,
				new Intent(TERKIRIM), 0);
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODOs Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(context, "sending...", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(context, "error generic", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(context, "no service", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(context, "pdu null", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(context, "radio off", Toast.LENGTH_SHORT)
							.show();
					break;
				}
			}

		}, new IntentFilter(KIRIM));

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODOs Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(context, "terkirim", Toast.LENGTH_SHORT)
							.show();
					break;

				case Activity.RESULT_CANCELED:
					Toast.makeText(context, "gagal terkirim",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(TERKIRIM));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phone_text, null, psn, kirpen, terkipen);

	}

	public class MyBinder extends Binder {
		BotService getService() {
			return BotService.this;
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODOs Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODOs Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODOs Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODOs Auto-generated method stub

	}

	public String getAndroidVersion() {
		String release = Build.VERSION.RELEASE;
		int sdkVersion = Build.VERSION.SDK_INT;
		return "Android SDK and OS Version: " + sdkVersion + " (" + release
				+ ")";
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String produk = Build.PRODUCT;

		return "Device Vendor and product : " + capitalize(manufacturer) + " "
				+ produk;
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}


}
