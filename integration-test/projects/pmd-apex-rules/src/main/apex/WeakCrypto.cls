public class WeakCrypto {
    public WeakCrypto() {
        Blob exampleIv = Blob.valueOf('0000000000000000');
        Blob key = Crypto.generateAesKey(128);
        Blob data = Blob.valueOf('Data to be encrypted');
        Blob encrypted = Crypto.encrypt('AES128', key, exampleIv, data);
    }
}
