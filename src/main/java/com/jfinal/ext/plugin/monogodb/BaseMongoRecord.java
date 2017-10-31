package com.jfinal.ext.plugin.monogodb;

import java.util.Date;

import com.jfinal.plugin.activerecord.Record;

public abstract class BaseMongoRecord<R extends Record> extends BaseMongoUtil<R> {

	private static final long serialVersionUID = 1L;
	
	public static final String _ID = "_id";
	public static final String ID = "id";
	public static final String CREATE_TIME = "create_time";
	public static final String UPDATE_TIME = "update_time";
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	
	public String get_id() {
		return get(_ID);
	}

	public BaseMongoRecord<R> set_id(String _id) {
		set(_ID, _id);
		return this;
	}

	/**
	 * id
	 */
	public String getId() {
		return get(ID);
	}

	/**
	 * id
	 * 
	 * @return
	 */
	public BaseMongoRecord<R> setId(String id) {
		set(ID, id);
		return this;
	}

	public Date getCreate_time() {
		return get(CREATE_TIME);
	}

	public BaseMongoRecord<R> setCreate_time(Date create_time) {
		set(CREATE_TIME, create_time);
		return this;
	}

	public Date getUpdate_time() {
		return get(UPDATE_TIME);
	}

	public BaseMongoRecord<R> setUpdate_time(Date update_time) {
		set(UPDATE_TIME, update_time);
		return this;
	}

	/**
	 * 数据批量设置
	 */
	public BaseMongoRecord<R> setData(String id, Date create_time, Date update_time) {
		setId(id);
		setCreate_time(create_time);
		setUpdate_time(update_time);
		return this;
	}

}
