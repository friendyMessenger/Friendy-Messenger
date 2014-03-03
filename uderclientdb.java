public class UserClientDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "UserClientDb_";

// Contacts table name
private static final String CLIENTS_TABLE_NAME = "UserClient";

private static final String CURR_CLIENT_TABLE_NAME = "CurrUserClient";

// Contacts Table Columns names
private static final String KEY_ID = "_id";
private static final String KEY_REMOTE_ID = "remoteId";
private static final String KEY_PUBLIC_KEY = "publicKey";
private static final String KEY_PRIVATE_KEY = "privateKey";
private static final String KEY_KEY_ID = "keyId";
private static final String KEY_SESSION_KEY = "sessionKey";
private static final String KEY_CURR_CLIENT = "currClientId";

private static final String TAG = "UserClientKsDb";
private static volatile UserClientKsDb instance = null;

public static UserClientKsDb getInstance(Context ctx) {
synchronized(UserClientKsDb.class) {
if (instance == null) {
instance = new UserClientKsDb(ctx.getApplicationContext());
}
}
return instance;
}

private UserClientKsDb(Context context) {
super(context, DATABASE_NAME_PREFIX, null, DATABASE_VERSION);
}

// Creating Tables
@Override
public void onCreate(SQLiteDatabase db) {
String cmd = "CREATE TABLE " + CLIENTS_TABLE_NAME + "("
+ KEY_ID + " INTEGER PRIMARY KEY," 
+ KEY_REMOTE_ID + " INTEGER,"
+ KEY_PUBLIC_KEY + " TEXT,"
+ KEY_PRIVATE_KEY + " TEXT,"
+ KEY_SESSION_KEY + " TEXT,"
+ KEY_KEY_ID + " INTEGER"
+ ")";
db.execSQL(cmd);

cmd = "CREATE TABLE " + CURR_CLIENT_TABLE_NAME + "("
+ KEY_ID + " INTEGER PRIMARY KEY," 
+ KEY_CURR_CLIENT + " INTEGER"
+ ")";
db.execSQL(cmd);
}

// Upgrading database
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
db.execSQL("DROP TABLE IF EXISTS " + CLIENTS_TABLE_NAME);
db.execSQL("DROP TABLE IF EXISTS " + CURR_CLIENT_TABLE_NAME);

// Create tables again
onCreate(db);
}

public long createClient() {
long clientId = MessageCrypt.getRndLong();
UserKeyPair kp = new UserKeyPair();
kp.generate();

SQLiteDatabase db = this.getWritableDatabase();

ContentValues values = new ContentValues();

values.put(KEY_ID, clientId);
values.put(KEY_REMOTE_ID, -1);
values.put(KEY_PUBLIC_KEY, kp.getPublicKey());
values.put(KEY_PRIVATE_KEY, kp.getPrivateKey());
values.put(KEY_KEY_ID, kp.getKeyId());
values.put(KEY_SESSION_KEY, new String(""));

if (-1 == db.insert(CLIENTS_TABLE_NAME, null, values)) {
SLogger.d(TAG, "createClient with id=" + clientId + " FAILURE");
clientId = -1;
} else {
SLogger.d(TAG, "createClient with id=" + clientId + " SUCCESS");
}

return clientId;
}

public UserClientKey getClient(long clientId) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(CLIENTS_TABLE_NAME, new String[] {
KEY_ID, 
KEY_REMOTE_ID, 
KEY_PUBLIC_KEY, 
KEY_PRIVATE_KEY,
KEY_KEY_ID,
KEY_SESSION_KEY}, 
KEY_ID + "=?",
new String[] { String.valueOf(clientId) }, null, null, null, null);

if (!cursor.moveToFirst()) {
SLogger.d(TAG, "client with clientId=" + clientId + " NOT FOUND");
return null;
}

UserClientKey uck = new UserClientKey();

uck.id = cursor.getLong(0);
uck.remoteId = cursor.getLong(1);
uck.publicKey = cursor.getString(2);
uck.privateKey = cursor.getString(3);
uck.keyId = cursor.getLong(4);
uck.sessionKey = cursor.getString(5);

