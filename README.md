# androtelebot
android telegram bot 

Telegram adalah applikasi chatting multiplatform dan foss yang asyik untuk dipake chat.
Salah satu featurenya adalah adanya bot api yang mereka sediakan ok tanpa panjang lebar ane akan paparkan tahapannya:
a. register ke @BotFather dengan cara /newbot dapet balasan please call name misal silvya is my wife kemudian dapet balasan meminta username yang akan digunakan Sylvia_bot kemudian dapet balasan token yang dipake untuk access bot.
b. tahap membuat algoritma dan source code dalam contoh ini menggunakan bahasa java dan pure android sdk.
Pertama kali buat DatabaseBot yang akan digunakan untuk menyimpan messages_id
didalamnya terdapat variable yang akan dipake untuk create dan update table database.
DatabaseBot.java

package com.resea.androtelebot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseBot extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "botgajebo.db";
	public static final int DATABASE_VERSION = 2;
	
	public final String ID = "_id";
	
	public final String UPDATES_ID_ROW = "Messages_id";
	public final String UPDATES_ID_TABLE = "Messages_id_table";
	
	private final String CREATE_UPDATES_ID_TABLE = "create table "+UPDATES_ID_TABLE+"("+ID+" integer primary key,"+UPDATES_ID_ROW+" text not null);";
	private final String UPDATE_UPDATES_ID_TABLE = "drop table if exists "+UPDATES_ID_TABLE;
	

	public DatabaseBot(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODOs Auto-generated constructor stub
	}



	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODOs Auto-generated method stub
		
		db.execSQL(CREATE_UPDATES_ID_TABLE);
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {
		// TODOs Auto-generated method stub
		
		db.execSQL(UPDATE_UPDATES_ID_TABLE);
		onCreate(db);
	}

}

fungsi onCreate digunakan untuk membuat table database dan  onUpgrade digunakan upgrade database jika ada versi yang baru.
Setelah itu kita membuat update provider yang digunakan untuk mengakses database yaitu creating database ,insert database,delete database dan update, tapi saya tidak menggunakan fungsi update.
Saya membuat uri matchers untuk menemukan kata kunci yang akan kita gunakan untuk prosess.
UpdateProvider.java

