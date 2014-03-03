public class UserMessagesDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "messagesdb_";

// Contacts table name
private static final String TABLE_MESSAGES = "messages";

// Contacts Table Columns names
private static final String KEY_ID = "_id";
private static final String KEY_DIRECTION = "direction";
private static final String KEY_PEER = "peer";
private static final String KEY_BYTES = "bytes";
private static final String KEY_DATE = "date";
private static final String KEY_FROM = "userFrom";
private static final String KEY_STATE = "state";
private static final String KEY_ENC_KEY_ID = "encKeyId";
private static final String KEY_SIGN_KEY_ID = "signKeyId";
private static final String KEY_MSG_TYPE = "msgType";
private static final String KEY_PLAIN_TEXT = "plainText";
private static final String KEY_INVITE_ID = "inviteId";
private static final String KEY_BYTES_TYPE = "bytesType";
private static final String KEY_STATUS = "status";
private static final String KEY_DISPLAY_NAME = "displayName";
private static final String KEY_MIME_TYPE = "mimeType";
private static final String KEY_FILE_PATH = "filePath";


private static final String TAG = "UserMessagesDb";

private static volatile Map<Long, UserMessagesDb> instanceMap = new HashMap<Long, UserMessagesDb>();
private String dbName = null;

private static final String MSG_CONTENT_PATH_PREFIX = "msg_content_";

public static UserMessagesDb getInstance(Context context, long userId) {
UserMessagesDb instance = null;
synchronized(UserMessagesDb.class) {
instance = instanceMap.get(userId);
if (instance == null) {
instanceMap.put(userId, new UserMessagesDb(context.getApplicationContext(), userId));
instance = instanceMap.get(userId);
}
}

return instance;
}

private UserMessagesDb(Context context, long userId) {
super(context, DATABASE_NAME_PREFIX + Long.toString(userId), null, DATABASE_VERSION);
this.dbName = DATABASE_NAME_PREFIX + Long.toString(userId);
}

// Creating Tables
@Override
public void onCreate(SQLiteDatabase db) {
String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
+ KEY_ID + " INTEGER PRIMARY KEY," 
+ KEY_DIRECTION + " INTEGER," 
+ KEY_PEER + " INTEGER,"
+ KEY_BYTES + " BLOB,"
+ KEY_DATE + " INTEGER," 
+ KEY_FROM + " INTEGER,"
+ KEY_STATE + " INTEGER,"
+ KEY_ENC_KEY_ID + " INTEGER,"
+ KEY_SIGN_KEY_ID + " INTEGER,"
+ KEY_MSG_TYPE + " INTEGER,"
+ KEY_INVITE_ID + " INTEGER,"
+ KEY_BYTES_TYPE + " INTEGER,"
+ KEY_STATUS + " INTEGER,"
+ KEY_DISPLAY_NAME + " TEXT,"
+ KEY_MIME_TYPE + " TEXT,"
+ KEY_FILE_PATH + " TEXT,"
+ KEY_PLAIN_TEXT + " TEXT "
+ ")";
db.execSQL(CREATE_CONTACTS_TABLE);
}

// Upgrading database
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

// Create tables again
onCreate(db);
}

static public boolean isMsgDoc(Message msg) {
switch(msg.bytesType) {
case Message.CONTENT_TYPE_FILE:
case Message.CONTENT_TYPE_PNG:
return true;
default:
return false;
}
}

public boolean addMessage(Message msg) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_ID, msg.id);
values.put(KEY_DIRECTION, msg.direction);
values.put(KEY_PEER, msg.peer);
values.put(KEY_DATE, msg.date);
values.put(KEY_FROM, msg.from);
values.put(KEY_STATE, msg.state);
values.put(KEY_ENC_KEY_ID, msg.encKeyId);
values.put(KEY_SIGN_KEY_ID, msg.signKeyId);
values.put(KEY_MSG_TYPE, msg.msgType);
values.put(KEY_BYTES_TYPE, msg.bytesType);
values.put(KEY_STATUS, msg.status);
values.put(KEY_DISPLAY_NAME, msg.displayName);
values.put(KEY_MIME_TYPE, msg.mimeType);

