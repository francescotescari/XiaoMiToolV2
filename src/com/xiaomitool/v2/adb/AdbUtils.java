package com.xiaomitool.v2.adb;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.logging.Log;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdbUtils {
    public static Map<String, Device.Status> parseDevices(List<String> output){
        HashMap<String, Device.Status> result= new HashMap<>();
        Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+)$");
        for (String line : output){
            if (line == null){
                continue;
            }
            line = line.trim();
            Matcher m = pattern.matcher(line);
            if (m.matches()){
                result.put(m.group(1), Device.Status.fromString(m.group(2)));
            }
        }
        return result;
    }
    public static Map<String, Device.Status> parseTrackDevices(String line){
        /*Log.debug("Parsing line: "+line);*/
        int lenght = line.length();
        int offset = 0;
        HashMap<String, Device.Status> result = new HashMap<>();
        Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+)$");
        while (offset < lenght-3){

            String num = line.substring(offset, offset+4);
            /*Log.debug("("+offset+") num: "+num);*/
            int n;
            try {
               n = Integer.parseInt(num, 16);
            } catch (Exception e){
                Log.warn(e.getMessage());
                break;
            }
            offset+=4;
            if (n == 0){
                continue;
            }
            String data = line.substring(offset, Integer.min(lenght,offset+n));
            offset+=n;
            /*Log.debug(data);*/
            Matcher m = pattern.matcher(data);
            if (m.matches()){
                result.put(m.group(1), Device.Status.fromString(m.group(2)));
            }
        }
        return result;
    }

    public static HashMap<String, String> parseGetProp(List<String> propList){
        HashMap<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]\\s*\\:\\s*\\[([^\\]]+)\\]");
        for (String line: propList){
            if (line == null){
                continue;
            }
            line = line.trim();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()){
                map.put(matcher.group(1), matcher.group(2));
                /*Log.debug("Found prop: "+matcher.group(1)+" -> "+matcher.group(2));*/
            }
        }
        return map;
    }

    public static int[] parseWmSize(String text){
        Pattern pattern = Pattern.compile("(\\d{3,4})x(\\d{3,4})");
        Matcher m = pattern.matcher(text);
        if (!m.find()){
            return null;
        }
        try {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            if (y/x < 4 && y/x > 0){
                return new int[]{x,y};
            }
            return null;
        } catch (Exception e){
            return null;
        }
    }

    public static HashMap<String, String> parseFastbootVars(List<String> output){
        Pattern pattern = Pattern.compile("\\s*\\(bootloader\\)\\s+([^\\s]+)\\s*:\\s*(.*)");
        HashMap<String, String> map = new HashMap<>();
        for (String line : output){
            if (line == null){
                continue;
            }
            line = line.trim();
            Matcher m = pattern.matcher(line);
            if (m.matches()){
              map.put(m.group(1),m.group(2));
            }
        }
        return map;
    }
    public static String parseFastbootVar(String var, String output){
        if (output == null || var == null){
            return null;
        }
        Pattern pattern = Pattern.compile("\\s*"+var+"\\s*:\\s*([^\\n]+)");
        Matcher m = pattern.matcher(output);
        if (!m.find()){
            return null;
        }
        return m.group(1).trim();
    }
    public static String parseFastbootOemInfo(List<String> output){
        Pattern pattern = Pattern.compile("Device unlocked:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        for (String line : output){
            Matcher m = pattern.matcher(line);
            if (m.find()){
                String bool = m.group(1);
                /*Log.debugLine();*/
                /*Log.debug(bool);*/
                /*Log.debugLine();*/
                return "true".equals(bool.trim().toLowerCase()) ? "unlocked" : ("false".equals(bool.trim().toLowerCase()) ? "locked" : "unknown");
            }
        }
        return "unknown";
    }
    public static String parseFastbootOemLks(List<String> output){
        Pattern pattern = Pattern.compile("lks\\s*=\\s*(\\d)", Pattern.CASE_INSENSITIVE);
        for (String line : output){
            Matcher m = pattern.matcher(line);
            if (m.find()){
                String bool = m.group(1);
                return "0".equals(bool) ? "unlocked" : "locked";
            }
        }
        return "unknown";
    }
    public static HashMap<String, String> parseFeaturesFile(Document document) throws Exception {
        String STRING_ID = "name";
        Element root = document.getDocumentElement();
        NodeList feats = root.getChildNodes();
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i<feats.getLength(); ++i){
            Node n = feats.item(i);
            if (n == null || n.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            Element e = (Element) n;
            String id = e.getAttribute(STRING_ID);
            String value = e.getTextContent();
            map.put(id,value);
        }
        return map;

    }

    public static HashMap<String, String> parseFeaturesFile(String features) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(features)));
        return parseFeaturesFile(document);
    }
    public static HashMap<String, String> parseFeaturesFile(File features) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(features);
        return parseFeaturesFile(document);
    }
    public static int parseSerialNumber(String sn){
        if (sn.startsWith("0x") || sn.startsWith("0X")){
            return parseSerialNumber(sn.substring(2));
        }
        try {
            byte[] bytes =  Hex.decodeHex(sn.toCharArray());
            return ByteBuffer.wrap(bytes).getInt();
        } catch (Exception e) {
            return 0;
        }
    }
    public static int parseFastbootTokenSerialNumber(String token){
        try {
            byte[] data = Base64.getDecoder().decode(token);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(data.length-4);
            return buffer.getInt();
        } catch (Exception e){
            Log.warn("Failed to parse fastboot token serial number: "+e.getMessage());
            return 0;
        }
    }


}
