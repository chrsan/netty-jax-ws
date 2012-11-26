/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Simple Web Service sample.
 *
 * @author Christer Sandberg
 */
@WebService(name = "echo", portName = "echoPort",
        serviceName = "echoService", targetNamespace = "http://fishtank.se")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public class EchoWebService {

    @WebMethod
    @WebResult(name = "echoResult", partName = "echoResult")
    public Echo echo(@WebParam(name = "echoRequest", partName = "echoRequest") Echo echo) {
        String value = echo.getValue();
        echo.setValue("Hello " + value);
        return echo;
    }

}
