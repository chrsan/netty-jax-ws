/**
 * Copyright (c) 2012, Christer Sandberg
 */
package se.fishtank.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple JAXB annotated POJO.
 *
 * @author Christer Sandberg
 */
@XmlRootElement(name = "echo", namespace = "http://fishtank.se")
@XmlAccessorType(XmlAccessType.FIELD)
public class Echo {

    @XmlAttribute(name = "value", required = true)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
