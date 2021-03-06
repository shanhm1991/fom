package org.spring.fom.support.task.download.helper;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.spring.fom.support.task.download.helper.util.HttpUtil;
import org.spring.fom.support.task.parse.ZipUtil;

/**
 * http的一些默认实现
 * 
 * @author shanhm1991@163.com
 *
 */
public class HttpHelper implements DownloadHelper, DownloadZipHelper {

	@Override
	public InputStream open(String url) throws Exception {
		return HttpUtil.open(url);
	}

	@Override
	public void download(String url, File file) throws Exception {
		HttpUtil.download(url, file);
	}

	@Override
	public int delete(String url) throws Exception {
		return HttpUtil.delete(url);
	}

	@Override
	public String getSourceName(String sourceUri) {
		return new File(sourceUri).getName();
	}

	@Override
	public long zipEntry(String name, String uri, ZipOutputStream zipOutStream) throws Exception {
		return ZipUtil.zipEntry(name, open(uri), zipOutStream);
	}

}
