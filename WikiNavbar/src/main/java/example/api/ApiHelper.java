package example.api;

import java.util.List;
import java.util.Set;

import net.minidev.json.parser.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import example.find.template.interwiki.utils.JsonUtils;
import example.my.servlet.Page;
import example.my.servlet.Pages;
import example.my.servlet.WikitextHelper;

public class ApiHelper {
    
    private static final int NAMESPACE_ARTICLE = 0;
    private static final int NAMESPACE_TEMPLATE = 10; 
    private static final int MAX_LIMIT = 500;

    private static final String DEFAULT_WIKI = "en";
    static String defaultApiUrl = api(DEFAULT_WIKI);
    
    private static String api(String lang) {
        return "http://" + lang + ".wikipedia.org/w/api.php";
    }

    public static Pages getLinksForPages(Pages pages) throws UnirestException {
        for (Page page : pages.getList()) {
            List<String> pageLinks = getLinksFromTemplate(page.getPageTitle());
            page.setTrueLinks(pageLinks);
        }
        return pages;
    }

    public static List<String> getLinksFromTemplate(String templateName) throws UnirestException {
        HttpResponse<JsonNode> json = expandTemplates(templateName);
        JSONObject expandtemplatesObject = json.getBody().getObject().getJSONObject("expandtemplates");
        String wikitext = expandtemplatesObject.getString("wikitext");
        return WikitextHelper.getLinksFromWikiText(wikitext);
    }

    private static HttpResponse<JsonNode> expandTemplates(String templateName) throws UnirestException {
        // @formatter:off
        return Unirest.get(defaultApiUrl)
            .queryString("format", "json")
            .queryString("action", "expandtemplates")
            .queryString("prop", "wikitext")
            .queryString("text", "{{" + templateName + "}}")
            .asJson();
        // @formatter:on
    }

    public static boolean doesTemplateExist(String pageLang, String pageTitle) throws UnirestException {
        HttpResponse<JsonNode> json = Unirest.get(api(pageLang)).queryString("format", "json").queryString("action", "query")
                .queryString("titles", pageTitle).asJson();
        JSONObject pages = json.getBody().getObject().getJSONObject("query").getJSONObject("pages");
        if (pages.has("-1")) {
            return false;
        }
        for (Object page: pages.keySet()) {
            JSONObject pageObj = pages.getJSONObject((String) page);
            if (pageObj.getInt("ns") == NAMESPACE_TEMPLATE) {
                return true;
            }
        }
        return false;
    }
    
    public static JSONObject findTranscludedInArticlesAndLangLinksFull(String pageLang, String pageTitle) throws UnirestException, ParseException {
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                // @formatter:off
                return Unirest.get(api(pageLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", pageTitle)
                        .queryString("generator", "transcludedin")
                        .queryString("gtilimit", MAX_LIMIT)
                        .queryString("gtinamespace", NAMESPACE_ARTICLE)
                        .queryString("gtishow", "!redirect")
                        .queryString("prop", "langlinks")
                        .queryString("lllimit", MAX_LIMIT);
                // @formatter:on
            }
        });
    }

    private static JSONObject queryToTheEnd(IHttpRequestBuilder requestBuilder) throws ParseException, UnirestException {
        HttpRequest httpRequest = requestBuilder.build();
        System.out.println(httpRequest.getUrl());
        JSONObject resultObj = httpRequest.asJson().getBody().getObject();
        JSONObject lastObj = resultObj;
        while (lastObj.has("query-continue")) {
            HttpRequest request = requestBuilder.build();
            JSONObject queryContinueObj = lastObj.getJSONObject("query-continue");
            for (Object queryContinueElem: queryContinueObj.keySet()) {
                JSONObject continueObj = queryContinueObj.getJSONObject((String) queryContinueElem);
                for (Object continueElem: continueObj.keySet()) {
                    String continueElemStr = (String) continueElem;
                    request.queryString(continueElemStr, continueObj.getString(continueElemStr));
                }
            }
            System.out.println(request.getUrl());
            lastObj = request.asJson().getBody().getObject();
            resultObj.remove("query-continue");
            resultObj = JsonUtils.merge(resultObj, lastObj);
        }
        return resultObj;
    }
    
    public static JSONObject findAllTemplatesWithLangLink(final String article, String articlesLang, String targetLang) throws ParseException, UnirestException {
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                // @formatter:off
                return Unirest.get(api(articlesLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", article)
                        .queryString("generator", "templates")
                        .queryString("gtllimit", MAX_LIMIT)
                        .queryString("gtlnamespace", NAMESPACE_TEMPLATE)
                        .queryString("prop", "langlinks")
                        .queryString("lllang", targetLang)
                        .queryString("lllimit", MAX_LIMIT);
                // @formatter:on
            }
        });
    }

    public static JSONObject findAllTemplatesWithLangLink(final Set<String> articles, String articlesLang, String targetLang) throws ParseException, UnirestException {
        // TODO: more than 50 titles can't be processed
        if (articles.size() > 50) {
            for (String article: articles) {
                articles.remove(article);
                if (articles.size() == 50) {
                    break;
                }
            }
        }
        
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                String titles = StringUtils.join(articles, "|");
                // @formatter:off
                return Unirest.get(api(articlesLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", titles)
                        .queryString("generator", "templates")
                        .queryString("gtllimit", MAX_LIMIT)
                        .queryString("gtlnamespace", NAMESPACE_TEMPLATE)
                        .queryString("prop", "langlinks")
                        .queryString("lllang", targetLang)
                        .queryString("lllimit", MAX_LIMIT);
                // @formatter:on
            }
        });
    }

}
