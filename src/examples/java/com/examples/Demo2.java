package com.examples;

import java.util.HashSet;
import java.util.Set;

import com.fom.context.Context;
import com.fom.context.FomContext;
import com.fom.context.Task;

/**
 * 
 * @author shanhm1991
 *
 */
@FomContext(remark="测试", threadMax=10, stopWithNoCron=true, execOnLoad=false)
public class Demo2 extends Context {

	private static final long serialVersionUID = -838223512003059760L;

	@SuppressWarnings("unchecked")
	@Override
	protected Set<Task<Boolean>> scheduleBatchTasks() throws Exception {
		Set<Task<Boolean>> set = new HashSet<>();
		set.add(new Task<Boolean>("demo2"){
			@Override
			protected Boolean exec() throws Exception {
				while(true){
					try{
						Thread.sleep(50000); 
					}catch(InterruptedException e){
						//ignore
					}
					
				}
			}
			
		});
		return set;
	}

}