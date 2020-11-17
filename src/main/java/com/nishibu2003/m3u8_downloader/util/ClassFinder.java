package com.nishibu2003.m3u8_downloader.util;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {
    private ClassLoader classLoader;

    public ClassFinder() {
        classLoader = Thread.currentThread().getContextClassLoader();
        //classLoader = ClassLoader.getSystemClassLoader();
    }

    public ClassFinder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private String fileNameToClassName(String name) {
        return name.substring(0, name.length() - ".class".length());
    }

    private String resourceNameToClassName(String resourceName) {
        return fileNameToClassName(resourceName).replace('/', '.');
    }

    private boolean isClassFile(String fileName) {
        return fileName.endsWith(".class");
    }

    private String packageNameToResourceName(String packageName) {
        return packageName.replace('.', '/');
    }

    public List<Class<?>> findClasses(String rootPackageName) throws Exception {
        String resourceName = packageNameToResourceName(rootPackageName);
        Enumeration<URL> urls = classLoader.getResources(resourceName);

        List<Class<?>> ret = new ArrayList<Class<?>>();
        
        while (urls.hasMoreElements()) {
        	URL url = urls.nextElement();
        	
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
            	List<Class<?>> list = findClassesWithFile(rootPackageName, new File(url.getFile()));
            	ret.addAll(list);
            } else if ("jar".equals(protocol)) {
            	List<Class<?>> list = findClassesWithJarFile(rootPackageName, url);
            	ret.addAll(list);
            } else {
                throw new IllegalArgumentException("Unsupported Class Load Protodol[" + protocol + "]");
            }
        }
        
        return ret;
    }

    private List<Class<?>> findClassesWithFile(String packageName, File dir) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        for (String path : dir.list()) {
            File entry = new File(dir, path);
            if (entry.isFile() && isClassFile(entry.getName())) {
            	Class<?> clazz = Class.forName(packageName + "." + fileNameToClassName(entry.getName()) ,true ,classLoader);
            	classes.add(clazz);
            } else if (entry.isDirectory()) {
                classes.addAll(findClassesWithFile(packageName + "." + entry.getName(), entry));
            }
        }

        return classes;
    }

    private List<Class<?>> findClassesWithJarFile(String rootPackageName, URL jarFileUrl) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        JarURLConnection jarUrlConnection = (JarURLConnection)jarFileUrl.openConnection();
        JarFile jarFile = null;

        try {
            jarFile = jarUrlConnection.getJarFile();
            Enumeration<JarEntry> jarEnum = jarFile.entries();

            String packageNameAsResourceName = packageNameToResourceName(rootPackageName);

            while (jarEnum.hasMoreElements()) {
                JarEntry jarEntry = jarEnum.nextElement();
                if (jarEntry.getName().startsWith(packageNameAsResourceName) && isClassFile(jarEntry.getName())) {
                	Class<?> clazz = Class.forName(resourceNameToClassName(jarEntry.getName()) ,true ,classLoader);
                	classes.add(clazz);
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        return classes;
    }
    
    public static void main(String[] args) throws Exception {
        ClassFinder classFinder = new ClassFinder();

        for (Class<?> clazz : classFinder.findClasses(args[0])) {
            System.out.println(clazz);
        }
    }

}