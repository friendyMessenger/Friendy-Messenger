public class UsersDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "usersdb_";

// Contacts table name
private static final String TABLE_USERS = "users";

// Contacts Table Columns names
private static final String KEY_UID = "uid";
private static final String KEY_USERNAME = "username";
private static final String KEY_ACCESS_TIME = "accessTime";
private static final String KEY_TYPE = "type";
private static final String KEY_AGE = "age";
private static final String KEY_GENDER = "gender";
private static final String KEY_SCHOOL = "school";
private static final String KEY_JOB = "job";
private static final String KEY_JOB_TITLE = "jobTitle";
private static final String KEY_ABOUT_ME = "aboutMe";
private static final String KEY_INTERESTS = "interests";
private static final String KEY_HOBBY = "hobby";
private static final String KEY_KEY_GEN_TIME = "keyGenTime";
private static final String KEY_KEY_ID = "keyId";
private static final String KEY_DATA = "keyData";
private static final String KEY_PIC_BYTES = "picBytes";

private static final String TAG = "UsersDb";

private static volatile Map<Long, UsersDb> instanceMap = new HashMap<Long, UsersDb>();
private String dbName = null;

public static UsersDb getInstance(Context ctx, long uid) {
UsersDb instance = null;
synchronized(UsersDb.class) {
instance = instanceMap.get(uid);
if (instance == null) {
instanceMap.put(uid, new UsersDb(ctx.getApplicationContext(), uid));
instance = instanceMap.get(uid);
}
}

return instance;
}

private UsersDb(Context context, long uid) {
super(context, DATABASE_NAME_PREFIX + Long.toString(uid), null, DATABASE_VERSION);
this.dbName = DATABASE_NAME_PREFIX + Long.toString(uid);
}

// Creating Tables
@Override
public void onCreate(SQLiteDatabase db) {
String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
+ KEY_UID + " INTEGER PRIMARY KEY," 
+ KEY_USERNAME + " TEXT," 
+ KEY_ACCESS_TIME + " INTEGER,"
+ KEY_TYPE + " INTEGER,"
+ KEY_AGE + " INTEGER,"
+ KEY_GENDER + " INTEGER,"
+ KEY_SCHOOL + " TEXT,"
+ KEY_JOB + " TEXT,"
+ KEY_JOB_TITLE + " TEXT,"
+ KEY_ABOUT_ME + " TEXT,"
+ KEY_INTERESTS + " TEXT,"
+ KEY_HOBBY + " TEXT,"
+ KEY_KEY_GEN_TIME + " INTEGER,"
+ KEY_KEY_ID + " INTEGER,"
+ KEY_DATA + " TEXT,"
+ KEY_PIC_BYTES + " BLOB"
+ ")";
db.execSQL(createUsersTable);
}

// Upgrading database
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
// Create tables again
onCreate(db);
}

boolean addUser(UserInfo user) { 
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_UID, user.uid);
values.put(KEY_USERNAME, user.username);
values.put(KEY_ACCESS_TIME, user.accessTime);
values.put(KEY_TYPE, user.type);
values.put(KEY_AGE, user.age);
values.put(KEY_GENDER, user.gender);
values.put(KEY_SCHOOL, user.school);
values.put(KEY_JOB, user.job);
values.put(KEY_JOB_TITLE, user.jobTitle);
values.put(KEY_ABOUT_ME, user.aboutMe);
values.put(KEY_INTERESTS, user.interests);
values.put(KEY_HOBBY, user.hobby);
values.put(KEY_PIC_BYTES, user.picBytes);
values.put(KEY_KEY_GEN_TIME, 0);

if (user.keyId != -1 && user.key != null) {
values.put(KEY_KEY_ID, user.keyId);
values.put(KEY_DATA, user.key);
}

if (-1 == db.insert(TABLE_USERS, null, values)) {
SLogger.e(TAG, this.dbName + " addUser uid=" + user.uid + " failed");
success = false;
} else {
SLogger.d(TAG, this.dbName + " addUser uid=" + user.uid + " succedded");
success = true;
}

