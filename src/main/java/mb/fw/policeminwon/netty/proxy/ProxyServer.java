package mb.fw.policeminwon.netty.proxy;

import java.util.List;
import java.util.Optional;

import org.springframework.web.reactive.function.client.WebClient;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import mb.fw.policeminwon.netty.proxy.client.AsyncConnectionClient;
import mb.fw.policeminwon.spec.InterfaceInfoList;

@Slf4j
public class ProxyServer {

	private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    private WebClient webClient;
    
	private int bindPort;
    private final List<AsyncConnectionClient> clients;
    private InterfaceInfoList interfaceInfoList;
	
	public ProxyServer(int bindPort, List<AsyncConnectionClient> clients, Optional<WebClient> optionalWebClient, InterfaceInfoList interfaceInfoList) {
		this.bindPort = bindPort;
		this.clients = clients;
		this.webClient = optionalWebClient.orElse(null);
		this.interfaceInfoList = interfaceInfoList;
	}
	
	public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        Thread serverThread = new Thread(() -> {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
//                    	 ch.pipeline().addLast(new mb.fw.net.common.codec.LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4, true));
                         ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO), new ProxyServerHandler(clients, webClient, interfaceInfoList));
                     }
                 });

                ChannelFuture f = b.bind(bindPort).sync();
                log.info("police-minwon-tcp-proxy-server started on port " + bindPort);
                f.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                shutdown();
            }
        });

        serverThread.setName("police-minwon-tcp-proxy-server");
        serverThread.start();
    }
	
    public void shutdown() {
		log.info("Shutdown police-minwon-tcp-proxy-server");
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }

}
