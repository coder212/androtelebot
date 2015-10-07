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
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Service;
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
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class BotService extends Service implements LocationListener {

	private final IBinder mBinder = new MyBinder();
	static Context context;
	ArrayList<String> list = new ArrayList<>();
	String[] load = { "Kata_Row" };
	String[] loadup = {"Updates_id"};
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGETLocation = false;
	Location location; // location
	double latitude = 0; // latitude
	double longitude = 0; // longitude
	int update_id;
	int update_idc = 0; 
	int message_id = 0;
	int chat_id = 0;
	String text = "";
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
	
	 public void showSettingsAlert(){
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
	      
	        // Setting Dialog Title
	        alertDialog.setTitle("GPS is settings");
	  
	        // Setting Dialog Message
	        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
	  
	        // On pressing Settings button
	        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                context.startActivity(intent);
	            }
	        });
	  
	        // on pressing cancel button
	        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	            }
	        });
	 }
	
	 public void stopUsingGPS(){
	        if(locationManager != null){
	            locationManager.removeUpdates(BotService.this);
	        }      
	    }
	     
	    /**
	     * Function to get latitude
	     * */
	    public double getLatitude(){
	        if(location != null){
	            latitude = location.getLatitude();
	        }
	         
	        // return latitude
	        return latitude;
	    }
	     
	    /**
	     * Function to get longitude
	     * */
	    public double getLongitude(){
	        if(location != null){
	            longitude = location.getLongitude();
	        }
	         
	        // return longitude
	        return longitude;
	    }
	     
	    /**
	     * Function to check GPS/wifi enabled
	     * @return boolean
	     * */
	    public boolean canGetLocation() {
	        return this.canGETLocation;
	    }
	 
	private void setUpdate_idc(){
		Cursor cursor = context
				.getContentResolver()
				.query(Uri
						.parse("content://com.resea.androtelebot.updateprovider/element"),
						loadup, null, null, null);
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToLast();
			String updates_id = cursor.getString(cursor
					.getColumnIndex("Updates_id"));
			update_idc = Integer.parseInt(updates_id);
		}else{
			update_idc = 0;
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
			String s = healthString + "\n";
			s += levelString + "\n";
			s += pluggedString + "\n";
			s += presentString + "\n";
			s += statusString + "\n";
			s += technologyString + "\n";
			s += temperatureString + "\n";
			s += voltageString;
			try {
				
				Log.d("MU", "string yang akan dikirim "+s);
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
					// message.getJSONObject("chat").getString("username");
					// Log.d("MU","username "+username);
					Log.d("MU", "get the text messages");
					
					update_id = jArray.getJSONObject(i).getInt("update_id");
					ContentValues values = new ContentValues();
					values.put("Updates_id", String.valueOf(update_id));

					context
							.getContentResolver()
							.insert(Uri
									.parse("content://com.resea.androtelebot.updateprovider/element"),
									values);
					if(update_id > update_idc){
						if (message_string.contains("\"text\"")) {
							text = message.getString("text");
							Log.d("MU", "the text is " + text);
						} else {
							//String sticker = message.getString("sticker");
							text = "less";
							Log.d("Mu", text);
							
						}
						message_id = message.getInt("message_id");
						Log.d("MU", "message id " + message_id);
						if (text.contains("siapa lo")) {
							String text_replay = "aku adalah seorang muslim jika aku sendirian dan komunis jika aku dalam kerumunan karena Allah berfirman setan ada dalam kerumunan (Tan Malaka)";
							Log.d("MU", "sending " + text_replay);
							String ut = Utils.sendMessage(chat_id, message_id, text_replay);
							Log.d("MU", ut);
						} else if (text.contains("ngomong mabuk")) {
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
							Random rnd = new Random();
							int indexrand = rnd.nextInt(list.size());
							String text_replay = list.get(indexrand);
							indexrand = rnd.nextInt(list.size());
							text_replay += " "+list.get(indexrand);
							indexrand = rnd.nextInt(list.size());
							text_replay += " "+list.get(indexrand);
							Log.d("MU", "sending");
							Utils.sendMessage(chat_id, message_id, text_replay);
						} else if (text.contains("help")) {

							String s = Utils.sendMessage(chat_id, message_id,
									"siapa lo - tentang bot \ninfo - informasi batrei device\ndetobin <desimal> - convert desimal ke biner\nngomong mabuk - ngomong random kayak orang mabuk\nbintode <biner> - convert biner ke desimal\ntrack - tracking my location");
							Log.d("MU", s);
						}else if(text.contains("info")){
							showBatteryInfo();
						}else if(text.contains("detobin")){
							String stringDes = text.replace("detobin ","");
							Log.d("MU", stringDes);
							try{
							    int decimal = Integer.parseInt(stringDes, 10);
							    Utils.sendMessage(chat_id, message_id, Integer.toBinaryString(decimal));
							}catch(Exception e){
								Utils.sendMessage(chat_id, message_id, "format invalid");
							}
						}else if(text.contains("bintode")){
							String stringBin = text.replace("bintode ","");
							Log.d("MU", stringBin);
							try{
							    int decimal = Integer.parseInt(stringBin, 2);
							    Utils.sendMessage(chat_id, message_id, Integer.toString(decimal));
							}catch(Exception e){
								Utils.sendMessage(chat_id, message_id, "format invalid");
							}
						}else if(text.contains("track")){
							if(latitude!=0 && longitude!=0 ){
								Utils.sendMessage(chat_id, message_id, "latitude: "+latitude+" , longitude: "+longitude);
							}else{
								Utils.sendMessage(chat_id, message_id, "gps dimatikan");
							}
						}
					}else {
						
						Log.d("MU", "update id sama dengan yang tadi hehe");
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

}
