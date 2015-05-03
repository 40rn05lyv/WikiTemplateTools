package org.wikipedia.templates.find.interwiki.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FindTemplateInterwikiBean {

    private String pageLang;
    private String templateTitle;
    private TemplateInterwikiStorage interwikiStorage = new TemplateInterwikiStorage();
    private Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    private Map<UnifiedTemplate, TemplateInterwikiCandidate> candidatesMap = new HashMap<UnifiedTemplate, TemplateInterwikiCandidate>();

    public FindTemplateInterwikiBean(String pageLang, String templateTitle) {
        this.pageLang = pageLang;
        if (templateTitle.startsWith("Template:")) {
            this.templateTitle = templateTitle.substring("Template:".length());
        } else {
            this.templateTitle = templateTitle;
        }
    }

    public Set<String> getForeignLangs() {
        return foreignLangToForeignArticlesMap.keySet();
    }

    public Set<String> getForeignArticles(String lang) {
        return foreignLangToForeignArticlesMap.get(lang);
    }

    public String getPageLang() {
        return pageLang;
    }

    public String getTitle() {
        return templateTitle;
    }

    public TemplateInterwikiStorage getInterwikiStorage() {
        return interwikiStorage;
    }

    public void addForeignLangAndForeignArticle(String foreignLang, String foreignArticleTitle) {
        Set<String> articles = foreignLangToForeignArticlesMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<String>();
            this.foreignLangToForeignArticlesMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleTitle);
    }

    public void addForeignLangAndForeignTemplateCandidate(String foreignLang, UnifiedTemplate foreignTemplateCandidate) {
        TemplateInterwikiCandidate templateMap = candidatesMap.get(foreignTemplateCandidate);
        if (templateMap == null) {
            templateMap = new TemplateInterwikiCandidate(foreignTemplateCandidate);
            candidatesMap.put(foreignTemplateCandidate, templateMap);
        }
        templateMap.add(foreignLang);
    }

    public List<TemplateInterwikiCandidate> getCandidatesOrdered() {
        List<TemplateInterwikiCandidate> result = new ArrayList<TemplateInterwikiCandidate>();
        result.addAll(candidatesMap.values());
        Collections.sort(result, new Comparator<TemplateInterwikiCandidate>() {
            @Override
            public int compare(TemplateInterwikiCandidate o1, TemplateInterwikiCandidate o2) {
                return o2.size().compareTo(o1.size());
            }
        });
        return result;
    }

}
