package com.fom.examples;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fom.context.helper.ParseHelper;
import com.fom.context.reader.Reader;
import com.fom.context.reader.TextReader;
import com.fom.db.handler.EsHandler;

/**
 * 
 * @author shanhm
 *
 */
public class ImportEsExampleHelper implements ParseHelper<Map<String, Object>> {
	
	private static final String POOL = "example_es";
	
	private final String esIndex;
	
	private final String esType;
	
	private final Logger log;

	public ImportEsExampleHelper(String name, String esIndex, String esType) {
		log = Logger.getLogger(name);
		this.esIndex = esIndex;
		this.esType = esType;
	}

	@Override
	public Reader getReader(String sourceUri) throws Exception {
		return new TextReader(sourceUri, "#");
	}

	@Override
	public List<Map<String, Object>> praseLineData(List<String> columns, long batchTime) throws Exception {
		log.info("解析行数据:" + columns);
		Map<String,Object> map = new HashMap<>();
		map.put("ID", columns.get(0));
		map.put("NAME", columns.get(1)); 
		map.put("SOURCE", "local");
		map.put("FILETYPE", "txt");
		map.put("IMPORTWAY", "pool");
		return Arrays.asList(map);
	}
	
	@Override
	public void batchProcessLineData(List<Map<String, Object>> lineDatas, long batchTime) throws Exception {
		Map<String,Map<String,Object>> map = new HashMap<>();
		for(Map<String, Object> m : lineDatas){
			map.put(String.valueOf(m.get("ID")), m);
		}
		EsHandler.handler.bulkInsert(POOL, esIndex, esType, map); 
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