package com.resea.androtelebot;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class UpdateProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.resea.androtelebot.updateprovider/element");
	private static final int ALLROWS = 1;
	private static final int NAME=2;
	private static final int NAME_TO_DELETE=3;
	private static final int NAME_FILTER=4;
	private static final String CALLER_IS_SYNC_ADAPTER = "update";
	private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.resea.androtelebot.updateprovider","element",ALLROWS);
		uriMatcher.addURI("com.resea.androtelebot.updateprovider","element/list/*", NAME);
		uriMatcher.addURI("com.resea.androtelebot.updateprovider", "element/lists", NAME_TO_DELETE);
		uriMatcher.addURI("com.resea.androtelebot.updateprovider","element/filter/*", NAME_FILTER);
	}

	
	private boolean CallerIsSyncAdapter(Uri uri){
		final String is_sync_adapter = uri.getQueryParameter(CALLER_IS_SYNC_ADAPTER);
		return is_sync_adapter != null && !is_sync_adapter.equals("0");
	}

	private DatabaseBot botDb;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODOs Auto-generated method stub
		int UriType = uriMatcher.match(uri);
		SQLiteDatabase db = botDb.getWritableDatabase();
		int rowsDeleted = 0;
		switch (UriType){
		    case ALLROWS:
			    rowsDeleted = db.delete(botDb.UPDATES_ID_TABLE,selection,selectionArgs);
			    break;
		    case NAME_TO_DELETE:
			    rowsDeleted = db.delete(botDb.UPDATES_ID_TABLE,botDb.UPDATES_ID_ROW+"= '"+selection+"'", selectionArgs);
			    break;
	 	    default :
			    throw new IllegalArgumentException("Unknown Uri : "+uri);
		}
		getContext().getContentResolver().notifyChange(uri,null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODOs Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODOs Auto-generated method stub
		Log.d("ListProvider","firsttime");
		int uritype = uriMatcher.match(uri);
		SQLiteDatabase db = botDb.getWritableDatabase();
		long id = 0;
		switch(uritype){
		    case ALLROWS:
		    	id = db.insert(botDb.UPDATES_ID_TABLE, null, values);
		        break;
		    default :
		    	throw new IllegalArgumentException("unKnown uri : "+uri);
		}
		getContext().getContentResolver().notifyChange(uri,null,!CallerIsSyncAdapter(uri));
		return Uri.parse("element"+"/"+id);
	}

	@Override
	public boolean onCreate() {
		// TODOs Auto-generated method stub
		botDb = new DatabaseBot(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODOs Auto-generated method stub
		Log.d("ListProvider",uri.toString());
		SQLiteQueryBuilder queryB = new SQLiteQueryBuilder();
		checkColumns(projection);
		queryB.setTables(botDb.UPDATES_ID_TABLE);
		int uriType = uriMatcher.match(uri);
		Log.d("ListProvider", "uriType "+uriType);
		switch (uriType) {
		case ALLROWS:
			
			break;
		
		case NAME:
			
			queryB.appendWhere(botDb.UPDATES_ID_ROW+"= '"+uri.getLastPathSegment()+"'");
			break;
		
		case NAME_FILTER:
			
			queryB.appendWhere(botDb.UPDATES_ID_ROW+" LIKE '%"+uri.getLastPathSegment()+"%'");
			break;

		default:
			throw new IllegalArgumentException("Unknown uri : "+uri);
		}
		SQLiteDatabase db = botDb.getReadableDatabase();
		Cursor cursor = queryB.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private void checkColumns(String[] projection) {
		// TODOs Auto-generated method stub
		String[] avail = {botDb.UPDATES_ID_ROW};

		if (projection != null){
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availColumns = new HashSet<String>(Arrays.asList(avail));
			if(!availColumns.containsAll(requestedColumns)){
				throw new IllegalArgumentException("proyeksi database tak dikenal.");
			}
		}
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODOs Auto-generated method stub
		return 0;
	}

}

selanjutnya kita membuat  utils yang akan kita gunakan untuk akses bot api,
public static final String URL_TELEGRAM = "https://api.telegram.org/bot";
public static final String TOKEN = "YOUR TOKEN HERE";
public static final String SEND_MESSAGE = "sendMessage";
public static final String GET_UPDATES = "getUpdates";
URL_TELEGRAM adalah variable dari alamat telegram bot api
TOKEN adalah akses token yang kita dapatkan dari botfather tadi
SEND MESSAGES adalah service yang akan kita request ke bot api
get_updates juga sama kayak send messages tapi ini untuk mendapatkan data chat telegram yang akan di respon bot
private static HttpPost httppost;
private static HttpClient httpclient;
private static List<NameValuePair> nameValuePairs ;
variable diatas kita gunakan untuk request ke bot api
public static String getUpdates(int offset) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("offset",Integer.toString(offset)));
		return sendData(TOKEN+"/"+GET_UPDATES, nameValuePairs);
		
	}
fungsi ini kita gunakan untuk mendapatkan update chat yang terbaru

public static String sendMessage(int chat_id, int message_id, String message) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(chat_id)));
		nameValuePairs.add(new BasicNameValuePair("text", message));
		nameValuePairs.add(new BasicNameValuePair("reply_to_message_id", Integer.toString(message_id)));
		return sendData(TOKEN+"/"+SEND_MESSAGE, nameValuePairs);
	}
fungsi diatas kita gunakan untuk mengirimkan respon bot terhadap chat ke bot api

public static String sendData(String urlex, List<NameValuePair> nameValuePairs) throws ClientProtocolException, IOException{
		httpclient=new DefaultHttpClient();
		httppost= new HttpPost(URL_TELEGRAM+urlex);
		Log.d("MU","trying konek");
		//httppost.setHeader(new BasicHeader("token", TOKEN));
		//StringEntity entity = new StringEntity("token", "UTF-8");
		//Log.d("MU","set entitas");
		
		//httppost.setEntity(entity);
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		Log.d("MU","entiti value pairs was set");

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		
		return httpclient.execute(httppost,responseHandler);
	}
fungsi ini kita gunakan untuk send request dan set parameter httpost dan mendapatkan response berupa data.
Utils.java

package com.resea.androtelebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class Utils {
	
	public static final String URL_TELEGRAM = "https://api.telegram.org/bot";
	public static final String TOKEN = "YOUR TOKEN HERE";
	public static final String SEND_MESSAGE = "sendMessage";
	public static final String GET_UPDATES = "getUpdates";
	private static HttpPost httppost;
	private static HttpClient httpclient;
	private static List<NameValuePair> nameValuePairs ;
	
	public static String getUpdates(int offset) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("offset",Integer.toString(offset)));
		return sendData(TOKEN+"/"+GET_UPDATES, nameValuePairs);
		
	}
	
	public static String sendMessage(int chat_id, int message_id, String message) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(chat_id)));
		nameValuePairs.add(new BasicNameValuePair("text", message));
		nameValuePairs.add(new BasicNameValuePair("reply_to_message_id", Integer.toString(message_id)));
		return sendData(TOKEN+"/"+SEND_MESSAGE, nameValuePairs);
	}
	
	public static String sendData(String urlex, List<NameValuePair> nameValuePairs) throws ClientProtocolException, IOException{
		httpclient=new DefaultHttpClient();
		httppost= new HttpPost(URL_TELEGRAM+urlex);
		Log.d("MU","trying konek");
		//httppost.setHeader(new BasicHeader("token", TOKEN));
		//StringEntity entity = new StringEntity("token", "UTF-8");
		//Log.d("MU","set entitas");
		
		//httppost.setEntity(entity);
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		Log.d("MU","entiti value pairs was set");

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		
		return httpclient.execute(httppost,responseHandler);
	}

}


