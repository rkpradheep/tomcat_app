package com.server.test;

import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.server.framework.common.Util;

public class CLoader
{
	public static String get() throws Exception
	{

		//URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("file:" + com.server.framework.common.Util.HOME_PATH + "/tomcat_build/webapps/ROOT/WEB-INF/lib/com.server.test.DynamicCP.jar")}, com.server.test.CLoader.class.getClassLoader());
		ClassLoader classLoader = new ClassLoader()
		{
			@Override
			protected Class<?> findClass(String className) throws ClassNotFoundException
			{
				if(!className.equals("com.server.test.DynamicCP"))
				{
					return this.getClass().getClassLoader().loadClass(className);
				}
				String libPath = Util.HOME_PATH + "/tomcat_build/webapps/ROOT/WEB-INF/lib/";
				try(JarFile jarFile = new JarFile(libPath + "com.server.test.DynamicCP.jar"))
				{
					byte[] classData = IOUtils.toByteArray(jarFile.getInputStream(jarFile.getJarEntry("com.server.test.DynamicCP.class")));
					return defineClass(className, classData, 0, classData.length);
				}
				catch(IOException ex)
				{
					return null;
				}
			}
		};

		return (String) classLoader.loadClass("com.server.test.DynamicCP").getDeclaredMethod("run").invoke(null);
	}
}