if (msg.bytesType == Message.CONTENT_TYPE_TEXT) {
try {
values.put(KEY_PLAIN_TEXT, new String(msg.bytes, "UTF-8"));
values.put(KEY_BYTES, msg.bytes);
} catch (UnsupportedEncodingException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}
} else if (isMsgDoc(msg)) {


File docDir = getMsgDocDir(msg.id);
docDir.mkdir();
File docFile = new File(docDir, msg.displayName);
FileOutputStream os = null;
boolean isWriteOk = false;

try {
os = new FileOutputStream(docFile);
os.write(msg.bytes);
os.flush();
isWriteOk = true;
} catch (FileNotFoundException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
} catch (IOException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
} finally {
if (os != null)
try {
os.close();
} catch (IOException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}
}

if (isWriteOk)
values.put(KEY_FILE_PATH, docFile.getAbsolutePath());

}

values.put(KEY_INVITE_ID, msg.inviteId);

if (-1 == db.insert(TABLE_MESSAGES, null, values)) {
SLogger.e(TAG, "added msg id=" + msg.id + " type=" + msg.msgType 
+ " peer=" + msg.peer + " date=" + msg.date + " from=" + msg.from
+ " dir=" + msg.direction
+ " state=" + msg.state);

success = false;
} else {
success = true;
SLogger.i(TAG, "added msg id=" + msg.id + " type=" + msg.msgType 
+ " peer=" + msg.peer + " date=" + msg.date + " from=" + msg.from
+ " dir=" + msg.direction + " state=" + msg.state);
}

return success;
}

private Message cursorToMessage(Cursor cursor) {
Message msg = new Message();
msg.id = Long.parseLong(cursor.getString(0));
msg.direction = Integer.parseInt(cursor.getString(1));
msg.peer = Long.parseLong(cursor.getString(2));
msg.bytes = cursor.getBlob(3);
msg.date = Long.parseLong(cursor.getString(4));
msg.from = Long.parseLong(cursor.getString(5));
msg.state = Integer.parseInt(cursor.getString(6));
msg.encKeyId = Long.parseLong(cursor.getString(7));
msg.signKeyId = Long.parseLong(cursor.getString(8));
msg.msgType = Integer.parseInt(cursor.getString(9));
msg.inviteId = Long.parseLong(cursor.getString(10));
msg.bytesType = Integer.parseInt(cursor.getString(11));
msg.status = Integer.parseInt(cursor.getString(12));
msg.displayName = cursor.getString(13);
msg.mimeType = cursor.getString(14);
msg.filePath = cursor.getString(15);

return msg;
}


public boolean updateMessageState(long id, int state) {
SQLiteDatabase db = this.getWritableDatabase();
boolean success = false;

ContentValues values = new ContentValues();
values.put(KEY_STATE, state);

if (1 == db.update(TABLE_MESSAGES, values, KEY_ID + " = ?",
new String[] { String.valueOf(id)})) {
success = true;
} else {
SLogger.e(TAG, "update message with uid=" + id + " failed");
success = false;
}

return success;
}

public int countMessages(long peer, int msgType, String bodyLike) {
String selectQuery = "SELECT * FROM " + TABLE_MESSAGES 
+ " WHERE " + KEY_MSG_TYPE + "=" + Integer.toString(msgType);

selectQuery+= " AND (" + KEY_PEER + "=" + Long.toString(peer) + " OR " + KEY_FROM + "=" + Long.toString(peer) + " )";

if (bodyLike != null && !bodyLike.isEmpty())
selectQuery+= " AND (" + KEY_PLAIN_TEXT + " LIKE '%" + bodyLike + "%' OR " + KEY_DISPLAY_NAME + " LIKE '%" + bodyLike + "%')";

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

return cursor.getCount();
}

public List<Long> getLastMessagesIds(long peer, int msgType, long limit) {
List<Long> ids = new ArrayList<Long>();
String selectQuery = "SELECT " + KEY_ID + " FROM ("
+ "SELECT " + KEY_ID + " FROM " + TABLE_MESSAGES 
+ " WHERE " + KEY_MSG_TYPE + "=" + Integer.toString(msgType)
+ " AND (" + KEY_PEER + "=" + Long.toString(peer) + " OR " + KEY_FROM + "=" + Long.toString(peer) + " )" 
+ " ORDER BY " + KEY_ID + " DESC LIMIT " + Long.toString(limit)
+ ") sub ORDER BY " + KEY_ID + " ASC";

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
if (!cursor.moveToLast())
return ids;

if (cursor.moveToFirst()) {
do {
// Adding contact to list
ids.add(cursor.getLong(0));
} while (cursor.moveToNext());
}

return ids;
}

