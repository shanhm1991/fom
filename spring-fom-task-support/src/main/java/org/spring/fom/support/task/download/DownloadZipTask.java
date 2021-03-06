package org.spring.fom.support.task.download;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.spring.fom.support.task.download.helper.DownloadZipHelper;
import org.spring.fom.support.task.parse.IoUtil;
import org.spring.fom.support.task.parse.ZipUtil;
import org.springframework.fom.Task;

/**
 * 根据sourceUri列表下载并打包成zip的任务实现，以zipName作为task的id
 * <br>
 * <br>下载策略：
 * <br>1.检查下载目录以及缓存下载目录是否存在，不存在则创建
 * <br>2.检查缓存目录下是否已经存在zipName的zip文件
 * <br>2.1.如果存在，则检查是否有效
 * <br>2.1.1.如果有效，则判断zip是否已经达到阈值，
 * <br>2.1.1.1.如果达到阈值，则命名成指定格式的名称，然后进行步骤3
 * <br>2.1.1.2.如果未达到阈值，则进行步骤3
 * <br>2.1.2.如果无效，则删除，然后进行步骤3
 * <br>2.2.如果不存在，则进行步骤3
 * <br>3.获取缓存目录下所有zip中已下载的文件列表nameSet，遍历uriList中的sourceUri
 * <br>3.1打开zip文件的输出流，向其中写入下载的sourceuri文件流（忽略nameSet中已存在的），然后判断是否达到阈值
 * <br>3.1如果未达到阈值，则继续
 * <br>3.2如果达到阈值，则重新命名成指定格式的名称，判断是否命名成功
 * <br>3.2.1如果命名成功，则重复3.1步骤
 * <br>3.2.2如果命名失败，但不是最后一次命名(uriList遍历到最后)则重复3.1步骤，否则直接任务失败
 * <br>4.将缓存目录下所有的命名的文件移到目标下载目录中
 * <br>上述任何步骤失败或异常均会使任务提前失败结束
 * <br>重新命名步骤说明：
 * <br>正式重新命名前会决定是否删除源文件，另外可以插入一些自己的操作（比如加入一些自己需要的文件）
 * <br>命名文件使用的名称格式、命名序号的获取以及获取zip的下载文件列表均可以自己实现覆盖
 * 
 * @author shanhm1991@163.com
 *
 */
public class DownloadZipTask extends Task<Boolean> {
	
	public static final float FILE_UNIT = 1024.0f;
	
	public static final int SUCCESS_MIN = 200;

	public static final int SUCCESS_MAX = 207;

	private final DecimalFormat numFormat  = new DecimalFormat("#.###");
	
	private final String downloadCache;
	
	private final DownloadZipHelper helper;

	private final List<String> uriSet;

	private final String destPath;

	private final int zipEntryMax;

	private final long zipSizeMax;

	private final boolean isDelSrc;

	private String cachePath;

	private File zip; 

	//当前已命名的文件的最大序号
	private int index;

	//当前zip中下载的文件名称列表
	private Set<String> currentDownloadFiles;

	/**
	 * @param uriList 资源uri列表
	 * @param zipName 打包zip的名称(不带后缀)
	 * @param destPath 目标下载目录
	 * @param zipEntryMax 打包zip的最大文件数(真实下载的文件)
	 * @param zipSizeMax 打包zip的最大字节数
	 * @param isDelSrc 下载结束是否删除资源文件
	 * @param helper ZipDownloaderHelper下载方法实现
	 */
	public DownloadZipTask(List<String> uriList, String zipName, String destPath, 
			int zipEntryMax, long zipSizeMax, boolean isDelSrc, DownloadZipHelper helper) {
		super(zipName);
		if(uriList == null || uriList.isEmpty() || StringUtils.isBlank(zipName) || StringUtils.isBlank(destPath)
				|| zipEntryMax < 0 || zipSizeMax < 0 || helper == null) {
			throw new IllegalArgumentException();
		}
		this.uriSet = uriList;
		this.destPath = destPath;
		this.zipEntryMax = zipEntryMax;
		this.zipSizeMax = zipSizeMax;
		this.isDelSrc = isDelSrc;
		this.helper = helper;
		
		if(StringUtils.isBlank(System.getProperty("cache.download"))){ 
			downloadCache = "cache/download";
		}else{
			downloadCache = System.getProperty("cache.download");
		}
	}

