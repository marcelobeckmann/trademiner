
package com.rapidminer.deployment.client.wsimport;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.rapidminer.deployment.client.wsimport package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetBookmarkedProducts_QNAME = new QName("http://ws.update.deployment.rapid_i.com/", "getBookmarkedProducts");
    private final static QName _GetBookmarkedProductsResponse_QNAME = new QName("http://ws.update.deployment.rapid_i.com/", "getBookmarkedProductsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.rapidminer.deployment.client.wsimport
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetBookmarkedProducts }
     * 
     */
    public GetBookmarkedProducts createGetBookmarkedProducts() {
        return new GetBookmarkedProducts();
    }

    /**
     * Create an instance of {@link GetBookmarkedProductsResponse }
     * 
     */
    public GetBookmarkedProductsResponse createGetBookmarkedProductsResponse() {
        return new GetBookmarkedProductsResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBookmarkedProducts }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.update.deployment.rapid_i.com/", name = "getBookmarkedProducts")
    public JAXBElement<GetBookmarkedProducts> createGetBookmarkedProducts(GetBookmarkedProducts value) {
        return new JAXBElement<GetBookmarkedProducts>(_GetBookmarkedProducts_QNAME, GetBookmarkedProducts.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBookmarkedProductsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.update.deployment.rapid_i.com/", name = "getBookmarkedProductsResponse")
    public JAXBElement<GetBookmarkedProductsResponse> createGetBookmarkedProductsResponse(GetBookmarkedProductsResponse value) {
        return new JAXBElement<GetBookmarkedProductsResponse>(_GetBookmarkedProductsResponse_QNAME, GetBookmarkedProductsResponse.class, null, value);
    }

}
