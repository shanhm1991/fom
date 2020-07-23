package org.eto.fom.example.context.upload;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eto.fom.boot.listener.FomListener;
import org.eto.fom.context.annotation.FomContext;
import org.eto.fom.context.core.Context;
import org.eto.fom.task.updownload.UploadTask;
import org.eto.fom.task.updownload.helper.impl.HttpHelper;

/**
 * 
 * @author shanhm
 *
 */
@FomContext(remark="上传文件到Http服务", stopWithNoCron=true)
public class UploadHttpExample extends Context {

	private static final long serialVersionUID = -6676559884214726673L;

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<UploadTask> scheduleBatch() throws Exception { 
		String path = FomListener.getRealPath("/source") + File.separator + "http.jpg";
		Set<UploadTask> set = new HashSet<>();
		set.add(new UploadTask(path, "http://localhost:4040/fom/", false, new HttpHelper()));
		return set;
	}

}
