package org.wikipedia.templates.find.interwiki.http;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class FindTemplateInterwikiBean {

    Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    Map<String, Set<Integer>> foreignLangToForeignArticleIdsMap = new HashMap<String, Set<Integer>>();
    Map<String, Multiset<String>> foreignLangToForeignTemplateCandidatesMap = new HashMap<String, Multiset<String>>();
    private String pageLang;
    private String templateTitle;

    public FindTemplateInterwikiBean(String pageLang, String templateTitle) {
        this.pageLang = pageLang;
        if (!templateTitle.startsWith("Template:")) {
            this.templateTitle = "Template:" + templateTitle;
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

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void addForeignLangAndForeignArticle(String foreignLang, String foreignArticleTitle) {
        Set<String> articles = foreignLangToForeignArticlesMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<String>();
            this.foreignLangToForeignArticlesMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleTitle);
    }

    public void addForeignLangAndForeignTemplateCandidate(String foreignLang, String foreignTemplateCandidate) {
        Multiset<String> templateMap = foreignLangToForeignTemplateCandidatesMap.get(foreignLang);
        if (templateMap == null) {
            templateMap = HashMultiset.create();
            foreignLangToForeignTemplateCandidatesMap.put(foreignLang, templateMap);
        }
        templateMap.add(foreignTemplateCandidate);
    }

    public Multiset<Pair<String, String>> getForeignTemplateCandidates() {
        Multiset<Pair<String, String>> result = HashMultiset.create();
        for (String lang : foreignLangToForeignTemplateCandidatesMap.keySet()) {
            Multiset<String> articles = foreignLangToForeignTemplateCandidatesMap.get(lang);
            for (String article : articles.elementSet()) {
                result.add(new ImmutablePair<String, String>(lang, article), articles.count(article));
            }
        }
        return result;
    }

}
