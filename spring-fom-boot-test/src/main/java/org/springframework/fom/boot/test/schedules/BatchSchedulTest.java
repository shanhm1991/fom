package org.springframework.fom.boot.test.schedules;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.fom.Result;
import org.springframework.fom.annotation.FomSchedule;
import org.springframework.fom.boot.test.TestTask;
import org.springframework.fom.interceptor.ScheduleCompleter;
import org.springframework.fom.interceptor.ScheduleFactory;
import org.springframework.fom.interceptor.ScheduleTerminator;
import org.springframework.fom.interceptor.TaskTimeoutHandler;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@FomSchedule(cron = "0/30 * * * * ?", threadCore = 4, taskOverTime = 4, cancelTaskOnTimeout = true, remark = "定时批任务测试")
public class BatchSchedulTest implements ScheduleFactory<Long>, ScheduleCompleter<Long>, ScheduleTerminator, TaskTimeoutHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BatchSchedulTest.class);
	
	@Override
	public Collection<TestTask> newSchedulTasks() throws Exception {
		List<TestTask> list = new ArrayList<>();
		for(int i = 1; i <= 10; i++){
			list.add(new TestTask(i));
		}
		return list;
	}
	
	@Override
	public void onScheduleComplete(long schedulTimes, long schedulTime, List<Result<Long>> results) throws Exception {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(schedulTime);
		LOG.info( "第{}次在{}提交的任务全部完成，结果为{}", schedulTimes, date, results);
	}
	
	@Override
	public void onScheduleTerminate(long schedulTimes, long lastTime) {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastTime);
		LOG.info("任务关闭，共执行{}次任务，最后一次执行时间为{}", schedulTimes, date);
	}

	@Override
	public void handleTimeout(String taskId, long costTime) {
		LOG.info("处理超时任务{}，已经耗时{}ms", taskId, costTime); 
	}
}
