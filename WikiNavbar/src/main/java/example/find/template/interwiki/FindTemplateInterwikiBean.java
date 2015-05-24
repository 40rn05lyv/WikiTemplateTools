package example.find.template.interwiki;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class FindTemplateInterwikiBean {
    
    Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    Map<String, Multiset<String>> foreignLangToForeignTemplateCandidatesMap = new HashMap<String, Multiset<String>>();
    private String pageLang;
    private String templateTitle;

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
            templateMap = TreeMultiset.create();
            foreignLangToForeignTemplateCandidatesMap.put(foreignLang, templateMap);
        }
        templateMap.add(foreignTemplateCandidate);
    }

    public Set<String> getForeignLangs() {
        return foreignLangToForeignArticlesMap.keySet();
    }

    public Set<String> getForeignArticles(String lang) {
        return foreignLangToForeignArticlesMap.get(lang);
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

    public String getTemplateTitle() {
        return templateTitle;
    }
    
    public Multiset<String> getForeignTemplateCandidates(String foreignLang) {
        Multiset<String> result = foreignLangToForeignTemplateCandidatesMap.get(foreignLang);
        if (result == null) {
            return HashMultiset.create();
        }
        return result;
    }
    
    public Multiset<Pair<String, String>> getForeignTemplateCandidates() {
        Multiset<Pair<String, String>> result = TreeMultiset.create();
        for (String lang: foreignLangToForeignTemplateCandidatesMap.keySet()) {
            Multiset<String> articles = foreignLangToForeignTemplateCandidatesMap.get(lang);
            for (String article: articles.elementSet()) {
                result.add(new ImmutablePair<String, String>(lang, article), articles.count(article));
            }
        }
        return result;
    }

}
