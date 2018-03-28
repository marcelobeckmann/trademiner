
package com.rapidminer.deployment.client.wsimport;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "UpdateServiceService", targetNamespace = "http://ws.update.deployment.rapid_i.com/", wsdlLocation = "http://localhost:8080/UpdateServer/UpdateServiceService?wsdl")
public class UpdateServiceService
    extends Service
{

    private final static URL UPDATESERVICESERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/UpdateServer/UpdateServiceService?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        UPDATESERVICESERVICE_WSDL_LOCATION = url;
    }

    public UpdateServiceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public UpdateServiceService() {
        super(UPDATESERVICESERVICE_WSDL_LOCATION, new QName("http://ws.update.deployment.rapid_i.com/", "UpdateServiceService"));
    }

    /**
     * 
     * @return
     *     returns UpdateService
     */
    @WebEndpoint(name = "UpdateServicePort")
    public UpdateService getUpdateServicePort() {
        return (UpdateService)super.getPort(new QName("http://ws.update.deployment.rapid_i.com/", "UpdateServicePort"), UpdateService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns UpdateService
     */
    @WebEndpoint(name = "UpdateServicePort")
    public UpdateService getUpdateServicePort(WebServiceFeature... features) {
        return (UpdateService)super.getPort(new QName("http://ws.update.deployment.rapid_i.com/", "UpdateServicePort"), UpdateService.class, features);
    }

}
