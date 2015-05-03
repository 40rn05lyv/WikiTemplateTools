package org.wikipedia.templates.find.interwiki.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wikipedia.api.Constants;
import org.wikipedia.api.http.ApiHelper;

public class TemplateInterwikiStorage {

    private List<UnifiedTemplate> templates = new ArrayList<UnifiedTemplate>();

    public void addInterwiki(String fromLang, String fromTitle, String toLang, String toTitle) {
        UnifiedTemplate template = findUnifiedTemplate(fromLang, fromTitle);
        if (template == null) {
            template = findUnifiedTemplate(toLang, toTitle);
        }
        if (template == null) {
            template = new UnifiedTemplate();
            templates.add(template);
        }
        template.putInterwiki(fromLang, fromTitle);
        template.putInterwiki(toLang, toTitle);
    }

    public UnifiedTemplate findUnifiedTemplate(String lang, String title) {
        if (lang == null || title == null) {
            return null;
        }
        title = UnifiedTemplate.unify(lang, title);
        for (UnifiedTemplate template : templates) {
            if (template.has(lang) && template.get(lang).contains(title)) {
                return template;
            }
        }
        return null;
    }

    public List<UnifiedTemplate> getTemplates() {
        return templates;
    }

    private static Map<String, String> templateNamespaceMap = new HashMap<String, String>();

    public static String getTemplateNamespaceName(String lang) {
        String namespaceName = templateNamespaceMap.get(lang);
        if (namespaceName != null) {
            return namespaceName;
        }
        namespaceName = ApiHelper.findNamespaceName(lang, Constants.NAMESPACE_TEMPLATE);
        templateNamespaceMap.put(lang, namespaceName);
        return namespaceName;
    }

}
