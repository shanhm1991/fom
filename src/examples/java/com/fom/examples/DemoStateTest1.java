package com.fom.examples;

import java.util.ArrayList;
import java.util.List;

import com.fom.context.Context;
import com.fom.context.Task;
import com.fom.context.FomContext;

/**
 * 
 * @author shanhm1991
 *
 */
@FomContext(remark="状态测试")
public class DemoStateTest1 extends Context {

	private static final long serialVersionUID = -838223512003059760L;

	@Override
	protected List<String> getTaskIdList() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("task1");
		return list;
	}

	@Override
	protected Task createTask(String taskId) throws Exception {
		
		return new Task(taskId){

			@Override
			protected boolean exec() throws Exception {
				
				Thread.sleep(40000);
				
				return true;
			}
			
		};
	}

}
