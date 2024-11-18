package app.wio.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // Generate a random key for HS512 algorithm
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        // Encode the key as a Base64 string
        String base64EncodedKey = Encoders.BASE64.encode(keyBytes);
        // Output the key
        System.out.println("Your secret key is: " + base64EncodedKey);
    }
}