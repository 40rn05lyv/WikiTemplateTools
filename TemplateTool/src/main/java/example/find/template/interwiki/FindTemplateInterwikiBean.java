package example.find.template.interwiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import example.api.TemplateInterwikiCandidate;
import example.api.TemplateInterwikiStorage;
import example.api.TemplateInterwikiStorage.UnifiedTemplate;

public class FindTemplateInterwikiBean {

    Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    Map<String, Set<Integer>> foreignLangToForeignArticleIdsMap = new HashMap<String, Set<Integer>>();
    Map<String, Multiset<String>> foreignLangToForeignTemplateCandidatesMap = new HashMap<String, Multiset<String>>();
    private String pageLang;
    private String templateTitle;
    private TemplateInterwikiStorage interwikiStorage = new TemplateInterwikiStorage();

    public void addForeignLangAndForeignArticle(String foreignLang, String foreignArticleTitle) {
        Set<String> articles = foreignLangToForeignArticlesMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<String>();
            this.foreignLangToForeignArticlesMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleTitle);
    }

    public void addForeignLangAndForeignArticle(String foreignLang, Integer foreignArticleId) {
        Set<Integer> articles = foreignLangToForeignArticleIdsMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<Integer>();
            this.foreignLangToForeignArticleIdsMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleId);
    }

    public void addForeignLangAndForeignTemplateCandidate(String foreignLang, String foreignTemplateCandidate) {
        Multiset<String> templateMap = foreignLangToForeignTemplateCandidatesMap.get(foreignLang);
        if (templateMap == null) {
            templateMap = HashMultiset.create();
            foreignLangToForeignTemplateCandidatesMap.put(foreignLang, templateMap);
        }
        templateMap.add(foreignTemplateCandidate);
    }

    Map<UnifiedTemplate, TemplateInterwikiCandidate> candidatesMap2 = new HashMap<UnifiedTemplate, TemplateInterwikiCandidate>();

    public void addForeignLangAndForeignTemplateCandidate(String foreignLang, UnifiedTemplate foreignTemplateCandidate) {
        TemplateInterwikiCandidate templateMap = candidatesMap2.get(foreignTemplateCandidate);
        if (templateMap == null) {
            templateMap = new TemplateInterwikiCandidate(foreignTemplateCandidate);
            candidatesMap2.put(foreignTemplateCandidate, templateMap);
        }
        templateMap.add(foreignLang);
    }

    public Collection<TemplateInterwikiCandidate> getCandidates() {
        return candidatesMap2.values();
    }

    public List<TemplateInterwikiCandidate> getCandidatesOrdered() {
        List<TemplateInterwikiCandidate> result = new ArrayList<TemplateInterwikiCandidate>();
        result.addAll(candidatesMap2.values());
        Collections.sort(result, new Comparator<TemplateInterwikiCandidate>() {
            @Override
            public int compare(TemplateInterwikiCandidate o1, TemplateInterwikiCandidate o2) {
                return o2.size().compareTo(o1.size());
            }
        });
        return result;
    }

    public Set<String> getForeignLangs() {
        return foreignLangToForeignArticlesMap.keySet();
    }

    public Set<String> getForeignArticles(String lang) {
        return foreignLangToForeignArticlesMap.get(lang);
    }

    public Set<Integer> getForeignArticleIds(String lang) {
        return foreignLangToForeignArticleIdsMap.get(lang);
    }

    public void setPageLang(String pageLang) {
        this.pageLang = pageLang;
    }

    public void setTemplateTitle(String templateTitle) {
        if (!templateTitle.startsWith("Template:")) {
            templateTitle = "Template:" + templateTitle;
        }
        this.templateTitle = templateTitle;
    }

    public String getPageLang() {
        return pageLang;
    }

    public String getSimpleTitle() {
        if (templateTitle.startsWith("Template:")) {
            return templateTitle.substring("Template:".length());
        }
        return templateTitle;
    }

    public String getTemplateTitle() {
        return templateTitle;
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

    public TemplateInterwikiStorage getInterwikiStorage() {
        return interwikiStorage;
    }

}
