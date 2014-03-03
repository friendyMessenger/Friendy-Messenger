package app.friendy.messenger

class MessengerFriendyUS implements MessengerFriendyUS {

private static final String TAG = "MessengerFriendyUS";
private MessengerUser user = null;

public MessengerFriendyUS(MessengerUser user) {
this.user = user;
}

@Override
public KeyQuery getUserCurrentPubKey(long uid) {
// TODO Auto-generated method stub

UserInfo uinfo = user.getUser(uid);
if (uinfo.keyId != -1 && uinfo.key != null) {
KeyQuery kq = new KeyQuery();
kq.error = Errors.SUCCESS;
kq.publicKey = uinfo.key;
kq.keyId = uinfo.keyId;
return kq;
}

KeyQuery kq = Server.pubKeyQueryUserCurrentKey(uid); 
if (kq != null && kq.error == Errors.SUCCESS && kq.publicKey != null) {
user.saveKey(uid, kq.keyId, kq.publicKey);
user.setCurrKey(uid, kq.keyId, kq.publicKey);
}

return kq;
}

@Override
public KeyQuery getPubKeyById(long uid, long keyId) {
// TODO Auto-generated method stub
String keyData = user.getKey(uid, keyId);
if (keyData != null) {
KeyQuery kq = new KeyQuery();
kq.error = Errors.SUCCESS;
kq.publicKey = keyData;
kq.keyId = keyId;

return kq;
}

KeyQuery kq = Server.pubKeyQueryByKeyId(uid, keyId);
if (kq != null && kq.error == Errors.SUCCESS && kq.publicKey != null) {
user.saveKey(uid, kq.keyId, kq.publicKey);
}

return kq;
}
}

class EncryptMessageAndSend implements WaitableCompletionTask {
private static final String TAG = "EncryptMessageAndSend";
private UserInfo contact = null;
private String error = null;
private MessengerUser user = null;
private TaskCompleteCallback clb = null;
private Message msg = null;

public String id = UUID.randomUUID().toString();

EncryptMessageAndSend(MessengerUser user, UserInfo contact, Message msg, TaskCompleteCallback clb) {
this.contact = contact;
this.user = user;
this.msg = msg;
this.clb = clb;
}

@Override
public void waitForComplete() {
// TODO Auto-generated method stub

}
@Override
public void run() {
// TODO Auto-generated method stub
RealClock clock = new RealClock();
clock.start();

UserKeyPair kp = user.getKeyPair();
if (kp == null) {
SLogger.e(TAG, "user key pair not ready");
if (clb != null)
clb.onTaskComplete(Errors.UNSUCCESSFUL, "user key pair not ready", id);
return;
}
SLogger.i(TAG, "EncryptMessageAndSend:user.getKeyPair time=" + clock.elapsedTime());

clock.start();

Message msg = this.msg;
msg.from = user.getUid();
msg.peer = contact.uid;
msg.date = System.currentTimeMillis();

MessageCryptResult result = MessageCrypt.encryptMessage(kp.getPrivateKey(), kp.getKeyId(),
msg, user.getCryptResolver(), new RealClock());

SLogger.i(TAG, "EncryptMessageAndSend:encryptMessage time=" + clock.elapsedTime());

if (result.error != Errors.SUCCESS) {
SLogger.e(TAG, "invalid message encryption with err=" + result.error);
error = new String("invalid message encryption");
if (clb != null)
clb.onTaskComplete(result.error, "invalid msg encryption", id);
return;
}

SLogger.d(TAG, "EncryptMessageAndSend:bytes.length=" + result.encrypted.length);

clock.start();
long msgId = Server.sendMessage(contact.uid, result.encrypted, result.encKeyId); 
SLogger.i(TAG, "EncryptMessageAndSend:sendMessage time=" + clock.elapsedTime());

if (msgId <= 0) {
SLogger.e(TAG, "invalid msg sending");
error = new String("invalid msg sending");
if (clb != null)
clb.onTaskComplete(Errors.UNSUCCESSFUL, "invalid msg sending", id);
return;
}

SLogger.d(TAG, "EncryptMessageAndSend:msgid=" + msgId + " assigned");
msg.id = msgId;
user.onPostSendMessage(msg);
if (clb != null)
clb.onTaskComplete(Errors.SUCCESS, "success", id);
}

@Override
public void onComplete() {
// TODO Auto-generated method stub

}
}

