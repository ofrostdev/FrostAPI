package com.github.ofrostdev.api.utils;

import java.io.*;
import java.util.Base64;

public class Base64API {

    public static String encodeString(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String decodeString(String base64) {
        return new String(Base64.getDecoder().decode(base64));
    }

    public static String encodeObject(Serializable object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object decodeObject(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T decodeObject(String base64, Class<T> clazz) {
        Object obj = decodeObject(base64);
        if (clazz.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }
}
