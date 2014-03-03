public class UserClientKey implements IClientKey {
public long id = -1;
public long keyId = -1;
public long remoteId = -1;
public String publicKey = null; 
public String privateKey = null;
public String sessionKey = null;

@Override
public long getId() {
// TODO Auto-generated method stub
return id;
}

@Override
public KeyQuery getPrivateKey() {
// TODO Auto-generated method stub
KeyQuery kq = new KeyQuery(Errors.SUCCESS);
kq.keyId = keyId;
kq.privateKey = privateKey;

return kq;
}

@Override
public KeyQuery getPublicKey() {
// TODO Auto-generated method stub
KeyQuery kq = new KeyQuery(Errors.SUCCESS);
kq.keyId = keyId;
kq.publicKey = publicKey;

return kq;
}

@Override
public long getRemoteId() {
// TODO Auto-generated method stub
return remoteId;
}

@Override
public KeyQuery getSessionKey() {
// TODO Auto-generated method stub

KeyQuery kq = new KeyQuery(Errors.SUCCESS);
kq.keyId = keyId;
kq.sessionKey = sessionKey;

return kq;
}
}