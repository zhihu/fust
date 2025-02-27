#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.grpc;

import ${package}.business.ServiceConfiguration;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Import(ServiceConfiguration.class)
public class GrpcMain {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(GrpcMain.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = application.run(args);
        GrpcServer server = context.getBean(GrpcServer.class);
        server.start();
    }
}