selanjutnya kita membuat bot service yang dinamakan BotService.java
yang digunakan untuk proses background update dan send response berikut ini source codenya semoga bisa dipahami yak.

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
	int chat_id = 0;
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
			
			String s = getDeviceName()+"\n";
			s += getAndroidVersion()+"\n";
			s += "Models : "+Build.MODEL+"\n";
			s += "Board: "+Build.BOARD+"\n";
			s += "Hardware : "+Build.HARDWARE+"\n";
			s += "Boatloader: "+Build.BOOTLOADER+"\n";
			s += "Display : "+Build.DISPLAY+"\n";
			s += "other : "+Build.CPU_ABI+"\n";
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
					// 
					// Log.d("MU","username "+username);
					Log.d("MU", "get the text messages");
					
					message_id = message.getInt("message_id");

					update_id = jArray.getJSONObject(i).getInt("update_id");
					ContentValues values = new ContentValues();
					values.put("Messages_id", String.valueOf(message_id));

					context.getContentResolver()
							.insert(Uri
									.parse("content://com.resea.androtelebot.updateprovider/element"),
									values);
					if (message_id > msg_idc) {
						if (message_string.contains("\"text\"")) {
							text = message.getString("text");
							userName = message.getJSONObject("from").getString("username");
							Log.d("MU", "the text is " + text);
						} else {
							// String sticker = message.getString("sticker");
							text = "less";
							Log.d("Mu", text);

						}
						
						Log.d("MU", "message id " + message_id);

						if (text.toLowerCase().startsWith("\echo ")) {
							String msg = text.replace("echo ", "");
							Utils.sendMessage(chat_id, message_id, msg);
						} else if (text.toLowerCase().startsWith("/siapa ")) {
							String text_replay = "aku adalah seorang muslim jika aku sendirian dan komunis jika aku dalam kerumunan karena Allah berfirman setan ada dalam kerumunan (Tan Malaka).\ndibuat oleh kucengaerdev laboratory";
							Log.d("MU", "sending " + text_replay);
							String ut = Utils.sendMessage(chat_id, message_id,
									text_replay);
							Log.d("MU", ut);
						} else if (text.toLowerCase().startsWith("/bantuan ")) {

							String s = Utils
									.sendMessage(
											chat_id,
											message_id,
											"/siapa - tentang bot \n/info - informasi batrei device\n/detobin <desimal> - convert desimal ke biner\n/bintode <biner> - convert biner ke desimal\n/track - tracking my location\n/echo <text> - printing the text.\n/bantuan - untuk bantuan");
							Log.d("MU", s);
						} else if (text.toLowerCase().startsWith("/info ")) {
							showBatteryInfo();
						} else if (text.toLowerCase().startsWith("/detobin ")) {
							String stringDes = text.replace("/detobin ", "");
							Log.d("MU", stringDes);
							try {
								int decimal = Integer.parseInt(stringDes, 10);
								Utils.sendMessage(chat_id, message_id,
										Integer.toBinaryString(decimal));
							} catch (Exception e) {
								Utils.sendMessage(chat_id, message_id,
										"format invalid");
							}
						} else if (text.toLowerCase().startsWith ("/bintode ")) {
							String stringBin = text.replace("/bintode ", "");
							Log.d("MU", stringBin);
							try {
								int decimal = Integer.parseInt(stringBin, 2);
								Utils.sendMessage(chat_id, message_id,
										Integer.toString(decimal));
							} catch (Exception e) {
								Utils.sendMessage(chat_id, message_id,
										"format invalid");
							}
						} else if (text.toLowerCase().startsWith("/track ")) {
							if (latitude != 0 && longitude != 0) {
								Utils.sendMessage(chat_id, message_id,
										"latitude: " + latitude
												+ " , longitude: " + longitude);
							} else {
								Utils.sendMessage(chat_id, message_id,
										"gps dimatikan");
							}
						} else if (text.toLowerCase().startsWith("san ")) {
							String texts = text.toLowerCase().replace("san ", "");
							if (texts.toLowerCase().contains("jam ")) {
								if(userName.equalsIgnoreCase("martin_luther")){
									Utils.sendMessage(
											chat_id,
											message_id,
											"sekarang udah jam "
													+ sdf.format(new Date(System
															.currentTimeMillis()))+" ,sayang.");
								}else{
									 
									Utils.sendMessage(chat_id, message_id, "sekarang jam "+sdf.format(new Date(System.currentTimeMillis()))+" kak.");
								}
								
								
							}else if (texts.contains("rizky ")){
								Utils.sendMessage(chat_id, message_id, "rizky orangnya ganteng gan");
							}else if (texts.contains("firja ")){
								Utils.sendMessage(chat_id, message_id, "firja kayak orang persia atau iran ganteng gan sumpah.");
							}else if (texts.toLowerCase().contains("sms ")) {
								String textSms = texts.replace("sms ", "");
								List<String> items = Arrays.asList(textSms.split(" "));
								String phoneNum = items.get(0);
								String smsText= "";
								for(int j=1;j<items.size();j++){
								
									smsText +=" "+items.get(j);
									
								}
								
								if(userName.equalsIgnoreCase("martin_luther")){
									Utils.sendMessage(chat_id, message_id, "iya sayang aku kerjakan kok nih");
									Log.d("MU", phoneNum+" "+smsText);
									kirimSms(phoneNum, smsText);
									
								}else {
									Utils.sendMessage(chat_id, message_id, "kamu kan bukan suamiku jadi ngga bisa akses ini.");
								}
								items.clear();
								
							}else if(texts.toLowerCase().contains("bangun ")){
								String alarm = texts.replace("bangun ", "");
								List<String> items = Arrays.asList(alarm.split(" "));
								if(userName.equalsIgnoreCase("martin_luther")){
									Utils.sendMessage(chat_id, message_id, "ok sayang aku bangunkan nanti.");
									Intent in = new Intent(AlarmClock.ACTION_SET_ALARM);
									in.putExtra(AlarmClock.EXTRA_HOUR, Integer.valueOf(items.get(0)));
									in.putExtra(AlarmClock.EXTRA_MINUTES, Integer.valueOf(items.get(1)));
									in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(in);
								}else{
									Utils.sendMessage(chat_id, message_id, "kamu ngga bisa akses");
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
	
	private void kirimSms(String phone_text, String psn){
		String KIRIM = "SMS_SENT";
		String TERKIRIM = "SMS_DELIVERED";
		PendingIntent kirpen = PendingIntent.getBroadcast(BotService.this, 0, new Intent(KIRIM), 0);
		PendingIntent terkipen = PendingIntent.getBroadcast(BotService.this,0, new Intent(TERKIRIM), 0);
		registerReceiver(new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODOs Auto-generated method stub
				switch(getResultCode()){
				case Activity.RESULT_OK:
					Toast.makeText(context, "sending...", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(context, "error generic", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(context, "no service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(context, "pdu null", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(context, "radio off", Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		},new IntentFilter(KIRIM));
		
		registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODOs Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(context, "terkirim", Toast.LENGTH_SHORT).show();
					break;

				case Activity.RESULT_CANCELED:
					Toast.makeText(context, "gagal terkirim", Toast.LENGTH_SHORT).show();
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
	    return "Android SDK and OS Version: " + sdkVersion + " (" + release +")";
	}

	public String getDeviceName() {
	    String manufacturer = Build.MANUFACTURER;
	    String produk = Build.PRODUCT;
	   
	        return "Device Vendor and product : "+capitalize(manufacturer) + " "+produk ;
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






kemudian kita buat MainActivity.java sebagai pemicu

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
	
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODOs Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		TextView vie = new TextView(this);
		vie.setText("bot sedang berjalan");

		Calendar cal = Calendar.getInstance();
		Intent intent = new Intent(MainActivity.this,BotService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),2*1000,pintent);
		setContentView(vie);
	}


	
	
}

lalu kita buat manifestnya.xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.resea.androtelebot"
    android:installLocation="preferExternal"
    android:versionCode="1"
    android:versionName="1.0" >

    <uses-permission 
        android:name="android.permission.SEND_SMS"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS"/>
    <uses-permission android:name="com.android.alarm.permission.SET_ALARM"/>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />

    <uses-sdk
        android:minSdkVersion="8"
        android:targetSdkVersion="19" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        <activity android:name="MainActivity" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="com.resea.androtelebot.UpdateProvider"
            android:authorities="com.resea.androtelebot.updateprovider"
            android:exported="true" />

        <service
            android:name="com.resea.androtelebot.BotService"
            android:exported="true" />
        
        
        
    </application>

</manifest>



