/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.xml.ws.handler.MessageContext;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Represents a JAX-WS connection.
 *
 * @author Christer Sandberg
 */
public class JaxwsConnection extends WSHTTPConnection {

    private static final PropertyMap model;

    static {
        model = parse(JaxwsConnection.class);
    }

    /** The HTTP request for this connection. */
    private final HttpRequest httpRequest;

    /** The HTTP response for this connection. */
    private final HttpResponse httpResponse;

    /** The JAX-WS request URL for this connection. */
    private final JaxwsRequestUrl jaxwsRequestUrl;

    /** The {@link WebServiceContextDelegate} associated with this connection. */
    private final WebServiceContextDelegate webServiceContextDelegate;

    /** HTTP request headers. */
    private Map<String, List<String>> requestHeaders;

    /** HTTP response headers. */
    private Map<String, List<String>> responseHeaders;

    /** Response output stream. */
    private ResponseOutputStream outputStream;

    /**
     * Create a new instance.
     *
     * @param httpRequest HTTP request.
     * @param httpResponse HTTP response.
     * @param jaxwsRequestUrl JAX-WS request URL.
     * @param webServiceContextDelegate Web Service context delegate.
     */
    public JaxwsConnection(HttpRequest httpRequest, HttpResponse httpResponse,
                           JaxwsRequestUrl jaxwsRequestUrl,
                           WebServiceContextDelegate webServiceContextDelegate) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.jaxwsRequestUrl = jaxwsRequestUrl;
        this.webServiceContextDelegate = webServiceContextDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property({ MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS })
    public @NotNull Map<String, List<String>> getRequestHeaders() {
        if (requestHeaders == null)
            initializeRequestHeaders();

        return requestHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Set<String> getRequestHeaderNames() {
        return httpRequest.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRequestHeaderValues(@NotNull String headerName) {
        if (requestHeaders == null)
            initializeRequestHeaders();

        return requestHeaders.get(headerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestHeader(@NotNull String headerName) {
        return httpRequest.getHeader(headerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponseHeaders(@NotNull Map<String, List<String>> headers) {
        responseHeaders = headers;
        if (headers == null)
            return;

        httpResponse.clearHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            if (name.equalsIgnoreCase(HttpHeaders.Names.CONTENT_TYPE) ||
                    name.equalsIgnoreCase(HttpHeaders.Names.CONTENT_LENGTH)) continue;

            for (String value : entry.getValue())
                httpResponse.addHeader(name, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponseHeader(String key, List<String> value) {
        initializeResponseHeaders();
        responseHeaders.put(key, value);
        for (String v : value)
            httpResponse.addHeader(key, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property(MessageContext.HTTP_RESPONSE_HEADERS)
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentTypeResponseHeader(@NotNull String value) {
        setResponseHeader(HttpHeaders.Names.CONTENT_TYPE, Collections.singletonList(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property(MessageContext.HTTP_RESPONSE_CODE)
    public int getStatus() {
        return httpResponse.getStatus().getCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(int status) {
        httpResponse.setStatus(HttpResponseStatus.valueOf(status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull InputStream getInput() throws IOException {
        return new ChannelBufferInputStream(httpRequest.getContent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull OutputStream getOutput() throws IOException {
        if (outputStream == null)
            outputStream = new ResponseOutputStream(httpResponse);

        return outputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull WebServiceContextDelegate getWebServiceContextDelegate() {
        return webServiceContextDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecure() {
        return jaxwsRequestUrl.isSecure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property(MessageContext.HTTP_REQUEST_METHOD)
    public @NotNull String getRequestMethod() {
        return httpRequest.getMethod().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property(MessageContext.QUERY_STRING)
    public String getQueryString() {
        return jaxwsRequestUrl.queryString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Property(MessageContext.PATH_INFO)
    public String getPathInfo() {
        return jaxwsRequestUrl.pathInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestURI() {
        return httpRequest.getUri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getRequestScheme() {
        return jaxwsRequestUrl.isSecure ? "https" : "http";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getServerName() {
        return jaxwsRequestUrl.serverName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getServerPort() {
        return jaxwsRequestUrl.serverPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getContextPath() {
        return jaxwsRequestUrl.contextPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getBaseAddress() {
        return jaxwsRequestUrl.baseAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol() {
        return httpRequest.getProtocolVersion().getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentLengthResponseHeader(int value) {
        setResponseHeader(HttpHeaders.Names.CONTENT_LENGTH, Collections.singletonList(String.valueOf(value)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }

    private void initializeRequestHeaders() {
        Set<String> headerNames = httpRequest.getHeaderNames();
        requestHeaders = new HashMap<String, List<String>>(headerNames.size());
        for (String name : headerNames)
            requestHeaders.put(name, httpRequest.getHeaders(name));
    }

    private void initializeResponseHeaders() {
        if (responseHeaders == null)
            responseHeaders = new HashMap<String, List<String>>();
    }

    static class ResponseOutputStream extends ByteArrayOutputStream {

        final HttpResponse response;

        ResponseOutputStream(HttpResponse response) {
            this.response = response;
        }

        @Override
        public void close() throws IOException {
            response.setContent(ChannelBuffers.wrappedBuffer(toByteArray()));
        }

    }

}
