package com.resea.androtelebot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseBot extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "botgajebo.db";
	public static final int DATABASE_VERSION = 1;
	
	public final String ID = "_id";
	public final String KATA_ROW = "Kata_Row";
	public final String KATA_TABLE = "Kata_Random";
	
	private final String CREATE_KATA_TABLE = "create table "+KATA_TABLE+"("+ID+" integer primary key,"+KATA_ROW+" text not null);";
	private final String UPDATE_TABLE_KATA = "drop table if exists "+KATA_TABLE;
	
	

	public DatabaseBot(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODOs Auto-generated constructor stub
	}



	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODOs Auto-generated method stub
		db.execSQL(CREATE_KATA_TABLE);
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {
		// TODOs Auto-generated method stub
		db.execSQL(UPDATE_TABLE_KATA);
		onCreate(db);
	}

}
