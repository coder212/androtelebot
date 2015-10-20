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
	public static final String SEND_LOCATION = "sendLocation";
	public static final String SEND_CHAT_ACTION = "sendChatAction";
	public static final String SEND_FORWARD = "forwardMessage";
	private static HttpPost httppost;
	private static HttpClient httpclient;
	private static List<NameValuePair> nameValuePairs ;
	
	public static String sendHelp(int from_id, String message) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(from_id)));
		nameValuePairs.add(new BasicNameValuePair("text", message));
		return sendData(TOKEN+"/"+SEND_MESSAGE, nameValuePairs);
	}
	
	public static String forwardMessage(int chat_id, int from_chat_id, int message_id) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(chat_id)));
		nameValuePairs.add(new BasicNameValuePair("from_chat_id", Integer.toString(from_chat_id)));
		nameValuePairs.add(new BasicNameValuePair("message_id", Integer.toString(message_id)));
		return sendData(TOKEN+"/"+SEND_FORWARD, nameValuePairs);
	}
	
	public static String sendChatAction(int chat_id, String action) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(chat_id)));
		nameValuePairs.add(new BasicNameValuePair("action", action));
		return sendData(TOKEN+"/"+SEND_CHAT_ACTION, nameValuePairs);
	}
	
	public static String sendLocation(int chat_id, int message_id, float lat, float lon) throws ClientProtocolException, IOException{
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("chat_id", Integer.toString(chat_id)));
		nameValuePairs.add(new BasicNameValuePair("latitude",Float.toString(lat) ));
		nameValuePairs.add(new BasicNameValuePair("longitude", Float.toString(lon)));
		nameValuePairs.add(new BasicNameValuePair("reply_to_message_id", Integer.toString(message_id)));
		return sendData(TOKEN+"/"+SEND_LOCATION, nameValuePairs);
	}
	
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
