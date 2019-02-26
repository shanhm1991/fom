package com.fom.examples;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fom.context.helper.ZipParseHelper;
import com.fom.context.reader.Reader;
import com.fom.context.reader.TextReader;
import com.fom.db.handler.JdbcHandler;
import com.fom.util.PatternUtil;

/**
 * 
 * @author shanhm
 *
 */
public class ImportOracleExample2Helper implements ZipParseHelper<Map<String, Object>> {

	private static final String POOL = "example_oracle";

	private static final String SQL = 
			"insert into demo(id,name,source,filetype,importway) "
					+ "values (#id#,#name#,#source#,#fileType#,#importWay#)";

	private final String pattern;

	private final Logger log;

	public ImportOracleExample2Helper(String name, String pattern) {
		this.pattern = pattern;
		this.log = Logger.getLogger(name);
	}

	@Override
	public Reader getReader(String sourceUri) throws Exception {
		return new TextReader(sourceUri, "#");
	}

	@Override
	public List<Map<String, Object>> praseLineData(List<String> columns, long batchTime) throws Exception {
		log.info("解析行数据:" + columns);
		Map<String,Object> map = new HashMap<>();
		map.put("id", columns.get(0));
		map.put("name", columns.get(1)); 
		map.put("source", "local");
		map.put("fileType", "zip(txt)");
		map.put("importWay", "pool");
		return Arrays.asList(map);
	}

	@Override
	public void batchProcessLineData(List<Map<String, Object>> lineDatas, long batchTime) throws Exception {
		JdbcHandler.handler.batchExecute(POOL, SQL, lineDatas);
		log.info("处理数据入库:" + lineDatas.size());
	}
	
	@Override
	public boolean matchEntryName(String entryName) {
		return PatternUtil.match(pattern, entryName);
	}

	@Override
	public boolean delete(String sourceUri) {
		return new File(sourceUri).delete();
	}

	@Override
	public long getSourceSize(String sourceUri) {
		return new File(sourceUri).length();
	}
	
}
