package org.eto.fom.boot.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.eto.fom.util.pool.PoolManager;

/**
 * 
 * @author shanhm
 *
 */
public class PoolListener implements ServletContextListener{

	private static Logger log = Logger.getLogger(PoolListener.class); 

	public PoolListener(){

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		try{
			File poolXml = new File(context.getRealPath(context.getInitParameter("poolConfigLocation")));
			if(!poolXml.exists()){
				return;
			}
			PoolManager.listen(poolXml);
		}catch(Exception e){
			log.warn("pool init failed", e); 
			return;
		}
	}
}