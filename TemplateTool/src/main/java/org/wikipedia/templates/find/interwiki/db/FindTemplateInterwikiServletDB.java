package org.wikipedia.templates.find.interwiki.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.parser.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.api.db.ConnectionFactory;
import org.wikipedia.api.db.QueryHelper;
import org.wikipedia.templates.find.interwiki.FindTemplateInterwikiBean;
import org.wikipedia.templates.find.interwiki.db.TemplateInterwikiStorage.UnifiedTemplate;

import com.mashape.unirest.http.exceptions.UnirestException;

public class FindTemplateInterwikiServletDB extends HttpServlet {

    private static final long serialVersionUID = -1893098398192062889L;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ConnectionFactory.init();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        ConnectionFactory.stop();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String pageTitle = req.getParameter("pageTitle");
        String pageLang = req.getParameter("pageLang");
        String langsToSearchParam = req.getParameter("searchLangs");

        List<String> langsToSearch = null;
        if (langsToSearchParam != null) {
            langsToSearch = Arrays.asList(StringUtils.split(langsToSearchParam, "|"));
        }
        boolean limitLangs = langsToSearch != null && langsToSearch.size() > 0;

        FindTemplateInterwikiBean bean = new FindTemplateInterwikiBean();
        bean.setPageLang(pageLang);
        bean.setTemplateTitle(pageTitle);

        boolean hasTransclusions = false;
        try {
            hasTransclusions = fillTranscludedInArticlesAndLangLinks(bean);
            if (hasTransclusions) {
                boolean hasFullMatches = false;
                if (isEnglishNameOnly(pageTitle)) {
                    for (String foreignLang : bean.getForeignLangs()) {
                        if (limitLangs && !langsToSearch.contains(foreignLang)) {
                            continue;
                        }
                        if (QueryHelper.doesTemplateExist(foreignLang, pageTitle)) {
                            System.out.println("Page with the same name exists in " + foreignLang + " wikipedia.");
                            hasFullMatches = true;
                            continue;
                        }
                    }
                }
                if (!hasFullMatches) {
                    for (String foreignLang : bean.getForeignLangs()) {
                        if (limitLangs && !langsToSearch.contains(foreignLang)) {
                            continue;
                        }
                        fillTemplatesInForeignArticlesWithoutLang(bean, foreignLang);
                    }
                }
            }
        } catch (UnirestException | ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // TODO: show something when hasFullMatches is true
        // TODO: show number of transclusion
        req.setAttribute("hasTransclusions", hasTransclusions);
        req.setAttribute("candidates", bean.getCandidatesOrdered());
        req.setAttribute("template", pageTitle);
        req.setAttribute("templateLang", pageLang);
        long end = System.currentTimeMillis();
        System.out.println("FindTemplateInterwikiServletDB processed request in " + (end - start) + "ms.");
        req.getRequestDispatcher("/findinterwikidb.jsp").forward(req, resp);
    }

    public static boolean isEnglishNameOnly(String template) {
        return template.matches("[.A-Za-z0-9_-]*");
    }

    public static boolean fillTranscludedInArticlesAndLangLinks(FindTemplateInterwikiBean bean) throws UnirestException, ParseException,
            UnsupportedEncodingException {
        List<Pair<String, String>> transcludedInLangLinks = QueryHelper.findTranscludedInArticlesAndLangLinksFull(bean.getPageLang(),
                bean.getSimpleTitle());
        if (transcludedInLangLinks.isEmpty()) {
            return false;
        }
        for (Pair<String, String> transcludedInLangLink : transcludedInLangLinks) {
            bean.addForeignLangAndForeignArticle(transcludedInLangLink.getLeft(), transcludedInLangLink.getRight());
        }
        return true;
    }

    // Find all templates without langwiki in pageLang for all foreign articles
    public static void fillTemplatesInForeignArticlesWithoutLang(FindTemplateInterwikiBean bean, String foreignLang) throws ParseException,
            UnirestException, UnsupportedEncodingException {
        Set<String> foreignArticles = bean.getForeignArticles(foreignLang);
        String excludeLang = bean.getPageLang();
        for (String foreignArticle : foreignArticles) {
            Set<String> templates = QueryHelper.findAllTemplates(bean.getInterwikiStorage(), foreignLang, foreignArticle);
            for (String template : templates) {
                UnifiedTemplate unifiedTemplate = bean.getInterwikiStorage().findUnifiedTemplate(foreignLang, template);
                if (!unifiedTemplate.has(excludeLang)) {
                    bean.addForeignLangAndForeignTemplateCandidate(foreignLang, unifiedTemplate);
                }
            }
        }
    }

}
