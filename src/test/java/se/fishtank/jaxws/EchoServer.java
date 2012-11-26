/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JAX-WS server sample.
 *
 * @author Christer Sandberg
 */
public class EchoServer {

    public static void main(String[] args) throws Exception {
        EchoWebService echoWebService = new EchoWebService();

        Map<String, Object> mappings = new HashMap<String, Object>(1);
        mappings.put("/echoService", echoWebService);

        JaxWsServer server = new JaxWsServer();
        server.start(new InetSocketAddress("localhost", 4040), mappings);

        System.out.println("WSDL published at http://localhost:4040/echoService?wsdl");
        System.out.println("Press 'Enter' to exit the server...");
        System.in.read();

        server.stop();
    }

}
