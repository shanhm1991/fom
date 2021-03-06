package org.springframework.fom.support.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.fom.Result;
import org.springframework.fom.ScheduleContext;
import org.springframework.fom.ScheduleInfo;
import org.springframework.fom.ScheduleStatistics;
import org.springframework.fom.logging.LogLevel;
import org.springframework.fom.logging.LoggerConfiguration;
import org.springframework.fom.logging.LoggingSystem;
import org.springframework.fom.logging.log4j.Log4jLoggingSystem;
import org.springframework.fom.support.Response;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@Validated
public class FomServiceImpl implements FomService, ApplicationContextAware{
	
	@Value("${spring.fom.config.cache.enable:true}")
	private boolean configCacheEnable;
	
	@Value("${spring.fom.config.cache.path:cache/schedule}")
	private String configCachePath;

	private ApplicationContext applicationContext;

	private final Map<String, ScheduleContext<?>> scheduleMap = new HashMap<>();

	private static LoggingSystem loggingSystem; 

	static{
		try{
			loggingSystem = LoggingSystem.get(FomServiceImpl.class.getClassLoader());
		}catch(IllegalStateException e){
			System.err.println(e.getMessage()); 
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@PostConstruct 
	private void init(){
		String[] scheduleNames = applicationContext.getBeanNamesForType(ScheduleContext.class);
		for(String scheduleName : scheduleNames){
			scheduleMap.put(scheduleName, (ScheduleContext<?>)applicationContext.getBean(scheduleName));
		}
	}

	@Override
	public List<ScheduleInfo> list() {
		List<ScheduleInfo> list = new ArrayList<>(scheduleMap.size());
		for(ScheduleContext<?> schedule : scheduleMap.values()){
			ScheduleInfo scheduleInfo = schedule.getScheduleInfo();
			scheduleInfo.setLoggerLevel(getLoggerLevel(schedule.getScheduleName())); 
			list.add(scheduleInfo);
		}
		return list;
	}

	@Override
	public ScheduleInfo info(String scheduleName) {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName); 
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.getScheduleInfo();
	}

	@Override
	public ScheduleInfo info(Class<?> clazz) {
		if(ScheduleContext.class.isAssignableFrom(clazz)){ 
			ScheduleContext<?> scheduleContext = (ScheduleContext<?>)applicationContext.getBean(clazz);
			Assert.notNull(scheduleContext, "schedule of " + clazz + " not exist.");
			return scheduleContext.getScheduleInfo();
		}

		String[] beanNames = applicationContext.getBeanNamesForType(clazz);
		Assert.isTrue(beanNames != null && beanNames.length == 1, "cannot determine schedule by class:" + clazz); 

		String beanName = beanNames[0];
		ScheduleContext<?> scheduleContext = (ScheduleContext<?>)applicationContext.getBean("$" + beanName);
		Assert.notNull(scheduleContext, "schedule of " + clazz + " not exist.");

		return scheduleContext.getScheduleInfo();
	}

	@Override
	public String getLoggerLevel(String scheduleName) {
		Assert.notNull(loggingSystem, "No suitable logging system located");

		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");

		String loggerName = schedule.getLogger().getName();
		if(loggingSystem instanceof Log4jLoggingSystem){
			Log4jLoggingSystem log4jLoggingSystem = (Log4jLoggingSystem)loggingSystem;
			return log4jLoggingSystem.getLogLevel(loggerName);
		}

		LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(loggerName);
		if(loggerConfiguration != null){
			LogLevel logLevel = loggerConfiguration.getConfiguredLevel();
			if(logLevel == null){
				logLevel = loggerConfiguration.getEffectiveLevel();
			}
			if(logLevel != null){
				return logLevel.name();
			}
			return "NULL";
		}else{
			// 向上找一个最近的父Logger
			List<LoggerConfiguration> list =loggingSystem.getLoggerConfigurations();
			for(LoggerConfiguration logger : list){
				String name = logger.getName();
				if(name.startsWith(loggerName)){
					LogLevel logLevel = logger.getConfiguredLevel();
					if(logLevel == null){
						logLevel = logger.getEffectiveLevel();
					}
					if(logLevel != null){
						return logLevel.name();
					}
				}
			}
			return "NULL";
		}
	}

	@Override
	public void updateloggerLevel(
			@NotBlank(message = "scheduleName cannot be empty.") String scheduleName,
			@NotBlank(message = "levelName cannot be empty.") String levelName) {
		Assert.notNull(loggingSystem, "No suitable logging system located");

		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");

		String loggerName = schedule.getLogger().getName();
		if(loggingSystem instanceof Log4jLoggingSystem){
			Log4jLoggingSystem log4jLoggingSystem = (Log4jLoggingSystem)loggingSystem;
			log4jLoggingSystem.setLogLevel(loggerName, levelName);
			return;
		}

		try{
			LogLevel level = LogLevel.valueOf(levelName);
			loggingSystem.setLogLevel(loggerName, level);
		}catch(IllegalArgumentException e){
			throw new UnsupportedOperationException(levelName + " is not a support LogLevel.");
		}
	}

	@Override
	public Response<Void> start(String scheduleName) {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.scheduleStart();
	}

	@Override
	public Response<Void> shutdown(String scheduleName){
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.scheduleShutdown();
	}

	@Override
	public Response<Void> exec(String scheduleName) {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.scheduleExecNow();
	}
	
	@Override
	public Map<String, String> getWaitingTasks(String scheduleName) { 
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.getWaitingTasks();
	}

	@Override
	public List<Map<String, String>> getActiveTasks(String scheduleName) {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.getActiveTasks();
	}

	@Override
	public Map<String, Object> getSuccessStat(String scheduleName, String statDay) throws ParseException { 
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.getSuccessStat(statDay);
	}

	@Override
	public Map<String, Object> saveStatConf(String scheduleName, String statDay, String statLevel, int saveDay) throws ParseException {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		
		Map<String, Object> successStat = null;
		ScheduleStatistics scheduleStatistics = schedule.getScheduleStatistics();
		if(scheduleStatistics.setLevel(statLevel.split(","))){
			successStat = schedule.getSuccessStat(statDay);
		}
		
		scheduleStatistics.setSaveDay(saveDay); 
		return successStat;
	}
	
	@Override
	public List<Map<String, String>> getFailedStat(String scheduleName){
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		return schedule.getFailedStat();
	}

	@Override
	public void saveConfig(String scheduleName, HashMap<String, Object> map) throws Exception {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		
		schedule.saveConfig(map, true);
		
		if(configCacheEnable){
			File dir = new File(configCachePath);
			if(!dir.exists() && !dir.mkdirs()){
				return;
			}
			
			File cacheFile = new File(dir.getPath() + File.separator + scheduleName + ".cache");
			if(cacheFile.exists()){
				cacheFile.delete();
			}
			
			Map<String, Object> configMap = schedule.getScheduleConfig().getConfMap();
			try(FileOutputStream out = new FileOutputStream(cacheFile);
					ObjectOutputStream oos = new ObjectOutputStream(out)){
				 oos.writeObject(configMap);
			}
		}
	}

	@Override
	public String buildExport(@NotBlank(message = "scheduleName cannot be empty.") String scheduleName) {
		ScheduleContext<?> schedule = scheduleMap.get(scheduleName);
		Assert.notNull(schedule, "schedule names " + scheduleName + " not exist.");
		
		ScheduleStatistics scheduleStatistics = schedule.getScheduleStatistics();
		Map<String, List<Result<?>>> success = scheduleStatistics.copySuccessMap();
		Map<String, List<Result<?>>> faield = scheduleStatistics.copyFaieldMap();
		
		TreeSet<String> daySet = new TreeSet<>();
		daySet.addAll(success.keySet());
		daySet.addAll(faield.keySet());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss SSS");
		StringBuilder builder = new StringBuilder();
		for(String day : daySet){
			List<Result<?>> slist = success.get(day);
			if(slist != null){
				builder.append(day).append(" success:").append("\n");
				for(Result<?> result : slist){
					builder.append(result.getTaskId()).append(", ");
					builder.append("submitTime=").append(dateFormat.format(result.getSubmitTime())).append(", ");
					builder.append("startTime=").append(dateFormat.format(result.getStartTime())).append(", ");
					builder.append("cost=").append(result.getCostTime()).append("ms, ");
					builder.append("result=").append(result.getContent()).append("\n");
				}
				builder.append("\n");
			}
			
			List<Result<?>> flist = faield.get(day);
			if(flist != null){
				builder.append(day).append(" failed:").append("\n");
				for(Result<?> result : flist){
					builder.append(result.getTaskId()).append(", ");
					builder.append("submitTime=").append(dateFormat.format(result.getSubmitTime())).append(", ");
					builder.append("startTime=").append(dateFormat.format(result.getStartTime())).append(", ");
					builder.append("cost=").append(result.getCostTime()).append("ms, ");
					Throwable throwable = result.getThrowable();
					if(throwable == null){
						builder.append("cause=null").append("\n");
					}else{
						Throwable cause = throwable;
						while((cause = throwable.getCause()) != null){
							throwable = cause;
						} 
						
						builder.append("cause=").append(throwable.toString()).append("\n");
						for(StackTraceElement stack : throwable.getStackTrace()){ 
							builder.append(stack).append("\n");
						}
					}
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}
}
