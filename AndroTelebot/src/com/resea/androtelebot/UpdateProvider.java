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

