package org.springframework.fom.test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.fom.annotation.EnableFom;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@EnableFom
@ComponentScan("org.springframework.fom.test")
@PropertySource("conf/conf.properties")
@Configuration
public class SimpleConfiguration {

}