return success;
}

private UserInfo cursorToUserInfo(Cursor cursor) {
UserInfo user = new UserInfo();

user.uid = cursor.getLong(0);
user.username = cursor.getString(1);
user.accessTime = cursor.getLong(2);
user.type = cursor.getInt(3);
user.age = cursor.getLong(4);
user.gender = cursor.getLong(5);
user.school = cursor.getString(6);
user.job = cursor.getString(7);
user.jobTitle = cursor.getString(8);
user.aboutMe = cursor.getString(9);
user.interests = cursor.getString(10);
user.hobby = cursor.getString(11);
user.keyId = cursor.getLong(12);
user.key = cursor.getString(13);

return user;
}

public UserInfo getUser(long uid) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(TABLE_USERS, 
new String[] { KEY_UID, KEY_USERNAME, KEY_ACCESS_TIME, KEY_TYPE,
KEY_AGE, KEY_GENDER, KEY_SCHOOL, KEY_JOB, KEY_JOB_TITLE, KEY_ABOUT_ME,
KEY_INTERESTS, KEY_HOBBY, KEY_KEY_ID, KEY_DATA}, 
KEY_UID + "=?",
new String[] { String.valueOf(uid) }, null, null, null, null);
if (! cursor.moveToFirst()) {
SLogger.d(TAG, this.dbName + " getUser for uid=" + uid + " failed");
return null;
}

UserInfo user = cursorToUserInfo(cursor);
SLogger.d(TAG, this.dbName + " getUser for uid=" + uid + " succedded, username=" + user.username + " type=" + user.type);

return user;
}

public long getKeyGenTime(long uid) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(TABLE_USERS, 
new String[] { KEY_UID, KEY_KEY_GEN_TIME}, 
KEY_UID + "=?",
new String[] { String.valueOf(uid) }, null, null, null, null);
if (! cursor.moveToFirst()) {
SLogger.e(TAG, "getKeyGenTime for uid=" + uid + " failed");
return -1;
}

long keyGenTime = cursor.getLong(1);

SLogger.d(TAG, "getKeyGenTime for uid=" + uid + " succedded, keyGenTime=" + keyGenTime);
return keyGenTime;
}


public boolean setKeyGenTime(long uid, long keyGenTime) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_KEY_GEN_TIME, keyGenTime);

long nRows = db.update(TABLE_USERS, values, KEY_UID + " =?",
new String[] { String.valueOf(uid)});

if (1 == nRows) {
SLogger.d(TAG, this.dbName + " setKeyGenTime with uid=" + uid + " keyGenTime=" + keyGenTime + " succeded");
success = true;
} else {
SLogger.e(TAG, this.dbName + " setKeyGenTime with uid=" + uid + " failed, nRows=" + nRows);
success = false;
}

return success;
}

public boolean setCurrKey(long uid, long keyId, String data) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_KEY_ID, keyId);
values.put(KEY_DATA, data);

if (1 == db.update(TABLE_USERS, values, KEY_UID + " = ?",
new String[] { String.valueOf(uid)})) {
SLogger.d(TAG, this.dbName + " setKey with uid=" + uid + " keyId=" + keyId + " succeded");
success = true;
} else {
SLogger.e(TAG, this.dbName + " setKey with uid=" + uid + " keyId=" + keyId + " failed");
success = false;
}

return success;
}

public byte[] getUserPic(long uid) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(TABLE_USERS, 
new String[] { KEY_UID, KEY_PIC_BYTES}, 
KEY_UID + "=?",
new String[] { String.valueOf(uid) }, null, null, null, null);
if (! cursor.moveToFirst()) {
SLogger.d(TAG, "getUserPic for uid=" + uid + " failed");
return null;
}

byte[] picBytes = cursor.getBlob(1);
if (picBytes != null)
SLogger.d(TAG, "getUserPic for uid=" + uid + " succedded, picBytes.length=" + picBytes.length);
else
SLogger.d(TAG, "getUserPic for uid=" + uid + " succedded, BUT picBytes=" + null);

