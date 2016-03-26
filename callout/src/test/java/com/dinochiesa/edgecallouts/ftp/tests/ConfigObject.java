package com.dinochiesa.edgecallouts.ftp.tests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

@JacksonXmlRootElement(localName = "JavaCallout")
public class ConfigObject {

    public static ConfigObject readConfig(String filename) throws IOException {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(true);
        XmlMapper xmlMapper = new XmlMapper(module);
        ConfigObject o = (ConfigObject) xmlMapper.readValue(new File(filename), ConfigObject.class);
        return o;
    }

    public Map<String,String> getProperties() {
        Map<String,String> m = new HashMap<String,String>();
        for (Property p : _properties) {
            m.put(p.name, p.value);
        }
        return m;
    }
    
    @JacksonXmlProperty(isAttribute = true)
    public String name;
    
    @JacksonXmlProperty(localName = "DisplayName")
    public String _displayName;

    @JacksonXmlProperty(localName = "Properties")
    public List<Property> _properties;
    
    @JacksonXmlProperty(localName = "ClassName")
    public String _className;
    
    @JacksonXmlProperty(localName = "ResourceURL")
    public String _url;
}

class Property {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JacksonXmlText(value = true)
    public String value;
}
