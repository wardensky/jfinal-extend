/**
 * Copyright (c) 2015-2016, BruceZCQ (zcq@zhucongqi.cn).
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
package com.jfinal.ext2.upload.filerenamepolicy;

import java.io.File;

import com.jfinal.ext2.kit.RandomKit;
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * 随机文件名
 * @author BruceZCQ
 */
public class RandomFileRenamePolicy implements FileRenamePolicy {
	
	@Override
	public File rename(File f) {
		if (null == f) {
			return null;
		}
		String name = f.getName();
		String ext = "";
		int dot = name.lastIndexOf(".");
		if (dot != -1) {
			ext = name.substring(dot);
		 }
		return (new File(f.getParent(), RandomKit.randomMD5Str() + ext));
	}
}
