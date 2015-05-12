package org.wikipedia.templates.find.interwiki.db;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.api.UnifiedPage;
import org.wikipedia.api.db.QueryHelper;
import org.wikipedia.templates.find.interwiki.db.TemplateInterwikiFinderResult.FindTemplateInterwikiResultEnum;

public class TemplateInterwikiFinderViaTransclude extends AbstractTemplateInterwikiFinder {

    public TemplateInterwikiFinderViaTransclude(String templateLang, String templateName, Set<String> searchLangs) {
        super(templateLang, templateName, searchLangs);
    }

    @Override
    public TemplateInterwikiFinderResult process() {
        TemplateInterwikiFinderResult result = new TemplateInterwikiFinderResult();
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
        // TODO: check whether interwiki for languages exists
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
    
}