class DecryptMessagesTask implements Runnable {
private static final String TAG = "DecryptMessagesTask";
private MessengerUser user = null;

DecryptMessagesTask(MessengerUser user) {
this.user = user;
}

@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "DecryptMessagesTask running");
user.decryptMessages(5);
SLogger.d(TAG, "DecryptMessagesTask completed");
}
}

class QueryUserContacts implements Runnable {
private static final String TAG = "QueryUserContacts";
private MessengerUser user = null;

QueryUserContacts(MessengerUser user) {
this.user = user;
}

@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "queryUserContacts running");
user.updateFriends();
user.updateInvites();
SLogger.d(TAG, "queryUserContacts completed");
}
}

class QueryMessagesTask implements Runnable {
private static final String TAG = "QueryMessagesTask";
private MessengerUser user = null;

QueryMessagesTask(MessengerUser user) {
this.user = user;
}

@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "QueryMessagesTask running");
user.queryNewMessages();
SLogger.d(TAG, "QueryMessagesTask completed");
}
}
class QueryProfileTask implements Runnable {
private static final String TAG = "QueryProfileTask";
private MessengerUser user = null;

QueryProfileTask(MessengerUser user) {
this.user = user;
}

@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "QueryProfileTask running");
user.queryProfile();
SLogger.d(TAG, "QueryProfileTask completed");
}
}

class UpdateKeyPairTask implements Runnable {
private static final String TAG = "UpdateKeyPairTask";
private MessengerUser user = null;

public UpdateKeyPairTask(MessengerUser user) {
this.user = user;
}

@Override
public void run() {
// TODO Auto-generated method stub
user.updateKeyPair();
}
}

class RegenKeyTask implements Runnable {
private static final String TAG = "RegenKeyTask";
private MessengerUser user = null;

RegenKeyTask(MessengerUser user) {
this.user = user;
}
@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "RegenKeyTask running");

long time = user.getKeyGenTime();
boolean regenKey = false;

if (time <= 0)
regenKey = true;
else {
long currTime = System.currentTimeMillis();
if (time >= currTime)
regenKey = true;
else if ((currTime - time) >= 3600*1000)
regenKey = true;
}

if (regenKey) {
user.regenKey();
user.setKeyGenTime(System.currentTimeMillis());
}

SLogger.d(TAG, "RegenKeyTask completed");
}
}

class UpdateProfileTask implements Runnable {
private static final String TAG = "UpdateProfileTask";
private MessengerUser user = null;
private UserInfo uinfo = null;

public UpdateProfileTask(MessengerUser user, UserInfo uinfo) {
this.user = user;
this.uinfo = uinfo;
SLogger.d(TAG, "UpdateProfileTask constructor");
}

@Override
public void run() {
// TODO Auto-generated method stub
SLogger.d(TAG, "UpdateProfileTask running");
user.updateProfile(uinfo);
SLogger.d(TAG, "UpdateProfileTask completed");
}
}

