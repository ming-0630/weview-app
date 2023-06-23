package org.weviewapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil{
    private final TextEncryptor encryptor;

    @Autowired
    public EncryptionUtil() {
//        String salt = KeyGenerators.string().generateKey();
//        System.out.println(salt);
        encryptor = Encryptors.text("YOURSECRETKEY", "c7f34f8774579c17");
    }

    public String encrypt(String plaintext) {
        return encryptor.encrypt(plaintext);
    }

    public String decrypt(String ciphertext) {
        return encryptor.decrypt(ciphertext);
    }
}
