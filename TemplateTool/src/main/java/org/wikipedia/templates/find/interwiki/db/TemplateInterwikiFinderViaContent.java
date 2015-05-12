package org.wikipedia.templates.find.interwiki.db;

import java.util.Set;

import org.wikipedia.api.Constants;
import org.wikipedia.api.PageInterwikiStorage;
import org.wikipedia.api.UnifiedPage;
import org.wikipedia.api.db.QueryHelper;
import org.wikipedia.templates.find.interwiki.db.TemplateInterwikiFinderResult.FindTemplateInterwikiResultEnum;

public class TemplateInterwikiFinderViaContent extends AbstractTemplateInterwikiFinder {

    public TemplateInterwikiFinderViaContent(String templateLang, String templateName, Set<String> searchLangs) {
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
        // TODO: check whether interwiki for languages exists
        boolean hasLinks = fillLinksToArticlesAndLangLinks();
        if (!hasLinks) {
            result.setResult(FindTemplateInterwikiResultEnum.NO_LINKS);
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

    private boolean fillLinksToArticlesAndLangLinks() {
        Set<String> pureLinks = QueryHelper.findPureLinksInTemplate(templateLang, templateName, Constants.NAMESPACE_ARTICLE);
        PageInterwikiStorage storage = QueryHelper.findLangLinks(templateLang, pureLinks, Constants.NAMESPACE_ARTICLE, interwikiStorage);
        if (storage.isEmpty()) {
            return false;
        }
        for (UnifiedPage page : storage.getPages()) {
            for (String lang : page.getLangs()) {
                addForeignLangAndForeignArticle(lang, page.getOne(lang));
            }
        }
        return true;
    }

    private void fillTemplatesInForeignArticlesWithoutLang(String foreignLang) {
        Set<String> foreignArticles = getForeignArticles(foreignLang);
        String excludeLang = templateLang;
        for (String article : foreignArticles) {
            Set<String> linkHereTemplates = QueryHelper.findAllTemplatesThatHaveLinksToPage(foreignLang, article,
                    Constants.NAMESPACE_ARTICLE);
            QueryHelper.findLangLinks(foreignLang, linkHereTemplates, Constants.NAMESPACE_TEMPLATE, interwikiStorage);
            for (UnifiedPage page : interwikiStorage.getPages()) {
                if (!page.has(excludeLang)) {
                    addForeignLangAndForeignTemplateCandidate(foreignLang, page);
                }
            }
        }
    }

}