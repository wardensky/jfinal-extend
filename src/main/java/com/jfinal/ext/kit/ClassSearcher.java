/**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jfinal.ext.kit;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.chainsaw.Main;

import com.google.common.collect.Lists;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;

public class ClassSearcher {

	protected static final Log logger = Log.getLog(ClassSearcher.class);

	private String classpath = PathKit.getRootClassPath();

	private String libDir = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "lib";
	private String jarFilter = "";
	private List<String> scanPackages = Lists.newArrayList();

	private boolean includeAllJarsInLib = false;

	private List<String> includeJars = Lists.newArrayList();

	private String targetDirName = "target";

	private List<String> excludeTargetDirName = Lists.newArrayList();

	@SuppressWarnings("rawtypes")
	private Class target;

	@SuppressWarnings("unchecked")
	private static <T> List<Class<? extends T>> extraction(Class<T> clazz, List<String> classFileList) {
		List<Class<? extends T>> classList = Lists.newArrayList();
		for (String classFile : classFileList) {
			// 无法获得class则忽略
			try {
				Class<?> classInFile = Reflect.on(classFile).get();
				if (clazz.isAssignableFrom(classInFile) && clazz != classInFile) {
					classList.add((Class<? extends T>) classInFile);
				}
			} catch (Exception e) {
			}
		}

		return classList;
	}

	@SuppressWarnings("rawtypes")
	public static ClassSearcher of(Class target) {
		return new ClassSearcher(target);
	}

	/**
	 * @param baseDirName
	 *            查找的文件夹路径
	 * @param targetFileName
	 *            需要查找的文件名
	 */
	private List<String> findFiles(String baseDirName, String targetFileName) {
		/**
		 * 算法简述： 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件，
		 * 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 队列不空，重复上述操作，队列为空，程序结束，返回结果。
		 */
		List<String> classFiles = Lists.newArrayList();
		File baseDir = new File(baseDirName);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			logger.error("search error：" + baseDirName + "is not a dir！");
		} else {
			String[] files = baseDir.list();
			for (int i = 0; i < files.length; i++) {
				File file = new File(baseDirName + File.separator + files[i]);
				if (file.isDirectory()) {
					classFiles.addAll(findFiles(baseDirName + File.separator + files[i], targetFileName));
				} else {
					if (wildcardMatch(targetFileName, file.getName())) {
						String fileName = file.getAbsolutePath();

						boolean exclude = false;
						for (String name : excludeTargetDirName) {
							if (fileName.contains(name)) {
								exclude = true;
								break;
							}
						}

						if (exclude) {
							continue;
						}

						StringBuilder sb;
						if (fileName.contains(targetDirName)) {
							sb = new StringBuilder();
							// 由于unix中file.separator为斜杠"/"，下面这段代码可以处理windows和unix下的所有情况
							String temp[] = fileName.replaceAll("\\\\", "/").split("/");
							int len = temp.length - 4;
							for (int j = 0; j < temp.length; j++) {

								if (targetDirName.equals(temp[j])) {
									len = j + 1;
									break;
								}
							}
							for (int k = 0; k <= len; k++) {
								sb.append(temp[k] + File.separator);
							}
						} else {
							sb = new StringBuilder(classpath + File.separator);
						}
						String open = sb.toString();

						String close = ".class";
						int start = fileName.indexOf(open);
						int end = fileName.indexOf(close, start + open.length());
						String className = fileName.substring(start + open.length(), end).replace(File.separator, ".");
						classFiles.add(className);
					}
				}
			}
		}
		return classFiles;
	}

	/**
	 * 通配符匹配
	 *
	 * @param pattern
	 *            通配符模式
	 * @param fileName
	 *            待匹配的字符串
	 */
	private static boolean wildcardMatch(String pattern, String fileName) {
		int patternLength = pattern.length();
		int strLength = fileName.length();
		int strIndex = 0;
		char ch;
		for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if (ch == '*') {
				// 通配符星号*表示可以匹配任意多个字符
				while (strIndex < strLength) {
					if (wildcardMatch(pattern.substring(patternIndex + 1), fileName.substring(strIndex))) {
						return true;
					}
					strIndex++;
				}
			} else if (ch == '?') {
				// 通配符问号?表示匹配任意一个字符
				strIndex++;
				if (strIndex > strLength) {
					// 表示str中已经没有字符匹配?了。
					return false;
				}
			} else {
				if ((strIndex >= strLength) || (ch != fileName.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return strIndex == strLength;
	}

	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> search() {
		List<String> classFileList = Lists.newArrayList();
		if (scanPackages.isEmpty()) {
			classFileList = findFiles(classpath, "*.class");
		} else {
			for (String scanPackage : scanPackages) {
				classFileList = findFiles(
						classpath + File.separator + scanPackage.replaceAll("\\.", "\\" + File.separator), "*.class");
			}
		}
		this.findDefaultLibDir();
		classFileList.addAll(findjarFiles(libDir));
		return extraction(target, classFileList);
	}

	private void findDefaultLibDir() {
		File baseDir = new File(this.libDir);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			File f = new File(System.getProperty("user.dir"));
			this.libDir = f.getParent() + File.separator + "lib";
			logger.info("new dir = " + this.libDir);
		}
	}

	public static void main(String[] args) {
		String entryName = "org/hhtd_constants/BusiConst.class";
	 
		String separator =entryName.contains("/") ? "/":"\\";
		 
		String className = entryName.replaceAll( separator, ".").substring(0, entryName.length() - 6);
		System.out.println(className);
	}

	/**
	 * 查找jar包中的class
	 */
	private List<String> findjarFiles(String baseDirName) {
		System.out.println("execute findjarFiles");
		List<String> classFiles = Lists.newArrayList();
		File baseDir = new File(baseDirName);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			logger.error("file search error:" + baseDirName + " is not a dir！");
		} else {
			File[] files = baseDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					classFiles.addAll(findjarFiles(file.getAbsolutePath()));
				} else {
					if (includeAllJarsInLib || includeJars.contains(file.getName())) {
						if (!file.getName().endsWith(".jar")) {
							continue;
						}
						if (!file.getName().contains(this.jarFilter)) {
							continue;
						}
						logger.info(file.getName());
						JarFile localJarFile = null;
						try {
							localJarFile = new JarFile(new File(baseDirName + File.separator + file.getName()));
							Enumeration<JarEntry> entries = localJarFile.entries();
							while (entries.hasMoreElements()) {
								JarEntry jarEntry = entries.nextElement();
								String entryName = jarEntry.getName();
								if (scanPackages.isEmpty()) {
									if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
									 
										// org/hhtd_constants/BusiConst.class
										String className = entryName.replaceAll(entryName.contains("/") ? "/":"\\", ".").substring(0,
												entryName.length() - 6);
										classFiles.add(className);
										logger.info("className" + className);
									}
								} else {
									for (String scanPackage : scanPackages) {
										System.out.println("scanPackage = " + scanPackage);
										logger.info("scanPackage = " + scanPackage);
										scanPackage = scanPackage.replaceAll("\\.", "\\" + File.separator);
										if (!jarEntry.isDirectory() && entryName.endsWith(".class")
												&& entryName.startsWith(scanPackage)) {
											String className = entryName.replaceAll(entryName.contains("/") ? "/":"\\", ".").substring(0,
													entryName.length() - 6);
											classFiles.add(className);
										}
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (localJarFile != null) {
									localJarFile.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
		}
		return classFiles;
	}

	@SuppressWarnings("rawtypes")
	public ClassSearcher(Class target) {
		this.target = target;
	}

	public ClassSearcher injars(List<String> jars) {
		if (jars != null) {
			includeJars.addAll(jars);
		}
		return this;
	}

	public ClassSearcher inJars(String... jars) {
		if (jars != null) {
			for (String jar : jars) {
				includeJars.add(jar);
			}
		}
		return this;
	}

	public ClassSearcher includeAllJarsInLib(boolean includeAllJarsInLib) {
		this.includeAllJarsInLib = includeAllJarsInLib;
		return this;
	}

	public ClassSearcher classpath(String classpath) {
		this.classpath = classpath;
		return this;
	}

	public ClassSearcher libDir(String libDir) {
		this.libDir = libDir;
		return this;
	}

	public ClassSearcher jarFilter(String jarFilter) {
		this.jarFilter = jarFilter;
		return this;
	}

	public ClassSearcher scanPackages(List<String> scanPaths) {
		if (scanPaths != null) {
			scanPackages.addAll(scanPaths);
		}
		return this;
	}

	public ClassSearcher target(String targetDirName, List<String> excludeTargetDirName) {
		if (StrKit.notBlank(targetDirName)) {
			this.targetDirName = targetDirName;
		}

		if (excludeTargetDirName != null) {
			this.excludeTargetDirName = excludeTargetDirName;
		}
		return this;
	}
}