public class MessengerUser {
private static final String TAG = "MessengerUser";
private String username = null;
private volatile UserKeyPair kp = null;
private long uid = -1;
private UsersDb usersDb = null;
private UserMessagesDb messagesDb = null;
private UserKeysDb userKeysDb = null;
private CryptMessagesDb cryptMessagesDb = null;
private UserFileDb fileDb = null;
private NoticesDb noticesDb = null;
private PubKeysDb pubKeysDb = null;
private CryptResolver cryptResolver = null;
private ScheduledExecutorService exec = null;
private boolean timerStopping = false;

public long getUid() {
return this.uid;
}

public String getUsername() {
return this.username;
}

public MessengerUser(long uid, String username) {
this.uid = uid;
this.username = username;
Context context = CApp.getInstance().getApplication().getApplicationContext();

usersDb = UsersDb.getInstance(context,
this.uid);

noticesDb = NoticesDb.getInstance(context, this.uid);

messagesDb = UserMessagesDb.getInstance(context,
this.uid);

userKeysDb = UserKeysDb.getInstance(context,
this.uid);


cryptMessagesDb = CryptMessagesDb.getInstance(context, this.uid);

fileDb = UserFileDb.getInstance(context,
this.uid, CApp.getInstance().getPrivateFilesDir());


pubKeysDb = PubKeysDb.getInstance(context);

cryptResolver = new CryptResolver(this);
}


public CryptResolver getCryptResolver() {
return cryptResolver;
}

public boolean saveKey(long uid, long keyId, String data) {
return pubKeysDb.saveKey(uid, keyId, data);
}

public String getKey(long uid, long keyId) {
return pubKeysDb.getKey(uid, keyId);
}

public void pause() {
stopTimers();
}

public boolean resume() {

exec = Executors.newScheduledThreadPool(4);

exec.schedule(new UpdateKeyPairTask(this), 0, TimeUnit.MILLISECONDS);
exec.schedule(new QueryProfileTask(this), 0, TimeUnit.MILLISECONDS);
exec.schedule(new QueryUserContacts(this), 0, TimeUnit.MILLISECONDS);
exec.schedule(new RegenKeyTask(this), 0, TimeUnit.MILLISECONDS);
exec.schedule(new QueryMessagesTask(this), 0, TimeUnit.MILLISECONDS);
exec.schedule(new DecryptMessagesTask(this), 0, TimeUnit.MILLISECONDS); 

startTimers();
return true;
}

private void stopTimers() {
synchronized(this) {
timerStopping = true;
}

if (exec != null) {
exec.shutdown();
try {
exec.awaitTermination(1000, TimeUnit.MILLISECONDS);
} catch (InterruptedException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}
exec = null;
}
}

private void startTimers() { 
synchronized(this) {
timerStopping = false;
}
exec.scheduleAtFixedRate(new QueryMessagesTask(this), 200, 200, TimeUnit.MILLISECONDS);
exec.scheduleAtFixedRate(new DecryptMessagesTask(this), 500, 2000, TimeUnit.MILLISECONDS);
exec.scheduleAtFixedRate(new RegenKeyTask(this), 5000, 5000, TimeUnit.MILLISECONDS);
exec.scheduleAtFixedRate(new QueryUserContacts(this), 500, 2000, TimeUnit.MILLISECONDS);
}

public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
ScheduledFuture<?> future = null;

if (timerStopping)
return null;

synchronized(this) {
if (!timerStopping) {
future = exec.scheduleAtFixedRate(command, initialDelay, period, unit);
}
}

return future;
}

public Future<?> schedule(Runnable command, long delay, TimeUnit unit) {
Future<?> future = null;

if (timerStopping)
return null;

synchronized(this) {
if (!timerStopping) {
future = exec.schedule(command, delay, unit);
}
}

return future;
}

public boolean regenKey() {
SLogger.d(TAG, "regenKey for uid=" + this.getUid());

boolean success = false;
synchronized(this) {
UserKeyPair kp = new UserKeyPair();
kp.generate();
success = userKeysDb.addKeyPair(kp);
if (success) {
success = updateKeyPairInternal();
}
}

return success;
}


public UserKeyPair getKeyPairByKeyId(String keyId) { 
UserKeyPair kp = null;

synchronized(this) {
kp = userKeysDb.getKeyPairByKeyId(keyId);
}

return kp;
}

public boolean updateKeyPair() {
boolean result = false;
synchronized(this) {
result = updateKeyPairInternal();
}
return result;
}

private boolean updateKeyPairInternal() {
boolean success = false;
kp = userKeysDb.getLastKeyPair();
if (kp == null) {
UserKeyPair kp = new UserKeyPair();
kp.generate();
userKeysDb.addKeyPair(kp);
this.kp = userKeysDb.getLastKeyPair();
if (this.kp == null) {
SLogger.e(TAG, "keys not added");
success = false;
}
}

if (kp != null) {
if (CApp.getInstance().debug)
SLogger.d(TAG, "user=" + uid + " setPublicKey=" + kp.getPublicKey()
+ " privateKey=" + kp.getPrivateKey());
int error = Server.pubKeyRegister(kp);
if (error == Errors.OBJECT_ALREADY_EXISTS) {
SLogger.i(TAG, "public key already registred");
success = true;
} else if (error == Errors.SUCCESS) {
SLogger.i(TAG, "public key registred successfuly");
success = true;
} else {
success = false; 
}
}

return success;
}

