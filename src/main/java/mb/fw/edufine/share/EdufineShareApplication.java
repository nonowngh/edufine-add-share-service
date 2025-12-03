package mb.fw.edufine.share;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mb.indigo2.springsupport.AdaptorConfig;

import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import mb.fw.adaptor.util.AdaptorStarter;
import mb.fw.atb.util.MDCLogging;

@Slf4j
@ImportResource({"classpath:bean.xml"})
@ComponentScan(basePackages = {"mb.fw.edufine", "mb.fw.adaptor"})
@EnableScheduling
@SpringBootApplication
	public class EdufineShareApplication {

	public static void main(String[] args) throws Exception {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        AdaptorStarter.init();
        String adaptorName = AdaptorConfig.getInstance().getAdaptorName();
        log.info("adaptorName: {}", adaptorName);
        MDCLogging.create("NONE", "NONE", adaptorName);
        SpringApplication.run(EdufineShareApplication.class, args);
	}
}
