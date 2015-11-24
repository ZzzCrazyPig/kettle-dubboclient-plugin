package com.gosun.di.ui.trans.steps.dubboclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DynamicClassLoader {
	
	private URLClassLoader appClassLoader;
	private Method addURLMethod;
	
	private static class DynamicClassLoaderHolder {
		
		private static DynamicClassLoader instance;
		
		static {
			instance = new DynamicClassLoader();
		}
		
	}
	
	public static DynamicClassLoader getDynamicClassLoader() {
		return DynamicClassLoaderHolder.instance;
	}
	
	private DynamicClassLoader() {
		appClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		try {
			addURLMethod = ReflectionUtil.getMethod(URLClassLoader.class, "addURL", new Class<?>[]{
				URL.class
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean loadJar(String[] jarFiles) {
		for(String jarFile : jarFiles) {
			if(!loadJar(jarFile)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean loadJar(String jarFile) {
		File file = new File(jarFile);
		if(!file.exists()) {
			return false;
		}
		try {
			ReflectionUtil.invoke(addURLMethod, appClassLoader, file.toURI().toURL());
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return appClassLoader.loadClass(name);
	}
	
	/**
	 * 在指定的jar文件里面查找所有的接口(包括包名)
	 * @param jarFile
	 * @return
	 * @throws Exception 
	 */
	public List<String> findInterfacesInJar(String jarFile) {
		loadJar(jarFile);
		List<String> interfaceNames = new ArrayList<String>();
		List<String> classNames = null;
		try {
			classNames = listClassNamesFromJar(jarFile);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(String className : classNames) {
			try {
				Class<?> clazz = appClassLoader.loadClass(className);
				if(clazz.isInterface()) {
					interfaceNames.add(className);
				}
			} catch(Exception e) {
				System.out.println("有异常");
			} finally {
				
			}
		}
		return interfaceNames;
	}
	
	/**
	 * 加载指定目录下的所有jar到ClassLoader中
	 * @param dir
	 */
	public void loadJarInDir(String dir) {
		String[] jarNames = findJarNamesInDir(dir);
		for(String jarName : jarNames) {
			String jarFile = dir + "/" + jarName;
			loadJar(jarFile);
		}
	}
	
	/**
	 * 在指定目录中查找出所有的jar文件
	 * @param dir
	 * @return
	 */
	public String[] findJarNamesInDir(String dir) {
		String[] jarNames = null;
		File file = new File(dir);
		if(file.exists() && file.isDirectory()) {
			jarNames = file.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if(name.endsWith(".jar")) {
						return true;
					}
					return false;
				}
			});
		}
		return jarNames;
	}
	
	public List<String> listClassNamesFromJar(String jarFile) throws Exception {
		List<String> classNames = new ArrayList<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
		    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
		        // This ZipEntry represents a class. Now, what class does it represent?
		        String className = entry.getName().replace('/', '.'); // including ".class"
		        classNames.add(className.substring(0, className.length() - ".class".length()));
		    }
		}
		zip.close();
		return classNames;
	}

}
