package com.fom.log;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm
 *
 * <br>slf4j将apache的log4j适配成功能更强大的接口，但是没有暴露Logger的一些操作方法
 * <br>这里相当于做了两步操作，先通过apache的log4j创建自定义属性的Logger对象，再将其适配成slf4j的Logger接口
 */
public class SlfLoggerFactory {

	public static Logger getLogger(String name){
		org.apache.log4j.Logger logger = LogManager.exists(name);
		if(logger != null){
			return LoggerFactory.getLogger(name);
		}
		logger = org.apache.log4j.Logger.getLogger(name); 
		logger.setLevel(Level.INFO);  
		logger.setAdditivity(false); 
		logger.removeAllAppenders();
		LoggerAppender appender = new LoggerAppender();
		PatternLayout layout = new PatternLayout();  
		layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss SSS} [%p] %t [%F:%L] %m%n");  
		appender.setLayout(layout); 
		appender.setEncoding("UTF-8");
		appender.setAppend(true);
		if(StringUtils.isBlank(System.getProperty("log.root"))){
			appender.setFile("log" + File.separator + name + ".log");
		}else{
			appender.setFile(System.getProperty("log.root") + File.separator + name + ".log");
		}
		appender.setRolling("false"); 
		appender.activateOptions();
		logger.addAppender(appender);  
		return LoggerFactory.getLogger(name);
	}
}