SLogger.d(TAG, "getClient with id=" + clientId + " FOUND");

return uck;
}

public UserClientKey getClientByRemoteId(long remoteId) {
SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(CLIENTS_TABLE_NAME, new String[] {
KEY_ID, 
KEY_REMOTE_ID, 
KEY_PUBLIC_KEY, 
KEY_PRIVATE_KEY,
KEY_KEY_ID,
KEY_SESSION_KEY}, 
KEY_REMOTE_ID + "=?",
new String[] { String.valueOf(remoteId) }, null, null, null, null);

if (!cursor.moveToFirst()) {
SLogger.d(TAG, "client with remoteId=" + remoteId + " NOT FOUND");
return null;
}

UserClientKey uck = new UserClientKey();

uck.id = cursor.getLong(0);
uck.remoteId = cursor.getLong(1);
uck.publicKey = cursor.getString(2);
uck.privateKey = cursor.getString(3);
uck.keyId = cursor.getLong(4);
uck.sessionKey = cursor.getString(5);

SLogger.d(TAG, "getClient with remoteId=" + remoteId + " FOUND");

return uck;
}

public boolean setClientRemoteId(long clientId, long remoteId) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_REMOTE_ID, remoteId);

if (1 == db.update(CLIENTS_TABLE_NAME, values, KEY_ID + " = ?",
new String[] { String.valueOf(clientId)})) {
SLogger.d(TAG, "setClientRemoteId id=" + clientId + " remoteId=" + remoteId);
success = true;
} else {
SLogger.e(TAG, "setClientRemoteId FAILED with id=" + clientId);
success = false;
}

return success;
}

public boolean setClientSessionKey(long clientId, String sessionKey) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_SESSION_KEY, sessionKey);

if (1 == db.update(CLIENTS_TABLE_NAME, values, KEY_ID + " = ?",
new String[] { String.valueOf(clientId)})) {
SLogger.d(TAG, "setClientSessionKey with id=" + clientId + " sessionKey=" + sessionKey);
success = true;
} else {
SLogger.e(TAG, "setClientSessionKey FAILED with id=" + clientId);
success = false;
}

return success;
}

public void deleteClient(long clientId) {
SQLiteDatabase db = this.getWritableDatabase();
int count = db.delete(CLIENTS_TABLE_NAME, 
KEY_ID + "=?",
new String[] { String.valueOf(clientId)});
SLogger.d(TAG, "delete client with clientId=" + clientId+ " count=" + count);
}

public long getCurrentClient() {
// TODO Auto-generated method stub

SQLiteDatabase db = this.getReadableDatabase();

Cursor cursor = db.query(CURR_CLIENT_TABLE_NAME, new String[] {
KEY_CURR_CLIENT}, 
KEY_ID + "=?",
new String[] { String.valueOf(0) }, null, null, null, null);

if (!cursor.moveToFirst()) {
SLogger.e(TAG, "getCurrentClient FAILED");
return -1;
}

return cursor.getLong(0);
}

public boolean setCurrentClient(long clientId) {

// TODO Auto-generated method stub
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_CURR_CLIENT, clientId);

try {
if (1 == db.update(CURR_CLIENT_TABLE_NAME, values, KEY_ID + " = ?",
new String[] { String.valueOf(0)})) {
SLogger.d(TAG, "Curr client set to " + clientId);
success = true;
} 
} catch (Exception e) {
SLogger.exception(TAG, e);
}

if (!success) {
values = new ContentValues();
values.put(KEY_ID, 0);
values.put(KEY_CURR_CLIENT, clientId);

try {
if (-1 == db.insert(CURR_CLIENT_TABLE_NAME, null, values)) {
SLogger.e(TAG, "createCurrentClient failed");
} else {
SLogger.d(TAG, "createCurrentClient succed");
success = true;
} 
} catch (Exception e) {
SLogger.exception(TAG, e);
}
}

return success; 
}
}