	@Override
	public boolean beforeExec() throws Exception {
		File dest = new File(destPath);
		if(!dest.exists() && !dest.mkdirs()){
			logger.error("directory create failed: {}", dest); 
			return false;
		}

		if(StringUtils.isBlank(getScheduleName())){
			this.cachePath = downloadCache + File.separator + id;
		}else{
			this.cachePath = downloadCache + File.separator + getScheduleName() + File.separator + id;
		}

		File file = new File(cachePath);
		if(!file.exists() && !file.mkdirs()){
			logger.error("directory create failed: {}", cachePath); 
			return false;
		}

		this.zip = new File(cachePath + File.separator + id + ".zip");
		return true;
	}

	@Override
	public Boolean exec() throws Exception {
		return indexDownloadZip(zip.exists(), false) 
				&& downloadIntoZip() 
				&& indexDownloadZip(false, true);
	}

	@Override
	public void afterExec(boolean isExecSuccess, Boolean content, Throwable e) throws Exception {
		File tempDir = new File(cachePath);
		File[] files = tempDir.listFiles();
		if(ArrayUtils.isEmpty(files)){
			return;
		}
		for(File f : files){
			if(!f.renameTo(new File(destPath + File.separator + f.getName()))){
				logger.error("file move failed: {}", f.getName());
				return;
			}
		}
		if(!tempDir.delete()){
			logger.error("directory delete failed: {}", cachePath);
		}
	}

	private boolean downloadIntoZip() throws Exception { 
		ZipOutputStream zipOutStream = null;
		boolean isStreamClosed = true;
		Set<String> historyDownloadFiles = getHistoryDownloadFiles();
		try{
			for(String uri : uriSet){ 
				if(isStreamClosed){
					zipOutStream = new ZipOutputStream(
							new CheckedOutputStream(new FileOutputStream(zip), new CRC32()));
					isStreamClosed = false; 
				}
				long sTime = System.currentTimeMillis();
				String name = helper.getSourceName(uri);
				if(historyDownloadFiles.contains(name)){
					logger.warn("ignore, file[{}] was already downloaded.", name); 
					continue;
				}

				currentDownloadFiles.add(name); 
				String size = numFormat.format(helper.zipEntry(name, uri, zipOutStream) / FILE_UNIT);
				logger.info("finish download[{}({}KB)], cost={}ms", name, size, System.currentTimeMillis() - sTime);
				if(currentDownloadFiles.size() >= zipEntryMax || zip.length() >= zipSizeMax){
					//流管道关闭，如果继续写文件需要重新打开
					IoUtil.close(zipOutStream);
					isStreamClosed = true; 
					if(!indexDownloadZip(false, false)){
						return false;
					}
				}
			}
		}finally{
			IoUtil.close(zipOutStream);
		}
		return true;
	}

	private Set<String> getHistoryDownloadFiles() throws Exception{ 
		Set<String> historyDownloadFiles = new HashSet<>();
		File tempDir = new File(cachePath);
		File[] array = tempDir.listFiles();
		if(ArrayUtils.isEmpty(array)){
			return historyDownloadFiles;
		}

		for(File file : array){
			historyDownloadFiles.addAll(getZipEntryNames(file));
		}
		return historyDownloadFiles;
	}

