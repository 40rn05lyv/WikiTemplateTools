package org.wikipedia.api.http;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import net.minidev.json.parser.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wikipedia.api.Constants;

import com.google.common.base.Optional;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

public class ApiHelper {

    private static final String DEFAULT_WIKI = "en";
    static String defaultApiUrl = api(DEFAULT_WIKI);

    private static String api(String lang) {
        return "http://" + lang + ".wikipedia.org/w/api.php";
    }

    public static boolean doesTemplateExist(String pageLang, String pageTitle) throws UnirestException {
        HttpResponse<JsonNode> json = Unirest.get(api(pageLang)).queryString("format", "json").queryString("action", "query")
                .queryString("titles", pageTitle).asJson();
        JSONObject pages = json.getBody().getObject().getJSONObject("query").getJSONObject("pages");
        if (pages.has("-1")) {
            return false;
        }
        for (Object page : pages.keySet()) {
            JSONObject pageObj = pages.getJSONObject((String) page);
            if (pageObj.getInt("ns") == Constants.NAMESPACE_TEMPLATE) {
                return true;
            }
        }
        return false;
    }

    public static JSONObject findTranscludedInArticlesAndLangLinksFull(String pageLang, String pageTitle) throws UnirestException,
            ParseException {
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                // @formatter:off
                return Unirest.get(api(pageLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", pageTitle)
                        .queryString("generator", "transcludedin")
                        .queryString("gtilimit", Constants.MAX_LIMIT)
                        .queryString("gtinamespace", Constants.NAMESPACE_ARTICLE)
                        .queryString("gtishow", "!redirect")
                        .queryString("prop", "langlinks")
                        .queryString("lllimit", Constants.MAX_LIMIT);
                // @formatter:on
            }
        });
    }

    public static JSONObject findTranscludedInArticles(String pageLang, String pageTitle) throws UnirestException, ParseException {
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                // @formatter:off
                return Unirest.get(api(pageLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", pageTitle)
                        .queryString("prop", "transcludedin")
                        .queryString("tilimit", Constants.MAX_LIMIT)
                        .queryString("tinamespace", Constants.NAMESPACE_ARTICLE)
                        .queryString("tishow", "!redirect");
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
            for (Object queryContinueElem : queryContinueObj.keySet()) {
                JSONObject continueObj = queryContinueObj.getJSONObject((String) queryContinueElem);
                for (Object continueElem : continueObj.keySet()) {
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

    public static JSONObject findAllTemplatesWithLangLink(final String article, String articlesLang, String targetLang)
            throws ParseException, UnirestException {
        return queryToTheEnd(new IHttpRequestBuilder() {
            @Override
            public HttpRequest build() {
                // @formatter:off
                return Unirest.get(api(articlesLang))
                        .queryString("format", "json")
                        .queryString("action", "query")
                        .queryString("titles", article)
                        .queryString("generator", "templates")
                        .queryString("gtllimit", Constants.MAX_LIMIT)
                        .queryString("gtlnamespace", Constants.NAMESPACE_TEMPLATE)
                        .queryString("prop", "langlinks")
                        .queryString("lllang", targetLang)
                        .queryString("lllimit", Constants.MAX_LIMIT);
                // @formatter:on
            }
        });
    }

    public static JSONObject findAllTemplatesWithLangLink(final Set<String> articles, String articlesLang, String targetLang)
            throws ParseException, UnirestException {
        // TODO: more than 50 titles can't be processed
        if (articles.size() > 50) {
            for (String article : articles) {
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
                        .queryString("gtllimit", Constants.MAX_LIMIT)
                        .queryString("gtlnamespace", Constants.NAMESPACE_TEMPLATE)
                        .queryString("prop", "langlinks")
                        .queryString("lllang", targetLang)
                        .queryString("lllimit", Constants.MAX_LIMIT);
                // @formatter:on
            }
        });
    }

    public static String findNamespaceName(String lang, int namespaceTemplate) {
        // @formatter:off
        HttpRequest request = Unirest.get(api(lang))
                .queryString("format", "json")
                .queryString("action", "query")
                .queryString("meta", "siteinfo")
                .queryString("siprop", "namespaces");
        // @formatter:on
        String namespaceName = null;
        try {
            JSONObject json = request.asJson().getBody().getObject();
            JSONObject namespacesObj = json.getJSONObject("query").getJSONObject("namespaces");
            String namespaceTemplateStr = String.valueOf(namespaceTemplate);
            if (namespacesObj.has(namespaceTemplateStr)) {
                JSONObject namespaceObj = namespacesObj.getJSONObject(namespaceTemplateStr);
                namespaceName = new String(namespaceObj.getString("*").getBytes("UTF-8"), "UTF-8");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return namespaceName;
    }

    public static Optional<String> expandTemplate(String lang, String template) {
        // @formatter:off
        HttpRequest request = Unirest.get(api(lang))
                .queryString("format", "json")
                .queryString("action", "expandtemplates")
                .queryString("text", "{{" + template + "}}");
        // @formatter:on
        Optional<String> expandedTemplate = Optional.absent();
        try {
            JSONObject json = request.asJson().getBody().getObject();
            if (json != null && json.has("expandtemplates")) {
                JSONObject expandTemplatesObject = json.getJSONObject("expandtemplates");
                if (expandTemplatesObject != null && expandTemplatesObject.has("*")) {
                    expandedTemplate = Optional.of(expandTemplatesObject.getString("*"));
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return expandedTemplate;
    }

}
