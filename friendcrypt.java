package app.friendy.messenger
public class FriendyDb extends SQLiteOpenHelper {

// All Static variables
// Database Version
private static final int DATABASE_VERSION = 1;

// Database Name
private static final String DATABASE_NAME_PREFIX = "friendydb_";

// Contacts table name
private static final String TABLE_NAME = "messages";

// Contacts Table Columns names
private static final String KEY_ID = "_id";
private static final String KEY_BYTES = "bytes";
private static final String KEY_ENC_KEY_ID = "encKeyId";
XXXXXX