	/**
	 * 达到阈值的downloadZip编入序列，即重命名成指定名称
	 * @param isRetry
	 * @param isLast
	 * @return
	 * @throws Exception
	 */
	private boolean indexDownloadZip(boolean isRetry, boolean isLast) throws Exception{ 
		if(!ZipUtil.valid(zip)){ 
			if(zip.exists() && !zip.delete()){
				logger.error("{} was damaged, and delete failed.", zip.getName()); 
				return false;
			}
			currentDownloadFiles = new HashSet<>();
			return true;
		}

		if(isRetry){
			currentDownloadFiles = getZipEntryNames(zip); 
		}
		if(!isLast && currentDownloadFiles.size() < zipEntryMax && zip.length() < zipSizeMax){
			return true;
		}

		if(isDelSrc){ 
			for(String entryName : currentDownloadFiles){
				for(String uri : uriSet){ 
					String uriName = helper.getSourceName(uri); 
					if(entryName.equals(uriName)){
						int code = helper.delete(uri);
						if(code < SUCCESS_MIN || code > SUCCESS_MAX){
							logger.error("delete src file failed: {}", entryName); 
							return false;
						}
						break;
					}
				}
			}
		}

		otherOptions(zip); 

		String destName = getNextName(geNextIndex(), currentDownloadFiles.size());
		File destFile = new File(cachePath + File.separator + destName);
		if(!zip.renameTo(destFile)){ 
			if(!isLast){
				//继续下载下一个文件，然后再次尝试
				return true;
			}else{
				//最后一次命名失败则直接结束，交给下次任务补偿
				logger.error("index zip failed: {}", destName); 
				return false;
			}
		}
		index++;
		currentDownloadFiles.clear();

		String size = numFormat.format(destFile.length() / FILE_UNIT);
		logger.info("index zip: {}({}KB)", destName, size);
		return true;
	}	

	/**
	 * 当打包zip达到阈值（文件数或者字节数）时，会将zip重新命名，命名的名称即调用getNextName获得，
	 * <br>默认名称：zipName_index_entryNum.zip
	 * @param index 序号，即当前命名的zip是第几个
	 * @param entryNum zip中包含的真实下载文件数
	 * @return 命名zip的名称
	 */
	protected String getNextName(int index, int entryNum){
		StringBuilder builder = new StringBuilder(); 
		builder.append(id).append("_");
		builder.append(index).append("_").append(entryNum).append(".zip");
		return builder.toString();
	}

	private int geNextIndex(){
		if(index > 0){
			return index + 1;
		}
		return getCurrentIndex() + 1;
	} 

	/**
	 * 当对zip重新命名即调用getNextName时，需要先获取当前已命名的zip文件的最大序号，
	 * <br>默认遍历当前下载目录下所有的zipName_index_entryNum.zip文件，截取并比较得出最大的index
	 * @return 当前下载目录下已命名的zip文件的最大序号
	 */
	protected int getCurrentIndex() {
		String[] array = new File(cachePath).list();
		if(ArrayUtils.isEmpty(array)){
			return index;
		}
		for(String name : array){
			if(!name.startsWith(id) || name.equals(zip.getName())){
				continue;
			}
			try{
				String n = name.substring(0,name.lastIndexOf('_'));
				n = n.substring(n.lastIndexOf('_') + 1,n.length());
				int i = Integer.parseInt(n);
				if(i > index){
					index = i;
				}
			}catch(Exception e){
				logger.error("", e);
			}
		}
		return index;
	}

	/**
	 * 在打包zip达到阈值之后，重新命名之前，可以对zip做一些附加操作（比如加入额外的文件）
	 * <br>考虑到失败补偿机制，建议在操作之前先检查下是否之前已被操作过
	 * <br>默认不做任何操作
	 * @param zip zip文件
	 * @throws Exception Exception
	 */
	protected void otherOptions(File zip) throws Exception{

	}

	/**
	 * 获取zip中真实下载的文件名称集合（在下载打包过程中，会判断要下载的文件在之前的zip中是否已经存在）
	 * <br>默认返回zip中所有entry的name
	 * @param zip zip文件
	 * @return zip中真实下载的文件名称集合
	 * @throws Exception Exception
	 */
	protected Set<String> getZipEntryNames(File zip) throws Exception {
		return ZipUtil.getEntrySet(zip);
	}
}
