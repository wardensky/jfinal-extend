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
package com.jfinal.ext.plugin.monogodb;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.log.Log;
import com.jfinal.plugin.IPlugin;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongodbPlugin implements IPlugin {
    
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAUL_PORT = 27017;
    private static final String ADMIN_DB = "admin";

    protected final Log logger = Log.getLog(getClass());

    private MongoClient client;
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

    public MongodbPlugin(String database) {
        this.host = DEFAULT_HOST;
        this.port = DEFAUL_PORT;
        this.database = database;
    }

    public MongodbPlugin(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }
    
    public MongodbPlugin(String host, int port, String database, String username, String password) {
    	this.host = host;
    	this.port = port;
    	this.database = database;
    	this.username = username;
    	this.password = password;
    }

    @Override
    public boolean start() {
    	if (StringUtils.isNotBlank(username)) {
    		// 用户名 数据库 密码
    		//连接mongodb数据库首先要连admin数据库，验证权限才行。
            MongoCredential credential = MongoCredential.createCredential(username, ADMIN_DB, password.toCharArray());  
            //IP port  
            ServerAddress addr = new ServerAddress(host, port);  
            client = new MongoClient(addr,Arrays.asList(credential)); 
		} else {
			client = new MongoClient(host, port);
		}
        MongoKit.init(client, database);
        return true;
    }

    @Override
    public boolean stop() {
        if (client != null) {
            client.close();
        }
        return true;
    }

}
