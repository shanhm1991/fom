package com.fom.context.config;

import org.apache.hadoop.fs.FileSystem;

import com.fom.util.HdfsUtil;
import com.fom.util.XmlUtil;

/**
 * hdfs.master hdfs集群主节点ip:port<br>
 * hdfs.slave  hdfs集群副节点ip:port<br>
 * signal.file 目录是否下载的标记文件
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 */
public class HdfsZipDownloaderConfig extends ZipDownloaderConfig implements IHdfsConfig {

	private String hdfs_master;

	private String hdfs_slave;

	FileSystem fs;

	String signalFile;

	protected HdfsZipDownloaderConfig(String name) {
		super(name);
	}

	@Override
	void load() throws Exception {
		super.load();
		hdfs_master = XmlUtil.getString(element, "hdfs.master", "");
		hdfs_slave = XmlUtil.getString(element, "hdfs.slave", "");
		fs = HdfsUtil.getFileSystem(hdfs_master, hdfs_slave);
		signalFile = XmlUtil.getString(element, "signal.file", "");
	}

	@Override
	boolean isValid() throws Exception {
		if(!super.isValid()){
			return false;
		}
		//...
		return true;
	}

	@Override
	public boolean equals(Object o){
		if(!(o instanceof HdfsZipDownloaderConfig)){
			return false;
		}
		if(o == this){
			return true;
		}

		HdfsZipDownloaderConfig c = (HdfsZipDownloaderConfig)o; 
		if(!super.equals(c)){
			return false;
		}

		return hdfs_master.equals(c.hdfs_master)
				&& hdfs_slave.equals(c.hdfs_slave)
				&& signalFile.equals(c.signalFile);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("\nhdfs.master=" + hdfs_master);
		builder.append("\nhdfs.slave=" + hdfs_slave);
		builder.append("\nsignal.file=" + signalFile);
		return builder.toString();
	}

	@Override
	public final String getSignalFile() {
		return signalFile;
	}

	@Override
	public FileSystem getFs() {
		return fs;
	}

}
