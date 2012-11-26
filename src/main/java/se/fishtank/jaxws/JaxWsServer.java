/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * A JAX-WS only server.
 *
 * @author Christer Sandberg
 */
public final class JaxWsServer {

    /** Whether the server is started or not. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Channel group for all channels. */
    private ChannelGroup channels;

    /** Bootstrap instance for this server. */
    private ServerBootstrap bootstrap;

    /**
     * Start the server.
     *
     * @param address Hostname and port.
     * @param mappings {@linkplain JaxwsHandler#JaxwsHandler(java.util.Map) Endpoint mappings.}
     * @return {@code false} if the server is already started, {@code true} otherwise.
     */
    public boolean start(final InetSocketAddress address, final Map<String, Object> mappings) {
        if (running.compareAndSet(false, true)) {
            channels = new DefaultChannelGroup("jax-ws-server");
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory());

            setBootstrapOptions(bootstrap);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                @Override
                public ChannelPipeline getPipeline() throws Exception {
                    JaxwsHandler handler = new JaxwsHandler(channels, mappings);

                    return Channels.pipeline(new HttpRequestDecoder(), new HttpChunkAggregator(65536),
                            new HttpResponseEncoder(), new ChunkedWriteHandler(), handler);
                }
            });

            channels.add(bootstrap.bind(address));
            return true;
        }

        return false;
    }

    /**
     * Stop the server.
     *
     * @return {@code false} if the server is already stopped, {@code true} otherwise.
     */
    public boolean stop() {
        if (running.compareAndSet(true, false)) {
            channels.close().awaitUninterruptibly();
            bootstrap.releaseExternalResources();
        }

        return false;
    }

    /**
     * Set server bootstrap options.
     *
     * @param bootstrap Bootstrap instance for the server.
     */
    private void setBootstrapOptions(ServerBootstrap bootstrap) {
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        // bootstrap.setOption("receiveBufferSize", 128 * 1024);
        // bootstrap.setOption("sendBufferSize", 128 * 1024);
        // bootstrap.setOption("backlog", 16384);
    }

}
