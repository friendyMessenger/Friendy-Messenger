public class UserKeyDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "UserKeyDb_";

// Contacts table name
private static final String TABLE_NAME = "PUBLIC_KEYS";

// Contacts Table Columns names
private static final String KEY_UID = "uid";
private static final String KEY_KEY_ID = "keyId";
private static final String KEY_DATA = "data";

private static final String TAG = "PubKeysDb";
private static volatile PubKeysDb instance = null;

public static PubKeysDb getInstance(Context ctx) {
synchronized(PubKeysDb.class) {
if (instance == null) {
instance = new PubKeysDb(ctx.getApplicationContext());
}
}
return instance;
}

private PubKeysDb(Context context) {
super(context, DATABASE_NAME_PREFIX, null, DATABASE_VERSION);
}

// Creating Tables
@Override
public void onCreate(SQLiteDatabase db) {
String cmd = "CREATE TABLE " + TABLE_NAME + "("
+ KEY_UID + " INTEGER NOT NULL,"
+ KEY_KEY_ID + " INTEGER NOT NULL,"
+ KEY_DATA + " TEXT NOT NULL,"
+ "PRIMARY KEY ( " + KEY_UID + " , " + KEY_KEY_ID + " ) "
+ ")";

db.execSQL(cmd);

}

// Upgrading database
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

// Create tables again
onCreate(db);
}

public boolean saveKey(long uid, long keyId, String data) { 
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();

values.put(KEY_UID, uid);
values.put(KEY_KEY_ID, keyId);
values.put(KEY_DATA, data);

if (-1 == db.insert(TABLE_NAME, null, values)) {
SLogger.d(TAG, "saveKey with uid=" + uid + " keyId=" + keyId + " FAILURE");
} else {
SLogger.d(TAG, "saveKey with uid=" + uid + " keyId=" + keyId + " SUCCESS");
success = true;
}
return success;
}

public String getKey(long uid, long keyId) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(TABLE_NAME, 
new String[] {KEY_DATA}, 
KEY_UID + "=?"
+ " and " + KEY_KEY_ID + "=?",
new String[] { String.valueOf(uid), String.valueOf(keyId)}, null, null, null, null);

if (!cursor.moveToFirst()) {
SLogger.d(TAG, "getKey for uid=" + uid + " keyId=" + keyId + " not found");
return null;
}

return cursor.getString(0);
}

public void deleteKey(long uid, long keyId) { 
SQLiteDatabase db = this.getWritableDatabase();

int count = db.delete(TABLE_NAME, 
KEY_UID + "=?" +
" AND " + KEY_KEY_ID,
new String[] { String.valueOf(uid), String.valueOf(keyId)});

SLogger.d(TAG, "deleteKey with uid=" + uid + " keyId=" + keyId + " count=" + count);
}

public List<Long> getKeys(long uid) {
List<Long> ids = new ArrayList<Long>();
String selectQuery = "SELECT * FROM " + TABLE_NAME
+ " WHERE " + KEY_UID + "=" + uid;

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
if (!cursor.moveToLast())
return ids;

if (cursor.moveToFirst()) {
do { 
// Adding contact to list
ids.add(cursor.getLong(1));
} while (cursor.moveToNext());
}

return ids;
}
}