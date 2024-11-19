package app.wio.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class SecretKeyGenerator {
    public static void main(String[] args) {

        // Generating a secret key in HS512 algorithm
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        // Base64 encoding the key
        String base64EncodedKey = Encoders.BASE64.encode(keyBytes);

        System.out.println("Your secret key is: " + base64EncodedKey);
    }
}