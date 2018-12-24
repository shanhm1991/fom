package com.fom.util.exception;

/**
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 */
public class WarnException extends Exception {
	private static final long serialVersionUID = -7951679678790524526L;

	public WarnException(String message) {
		super(message); 
	}
	
	public WarnException(String message, Exception e) {
		super(message, e); 
	}
}