private void updateFriendsDb(Map<Long, UserInfo> foundFriends) {
List<Long> usersIds = usersDb.getUsersIds(null, UserInfo.TYPE_FRIEND);
for (Long uid : usersIds) {
if (foundFriends == null || !foundFriends.containsKey(uid))//such uid now not a friend
usersDb.setUserType(uid, UserInfo.TYPE_OTHER);
}
}

public void updateInvites()
{
Map<Long, Long> invitesMap = Server.friendInvites();
if (invitesMap == null) {
return;
}

for (Long inviteId : invitesMap.keySet()) { 
if (null == getInvite(inviteId)) {
Notice notice = new Notice();
notice.type = Notice.TYPE_INVITE;
notice.inviteId = inviteId;
notice.uid = invitesMap.get(inviteId);
addNotice(notice);
}
}

List<Long> ids = getNoticesIds(Notice.TYPE_INVITE, -1);
for (Long id : ids) {
Notice notice = getNotice(id);
if (notice == null)
continue;

if (!invitesMap.containsKey(notice.inviteId))
deleteNotice(id);
}
}

public long getKeyGenTime() {
return usersDb.getKeyGenTime(this.getUid());
}

public void setKeyGenTime(long time) {
usersDb.setKeyGenTime(this.getUid(), time);
}

public void updateFriends()
{
Map<Long, UserInfo> friendsMap = null;

List<Long> friends = Server.friendsQuery();
if (friends == null || friends.size() == 0) {
SLogger.d(TAG, "no friends found for user=" + this.uid);
} else {
friendsMap = Server.queryUsers(friends);
if (friendsMap != null) {
for (Long uid : friendsMap.keySet()) {
UserInfo uinfo = friendsMap.get(uid);
this.syncUser(uinfo);
}
}
}

updateFriendsDb(friendsMap);
}

public void updateOthers() { 
List<Long> usersIds = usersDb.getUsersIds(null, UserInfo.TYPE_OTHER);
if (usersIds == null || usersIds.size() == 0)
return;

Map<Long, UserInfo> infoMap = Server.queryUsers(usersIds);
for (Long uid : infoMap.keySet()) {
UserInfo user = infoMap.get(uid);
this.syncUser(user);
}
}

public Bitmap getUserPic(long uid) {
byte[] picBytes = usersDb.getUserPic(uid);
if (picBytes == null)
return null;
return BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
}

public void onProfileChanged(UserInfo uinfo) {
if (CApp.getInstance().debug)
SLogger.d(TAG, "onProfileChanged uinfo=" + Json.mapToString(uinfo.toMap()));
schedule(new UpdateProfileTask(this, uinfo), 0, TimeUnit.MILLISECONDS);
}

public void setUserPicByBitmap(long uid, Bitmap bitmap) {

bitmap = CApp.getInstance().scaleBitmapByWidth(bitmap, AppSettings.MSG_PIC_WIDTH_SMALL);
ByteArrayOutputStream os = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);

UserInfo uinfo = new UserInfo();
uinfo.picBytes = os.toByteArray();

usersDb.setUserPic(uid, uinfo.picBytes);
this.onProfileChanged(uinfo);
}

public void setUserPicBytes(long uid, byte[] picBytes) {
UserInfo uinfo = new UserInfo();
uinfo.picBytes = picBytes;

usersDb.setUserPic(uid, uinfo.picBytes);
this.onProfileChanged(uinfo);
}

public boolean addUser(UserInfo user) {
return usersDb.addUser(user);
}

public List<Long> getUsersIds(String nameLike, int type) {
return usersDb.getUsersIds(nameLike, type);
}

public UserInfo getUser(long uid) {
return usersDb.getUser(uid);
}

public List<Long> getMessagesIdsLimitOffset(long peer, int msgType, String bodyLike, long limit, long offset) {
return messagesDb.getMessagesIdsLimitOffset(peer, msgType, bodyLike, limit, offset);
}

public List<Long> getLastMessagesIds(long peer, int msgType, long limit) {
return messagesDb.getLastMessagesIds(peer, msgType, limit);
}

public int countMessages(long peer, int msgType, String bodyLike) {
return messagesDb.countMessages(peer, msgType, bodyLike);
}

public int countMessages(int direction, int msgType, int state, long peer, long from) {
return messagesDb.countMessages(direction, msgType, state, peer, from);
}

