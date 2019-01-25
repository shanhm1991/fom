package com.fom.examples;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fom.context.executor.helper.abstractImporterHelper;
import com.fom.context.executor.reader.Reader;
import com.fom.context.executor.reader.TextReader;
import com.fom.examples.bean.ExamplesBean;
import com.fom.examples.dao.ExamplesDao;
import com.fom.util.SpringUtil;

/**
 * 
 * @author shanhm
 * @date 2019年1月24日
 *
 */
public class Example2Helper extends abstractImporterHelper<ExamplesBean> {

	public Example2Helper(String name) {
		super(name);
	}

	@Override
	public Reader getReader(String sourceUri) throws Exception {
		return new TextReader(sourceUri);
	}

	@Override
	public void praseLineData(List<ExamplesBean> lineDatas, String line, long batchTime) throws Exception {
		log.info("解析行数据:" + line);
		if(StringUtils.isBlank(line)){
			return;
		}
		ExamplesBean bean = new ExamplesBean(line);
		bean.setSource("local");
		bean.setFileType("txt/orc");
		bean.setImportWay("mybatis");
		lineDatas.add(bean); 
	}

	@Override
	public void batchProcessIfNotInterrupted(List<ExamplesBean> lineDatas, long batchTime) throws Exception {
		ExamplesDao demoDao = SpringUtil.getBean("mysqlDemoDao", ExamplesDao.class);
		demoDao.batchInsertDemo(lineDatas);
		log.info("处理数据入库:" + lineDatas.size());
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