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
package com.jfinal.ext2.plugin.activerecord.generator;

import java.util.List;

import com.jfinal.plugin.activerecord.generator.MappingKitGenerator;
import com.jfinal.plugin.activerecord.generator.TableMeta;

/**
 * MappingKitGenerator Extension: config generate mapping
 * @author BruceZCQ
 */
public class MappingKitGeneratorExt extends MappingKitGenerator {

	private boolean generateMappingArpKit = true;
	private boolean generateTableMapping = true;
	
	protected String tableMappingPutTableTemplate =
			"\tpublic void putTable(Class<? extends Model<?>> modelClass, String tableName) {%n"
			+"\t\tthis.modelToTableMap.put(modelClass, tableName);\n"
			+ "\t}\n";
	protected String tableMappingGetTableTemplate =
			"\n\tpublic String getTableName(Class<? extends Model<?>> modelClass) {%n"
			+ "\t\tif (!this.modelToTableMap.containsKey(modelClass)) {"
			+ "\n\t\t\treturn \"\";\n\t\t}\n\t\t"
			+ "return this.modelToTableMap.get(modelClass);\n\t}\n";
	protected String tableMappingTemplate = 
			"\tprivate final Map<Class<? extends Model<?>>, String> modelToTableMap = new HashMap<Class<? extends Model<?>>, String>();\n"
			+ "\tprivate static %s me = new %s();\n\n";
	protected String tableMappingMethodTemplate = "\tpublic static %s me() {\n"+
													"\t\treturn me;\n\t}\n\n";
	protected String tableMappingMethodContentTemplate =
				"\t\tthis.modelToTableMap.put(%s.class, \"%s\");%n";
	
	public MappingKitGeneratorExt(String mappingKitPackageName,
			String mappingKitOutputDir) {
		super(mappingKitPackageName, mappingKitOutputDir);
	}

	public void setMappingKitClassName(String mappingKitClassName) {
		this.mappingKitClassName = mappingKitClassName;
	}
	
	public void setGenerateMappingArpKit(boolean generateMappingArpKit) {
		this.generateMappingArpKit = generateMappingArpKit;
	}
	
	public void setGenerateTableMapping(boolean generateTableMapping) {
		this.generateTableMapping = generateTableMapping;
	}
	
	@Override
	protected void genClassDefine(StringBuilder ret) {
		if (this.generateTableMapping) {
			this.classDefineTemplate =
							"/**%n" +
							" * Generated by JFinal-Ext2.%n" +
							" */%n" +
							"public class %s {%n%n";
		}
		super.genClassDefine(ret);
		
		if (this.generateTableMapping) {
			ret.append(String.format(tableMappingTemplate, this.mappingKitClassName, this.mappingKitClassName));
			ret.append(String.format(tableMappingMethodTemplate, this.mappingKitClassName));
		}
	}

	@Override
	protected void genImport(StringBuilder ret) {
		if (this.generateMappingArpKit) {
			super.genImport(ret);
		} 
		
		if (this.generateTableMapping) {
			ret.append("import java.util.*;\nimport com.jfinal.plugin.activerecord.Model;\n\n");
		}
	}

	@Override
	protected void genMappingMethod(List<TableMeta> tableMetas,
			StringBuilder ret) {
		if (this.generateMappingArpKit) {
			super.genMappingMethod(tableMetas, ret);
			ret.append("\n");
		}
		
		if (this.generateTableMapping) {
			ret.append(String.format(tableMappingPutTableTemplate));
			ret.append(String.format(tableMappingGetTableTemplate));
			
			//init
			ret.append(String.format("\n\tprivate %s() {\n", this.mappingKitClassName));
			for (TableMeta tableMeta : tableMetas) {
				ret.append(String.format(tableMappingMethodContentTemplate, tableMeta.modelName, tableMeta.name));
			}
			ret.append("\t}\n");
		}
	}
}