public boolean setUserType(long uid, int type) {
return usersDb.setUserType(uid, type);
}

public boolean updateUser(UserInfo uinfo) {
return usersDb.updateUser(uinfo);
}

public void syncUser(UserInfo uinfo) {
SLogger.d(TAG, "syncUser user=" + this.uid + " uinfo.uid=" + uinfo.uid + " type=" + uinfo.type);
if (this.getUser(uinfo.uid) != null) {
this.updateUser(uinfo);
} else {
this.addUser(uinfo);
}
}

public void onPostSendMessage(Message msg) {
msg.direction = Message.DIRECTION_OUT;
messagesDb.addMessage(msg);
}

public UserKeyPair getKeyPair() {
if (kp == null) {
SLogger.e(TAG, "public key not ready");
}
return kp;
}

public UserInfo queryUserById(long uid) {
UserInfo info = Server.queryUser(uid);
if (info == null) {
SLogger.e(TAG, "user=" + uid + " query info failed" + LastError.get());
return null;
}

return info;
}

public void deleteMessage(long msgId) {
messagesDb.deleteMessage(msgId);
}

public Message getMessage(long msgId) {
return messagesDb.getMessage(msgId);
}

public void sendMessageDirect(UserInfo contact, Message msg, TaskCompleteCallback clb) {
RealClock clock = new RealClock();
clock.start();
SLogger.d(TAG, "sendMessageDirect from=" + this.uid + " to=" + contact.uid + " msgType=" 
+ msg.msgType + " bytesType=" + msg.bytesType + " bytes.length=" + ((msg.bytes != null) ? msg.bytes.length : 0));

EncryptMessageAndSend task = new EncryptMessageAndSend(this, contact, msg, clb);
task.run();

SLogger.i(TAG, "sendMessageDirect:time=" + clock.elapsedTime());
}

public void updateMessageState(long msgId, int state) {
messagesDb.updateMessageState(msgId, state);
}

public void handleIncomingMsg(Message msg) {
msg.direction = Message.DIRECTION_IN;
msg.state = Message.STATE_UNREAD;

SLogger.d(TAG, "handleIncomingMsg id=" + msg.id + " from=" + msg.from + " msgType=" + msg.msgType);

switch (msg.msgType) {
case Message.MSG_TYPE_GENERAL:
messagesDb.addMessage(msg); 
SLogger.d(TAG, "addMessage id=" + msg.id + " from=" + msg.from); 
break;
default:
SLogger.e(TAG, "received unknown msg type=" + msg.msgType + " from " + msg.from);
break;
}

}

public int countNotReadMessages() {
int msgsNotRead = 0;

List<Long> usersIds = this.getUsersIds(null, UserInfo.TYPE_FRIEND);
for (Long id : usersIds) {

msgsNotRead+= this.countMessages(Message.DIRECTION_IN, 
Message.MSG_TYPE_GENERAL,
Message.STATE_UNREAD, this.getUid(), id);
}

return msgsNotRead;
}

public void queryNewMessages() {

List<MessageInfo> msgList = Server.queryMessages(0);
if (msgList == null) {
return;
}

SLogger.d(TAG, "queryNewMessages:msgList.size=" + msgList.size());
int cmsgs = 0;
for (MessageInfo msg : msgList) {
SLogger.d(TAG, "queryNewMessages:try to add msg with id=" + msg.msgId);
if (!cryptMessagesDb.addMessage(msg)) {
SLogger.e(TAG, "can't add msg with id=" + msg.msgId);
} else { 
cmsgs++;
}
}

if (cmsgs > 0)
schedule(new DecryptMessagesTask(this), 0, TimeUnit.MILLISECONDS);
}

public long saveFile(String filePath, String fileType) {
return fileDb.addFile(filePath, fileType); 
}

public UserFile getFile(long id) {
return fileDb.getFile(id);
}

public void deleteFile(long id) {
fileDb.deleteFile(id);
}

