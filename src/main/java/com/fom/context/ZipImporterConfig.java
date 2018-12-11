package com.fom.context;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fom.util.XmlUtil;

/**
 * 
 * @author shanhm1991
 *
 */
public class ZipImporterConfig extends ImporterConfig {

	protected ZipImporterConfig(String name) {
		super(name);
	}

	String subReg;

	Pattern subPattern;
	
	@Override
	void load() throws Exception {
		super.load();
		subReg = XmlUtil.getString(element, "src.zip.subPattern", "");
		if(!StringUtils.isBlank(subReg)){
			subPattern = Pattern.compile(subReg);
		}
	}
	
	public final boolean matchZipFile(String fileName){
		if(subPattern == null){
			return true;
		}
		return subPattern.matcher(fileName).find();
	}

}
