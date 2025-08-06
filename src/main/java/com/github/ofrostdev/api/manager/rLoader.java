package com.github.ofrostdev.api.manager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class rLoader {

    private final File jarFile;
    private final String packageName;
    private final List<Class<?>> loadedClasses = new ArrayList<>();

    public rLoader(File jarFile, String packageName) {
        this.jarFile = jarFile;
        this.packageName = packageName;
    }

    public void loadClasses() {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            URL[] urls = { new URL("jar:file:" + jarFile.getAbsolutePath() + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    String className = name.replace("/", ".").replace(".class", "");

                    if (!className.startsWith(packageName)) continue;

                    try {
                        Class<?> clazz = Class.forName(className, false, cl);
                        loadedClasses.add(clazz);
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> void forEach(Class<T> type, Consumer<T> action) {
        for (Class<?> clazz : loadedClasses) {
            if (type.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    T instance = type.cast(clazz.getDeclaredConstructor().newInstance());
                    action.accept(instance);
                } catch (Exception e) {
                    System.err.println("[rLoader] Falha ao instanciar: " + clazz.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
