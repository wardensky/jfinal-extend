package com.jfinal.ext.plugin.monogodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;

public class WimsRecord extends Record{

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public <T extends WimsRecord> List<T> getList(String column, Class<T> clazz) {
		List<T> list = new ArrayList<>();
		List<Map<String, Object>> dbList = (List<Map<String, Object>>) getColumns().get(column);
		if (dbList != null && !dbList.isEmpty()) {
			for (Map<String, Object> map : dbList) {
				T record = null;
				try {
					record = clazz.newInstance();
					record.setColumns(map);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					list.add(record);
				}
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T extends WimsRecord> List<T> getList(String column, Class<T> clazz, Object defaultValue) {
		List<T> list = new ArrayList<>();
		List<Map<String, Object>> dbList = (List<Map<String, Object>>) getColumns().get(column);
		if (dbList != null && !dbList.isEmpty()) {
			for (Map<String, Object> map : dbList) {
				T record = null;
				try {
					record = clazz.newInstance();
					record.setColumns(map);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					list.add(record);
				}
			}
		} else {
			list = (List<T>) defaultValue;
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T extends WimsRecord> T getMap(String column, Class<T> clazz) {
		T record = null;
		Map<String, Object> map = (Map<String, Object>) getColumns().get(column);
		if (map != null && !map.isEmpty()) {
			try {
				record = clazz.newInstance();
				record.setColumns(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return record;
	}

	@SuppressWarnings("unchecked")
	public <T extends WimsRecord> T getMap(String column, Class<T> clazz, Object defaultValue) {
		T record = null;
		Map<String, Object> map = (Map<String, Object>) getColumns().get(column);
		if (map != null && !map.isEmpty()) {
			try {
				record = clazz.newInstance();
				record.setColumns(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			record = (T) defaultValue;
		}
		return record;
	}

	public Record setList(String column, List<? extends WimsRecord> value) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		if (value != null && !value.isEmpty()) {
			for (WimsRecord r : value) {
				list.add(r.getColumns());
			}
		}
		return super.set(column, list);
	}

	public Record setMap(String column, WimsRecord value) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (value != null) {
			map = value.getColumns();
		}
		return super.set(column, map);
	}

}
