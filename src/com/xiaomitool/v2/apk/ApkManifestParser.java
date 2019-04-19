package com.xiaomitool.v2.apk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class ApkManifestParser {
    private Document document;


    public void open(InputStream source) throws Exception {
        this.document = this.getDocBuilder().parse(source);
    }

    private DocumentBuilder getDocBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder;
    }

    private Element getRoot(){
        return this.document.getDocumentElement();
    }

    public String getPackageName(){
        Element element = getRoot();
        if (element == null){
            return null;
        }
        return element.getAttribute("package");
    }

}
