package org.wikipedia.templates.find.interwiki.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.parser.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wikipedia.api.http.ApiHelper;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multisets;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FindTemplateInterwikiServlet extends HttpServlet {

    private static final long serialVersionUID = -1893098398192062889L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String pageTitle = req.getParameter("pageTitle");
        String pageLang = req.getParameter("pageLang");
        String langsToSearchParam = req.getParameter("searchLangs");

        // TODO: search, maybe this template already has interwiki!

        List<String> langsToSearch = null;
        if (langsToSearchParam != null) {
            langsToSearch = Arrays.asList(StringUtils.split(langsToSearchParam, "|"));
        }
        boolean limitLangs = langsToSearch != null && langsToSearch.size() > 0;

        FindTemplateInterwikiBean bean = new FindTemplateInterwikiBean(pageLang, pageTitle);

        boolean hasTransclusions = false;
        try {
            hasTransclusions = fillTranscludedInArticlesAndLangLinks(bean);
            if (hasTransclusions) {
                boolean hasFullMatches = false;
                for (String foreignLang : bean.getForeignLangs()) {
                    if (limitLangs && !langsToSearch.contains(foreignLang)) {
                        continue;
                    }
                    if (ApiHelper.doesTemplateExist(foreignLang, pageTitle)) {
                        System.out.println("Page with the same name exists in " + foreignLang + " wikipedia.");
                        hasFullMatches = true;
                        continue;
                    }
                }
                if (!hasFullMatches) {
                    for (String foreignLang : bean.getForeignLangs()) {
                        if (limitLangs && !langsToSearch.contains(foreignLang)) {
                            continue;
                        }
                        fillTemplatesInForeignArticlesWithoutLang(bean, foreignLang);
                    }
                    printTemplateCandidates(bean);
                }
            }
        } catch (UnirestException | ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // TODO: show something when hasFullMatches is true 
        // TODO: show number of transclusion
        List<Triple<String, String, Integer>> list = new ArrayList<Triple<String, String, Integer>>();
        ImmutableMultiset<Pair<String, String>> candidates = Multisets.copyHighestCountFirst(bean.getForeignTemplateCandidates());
        for (Pair<String, String> candidate : candidates.elementSet()) {
            list.add(ImmutableTriple.of(candidate.getKey(), candidate.getValue(), candidates.count(candidate)));
        }
        req.setAttribute("hasTransclusions", hasTransclusions);
        req.setAttribute("list", list);
        req.setAttribute("template", pageTitle);
        req.setAttribute("templateLang", pageLang);
        long end = System.currentTimeMillis();
        System.out.println("FindTemplateInterwikiServlet processed request in " + (end-start) + "ms.");
        req.getRequestDispatcher("/findinterwikihttp.jsp").forward(req, resp);
    }

    public static boolean fillTranscludedInArticlesAndLangLinks(FindTemplateInterwikiBean bean) throws UnirestException, ParseException,
            UnsupportedEncodingException {
        JSONObject fullResultObj = ApiHelper.findTranscludedInArticlesAndLangLinksFull(bean.getPageLang(), bean.getTemplateTitle());
        if (!fullResultObj.has("query")) {
            return false;
        }
        boolean hasInterwiki = false;
        JSONObject pagesObj = fullResultObj.getJSONObject("query").getJSONObject("pages");
        for (Object pagesObjKey : pagesObj.keySet()) {
            JSONObject pageObj = pagesObj.getJSONObject((String) pagesObjKey);
            if (pageObj.has("langlinks")) {
                JSONArray langsArr = pageObj.getJSONArray("langlinks");
                for (int i = 0; i < langsArr.length(); i++) {
                    JSONObject langObj = langsArr.getJSONObject(i);
                    String lang = langObj.getString("lang");
                    String langTitle = langObj.getString("*");
                    langTitle = new String(langTitle.getBytes("UTF-8"), "UTF-8");
                    bean.addForeignLangAndForeignArticle(lang, langTitle);
                    hasInterwiki = true;
                }
            }
        }
        return hasInterwiki;
    }

    // Find all templates without langwiki in pageLang for all foreign articles
    public static void fillTemplatesInForeignArticlesWithoutLang(FindTemplateInterwikiBean bean, String foreignLang) throws ParseException,
            UnirestException, UnsupportedEncodingException {
        Set<String> foreignArticles = bean.getForeignArticles(foreignLang);
        for (String foreignArticle : foreignArticles) {
            JSONObject fullResultObj = ApiHelper.findAllTemplatesWithLangLink(foreignArticle, foreignLang, bean.getPageLang());
            if (fullResultObj.has("query")) {
                JSONObject pagesObj = fullResultObj.getJSONObject("query").getJSONObject("pages");
                for (Object page : pagesObj.keySet()) {
                    JSONObject pageObj = pagesObj.getJSONObject((String) page);
                    if (!pageObj.has("langlinks")) {
                        String foreignTemplateCandidate = pageObj.getString("title");
                        foreignTemplateCandidate = new String(foreignTemplateCandidate.getBytes("UTF-8"), "UTF-8");
                        bean.addForeignLangAndForeignTemplateCandidate(foreignLang, foreignTemplateCandidate);
                    }
                }
            }
        }
    }

    private static void printTemplateCandidates(FindTemplateInterwikiBean bean) {
        ImmutableMultiset<Pair<String, String>> candidates = Multisets.copyHighestCountFirst(bean.getForeignTemplateCandidates());
        for (Pair<String, String> candidate : candidates.elementSet()) {
            System.out.println(candidate.getKey() + " : " + candidate.getValue() + ". " + candidates.count(candidate) + " occurences.");
        }
    }

}
