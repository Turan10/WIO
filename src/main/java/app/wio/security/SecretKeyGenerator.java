package app.wio.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class SecretKeyGenerator {
    public static void main(String[] args) {

        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        String base64EncodedKey = Encoders.BASE64.encode(keyBytes);

        System.out.println("Your secret key is: " + base64EncodedKey);
    }
}