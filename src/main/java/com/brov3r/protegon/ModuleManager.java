package com.brov3r.protegon;

import com.avrix.Launcher;
import com.brov3r.protegon.modules.Module;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The {@code ModuleManager} class is responsible for loading and managing Protegon modules.
 * It locates and instantiates all classes that extend the {@link Module} class within the provided JAR file.
 */
public class ModuleManager {
    /**
     * A list of loaded Protegon modules.
     */
    private static final List<Module> modules = new ArrayList<>();

    /**
     * Retrieves the list of loaded modules.
     *
     * @return A list of loaded modules.
     */
    public static List<Module> getModules() {
        return modules;
    }

    /**
     * Loads Protegon modules from the JAR file that the application is running from.
     * This method locates all classes that extend the {@link Module} class and instantiates them.
     *
     * <p>If an error occurs during the loading process, it is logged to the console.</p>
     */
    public static void loadModules() {
        File jarFile;
        URL jarUrl;
        try {
            jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            jarUrl = jarFile.toURI().toURL();
        } catch (Exception e) {
            System.err.println("[!] Error resolving the JAR file location: " + e.getMessage());
            return;
        }

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, Launcher.class.getClassLoader())) {
            JarURLConnection jarConnection = (JarURLConnection) new URL("jar:file:" + jarFile + "!/").openConnection();
            JarFile jar = jarConnection.getJarFile();
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && entry.getName().startsWith("com/brov3r/protegon/modules")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        
                        if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                            continue;
                        }

                        if (Module.class.isAssignableFrom(clazz)) {
                            Module module = (Module) clazz.getDeclaredConstructor().newInstance();
                            modules.add(module);
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("[!] Class not found: " + className);
                        throw e;
                    } catch (InstantiationException e) {
                        System.err.println("[!] Failed to instantiate Protegon module class: " + className);
                        throw e;
                    } catch (IllegalAccessException e) {
                        System.err.println("[!] Illegal access when instantiating Protegon module class: " + className);
                        throw e;
                    } catch (NoSuchMethodException e) {
                        System.err.println("[!] No default constructor found for Protegon module class: " + className);
                        throw e;
                    } catch (InvocationTargetException e) {
                        System.err.println("[!] Error occurred while invoking constructor for class: '" + className + "'. Cause: " + e.getCause());
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[!] IO exception occurred while accessing the JAR file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[!] Critical error occurred while loading Protegon modules! Reason: " + e.getMessage());
        }

        if (modules.isEmpty()) return;

        // Use StringBuilder to collect module names in a single line, separated by commas
        StringBuilder moduleNames = new StringBuilder("[#] Protegon modules loaded: ");
        for (int i = 0; i < modules.size(); i++) {
            moduleNames.append(modules.get(i).getName());
            if (i < modules.size() - 1) {
                moduleNames.append(", ");
            }
        }
        System.out.println(moduleNames);  // Print the final string with all module names
    }
}
