package org.wikipedia.templates.find.interwiki.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.api.Constants;
import org.wikipedia.api.PageInterwikiStorage;
import org.wikipedia.api.PageUtils;
import org.wikipedia.api.UnifiedPage;
import org.wikipedia.api.db.QueryHelper;
import org.wikipedia.templates.find.interwiki.db.FindTemplateInterwikiResult.FindTemplateInterwikiResultEnum;

public class FindTemplateInterwikiProcessor {

    private String templateLang;
    private String templateName;
    private Set<String> searchLangs;
    private PageInterwikiStorage interwikiStorage;
    private Map<String, Set<String>> foreignLangToForeignArticlesMap = new HashMap<String, Set<String>>();
    private Map<UnifiedPage, InterwikiCandidate> candidatesMap = new HashMap<UnifiedPage, InterwikiCandidate>();

    public FindTemplateInterwikiProcessor(String templateLang, String templateName, Set<String> searchLangs) {
        this.templateLang = templateLang;
        this.templateName = PageUtils.removeNamespace(templateName);
        this.searchLangs = searchLangs;
        this.interwikiStorage = new PageInterwikiStorage(Constants.NAMESPACE_TEMPLATE);
    }

    public FindTemplateInterwikiResult process() {
        FindTemplateInterwikiResult result = new FindTemplateInterwikiResult();
        boolean templateExists = QueryHelper.doesTemplateExist(templateLang, templateName);
        if (!templateExists) {
            result.setResult(FindTemplateInterwikiResultEnum.TEMPLATE_DONT_EXIST);
            return result;
        }
        boolean hasTransclusions = fillTranscludedInArticlesAndLangLinks();
        if (!hasTransclusions) {
            result.setResult(FindTemplateInterwikiResultEnum.NO_TRANSCLUSIONS);
            return result;
        }
        for (String foreignLang : getForeignLangs()) {
            if (!searchLangs.contains(foreignLang)) {
                continue;
            }
            fillTemplatesInForeignArticlesWithoutLang(foreignLang);
        }
        result.setResult(FindTemplateInterwikiResultEnum.SUCCESS);
        result.setCandidates(getCandidatesOrdered());
        return result;
    }

    private boolean fillTranscludedInArticlesAndLangLinks() {
        List<Pair<String, String>> transcludedInLangLinks = QueryHelper.findTranscludedInArticlesAndLangLinksFull(templateLang,
                templateName);
        if (transcludedInLangLinks.isEmpty()) {
            return false;
        }
        for (Pair<String, String> transcludedInLangLink : transcludedInLangLinks) {
            addForeignLangAndForeignArticle(transcludedInLangLink.getLeft(), transcludedInLangLink.getRight());
        }
        return true;
    }

    // Find all templates without langwiki in pageLang for all foreign articles
    private void fillTemplatesInForeignArticlesWithoutLang(String foreignLang) {
        Set<String> foreignArticles = getForeignArticles(foreignLang);
        String excludeLang = templateLang;
        for (String foreignArticle : foreignArticles) {
            Set<String> templates = QueryHelper.findAllTemplates(interwikiStorage, foreignLang, foreignArticle);
            for (String template : templates) {
                UnifiedPage unifiedTemplate = interwikiStorage.findUnifiedPage(foreignLang, template);
                if (!unifiedTemplate.has(excludeLang)) {
                    addForeignLangAndForeignTemplateCandidate(foreignLang, unifiedTemplate);
                }
            }
        }
    }

    private Set<String> getForeignLangs() {
        return foreignLangToForeignArticlesMap.keySet();
    }

    private Set<String> getForeignArticles(String lang) {
        return foreignLangToForeignArticlesMap.get(lang);
    }

    private void addForeignLangAndForeignArticle(String foreignLang, String foreignArticleTitle) {
        Set<String> articles = foreignLangToForeignArticlesMap.get(foreignLang);
        if (articles == null) {
            articles = new HashSet<String>();
            this.foreignLangToForeignArticlesMap.put(foreignLang, articles);
        }
        articles.add(foreignArticleTitle);
    }

    private void addForeignLangAndForeignTemplateCandidate(String foreignLang, UnifiedPage foreignTemplateCandidate) {
        InterwikiCandidate templateMap = candidatesMap.get(foreignTemplateCandidate);
        if (templateMap == null) {
            templateMap = new InterwikiCandidate(foreignTemplateCandidate);
            candidatesMap.put(foreignTemplateCandidate, templateMap);
        }
        templateMap.add(foreignLang);
    }

    private List<InterwikiCandidate> getCandidatesOrdered() {
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
