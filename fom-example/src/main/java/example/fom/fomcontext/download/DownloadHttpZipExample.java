package example.fom.fomcontext.download;

import org.eto.fom.context.annotation.FomContext;
import org.eto.fom.context.core.Context;

/**
 * 
 * @author shanhm
 *
 */
@FomContext(remark="下载Http指定url列表并打包", cron="0/30 * * * * ?")
public class DownloadHttpZipExample extends Context {
	
//	private String dest; 
//	
//	public DownloadHttpZipExample(){
//		dest = new File("").getAbsolutePath() 
//				+ File.separator + "download" + File.separator + name;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	protected Set<DownloadZipTask> scheduleBatchTasks() throws Exception {
//		List<String> list = new ArrayList<String>();
//		list.add("http://localhost:4040/fom/index.html");
//		list.add("http://localhost:4040/fom/js/datatables.js");
//		list.add("http://localhost:4040/fom/js/datatables.css");
//		list.add("http://localhost:4040/fom/js/jquery-3.3.1.js");
//		list.add("http://localhost:4040/fom/js/tinybox.js"); 
//		list.add("http://localhost:4040/fom/images/details_close.png");
//		list.add("http://localhost:4040/fom/images/details_open.png");
//		list.add("http://localhost:4040/fom/images/edit.png");
//		list.add("http://localhost:4040/fom/images/exec.png");
//		list.add("http://localhost:4040/fom/images/load.gif");
//		list.add("http://localhost:4040/fom/images/save.png");
//		list.add("http://localhost:4040/fom/images/sort_asc_disabled.png");
//		list.add("http://localhost:4040/fom/images/sort_asc.png");
//		list.add("http://localhost:4040/fom/images/sort_both.png");
//		list.add("http://localhost:4040/fom/images/sort_desc_disabled.png");
//		list.add("http://localhost:4040/fom/images/sort_desc.png");
//		list.add("http://localhost:4040/fom/images/start.png");
//		list.add("http://localhost:4040/fom/images/stop.png");
//		
//		Set<DownloadZipTask> tasks = new HashSet<>();
//		tasks.add(new DownloadZipTask(list, "httpTest", dest, 10, 1024 * 1024, false, new HttpHelper()));
//		return tasks;
//	}

}