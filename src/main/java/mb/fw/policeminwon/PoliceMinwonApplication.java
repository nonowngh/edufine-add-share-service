package mb.fw.policeminwon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import com.mb.indigo2.springsupport.AdaptorConfig;

import lombok.extern.slf4j.Slf4j;
import mb.fw.adaptor.util.AdaptorStarter;
import mb.fw.atb.util.MDCLogging;

@Slf4j
@ImportResource({"classpath:bean.xml"})
@ComponentScan(basePackages = {"mb.fw.policeminwon", "mb.fw.adaptor"})
@SpringBootApplication
	public class PoliceMinwonApplication {

	public static void main(String[] args) throws Exception {
//		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
//        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
//        AdaptorStarter.init();
//        String adaptorName = AdaptorConfig.getInstance().getAdaptorName();
//        log.info("adaptorName: {}", adaptorName);
//        MDCLogging.create("NONE", "NONE", adaptorName);
        SpringApplication.run(PoliceMinwonApplication.class, args);
	}
}
