package org.springframework.fom.test.schedules;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.fom.Result;
import org.springframework.fom.ScheduleContext;
import org.springframework.fom.annotation.FomSchedule;
import org.springframework.fom.test.TestTask;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@FomSchedule(fixedRate = 15, threadCore = 5)
public class BatchFomContextTest extends ScheduleContext<Long> {

	@Override
	public List<TestTask> newSchedulTasks() throws Exception {
		List<TestTask> tasks = new ArrayList<>();
		for(int i = 0;i < 5; i++){
			tasks.add(new TestTask(i));
		} 
		return tasks;
	} 
	
	@Override
	public void onScheduleComplete(long batchTimes, long batchTime, List<Result<Long>> results) throws Exception {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(batchTime);
		logger.info("第{}次在{}提交的任务全部完成", batchTimes, date);
	}
}
