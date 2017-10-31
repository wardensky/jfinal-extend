package com.jfinal.ext.plugin.monogodb;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.ext.kit.Reflect;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public abstract class BaseMongoUtil<R extends Record> extends WimsRecord {
	private static final long serialVersionUID = 1L;

	public Class<R> entityClass;
	public String collectionName = null;
	
	public static final int PAGESIZE = 15;
	public static final int PAGE_START = 1;
	public static final int FAIL = -1;

	public BaseMongoUtil() {
		this.entityClass = Reflect.getSuperClassGenricType(getClass());
		Collection collection = entityClass.getAnnotation(Collection.class);
		// 集合名称取注解中name名称,若没有则实体名变小写
		if (collection != null && StringUtils.isNotBlank(collection.name())) {
			collectionName = collection.name();
		} else {
			collectionName = entityClass.getSimpleName().toLowerCase();
		}
	}

	/**
	 * 功能: page<br/>
	 */

	public Page<R> page(Integer pageNumber, Integer pageSize) {

		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}

		@SuppressWarnings("unchecked")
		Page<R> page = (Page<R>) MongoKit.paginate(collectionName, pageNumber, pageSize);
		for (R record : page.getList()) {
			ObjectId o = record.get("_id");
			record.set("_id", o.toString());
		}
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	public Page<R> page(Integer pageNumber, Integer pageSize, Map<String, Object> filter, Map<String, Object> like,
			Map<String, Object> sort) {
		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		@SuppressWarnings("unchecked")
		Page<R> page = (Page<R>) MongoKit.paginate(collectionName, pageNumber, pageSize, filter, like, sort);
		for (R record : page.getList()) {
			ObjectId o = record.get("_id");
			record.set("_id", o.toString());
		}
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	public Page<R> page(Integer pageNumber, Integer pageSize, Map<String, Object> filter, Map<String, Object> like,
			Map<String, Object[]> in, Map<String, Object> sort) {
		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		@SuppressWarnings("unchecked")
		Page<R> page = (Page<R>) paginate(collectionName, pageNumber, pageSize, filter, like, in, sort);
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	public Page<R> page(Integer pageNumber, Integer pageSize, Map<String, Object> filter, Map<String, Object> like,
			Map<String, Object[]> in, Map<String, Object> sort, Map<String, Object> timePeriod) {
		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		@SuppressWarnings("unchecked")
		Page<R> page = (Page<R>) paginate(collectionName, pageNumber, pageSize, filter, like, in, sort, timePeriod);
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	public Page<Record> pageRecord(Integer pageNumber, Integer pageSize, Map<String, Object> filter,
			Map<String, Object> like, Map<String, Object> sort) {
		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		Page<Record> page = MongoKit.paginate(collectionName, pageNumber, pageSize, filter, like, sort);
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	/**
	 * 功能: 查询<br/>
	 */
	public R findOne(String _id) {
		if (StringUtils.isEmpty(_id)) {
			return null;
		}
		ObjectId o = new ObjectId(_id);
		DBObject db = MongoKit.getCollection(collectionName).findOne(o);
		return toRecord(db);
	}

	/**
	 * 功能: 查询<br/>
	 */
	public R findOne(String parmName, Object parmValue) {
		Map<String, Object> parm = new HashMap<>();
		parm.put(parmName, parmValue);
		return findOne(parm, null, null);
	}

	/**
	 * 功能: 查询<br/>
	 */
	public List<R> findList(String parmName, Object parmValue) {
		Map<String, Object> parm = new HashMap<>();
		parm.put(parmName, parmValue);
		return findList(parm, null, null);
	}

	/**
	 * 功能: 查询<br/>
	 */
	public R findOne(Map<String, Object> filter, Map<String, Object> like, Map<String, Object> sort) {
		BasicDBObject query = new BasicDBObject();
		buildFilter(filter, query);
		buildLike(like, query);
		DBCursor dbCursor = MongoKit.getCollection(collectionName).find(query);

		sort(sort, dbCursor);
		if (dbCursor.hasNext()) {
			return toRecord(dbCursor.next());
		}
		return null;
	}

	public R findOne(Map<String, Object> filter, Map<String, Object> like, Map<String, Object[]> in,
			Map<String, Object> sort) {
		BasicDBObject query = new BasicDBObject();
		buildFilter(filter, query);
		buildLike(like, query);
		buildIn(in, query);
		DBCursor dbCursor = MongoKit.getCollection(collectionName).find(query);

		sort(sort, dbCursor);
		if (dbCursor.hasNext()) {
			return toRecord(dbCursor.next());
		}
		return null;
	}

	public List<R> findList(Map<String, Object> filter, Map<String, Object> like, Map<String, Object> sort) {
		BasicDBObject query = new BasicDBObject();
		buildFilter(filter, query);
		buildLike(like, query);
		DBCursor dbCursor = MongoKit.getCollection(collectionName).find(query);
		sort(sort, dbCursor);
		List<R> list = new ArrayList<>();
		while (dbCursor.hasNext()) {
			list.add(toRecord(dbCursor.next()));
		}
		return list;
	}

	public List<R> findList(Map<String, Object> filter, Map<String, Object> like, Map<String, Object[]> in,
			Map<String, Object> sort) {
		BasicDBObject query = new BasicDBObject();
		buildFilter(filter, query);
		buildLike(like, query);
		buildIn(in, query);
		DBCursor dbCursor = MongoKit.getCollection(collectionName).find(query);
		sort(sort, dbCursor);
		List<R> list = new ArrayList<>();
		while (dbCursor.hasNext()) {
			list.add(toRecord(dbCursor.next()));
		}
		return list;
	}

	public List<R> query(String queryString) {
		BasicDBObject query = BasicDBObject.parse(queryString);
		return query(query);
	}

	public List<R> query(BasicDBObject query) {
		DBCursor d = MongoKit.getCollection(collectionName).find(query);
		List<R> records = new ArrayList<>();
		while (d.hasNext()) {
			records.add(toRecord(d.next()));
		}
		return records;
	}

	/**
	 * 功能: 查询全部<br/>
	 */
	public List<R> findAll() {
		List<R> list = new ArrayList<>();
		DBCursor cursor = MongoKit.getCollection(collectionName).find();
		if (cursor.hasNext()) {
			Iterator<DBObject> i = cursor.iterator();
			while (i.hasNext()) {
				list.add(toRecord(i.next()));
			}
		}
		return list;
	}

	/**
	 * 功能: 删除<br/>
	 * 返回:受影响的行
	 */
	public int delete(String _id) {
		if (StringUtils.isEmpty(_id)) {
			return FAIL;
		}
		Map<String, Object> map = new HashMap<>();
		ObjectId o = new ObjectId(_id);
		map.put("_id", o);
		return MongoKit.remove(collectionName, map);
	}

	public int delete(String[] ids) {
		if (ids == null || ids.length < 1) {
			return FAIL;
		}
		int result = 0;
		for (String _id : ids) {
			Map<String, Object> map = new HashMap<>();
			ObjectId o = new ObjectId(_id);
			map.put("_id", o);
			result = MongoKit.remove(collectionName, map);
			if (result == FAIL) {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * 功能: 删除<br/>
	 * 返回:受影响的行
	 */
	public int deleteBy(Map<String, Object> map) {
		if (map.containsKey("_id")) {
			ObjectId o = new ObjectId(map.get("_id").toString());
			map.put("_id", o);
		}
		return MongoKit.remove(collectionName, map);
	}

	public int deleteBy(String filed, String value) {
		Map<String, Object> map = new HashMap<>();
		map.put(filed, value);
		return MongoKit.remove(collectionName, map);
	}

	public int deleteBy(String filed, String[] values) {
		if (values == null || values.length < 1) {
			return FAIL;
		}
		int result = 0;
		Map<String, Object> map = new HashMap<>();
		for (String v : values) {
			map.put(filed, v);
			result = MongoKit.remove(collectionName, map);
			if (result == FAIL) {
				return result;
			}
		}
		return result;
	}

	/**
	 * 功能: 增加<br/>
	 * 返回:受影响的行
	 */
	public int add(R record) {
		if (record == null) {
			return FAIL;
		}
		record.set("create_time", new Date());
		record.set("update_time", new Date());
		return MongoKit.save(collectionName, record);
	}

	/**
	 * 功能: 增加<br/>
	 * 返回:受影响的行
	 */
	@SuppressWarnings("unchecked")
	public int add(List<R> listRecord) {
		if (listRecord == null || listRecord.isEmpty()) {
			return FAIL;
		}
		for (R r : listRecord) {
			r.set("create_time", new Date());
			r.set("update_time", new Date());
		}
		return MongoKit.save(collectionName, (List<Record>) listRecord);
	}

	/**
	 * 功能: 更新<br/>
	 * 返回:受影响的行
	 */
	public int update(R record) {
		if (record == null || null != record.get("_id")) {
			return FAIL;
		}
		ObjectId o = new ObjectId(record.get("_id").toString());
		record.set("_id", o);
		record.set("update_time", new Date());
		return MongoKit.save(collectionName, record);
	}

	/**
	 * 功能: 更新<br/>
	 * 返回:受影响的行
	 */
	@SuppressWarnings("unchecked")
	public int update(List<R> listRecord) {
		if (listRecord == null || listRecord.isEmpty()) {
			return FAIL;
		}
		for (R r : listRecord) {
			ObjectId o = new ObjectId(r.get("_id").toString());
			r.set("_id", o);
			r.set("update_time", new Date());
		}
		return MongoKit.save(collectionName, (List<Record>) listRecord);
	}

	/**
	 * 功能: 保存或更新<br/>
	 * 返回:受影响的行
	 */
	@SuppressWarnings("unchecked")
	public int saveOrUpdate(List<R> listRecord) {
		if (listRecord == null || listRecord.isEmpty()) {
			return FAIL;
		}
		for (R r : listRecord) {
			if (null != r.get("_id")) {
				ObjectId o = new ObjectId(r.get("_id").toString());
				r.set("_id", o);
				r.set("update_time", new Date());
			}else {
				r.set("create_time", new Date());
				r.set("update_time", new Date());
			}
		}
		return MongoKit.save(collectionName, (List<Record>) listRecord);
	}

	/**
	 * 功能: 保存或更新<br/>
	 * 返回:受影响的行
	 */
	public int saveOrUpdate(R record) {
		if (record == null) {
			return FAIL;
		}
		if (null != record.get("_id")) {
			ObjectId o = new ObjectId(record.get("_id").toString());
			record.set("_id", o);
			record.set("update_time", new Date());
		}else {
			record.set("create_time", new Date());
			record.set("update_time", new Date());
		}
		return MongoKit.save(collectionName, record);
	}

	/**
	 * 功能:DBObject 转 Record <br/>
	 */
	@SuppressWarnings("unchecked")
	public R toRecord(DBObject db) {
		if (db == null) {
			return null;
		}

		R record = null;
		try {
			record = entityClass.newInstance();
			record.setColumns(db.toMap());
			Object _id = record.get("_id");
			if (null != _id) {
				ObjectId o = new ObjectId(_id.toString());
				record.set("_id", o.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return record;
	}

	public Page<Record> paginate(String collection, int pageNumber, int pageSize, Map<String, Object> filter,
			Map<String, Object> like, Map<String, Object[]> in, Map<String, Object> sort) {
		DBCollection logs = MongoKit.getCollection(collection);
		BasicDBObject conditons = new BasicDBObject();
		buildFilter(filter, conditons);
		buildLike(like, conditons);
		buildIn(in, conditons);
		DBCursor dbCursor = logs.find(conditons);
		page(pageNumber, pageSize, dbCursor);
		sort(sort, dbCursor);
		List<Record> records = new ArrayList<Record>();
		while (dbCursor.hasNext()) {
			records.add(toRecord(dbCursor.next()));
		}
		int totalRow = dbCursor.count();
		if (totalRow <= 0) {
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
		}
		int totalPage = totalRow / pageSize;
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		Page<Record> page = new Page<Record>(records, pageNumber, pageSize, totalPage, totalRow);
		return page;
	}

	public Page<Record> paginate(String collection, int pageNumber, int pageSize, Map<String, Object> filter,
			Map<String, Object> like, Map<String, Object[]> in, Map<String, Object> sort, Map<String, Object> timePeriod) {
		DBCollection logs = MongoKit.getCollection(collection);
		BasicDBObject conditons = new BasicDBObject();
		buildFilter(filter, conditons);
		buildLike(like, conditons);
		buildIn(in, conditons);
		buildTimePeriod(timePeriod, conditons);
		DBCursor dbCursor = logs.find(conditons);
		page(pageNumber, pageSize, dbCursor);
		sort(sort, dbCursor);
		List<Record> records = new ArrayList<Record>();
		while (dbCursor.hasNext()) {
			records.add(toRecord(dbCursor.next()));
		}
		int totalRow = dbCursor.count();
		if (totalRow <= 0) {
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
		}
		int totalPage = totalRow / pageSize;
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		Page<Record> page = new Page<Record>(records, pageNumber, pageSize, totalPage, totalRow);
		return page;
	}

	private void page(int pageNumber, int pageSize, DBCursor dbCursor) {
		dbCursor = dbCursor.skip((pageNumber - 1) * pageSize).limit(pageSize);
	}

	protected void sort(Map<String, Object> sort, DBCursor dbCursor) {
		if (sort != null) {
			DBObject dbo = new BasicDBObject();
			Set<Entry<String, Object>> entrySet = sort.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				Object val = entry.getValue();
				dbo.put(key, "asc".equalsIgnoreCase(val + "") ? 1 : -1);
			}
			dbCursor = dbCursor.sort(dbo);
		}
	}

	protected void buildFilter(Map<String, Object> filter, BasicDBObject conditons) {
		if (filter != null) {
			Set<Entry<String, Object>> entrySet = filter.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				Object val = entry.getValue();
				conditons.put(key, val);
			}

		}
	}

	protected void buildLike(Map<String, Object> like, BasicDBObject conditons) {
		if (like != null) {
			Set<Entry<String, Object>> entrySet = like.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				Object val = entry.getValue();
				conditons.put(key, MongoKit.getLikeStr(val));
			}
		}
	}

	protected void buildIn(Map<String, Object[]> in, BasicDBObject conditons) {
		if (in != null) {
			for (Entry<String, Object[]> entry : in.entrySet()) {
				String key = entry.getKey();
				Object[] array = entry.getValue();
				conditons.put(key, getInArray(array));
			}
		}
	}

	protected void buildTimePeriod(Map<String, Object> timePeriod, BasicDBObject conditons) {
		if (!MapUtils.isEmpty(timePeriod)) {
			BasicDBObject[] basicDBObject = new BasicDBObject[2];
			for (Map.Entry<String,Object> entry : timePeriod.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				BasicDBObject obj = null;
				if (key.indexOf("start") != -1) {
					obj = new BasicDBObject("$gt", value);
					basicDBObject[0] = new BasicDBObject(key, obj);
				}
				if (key.indexOf("end") != -1) {
					obj = new BasicDBObject("$lt", value);
					basicDBObject[1] = new BasicDBObject(key, obj);
				}
			}
			conditons.put("$and", basicDBObject);
		}
	}

	protected static BasicDBObject getInArray(Object[] array) {
		return new BasicDBObject("$in", array);
	}

	public void RecordCopy(R from, R to) {
		for (String key : from.getColumns().keySet()) {
			to.set(key, from.get(key));
		}
	}

	public void RecordCopy(R from, R to, String... ignoreColumns) {
		for (String key : from.getColumns().keySet()) {
			if (ignoreColumns.equals(key)) {
				continue;
			}

			to.set(key, from.get(key));
		}
	}

	/**
	 * 功能: 取指定列去重后的所有值(String类型 ).<br/>
	 * date: 2017年6月2日 下午1:52:23 <br/>
	 *
	 * @author zxguan@wisdombud.com </br>
	 *
	 * @param fieldName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> findDistinct(String fieldName, DBObject filter) {
		return (List<String>) MongoKit.getCollection(collectionName).distinct(fieldName, filter);
	}

	public Page<R> pageDistinct(Integer pageNumber, Integer pageSize, Map<String, Object> sort, Map<String, Object> timePeriod, String... needFileds) {
		if (pageNumber == null) {
			pageNumber = PAGE_START;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		@SuppressWarnings("unchecked")
		Page<R> page = (Page<R>) paginateDistinct(collectionName, pageNumber, pageSize, sort, timePeriod, needFileds);
		if (page.getTotalPage() < 1) {
			page = new Page<>(page.getList(), page.getPageNumber(), page.getPageSize(), page.getTotalPage() + 1,
					page.getTotalRow());
		}
		return page;
	}

	public Page<Record> paginateDistinct(String collection, int pageNumber, int pageSize, Map<String, Object> sort, Map<String, Object> timeFilter, String... needFileds) {
		List<Record> records = this.findDistinct(null, sort, timeFilter, needFileds);
		int totalRow = records.size();
		if (totalRow <= 0) {
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
		}
		int totalPage = totalRow / pageSize;
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		Integer fromIndex = (pageNumber - 1) * pageSize;
		Page<Record> page = new Page<Record>(this.subList(records, fromIndex, fromIndex + pageSize), pageNumber, pageSize, totalPage, totalRow);
		return page;
	}

	/**
	 * 功能: 根据needFileds去重, timeFilter过滤，sort排序.<br/>
	 * date: 2017年6月4日 下午4:06:45 <br/>
	 *
	 * @author zxguan@wisdombud.com </br>
	 *
	 * @param collection
	 * @param sort
	 * @param timeFilter
	 * @param needFileds
	 * @return
	 */
	public List<Record> findDistinct(String collection, Map<String, Object> sort, Map<String, Object> timeFilter, String... needFileds) {
		List<Record> records = Lists.newArrayList();
		DBCollection dbCollection = MongoKit.getCollection(collectionName);
		BasicDBObject matchFields = this.buildMatch(timeFilter);
		BasicDBObject groupFields = this.buildGroup(needFileds);
		BasicDBObject projectFields = this.buildProject(needFileds);
		BasicDBObject sortFields = this.buildSort(sort);
		List<BasicDBObject> dboList = this.buildPipeline(matchFields, groupFields, projectFields, sortFields);
		AggregationOutput output = dbCollection.aggregate(dboList);
		for(Iterator<DBObject> it = output.results().iterator(); it.hasNext();){
            BasicDBObject dbo = (BasicDBObject) it.next();
            dbo.remove(_ID);
            records.add(toRecord(dbo));
        }
		return records;
	}

	public List<BasicDBObject> buildPipeline(BasicDBObject... dbos) {
		if (null == dbos || dbos.length == 0)
			return null;
		List<BasicDBObject> list = Lists.newArrayList();
		for (BasicDBObject basic : dbos) {
			if (null == basic)
				continue;
			list.add(basic);
		}
		return list;
	}

	private List<Record> subList(List<Record> records, Integer fromIndex, Integer toIndex) {
		Integer size = records.size();
		if (null == records || fromIndex > size || fromIndex > toIndex) {
			return Lists.newArrayList();
		}
		if (toIndex > size-1) {
			return records.subList(fromIndex, size);
		} else {
			return records.subList(fromIndex, toIndex);
		}
	}

	private static String MATCH_STR = "$match";
    private static String GROUP_STR = "$group";
    private static String PROJECT_STR = "$project";
    private static String SORT_STR = "$sort";
    private static String _ID = "_id";

	/**
	 * 功能: 聚合函数--构建时间段过滤match.<br/>
	 * date: 2017年6月3日 下午2:01:33 <br/>
	 *
	 * @author zxguan@wisdombud.com </br>
	 *
	 * @return
	 */
	public BasicDBObject buildMatch(Map<String, Object> timePeriod) {
		if (MapUtils.isEmpty(timePeriod)) {
			return null;
		}
		BasicDBObject basicDbo = new BasicDBObject();
		BasicDBObject dbo = new BasicDBObject();
		for (Map.Entry<String,Object> entry : timePeriod.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			BasicDBObject obj = null;
			try {
				/**
				 * 功能：格式化时间格式与mongo中格式一致
				 * mongo中时间格式是由new ISODate() 将字符串转化为Date类型得到的
				 *
				 * */
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date date = sdf.parse(value + "T00:00:00.000Z");
				/**
				 * */

				if (key.indexOf("start") != -1) {
					obj = new BasicDBObject("$gt", date);
				}
				if (key.indexOf("end") != -1) {
					obj = new BasicDBObject("$lt", date);
				}
				dbo.put(key, obj);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		basicDbo.put(MATCH_STR, dbo);
		return basicDbo;
	}

	public String transform2Str(String[] ignored) {
		String str = "";
		if (ArrayUtils.isEmpty(ignored)) {
			return str;
		}
		for (String ignoredField : ignored) {
			str += ignoredField + ", ";
		}
		return str;
	}

	/**
	 * 功能: 构建分组.<br/>
	 * date: 2017年6月3日 下午2:01:33 <br/>
	 *
	 * @author zxguan@wisdombud.com </br>
	 *
	 * @param needFileds		查询的字段
	 * @return
	 */
	private BasicDBObject buildGroup(String[] needFileds) {
		Field[] fileds = entityClass.getDeclaredFields();
		BasicDBObject basicDbo = new BasicDBObject();
		BasicDBObject obj = new BasicDBObject();
		Map<String, String> map = Maps.newHashMap();
		for (Field field : fileds) {
			String str = this.transform2Str(needFileds).toLowerCase();
			String filedName = field.getName().toLowerCase();
			if (str.indexOf(filedName) != -1) {
				map.put(filedName, "$" + filedName);
			}
		}
		obj.put(_ID, map);
		basicDbo.put(GROUP_STR, obj);
		return basicDbo;
	}

	/**
	 * 功能: 构建查询.<br/>
	 * date: 2017年6月3日 下午2:02:34 <br/>
	 *
	 * @author zxguan@wisdombud.com </br>
	 *
	 * @param needFileds		查询的字段
	 * @return
	 */
	public BasicDBObject buildProject(String[] needFileds) {
		Field[] fileds = entityClass.getDeclaredFields();
		BasicDBObject basicDbo = new BasicDBObject();
		Map<String, Object> map = Maps.newHashMap();
		for (Field field : fileds) {
			String str = this.transform2Str(needFileds).toLowerCase();
			String filedName = field.getName().toLowerCase();
			if (str.indexOf(filedName) != -1) {
				map.put(filedName, "$_id." + filedName);
			}
		}
		map.put(_ID, 1);
		basicDbo.put(PROJECT_STR, map);
		return basicDbo;
	}

	public BasicDBObject buildSort(Map<String, Object> sort) {
    	BasicDBObject basicDbo = new BasicDBObject();
    	BasicDBObject dbo = new BasicDBObject();
        if (sort == null) {
        	return dbo;
        }
        Set<Entry<String, Object>> entrySet = sort.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object val = entry.getValue();
            dbo.put(key, "asc".equalsIgnoreCase(val + "") ? 1 : -1);
        }
        basicDbo.put(SORT_STR, dbo);
        return basicDbo;
    }

}
