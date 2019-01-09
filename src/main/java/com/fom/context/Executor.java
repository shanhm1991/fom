package com.fom.context;

import java.io.File;
import java.text.DecimalFormat;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.fom.context.config.Config;
import com.fom.context.config.ConfigManager;
import com.fom.context.config.IHdfsConfig;
import com.fom.context.exception.WarnException;
import com.fom.context.log.LoggerFactory;

/**
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 * @param <E>
 */
public abstract class Executor<E extends Config> extends Thread {

	E config;

	DecimalFormat numFormat  = new DecimalFormat("#.##");

	protected final Logger log;

	protected final String name;

	protected final String srcPath;

	protected final File srcFile;

	protected double srcSize;

	protected final String srcName;

	protected Executor(String name, String path) { 
		this.name = name;
		this.srcPath = path;
		this.srcFile = new File(path);
		this.srcName = srcFile.getName();
		
		config = getRuntimeConfig();
		if(config == null){
			throw new RuntimeException("任务取消.");
		}
		this.calculatSize();
		this.setName(config.getType() + "[" + srcName + "]");
		this.log = LoggerFactory.getLogger(config.getType() + "." + name);
	}

	private void calculatSize() {
		if(config instanceof IHdfsConfig){
			IHdfsConfig hconf = (IHdfsConfig)config;
			try {
				long len = hconf.getFs().getFileStatus(new Path(srcPath)).getLen();
				srcSize = len / 1024.0; 
				return;
			} catch (Exception e) {
				
			}
		}
		srcSize = srcFile.length() / 1024.0;
	}
	
	/**
	 * 获取最新的config
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final E getRuntimeConfig(){
		return (E)ConfigManager.get(name);
	}

	@Override
	public final void run(){
		config = getRuntimeConfig();
		if(config == null || !config.isRunning()){
			log.info("任务取消."); 
			return;
		}
		Thread.currentThread().setName(config.getType() + "[" + srcName + "]");
		long sTime = System.currentTimeMillis();
		try {
			onStart(config);

			execute(config);

			onComplete(config);
			log.info(config.getTypeName() + "任务结束, 耗时=" + (System.currentTimeMillis() - sTime) + "ms");
		} catch(WarnException e){
			log.warn(config.getTypeName() + "任务错误结束[" + e.getMessage() + "], 耗时=" + (System.currentTimeMillis() - sTime + "ms"));
		} catch (InterruptedException e) {
			//检测点:impoter每次batchProcessLineData之前
			log.warn(config.getTypeName() + "任务中断[" + e.getMessage() + "], 耗时=" + (System.currentTimeMillis() - sTime + "ms"));
		} catch(Throwable e) {
			log.error(config.getTypeName() + "任务异常结束, 耗时=" + (System.currentTimeMillis() - sTime + "ms"), e);
		} finally{
			onFinally();
		}
	}

	/**
	 * 继承自Executor，在任务线程启动时执行的第一个动作，可以完成一些准备操作
	 */
	protected void onStart(E config) throws Exception {

	}

	protected abstract void execute(E config) throws Exception;

	/**
	 * 继承自Executor，在任务线程完成时执行的动作
	 */
	protected void onComplete(E config) throws Exception {

	}

	void onFinally() {

	}

}
