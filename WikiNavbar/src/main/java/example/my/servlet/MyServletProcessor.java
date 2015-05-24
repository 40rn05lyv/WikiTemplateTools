package example.my.servlet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

public class MyServletProcessor {
    
    static String apiUrl = "http://en.wikipedia.org/w/api.php";
    static String startTitle = "Template:Navbox";

    ArrayList<InfoBean> list;
    int limit = 10;
    int maxLimit = 500;
    
    String continueParam = null;
    
    public MyServletProcessor() {
    }
    
    public void process(int limit, String queryLinksHereContinue) {
        this.limit = limit; 
        list = new ArrayList<InfoBean>();
        // @formatter:off
        HttpRequest request = Unirest.get(apiUrl)
            .queryString("format", "json")
            .queryString("action", "query")
            .queryString("generator", "transcludedin")
            .queryString("titles", startTitle)
            .queryString("gtinamespace", "10") // namespace of templates
            .queryString("gtishow", "!redirect") // do not include redirects
            .queryString("gtilimit", limit)
            .queryString("prop", "langlinks")
            .queryString("lllang", "uk")
            .queryString("lllimit", maxLimit);
        // @formatter:on
        
        if (queryLinksHereContinue != null) {
            request.queryString("gticontinue", queryLinksHereContinue);
        }

        try {
            processLinksHereResults(request.asJson());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void processLinksHereResults(HttpResponse<JsonNode> json) throws UnirestException {
        JSONObject queryObject = json.getBody().getObject().getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");
        for (Object key : pagesObject.keySet()) {
            JSONObject pageObject = pagesObject.getJSONObject((String) key);
            String englishTemplateName = pageObject.getString("title");
            if (englishTemplateName.endsWith("/doc") || englishTemplateName.endsWith("/sandbox")) {
                System.out.println("============================================================");
                System.out.println("Template " + englishTemplateName + " is not valid.");
                System.out.println("============================================================");
                continue;
            }
            boolean ukrainianTemplateExists = false;
            String ukrainianTemplateName = null;
            if (pageObject.has("langlinks")) {
                JSONArray pageLangLinks = pageObject.getJSONArray("langlinks");
                for (int i = 0; i < pageLangLinks.length(); i++) {
                    JSONObject pageLangLink = pageLangLinks.getJSONObject(i);
                    if (pageLangLink.getString("lang").equals("uk")) {
                        ukrainianTemplateExists = true;
                        ukrainianTemplateName = pageLangLink.getString("*");
                        break;
                    }
                }
            }
            if (ukrainianTemplateExists) {
                System.out.println("Ukrainian template for " + englishTemplateName + " exists: " + ukrainianTemplateName);
            } else {
                System.out.println("Ukrainian template for " + englishTemplateName + " does not exists");
                HttpResponse<JsonNode> templateArticlesLangs = queryTemplateArticlesLangs(englishTemplateName);
                processTemplateArticlesLangs(templateArticlesLangs, englishTemplateName);
            }
        }
        
        JSONObject queryContinueObject = json.getBody().getObject().getJSONObject("query-continue");
        JSONObject linksHereObject = queryContinueObject.getJSONObject("transcludedin");
        continueParam = linksHereObject.getString("gticontinue");
    }

    private HttpResponse<JsonNode> queryTemplateArticlesLangs(String templateName) throws UnirestException {
        // @formatter:off
        return Unirest.get(apiUrl)
            .queryString("format", "json")
            .queryString("action", "query")
            .queryString("generator", "links")
            .queryString("titles", templateName)
            .queryString("gplnamespace", "0") // namespace of articles
            .queryString("gpllimit", maxLimit)
            .queryString("prop", "langlinks")
            .queryString("lllang", "uk")
            .queryString("lllimit", maxLimit)
            .asJson();
        // @formatter:on
    }

    private void processTemplateArticlesLangs(HttpResponse<JsonNode> json, String templateName) throws UnirestException {
        if (json.getBody().getObject() == null) {
            System.out.println("No template articles found");
            return;
        }
        JSONObject queryObject = json.getBody().getObject().getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");
        List<String> articlesWithTranslation = new ArrayList<String>();
        List<String> articlesWithoutTranslation = new ArrayList<String>();
        for (Object key : pagesObject.keySet()) {
            JSONObject pageObject = pagesObject.getJSONObject((String) key);
            String pageTitle = pageObject.getString("title");
            if (pageObject.has("langlinks")) {
                articlesWithTranslation.add(pageTitle);
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

        List<String> templateLinks = getLinksFromTemplate(templateName);
        articlesWithTranslation.retainAll(templateLinks);
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

        list.add(new InfoBean(0, templateName, articlesWithTranslation.size(), articlesWithoutTranslation.size(), percentage,
                getContext(percentage)));
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

    private List<String> getLinksFromTemplate(String templateName) throws UnirestException {
        HttpResponse<JsonNode> json = expandTemplates(templateName);
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

    private HttpResponse<JsonNode> expandTemplates(String templateName) throws UnirestException {
        // @formatter:off
        return Unirest.get(apiUrl)
            .queryString("format", "json")
            .queryString("action", "expandtemplates")
            .queryString("prop", "wikitext")
            .queryString("text", "{{" + templateName + "}}")
            .asJson();
        // @formatter:on
    }

    public ArrayList<InfoBean> getList() {
        return list;
    }

    public String getContinue() {
        return continueParam;
    }
    
}
