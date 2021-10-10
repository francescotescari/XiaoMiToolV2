package com.xiaomitool.v2.language;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.Pair;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Lang {
  private static final String STRING_ELEMNENT_NAME = "string";
  private static final String STRING_ID = "id";
  private static HashMap<String, LinkedHashMap<String, RegionLanguage>> loadedLanguages =
      new HashMap<>();
  private static HashMap<String, String> dictionary = new HashMap<>();
  private static String langHost = null;

  public static void initOnlineLangs(String host) throws CustomHttpException {
    langHost = host + "/lang";
    String indexPath = langHost + "/index.json";
    JSONArray data = new JSONArray(EasyHttp.get(indexPath).getBody());
    for (int i = 0; i < data.length(); ++i) {
      JSONObject entry = data.getJSONObject(i);
      RegionLanguage rl = RegionLanguage.fromJsonEntry(entry);
      LinkedHashMap<String, RegionLanguage> subMap =
          loadedLanguages.computeIfAbsent(rl.getLangCode(), k -> new LinkedHashMap<>());
      subMap.put(rl.getRegionCode(), rl);
    }
  }

  public static List<Pair<String, String>> getComboChoices() {
    List<Pair<String, String>> result = new ArrayList<>();
    for (LinkedHashMap<String, RegionLanguage> langEntry : loadedLanguages.values()) {
      boolean fullName = langEntry.size() > 1;
      for (RegionLanguage regLang : langEntry.values()) {
        String name = regLang.getLangName();
        if (fullName) {
          name += " (" + regLang.getRegionName() + ")";
        }
        result.add(new Pair<>(regLang.toLocaleCode(), name));
      }
    }
    if (result.isEmpty()) {
      result.add(new Pair<>("en_US", "English"));
    }
    return result;
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
      el.setTextContent(l.toEnglish().replace("\n", "\\n"));
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

  public static void load() {
    String langCode = SettingsUtils.getLanguage();
    if (tryLoadLangCode(langCode)) {
      return;
    }
    RegionLanguage language = getSystemRegionLanguage();
    if (tryLoadRegionLanguage(language)) {
      return;
    }
    if (tryLoadLangCode("en_US")) {
      return;
    }
    Log.warn("Failed to load system or choice lang, switch to english");
    dictionary = null;
  }

  private static void loadLangCode(String langCode) throws Exception {
    String[] parts = langCode.split("_", 2);
    String lan = parts[0];
    String reg = parts.length > 1 ? parts[1] : null;
    RegionLanguage rl = getBestFittingRegionLanguage(lan, reg);
    String rightHash = null;
    if (rl != null) {
      lan = rl.getLangCode();
      reg = rl.getRegionCode();
      langCode = rl.toLocaleCode();
      rightHash = rl.getHash();
    }
    Path localFile = ResourcesManager.getLangFilePath(langCode);
    boolean present = Files.exists(localFile);
    if (rightHash != null) {
      boolean needToDownload = true;
      if (present) {
        String md5 = "";
        try (BufferedInputStream in =
            new BufferedInputStream(new FileInputStream(localFile.toFile()))) {
          md5 = DigestUtils.md5Hex(in);
        }
        needToDownload = !rightHash.equalsIgnoreCase(md5);
      }
      if (needToDownload) {
        downloadLangFile(langCode, localFile);
        present = true;
      }
    }
    if (!present) {
      throw new Exception("Lang file not present and not downloadable");
    }
    loadFromXmlFile(localFile);
  }

  private static void downloadLangFile(String langCode, Path destination) throws Exception {
    Log.info("Downloading lang file: " + langCode);
    EasyResponse response = EasyHttp.get(langHost + "/" + langCode + ".xml");
    if (!response.isAllRight()) {
      throw new Exception("Failed to download the lang file: " + response.getCode());
    }
    try {
      Files.createDirectories(destination.getParent());
    } catch (Exception e) {
    }
    try (FileWriter writer = new FileWriter(destination.toFile())) {
      writer.write(response.getBody());
    }
  }

  private static boolean tryLoadLangCode(String langCode) {
    if (langCode == null) {
      return false;
    }
    try {
      loadLangCode(langCode);
      return true;
    } catch (Exception e) {
      Log.warn("Failed to load language: " + langCode + ": " + e.getMessage());
      return false;
    }
  }

  private static boolean tryLoadRegionLanguage(RegionLanguage language) {
    if (language == null) {
      return false;
    }
    return tryLoadLangCode(language.toLocaleCode());
  }

  public static RegionLanguage getSystemRegionLanguage() {
    Locale locale = getSystemLocale();
    if (locale == null) {
      return null;
    }
    return getBestFittingRegionLanguage(locale.getLanguage(), locale.getCountry());
  }

  private static RegionLanguage getBestFittingRegionLanguage(String lang, String region) {
    LinkedHashMap<String, RegionLanguage> regionsForLang =
        lang == null ? null : loadedLanguages.get(lang.toLowerCase());
    if (regionsForLang == null) {
      return null;
    }
    RegionLanguage res = region == null ? null : regionsForLang.get(region.toUpperCase());
    if (res == null) {
      res = regionsForLang.values().iterator().next();
    }
    return res;
  }

  private static Locale getSystemLocale() {
    return Locale.getDefault();
  }

  private static class RegionLanguage {
    private String lang_code, region_code, lang_name, region_name, mhash;

    public RegionLanguage(
        String lang_code, String region_code, String lang_name, String region_name) {
      this.lang_code = lang_code;
      this.region_code = region_code;
      this.lang_name = lang_name;
      this.region_name = region_name;
    }

    public static RegionLanguage fromJsonEntry(JSONObject object) {
      RegionLanguage res =
          new RegionLanguage(
              object.getString("langCode"),
              object.getString("regCode"),
              object.getString("langName"),
              object.getString("regName"));
      res.mhash = object.optString("hash", null);
      return res;
    }

    public String getRegionCode() {
      return region_code;
    }

    public String getRegionName() {
      return region_name;
    }

    public String getLangCode() {
      return lang_code;
    }

    public String getLangName() {
      return lang_name;
    }

    public String toLocaleCode() {
      return getLangCode() + "_" + getRegionCode();
    }

    public String getHash() {
      return this.mhash;
    }

    public Locale toLocale() {
      return new Locale(this.lang_code, this.region_code);
    }

    @Override
    public String toString() {
      return String.format("%s_%s (%s - %s)", lang_code, region_code, lang_name, region_name);
    }
  }
}
