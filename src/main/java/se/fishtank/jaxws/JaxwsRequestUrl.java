/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * Contains URL related methods for a JAX-WS request.
 *
 * @author Christer Sandberg
 */
public final class JaxwsRequestUrl {

    /**
     * The absolute URL up to the context path.
     */
    public final String baseAddress;

    /**
     * The portion of the request URI that groups related server addresses.
     */
    public final String contextPath;

    /**
     * Extra portion of the request URI after the end of the expected address
     * of the service but before the query string or {@code null} if none exists.
     */
    public final String pathInfo;

    /**
     * HTTP query string or {@code null} if none exists.
     */
    public final String queryString;

    /**
     * Whether the request is a HTTPS request or not.
     */
    public final boolean isSecure;

    /**
     * The server name.
     */
    public final String serverName;

    /**
     * The server port.
     */
    public final int serverPort;

    /**
     * Private
     */
    private JaxwsRequestUrl(String baseAddress, String contextPath, String pathInfo, String queryString,
                            boolean isSecure, String serverName, int serverPort) {
        this.baseAddress = baseAddress;
        this.contextPath = contextPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.isSecure = isSecure;
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    /**
     * Create a new instance.
     *
     * @param context The channel handler context for the HTTP request.
     * @param request The HTTP request.
     * @return A new instance.
     */
    public static JaxwsRequestUrl newInstance(ChannelHandlerContext context, HttpRequest request) {
        String uri = request.getUri();

        String contextPath = "";
        String pathInfo = null;
        String queryString = null;
        if (!"/".equals(uri)) {
            String prefix = uri;
            int index = uri.indexOf('?');
            if (index != -1) {
                prefix = uri.substring(0, index);
                if ((index + 1) < uri.length())
                    queryString = uri.substring(index + 1);
            }

            if (!"/".equals(prefix)) {
                contextPath = prefix;
                index = prefix.indexOf('/', 1);
                if (index != -1) {
                    contextPath = prefix.substring(0, index);
                    pathInfo = prefix.substring(index);
                }
            }
        }

        boolean isSecure = context.getPipeline().get(SslHandler.class) != null;

        InetSocketAddress address = (InetSocketAddress) context.getChannel().getLocalAddress();
        String serverName = address.getHostName();
        int serverPort = address.getPort();

        StringBuilder baseAddress = new StringBuilder();
        baseAddress.append(isSecure ? "https" : "http");
        baseAddress.append("://");
        baseAddress.append(serverName);
        baseAddress.append(':');
        baseAddress.append(serverPort);
        baseAddress.append(contextPath);

        return new JaxwsRequestUrl(baseAddress.toString(), contextPath, pathInfo,
                queryString, isSecure, serverName, serverPort);
    }

}
