package com.xiaomitool.v2.language;

import com.xiaomitool.v2.gui.raw.RawManager;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.MultiMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Lang {
    private static final String STRING_ELEMNENT_NAME = "string";
    private static final String STRING_ID = "id";
    private static MultiMap<Language, RegionLanguage> regions = new MultiMap<>();
    private static HashMap<String, String> dictionary = new HashMap<>();

    static {
        regions.putSingle(Language.ENGLISH, new RegionLanguage(Language.ENGLISH, "US"));
        regions.putSingle(Language.ITALIAN, new RegionLanguage(Language.ITALIAN, "IT"));
        checkRegions();
    }

    private static void checkRegions() {
        List<Language> toRemove = new ArrayList<>();
        for (Map.Entry<Language, List<RegionLanguage>> entry : regions.entrySet()) {
            List<RegionLanguage> list = entry.getValue();
            List<RegionLanguage> toRemove2 = new ArrayList<>();
            for (RegionLanguage language : list) {
                if (!Files.exists(ResourcesManager.getLangFilePath(language.getXmlCode()))) {
                    toRemove2.add(language);
                }
            }
            for (RegionLanguage l : toRemove2) {
                list.remove(l);
            }
            if (list.size() == 0) {
                toRemove.add(entry.getKey());
            }
        }
        for (Language l : toRemove) {
            regions.remove(l);
        }
    }

    public static String text(String id, Object... params) throws Exception {
        String format = getFormat(id);
        return String.format(format, params);
    }

    private static String getFormat(String id) throws Exception {
        String format = dictionary.get(id);
        if (format == null) {
            throw new Exception("Missing text id:" + id);
        }
        return format;
    }

    public static void saveToXmlFile(Path filepath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("strings");
        doc.appendChild(root);
        for (LRes l : LRes.values()) {
            Element el = doc.createElement(STRING_ELEMNENT_NAME);
            el.setAttribute(STRING_ID, l.getKey());
            el.setTextContent("");
            root.appendChild(el);
        }
        DOMSource source = new DOMSource(doc);
        StreamResult file = new StreamResult(filepath.toFile());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.transform(source, file);
    }

    private static void loadFromInputStream(InputStream inputStream) throws Exception {
        dictionary = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        NodeList els = doc.getElementsByTagName(STRING_ELEMNENT_NAME);
        for (int i = 0; i < els.getLength(); ++i) {
            Node n = els.item(i);
            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            String id = e.getAttribute(STRING_ID);
            String value = e.getTextContent();
            if (id == null || value == null) {
                continue;
            }
            value = value.replaceAll("\\\\n", "\n");
            dictionary.put(id, value);
        }
        inputStream.close();
    }

    private static void loadFromXmlFile(Path filepath) throws Exception {
        FileInputStream inputStream = new FileInputStream(filepath.toFile());
        loadFromInputStream(inputStream);
        Log.info("Loading language from file: " + filepath.toString());
    }

    public static void loadLanguage(String lang) throws Exception {
        loadLanguage(lang, false);
    }

    private static void loadLanguage(String lang, boolean error) throws Exception {
        if (error) {
            loadFromInputStream(RawManager.getInputStream("en_US.xml"));
        } else {
            try {
                loadFromXmlFile(ResourcesManager.getLangFilePath(lang));
            } catch (Exception e) {
                Log.error("Failed to load language: " + lang + ", switching to english");
                loadLanguage(lang, true);
            }
        }
    }

    public static void loadSystemLanguage() throws Exception {
        RegionLanguage language = getSystemRegionLanguage();
        if (language == null) {
            Log.warn("Failed to load system language, switching to English");
            language = new RegionLanguage(Language.ENGLISH, "US");
        }
        loadLanguage(language.getXmlCode());
    }

    public static RegionLanguage getSystemRegionLanguage() {
        Language lang = Language.getFromSystem();
        if (lang == null) {
            return null;
        }
        List<RegionLanguage> rlangs = regions.get(lang);
        if (rlangs == null) {
            return null;
        }
        String region = Locale.getDefault().getCountry();
        if (region == null) {
            return null;
        }
        region = region.toUpperCase();
        for (RegionLanguage rlang : rlangs) {
            if (region.equals(rlang.getRegionCode())) {
                return rlang;
            }
        }
        if (rlangs.size() == 0) {
            return null;
        }
        return rlangs.get(0);
    }

    private enum Language {
        ENGLISH("en", "English"),
        ITALIAN("it", "Italiano");
        private String code, name;

        Language(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Language getFromSystem() {
            String code = Locale.getDefault().getLanguage();
            if (code == null) {
                return null;
            }
            code = code.toLowerCase();
            for (Language l : Language.values()) {
                if (code.equals(l.getCode())) {
                    return l;
                }
            }
            return null;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    private static class RegionLanguage {
        private Language language;
        private String region;
        private String region_name = null;

        public RegionLanguage(Language language, String region) {
            this(language, region, null);
        }

        public RegionLanguage(Language language, String region, String regionName) {
            this.language = language;
            this.region = region.toUpperCase();
            this.region_name = regionName;
        }

        public String getRegionCode() {
            return region;
        }

        public String getRegionName() {
            return region_name;
        }

        public String getLangCode() {
            return this.language.getCode();
        }

        public String getLangName() {
            return this.language.getName();
        }

        public String getXmlCode() {
            return getLangCode() + "_" + getRegionCode();
        }

        @Override
        public String toString() {
            return String.format("%s_%s (%s - %s)", getLangCode(), getRegionCode(), getLangName(), getRegionName());
        }
    }
}
