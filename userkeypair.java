public class UserKeyPair {
private static final String TAG = "UserKeyPair";
private String publicKey = null;
private String privateKey = null;
private long keyId = -1;

UserKeyPair(String publicKey, String privateKey, long keyId) {
if (CApp.getInstance().debug)
SLogger.d(TAG, "publicKey=" + publicKey + " privateKey=" + privateKey + " keyId=" + keyId);
this.privateKey = privateKey;
this.publicKey = publicKey;
this.keyId = keyId;
}
UserKeyPair() {

}

public void generate() {
KeyPair kp = MessageCrypt.genKeys();
privateKey = Json.privateKeyToString(kp.getPrivate());
publicKey = Json.publicKeyToString(kp.getPublic());
keyId = MessageCrypt.getRndLong();
}

public String getPublicKey() {
return this.publicKey;
}

public String getPrivateKey() {
return this.privateKey;
}

public long getKeyId() {
return this.keyId;
}
}