return picBytes;
}

public List<Long> getUsersIds(String nameLike, int type) {
SQLiteDatabase db = this.getReadableDatabase();
List<Long> userIds = new ArrayList<Long>();

String selectQuery = "SELECT " + KEY_UID + " FROM " + TABLE_USERS + " WHERE ";
if (nameLike != null && !nameLike.isEmpty())
selectQuery+= KEY_USERNAME + " LIKE " + "'%" + nameLike + "%'" + " AND " + KEY_TYPE + " = " + type;
else
selectQuery+= KEY_TYPE + " = " + type;

Cursor cursor = db.rawQuery(selectQuery, null);
if (cursor.moveToFirst()) {
do {
userIds.add(cursor.getLong(0));
} while (cursor.moveToNext());
}

SLogger.d(TAG, this.dbName + " getUsersIds selectQuery=" + selectQuery +
" found=" + Json.longListToString(userIds));

return userIds;
}

public boolean setUserType(long uid, int type) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_TYPE, type);

if (1 == db.update(TABLE_USERS, values, KEY_UID + " = ?",
new String[] { String.valueOf(uid)})) {
SLogger.d(TAG, this.dbName + " setUserType with uid=" + uid + " type=" + type);
success = true;
} else {
SLogger.e(TAG, this.dbName + " setUserType with uid=" + uid + " failed");
success = false;
}

return success;
}

public boolean updateUser(UserInfo user) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_UID, user.uid);
values.put(KEY_USERNAME, user.username);
values.put(KEY_ACCESS_TIME, user.accessTime);
values.put(KEY_TYPE, user.type);
values.put(KEY_AGE, user.age);
values.put(KEY_GENDER, user.gender);
values.put(KEY_SCHOOL, user.school);
values.put(KEY_JOB, user.job);
values.put(KEY_JOB_TITLE, user.jobTitle);
values.put(KEY_ABOUT_ME, user.aboutMe);
values.put(KEY_HOBBY, user.hobby);
values.put(KEY_INTERESTS, user.interests);
values.put(KEY_PIC_BYTES, user.picBytes);

if (user.keyId != -1 && user.key != null) {
values.put(KEY_KEY_ID, user.keyId);
values.put(KEY_DATA, user.key);
}

if (1 == db.update(TABLE_USERS, values, KEY_UID + " = ?",
new String[] { String.valueOf(user.uid)})) {
SLogger.d(TAG, this.dbName + " updateUser with uid=" + user.uid + " type=" + user.type + " succedded");
success = true;
} else {
SLogger.e(TAG, this.dbName + " updateUser with uid=" + user.uid + " failed");
success = false;
}

return success;
}

public boolean setUserPic(long uid, byte[] picBytes) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_PIC_BYTES, picBytes);

if (1 == db.update(TABLE_USERS, values, KEY_UID + " = ?",
new String[] { String.valueOf(uid)})) {
SLogger.d(TAG, this.dbName + " setUserPic with uid=" + uid + " succedded, picBytes.length=" + picBytes.length);
success = true;
} else {
SLogger.e(TAG, this.dbName + " setUserPic with uid=" + uid + " failed");
success = false;
}

return success;
}

public boolean setUserAccessTime(long uid, long accessTime) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_ACCESS_TIME, accessTime);

if (1 == db.update(TABLE_USERS, values, KEY_UID + " = ?",
new String[] { String.valueOf(uid)})) {
success = true;
} else {
SLogger.e(TAG, this.dbName + " setUserAccessTime with uid=" + uid + " failed");
success = false;
}

return success;
}

public void deleteUser(long uid) {
SLogger.d(TAG, this.dbName + " deleteUser uid=" + uid);
SQLiteDatabase db = this.getWritableDatabase();
db.delete(TABLE_USERS, KEY_UID + " = ?",
new String[] { String.valueOf(uid) });
}

public void deleteSelf(Context context) {
SQLiteDatabase db = this.getWritableDatabase();
db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
context.deleteDatabase(this.dbName);
}


}
 