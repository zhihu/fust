#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.grpc;

import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class GrpcServer {

    @Resource
    private HelloServiceHandler helloServiceHandler;

    /**
     *  启动grpc服务
     */
    public void start() {
        GrpcServerBuilder.builder(8888)
                .addService(helloServiceHandler)
                .build()
                .start();
    }
}