public void decryptMessages(int numMessages) {

List<Message> msgList = cryptMessagesDb.queryMessages(numMessages);
for (Message msg : msgList) {
RealClock clock = new RealClock();
clock.start();
cryptMessagesDb.deleteMessage(msg.id);
SLogger.i(TAG, "decryptMessages:deleteMessage time=" + clock.elapsedTime());

clock.start();
UserKeyPair kp = this.getKeyPairByKeyId(Long.toString(msg.encKeyId));
if (kp == null) {
SLogger.e(TAG, "cant found key pair for keyId=" + msg.encKeyId);
continue;
}
SLogger.i(TAG, "decryptMessages:getKeyPairByKeyId time=" + clock.elapsedTime());

clock.start();
MessageCryptResult result = MessageCrypt.decryptMessage(
kp.getPrivateKey(), 
kp.getKeyId(), 
msg.bytes,
this.getCryptResolver(), null);

SLogger.i(TAG, "decryptMessages:decryptMessage time=" + clock.elapsedTime());

clock.start();
if (result.error != Errors.SUCCESS) {
SLogger.e(TAG, "message not decrypted with error=" + result.error);
ACRA.getErrorReporter().handleException(null);
} else {
Message resultMsg = result.msg;
resultMsg.id = msg.id;
this.handleIncomingMsg(resultMsg);
}
SLogger.i(TAG, "decryptMessages:handleIncomingMsg time=" + clock.elapsedTime());
}
}

public long saveBitmapScale(Bitmap bitmap, int width) {

Bitmap scaledBm = CApp.getInstance().scaleBitmapByWidth(bitmap, width);

File bmFile = new File(CApp.getInstance().getPrivateFilesDir(), UUID.randomUUID().toString());
FileOutputStream out = null;
try {
out = new FileOutputStream(bmFile);
} catch (FileNotFoundException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}

if (out == null) {
FileOps.deleteFileRecursive(bmFile);
return -1;
}

scaledBm.compress(Bitmap.CompressFormat.PNG, AppSettings.PNG_QUALITY, out);
try {
out.close();
} catch (IOException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}

long id = saveFile(bmFile.getAbsolutePath(), "image/png");

FileOps.deleteFileRecursive(bmFile);
return id;
}

public void deleteHistory(long uid) {
// TODO Auto-generated method stub
messagesDb.deleteMessages(this.uid, uid);
messagesDb.deleteMessages(uid, this.uid);
}

public void deleteUser(long uid) {
// TODO Auto-generated method stub
usersDb.deleteUser(uid);
}

public boolean setCurrKey(long uid, long keyId, String data) {
return usersDb.setCurrKey(uid, keyId, data);
}

public long saveBitmap(Bitmap bitmap) {

File bmFile = new File(CApp.getInstance().getPrivateFilesDir(), UUID.randomUUID().toString());
FileOutputStream out = null;
try {
out = new FileOutputStream(bmFile);
} catch (FileNotFoundException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}

if (out == null) {
FileOps.deleteFileRecursive(bmFile);
return -1;
}

bitmap.compress(Bitmap.CompressFormat.PNG, AppSettings.PNG_QUALITY, out);
try {
out.close();
} catch (IOException e) {
// TODO Auto-generated catch block
SLogger.exception(TAG, e);
}

long id = saveFile(bmFile.getAbsolutePath(), "image/png");

FileOps.deleteFileRecursive(bmFile);
return id;
}

public UserInfo queryProfile() {
UserInfo uinfo = Server.queryProfile();
if (uinfo != null)
syncUser(uinfo);

return uinfo;
}

public void updateProfile(UserInfo uinfo) { 
if (CApp.getInstance().debug)
SLogger.d(TAG, "updateProfile uinfo=" + Json.mapToString(uinfo.toMap()));
Server.updateProfile(uinfo);
}

public long addNotice(Notice notice) {
return noticesDb.addNotice(notice);
}

public Notice getNotice(long id) {
return noticesDb.getNotice(id);
}

public Notice getInvite(long inviteId) {
return noticesDb.getInvite(inviteId);
}

public List<Long> getNoticesIds(int type, int state) {
return noticesDb.getNoticesIds(type, state);
}

public void deleteNotice(long id) {
noticesDb.deleteNotice(id);
}

public void deleteSelf() {
Context context = CApp.getInstance().getApplication().getApplicationContext();

usersDb.deleteSelf(context);
noticesDb.deleteSelf(context);
messagesDb.deleteSelf(context);
userKeysDb.deleteSelf(context);
fileDb.deleteSelf(context);
cryptMessagesDb.deleteSelf(context);
}
}