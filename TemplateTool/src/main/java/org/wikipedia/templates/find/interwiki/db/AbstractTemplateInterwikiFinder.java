package org.wikipedia.templates.find.interwiki.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wikipedia.api.Constants;
import org.wikipedia.api.PageInterwikiStorage;
import org.wikipedia.api.PageUtils;
import org.wikipedia.api.UnifiedPage;

public abstract class AbstractTemplateInterwikiFinder implements ITemplateInterwikiFinder {

    protected String templateLang;
    protected String templateName;
    protected Set<String> searchLangs;
    protected PageInterwikiStorage interwikiStorage;
    private Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    private Map<UnifiedPage, InterwikiCandidate> candidatesMap = new HashMap<UnifiedPage, InterwikiCandidate>();

    public AbstractTemplateInterwikiFinder(String templateLang, String templateName, Set<String> searchLangs) {
        this.templateLang = templateLang;
        this.templateName = PageUtils.removeNamespace(templateName);
        this.searchLangs = searchLangs;
        this.interwikiStorage = new PageInterwikiStorage(Constants.NAMESPACE_TEMPLATE);
    }

    protected Set<String> getForeignLangs() {
        return foreignLangToForeignArticlesMap.keySet();
    }

    protected Set<String> getForeignArticles(String lang) {
        return foreignLangToForeignArticlesMap.get(lang);
    }

    protected void addForeignLangAndForeignArticle(String foreignLang, String foreignArticleTitle) {
        Set<String> articles = foreignLangToForeignArticlesMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<String>();
            this.foreignLangToForeignArticlesMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleTitle);
    }

    protected void addForeignLangAndForeignTemplateCandidate(String foreignLang, UnifiedPage foreignTemplateCandidate) {
        InterwikiCandidate templateMap = candidatesMap.get(foreignTemplateCandidate);
        if (templateMap == null) {
            templateMap = new InterwikiCandidate(foreignTemplateCandidate);
            candidatesMap.put(foreignTemplateCandidate, templateMap);
        }
        templateMap.add(foreignLang);
    }

    protected List<InterwikiCandidate> getCandidatesOrdered() {
        List<InterwikiCandidate> result = new ArrayList<InterwikiCandidate>();
        result.addAll(candidatesMap.values());
        Collections.sort(result, new Comparator<InterwikiCandidate>() {
            @Override
            public int compare(InterwikiCandidate o1, InterwikiCandidate o2) {
                return o2.size().compareTo(o1.size());
            }
        });
        return result;
    }

}
