package com.examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fom.pool.handler.JdbcHandler;
import com.fom.task.TextZipParseTask;
import com.fom.task.reader.Reader;
import com.fom.task.reader.RowData;
import com.fom.task.reader.TextReader;
import com.fom.util.PatternUtil;

/**
 * 
 * @author shanhm
 *
 */
public class ImportOracleExample2Task extends TextZipParseTask<Map<String, Object>> {

	private static final String POOL = "example_oracle";

	private static final String SQL = 
			"insert into demo(id,name,source,filetype,importway) "
					+ "values (#id#,#name#,#source#,#fileType#,#importWay#)";

	private final String pattern;

	public ImportOracleExample2Task(String sourceUri, int batch, String pattern) {
		super(sourceUri, batch); 
		this.pattern = pattern;
	}

	@Override
	public Reader getReader(String sourceUri) throws Exception {
		return new TextReader(sourceUri, "#");
	}

	@Override
	public List<Map<String, Object>> parseRowData(RowData rowData, long batchTime) throws Exception {
		List<String> columns = rowData.getColumnList();
		Map<String,Object> map = new HashMap<>();
		map.put("id", columns.get(0));
		map.put("name", columns.get(1)); 
		map.put("source", "local");
		map.put("fileType", "zip(txt)");
		map.put("importWay", "pool");
		return Arrays.asList(map);
	}

	@Override
	public void batchProcess(List<Map<String, Object>> lineDatas, long batchTime) throws Exception {
		JdbcHandler.handler.batchExecute(POOL, SQL, lineDatas);
		log.info("处理数据入库:" + lineDatas.size());
	}
	
	@Override
	public boolean matchEntryName(String entryName) {
		return PatternUtil.match(pattern, entryName);
	}
}