public class UserKeyDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "userkeydbfriendy_";

// Contacts table name
private static final String TABLE_NAME = "userkeys";

// Contacts Table Columns names
private static final String KEY_ID = "_id";
private static final String KEY_PUBLIC = "public";
private static final String KEY_PRIVATE = "private";
private static final String KEY_KEY_ID = "keyid";

private static final String TAG = "UserKeysDb";
private static volatile Map<Long, UserKeysDb> instanceMap = new HashMap<Long, UserKeysDb>();
private String dbName = null;

public static UserKeysDb getInstance(Context ctx, long userId) {
UserKeysDb instance = null;
synchronized(UserKeysDb.class) {
instance = instanceMap.get(userId);
if (instance == null) {
instanceMap.put(userId, new UserKeysDb(ctx.getApplicationContext(), userId));
instance = instanceMap.get(userId);
}
}

return instance;
}

private UserKeysDb(Context context, long userId) {
super(context, DATABASE_NAME_PREFIX + Long.toString(userId), null, DATABASE_VERSION);
this.dbName = DATABASE_NAME_PREFIX + Long.toString(userId);
}

// Creating Tables
@Override
public void onCreate(SQLiteDatabase db) {
String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
+ KEY_PUBLIC + " TEXT," 
+ KEY_PRIVATE + " TEXT,"
+ KEY_KEY_ID + " INTEGER"
+ ")";
db.execSQL(CREATE_CONTACTS_TABLE);
}

// Upgrading database
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

// Create tables again
onCreate(db);
}

boolean addKeyPair(UserKeyPair kp) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_PUBLIC, kp.getPublicKey());
values.put(KEY_PRIVATE, kp.getPrivateKey());
values.put(KEY_KEY_ID, kp.getKeyId());

if (-1 == db.insert(TABLE_NAME, null, values)) {
success = false;
} else {
success = true;
}

growTable();

return success;
}

private UserKeyPair cursorToKeyPair(Cursor cursor) {

return new UserKeyPair(cursor.getString(1), cursor.getString(2), cursor.getLong(3));
}

public UserKeyPair getKeyPair(long id) {
SQLiteDatabase db = this.getReadableDatabase();
UserKeyPair kp = null;

Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_ID, KEY_PUBLIC, KEY_PRIVATE, KEY_KEY_ID}, 
KEY_ID + "=?",
new String[] { String.valueOf(id) }, null, null, null, null);

if (!cursor.moveToFirst())
return null;

kp = cursorToKeyPair(cursor);
return kp;
}

public UserKeyPair getKeyPairByKeyId(String KeyId) {
SQLiteDatabase db = this.getReadableDatabase();
UserKeyPair kp = null;

Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_ID, KEY_PUBLIC, KEY_PRIVATE, KEY_KEY_ID}, 
KEY_KEY_ID + "=?",
new String[] { KeyId }, null, null, null, null);

if (!cursor.moveToFirst())
return null;

kp = cursorToKeyPair(cursor);
return kp;
}

private void growTable() {
String selectQuery = "SELECT * FROM " + TABLE_NAME;

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);
int numKeys = cursor.getCount();

if (numKeys > AppSettings.MAX_KEYS) {
List<Long> ids = getFirstKeyIds(numKeys - AppSettings.MAX_KEYS);
deleteKeysByIds(ids);
}
}

private int deleteKeysByIds(List<Long> ids)
{

SQLiteDatabase db = this.getWritableDatabase();

int sum = 0;
for (Long id : ids) {
int count = db.delete(TABLE_NAME, 
KEY_ID + "=?",
new String[] { String.valueOf(id)});
if (count > 0) 
SLogger.d(TAG, "KeyPair with id=" + id + " deleted");
sum+= count;
}

return sum;
}

private List<Long> getFirstKeyIds(long limit) {
List<Long> ids = new ArrayList<Long>();

String selectQuery = "SELECT * FROM ("
+ "SELECT * FROM " + TABLE_NAME 
+ " ORDER BY " + KEY_ID + " ASC LIMIT " + Long.toString(limit)
+ ") sub ORDER BY " + KEY_ID + " ASC";

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
if (!cursor.moveToLast())
return ids;

if (cursor.moveToFirst()) {
do {
long id = cursor.getLong(0);
SLogger.d(TAG, "select keyPair with id=" + id);
ids.add(id);
} while (cursor.moveToNext());
}

return ids;
}

public UserKeyPair getLastKeyPair() {
UserKeyPair kp = null;
String selectQuery = "SELECT * FROM " + TABLE_NAME;

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
if (!cursor.moveToLast())
return null;

kp = cursorToKeyPair(cursor);
return kp;
}

public void deleteSelf(Context context) {
SQLiteDatabase db = this.getWritableDatabase();
db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
context.deleteDatabase(this.dbName);
}
}