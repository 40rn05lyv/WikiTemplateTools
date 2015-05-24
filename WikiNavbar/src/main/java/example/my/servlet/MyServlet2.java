package example.my.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

public class MyServlet2 extends HttpServlet {

    private static final long serialVersionUID = -9122125709896866661L;
    static String apiUrl = "http://en.wikipedia.org/w/api.php";
    static String apiUrl1 = "http://";
    static String apiUrl2 = ".wikipedia.org/w/api.php";

    InfoBean2 bean;

    Connection con;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String wiki = null;
        String titleFrom = null;
        String targetLang = null;
        try {
            String langFrom = req.getParameter("langFrom");
            targetLang = req.getParameter("langTo");
            titleFrom = req.getParameter("titleFrom");
            if (titleFrom.startsWith("Template:")) {
                titleFrom = titleFrom.substring("Template:".length());
            }
            String url = "http://" + langFrom + ".wikipedia.org/wiki/Template:" + titleFrom;
            wiki = Unirest.get(url).queryString("action", "raw").asString().getBody();

            HttpResponse<JsonNode> templateArticlesLangs = queryTemplateArticlesLangs(langFrom, "Template:" + titleFrom);
            processTemplateArticlesLangs(templateArticlesLangs, langFrom, titleFrom);

            if (bean.getExisting() != null) {
                for (Entry<String, String> entry : bean.getExisting().entrySet()) {
                    String rep = new String(entry.getValue().getBytes("UTF-8"), "UTF-8");
                    wiki = GeneralHelper.replaceWikiLink(wiki, entry.getKey(), rep);
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        req.setAttribute("text", wiki);
        req.setAttribute("titleToBeTranslated", titleFrom);
        req.setAttribute("targetLang", targetLang);
        req.getRequestDispatcher("/translate.jsp").forward(req, resp);
    }

    private HttpResponse<JsonNode> queryTemplateArticlesLangs(String langFrom, String templateName) throws UnirestException {
        // @formatter:off
        HttpRequest h = Unirest.get(apiUrl1 + langFrom + apiUrl2)
            .queryString("format", "json")
            .queryString("action", "query")
            .queryString("generator", "links")
            .queryString("titles", templateName)
            .queryString("gplnamespace", "0") // namespace of articles
            .queryString("gpllimit", 500)
            .queryString("prop", "langlinks")
            .queryString("lllang", "uk")
            .queryString("lllimit", 500);
        System.out.println(h.getUrl());
        return h.asJson();
        // @formatter:on
    }

    private void processTemplateArticlesLangs(HttpResponse<JsonNode> json, String fromLang, String templateName) throws UnirestException {
        if (json.getBody().getObject() == null) {
            System.out.println("No template articles found");
            return;
        }
        JSONObject queryObject = json.getBody().getObject().getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");
        Map<String, String> articlesWithTranslation = new HashMap<String, String>();
        List<String> articlesWithoutTranslation = new ArrayList<String>();
        for (Object key : pagesObject.keySet()) {
            JSONObject pageObject = pagesObject.getJSONObject((String) key);
            String pageTitle = pageObject.getString("title");
            if (pageObject.has("langlinks")) {
                JSONArray pageLangLinks = pageObject.getJSONArray("langlinks");
                for (int i = 0; i < pageLangLinks.length(); i++) {
                    JSONObject pageLangLink = pageLangLinks.getJSONObject(i);
                    if (pageLangLink.getString("lang").equals("uk")) {
                        String ukrainianName = pageLangLink.getString("*");
                        articlesWithTranslation.put(pageTitle, ukrainianName);
                        break;
                    }
                }
            } else {
                articlesWithoutTranslation.add(pageTitle);
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");
        double percentage = 0;
        if (articlesWithTranslation.isEmpty() && articlesWithoutTranslation.isEmpty()) {
            percentage = 0;
        } else {
            percentage = 100 * (double) articlesWithTranslation.size()
                    / (double) (articlesWithTranslation.size() + articlesWithoutTranslation.size());
        }
        System.out.println(articlesWithTranslation.size() + " article(s) are translated to Ukrainian.");
        System.out.println(articlesWithoutTranslation.size() + " article(s) are not translated to Ukrainian.");
        System.out.println("Percentage: " + df.format(percentage) + "%");
        System.out.println("Pages that needs translation: ");
        for (String articleWithoutTranslation : articlesWithoutTranslation) {
            System.out.println("\t" + articleWithoutTranslation);
        }

        List<String> templateLinks = getLinksFromTemplate(fromLang, templateName);
        Iterator<Entry<String, String>> it = articlesWithTranslation.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (!templateLinks.contains(entry.getKey())) {
                it.remove();
            }
        }
        articlesWithoutTranslation.retainAll(templateLinks);
        if (articlesWithTranslation.isEmpty() && articlesWithoutTranslation.isEmpty()) {
            percentage = 0;
        } else {
            percentage = 100 * (double) articlesWithTranslation.size()
                    / (double) (articlesWithTranslation.size() + articlesWithoutTranslation.size());
        }
        System.out.println(articlesWithTranslation.size() + " article(s) are translated to Ukrainian.");
        System.out.println(articlesWithoutTranslation.size() + " article(s) are not translated to Ukrainian.");
        System.out.println("Percentage: " + df.format(percentage) + "%");

        bean = new InfoBean2(templateName, articlesWithTranslation, articlesWithoutTranslation, percentage, getContext(percentage));
    }

    private String getContext(double number) {
        if (number > 80) {
            return "success";
        }
        if (number > 50) {
            return "warning";
        }
        return "";
    }

    private List<String> getLinksFromTemplate(String fromLang, String templateName) throws UnirestException {
        HttpResponse<JsonNode> json = expandTemplates(fromLang, templateName);
        JSONObject expandtemplatesObject = json.getBody().getObject().getJSONObject("expandtemplates");
        String wikitext = expandtemplatesObject.getString("wikitext");
        return getLinksFromWikiText(wikitext);
    }

    Pattern p1 = Pattern.compile("(\\[\\[.*?\\]\\])");

    private List<String> getLinksFromWikiText(String wikitext) {
        List<String> result = new ArrayList<String>();
        Matcher m = p1.matcher(wikitext);
        while (m.find()) {
            String wikiLink = m.group();
            int sharpIndex = wikiLink.indexOf('#');
            int pipeIndex = wikiLink.indexOf('|');
            int index = pipeIndex;
            if (sharpIndex != -1 && sharpIndex < pipeIndex) {
                index = sharpIndex;
            }
            if (index != -1) {
                String link = wikiLink.substring(2, index).trim();
                System.out.println("Found link: " + link);
                result.add(link);
            } else {
                String link = wikiLink.substring(2, wikiLink.length() - 2).trim();
                System.out.println("Found clean link: " + link);
                result.add(link);
            }
        }
        return result;
    }

    private HttpResponse<JsonNode> expandTemplates(String fromLang, String templateName) throws UnirestException {
        // @formatter:off
        return Unirest.get(apiUrl1 + fromLang + apiUrl2)
            .queryString("format", "json")
            .queryString("action", "expandtemplates")
            .queryString("prop", "wikitext")
            .queryString("text", "{{" + templateName + "}}")
            .asJson();
        // @formatter:on
    }

}
