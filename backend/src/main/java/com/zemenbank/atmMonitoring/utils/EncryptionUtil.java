package com.zemenbank.atmMonitoring.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Utility class for encrypting sensitive configuration values using Jasypt
 * 
 * Usage: Run this class with your encryption password as argument to encrypt values
 */
public class EncryptionUtil {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java EncryptionUtil <encryption-password> <value-to-encrypt>");
            System.out.println("Example: java EncryptionUtil mySecretKey myDatabasePassword");
            return;
        }

        String encryptionPassword = args[0];
        String valueToEncrypt = args[1];

        String encryptedValue = encrypt(valueToEncrypt, encryptionPassword);
        System.out.println("Original value: " + valueToEncrypt);
        System.out.println("Encrypted value: " + encryptedValue);
        System.out.println("Use in properties file as: ENC(" + encryptedValue + ")");
    }

    public static String encrypt(String input, String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        config.setPassword(password);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        
        encryptor.setConfig(config);
        return encryptor.encrypt(input);
    }

    public static String decrypt(String encryptedInput, String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        config.setPassword(password);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        
        encryptor.setConfig(config);
        return encryptor.decrypt(encryptedInput);
    }
} 