public List<Long> getMessagesIdsLimitOffset(long peer, int msgType, String bodyLike, long limit, long offset) {
List<Long> msgIds = new ArrayList<Long>();
String selectQuery = "SELECT " + KEY_ID + " FROM " + TABLE_MESSAGES 
+ " WHERE " + KEY_MSG_TYPE + "=" + Integer.toString(msgType);

selectQuery+= " AND (" + KEY_PEER + "=" + Long.toString(peer) + " OR " + KEY_FROM + "=" + Long.toString(peer) + " )";

if (bodyLike != null && !bodyLike.isEmpty())
selectQuery+= " AND (" + KEY_PLAIN_TEXT + " LIKE '%" + bodyLike + "%' OR " + KEY_DISPLAY_NAME + " LIKE '%" + bodyLike + "%')";

selectQuery+= " ORDER BY " + KEY_ID + " ASC "
+ " LIMIT " + Long.toString(limit) + " OFFSET " + Long.toString(offset);

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
if (!cursor.moveToLast())
return msgIds;

if (cursor.moveToFirst()) {
do {
msgIds.add(cursor.getLong(0));
} while (cursor.moveToNext());
}

return msgIds;
}
public int countMessages(int direction, int msgType, int state, long peer, long from) {
SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.query(TABLE_MESSAGES, 
new String[] {KEY_ID, KEY_DIRECTION, KEY_PEER, KEY_BYTES, 
KEY_DATE, KEY_FROM, KEY_STATE, KEY_ENC_KEY_ID, KEY_SIGN_KEY_ID, KEY_MSG_TYPE, KEY_INVITE_ID, KEY_BYTES_TYPE, KEY_STATUS,
KEY_DISPLAY_NAME, KEY_MIME_TYPE, KEY_FILE_PATH}, 
KEY_DIRECTION + "=?" 
+ " and " + KEY_MSG_TYPE + "=?"
+ " and " + KEY_PEER + "=?" 
+ " and " + KEY_FROM + "=?" 
+ " and " + KEY_STATE + "=?",
new String[] { String.valueOf(direction), String.valueOf(msgType), String.valueOf(peer), String.valueOf(from), String.valueOf(state)}, null, null, null, null);

return cursor.getCount();
}

public void deleteMessages(long peer, long from) {
SQLiteDatabase db = this.getWritableDatabase();
int count = db.delete(TABLE_MESSAGES, 
KEY_PEER + "=?"
+ " and " + KEY_FROM + "=?",
new String[] { String.valueOf(peer), String.valueOf(from) });
SLogger.d(TAG, "delete messages with peer=" + peer+ " from=" + from + " count=" + count);
}

public Message getMessage(long msgId) {
String selectQuery = "SELECT * FROM " + TABLE_MESSAGES 
+ " WHERE " + KEY_ID + "=" + msgId;

SQLiteDatabase db = this.getReadableDatabase();
Cursor cursor = db.rawQuery(selectQuery, null);

if (!cursor.moveToFirst())
return null;

Message msg = cursorToMessage(cursor);

return msg;
}

private File getMsgDocDir(long msgId) {
return new File(CApp.getInstance().getPrivateFilesDir(), MSG_CONTENT_PATH_PREFIX + msgId);
}

public void deleteMessage(long msgId) {

Message msg = getMessage(msgId);
if (msg != null && isMsgDoc(msg)) {
File docDir = getMsgDocDir(msg.id);
if (docDir.exists())
FileOps.deleteFileRecursive(docDir);
}

SQLiteDatabase db = this.getWritableDatabase();
int count = db.delete(TABLE_MESSAGES, 
KEY_ID + "=?",
new String[] { String.valueOf(msgId)});

SLogger.d(TAG, "delete message with id=" + msgId + " count=" + count);
}

public void deleteSelf(Context context) {
SQLiteDatabase db = this.getWritableDatabase();
db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
context.deleteDatabase(this.dbName);
}
}