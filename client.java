public class UserClientKeyStore implements IClientKeyStore {
private static final String TAG = "UserClientKeyStore";
private UserClientKsDb ksDb = null;

public UserClientKeyStore() {
Context context = CApp.getInstance().getApplication().getApplicationContext();
ksDb = UserClientKsDb.getInstance(context);
}

@Override
public PrivateKey getPrivateKey(long ownerId, long keyId) {
// TODO Auto-generated method stub
if (ownerId == 0) {
//server private key is not known by client
SLogger.e(TAG, "getPrivateKey: server private key is not known by client");
return null;
}
IClientKey ck = ksDb.getClientByRemoteId(ownerId);
if (ck == null) {
SLogger.e(TAG, "getPrivateKey: cant found client by id=" + ownerId);

return null;
}
KeyQuery kq = ck.getPrivateKey();
if (kq.error != Errors.SUCCESS) {
SLogger.e(TAG, "getPrivateKey: cant get client private key, err=" + kq.error);
return null;
}

return Json.stringToPrivateKey(kq.privateKey);
}

@Override
public PublicKey getPublicKey(long ownerId, long keyId) {
// TODO Auto-generated method stub
if (ownerId == 0) {
//server public key
return Json.stringToPublicKey(ServerSettings.srvPubKey);
}

IClientKey ck = ksDb.getClientByRemoteId(ownerId);
if (ck == null) {
SLogger.e(TAG, "getPublicKey: cant found client by id=" + ownerId);
return null;
}

KeyQuery kq = ck.getPublicKey();
if (kq.error != Errors.SUCCESS) {
SLogger.e(TAG, "getPublicKey: cant get client public key, err=" + kq.error);
return null;
}

return Json.stringToPublicKey(kq.publicKey);
}

@Override
public void deleteClient(long clientId) {
// TODO Auto-generated method stub
ksDb.deleteClient(clientId);
}

@Override
public long createClient() {
// TODO Auto-generated method stub
return ksDb.createClient();
}

@Override
public IClientKey getClient(long clientId) {
// TODO Auto-generated method stub
return ksDb.getClient(clientId);
}

@Override
public long getCurrentClient() {
// TODO Auto-generated method stub
return ksDb.getCurrentClient();
}

@Override
public boolean setCurrentClient(long clientId) {
// TODO Auto-generated method stub
return ksDb.setCurrentClient(clientId);
}

@Override
public boolean setClientRemoteId(long clientId, long remoteId) {
// TODO Auto-generated method stub
return ksDb.setClientRemoteId(clientId, remoteId);
}

@Override
public boolean setClientSessionKey(long clientId, String sessionKey) {
// TODO Auto-generated method stub
return ksDb.setClientSessionKey(clientId, sessionKey);
}

}