/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Config;

import identitycard2.RemoteIssuer.GetCertTask;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nguyenduyy
 */
public class ConfigParser {
    public static final String TAG_ADDRESS = "issuer-address";
    public static final String TAG_FIELDS = "fields";
    public static final String CONFIG_FILE_NAME = "config.xml";
    File file;
    Document document;
    
    public static ConfigParser mInstance = null;
    public static ConfigParser getInstance(){
        if(mInstance == null){
            mInstance = new ConfigParser();
        }
        return mInstance;
                
    }
    private ConfigParser(){
        try {
            URI uri = ConfigParser.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            
            File thisfile  =new File(uri);
            String basePath = thisfile.getParent();
            String p = basePath + "/"+ CONFIG_FILE_NAME;
            file = new File(p);
            
            DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file);
            
        } catch (Exception ex) {
            Logger.getLogger(ConfigParser.class.getName()).log(Level.SEVERE, null, ex);
            file = null;
            document = null;
        }
                
    }
    public GetCertTask getRemoteIssuer(){
        if(document != null){
            NodeList nl = document.getElementsByTagName(TAG_ADDRESS);
            Node ea = nl.item(0);
            String path = ea.getTextContent();
            try {
                URL url = new URL(path);
                GetCertTask ri = new GetCertTask();
                ri.setAddress(url);
                return ri;
            } catch (MalformedURLException ex) {
                Logger.getLogger(ConfigParser.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
        }
        else return null;

    }
    public ArrayList<String> getFields(){
        NodeList nl = document.getElementsByTagName(TAG_FIELDS);
        if(nl == null) return null;
        Node ea = nl.item(0);
        NodeList items =  ea.getChildNodes();
        if(items == null) return null;
        ArrayList<String> al = new ArrayList<>();
        for(int i =0 ; i< items.getLength(); i++){
            if(items.item(i).getNodeName().equals("item"))
            al.add(items.item(i).getTextContent());
        }
        return al;
    }
}
