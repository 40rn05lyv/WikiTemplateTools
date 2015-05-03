package org.wikipedia.templates.find.interwiki.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnifiedTemplate {
    
    private Map<String, Set<String>> langToTitlesMap = new HashMap<String, Set<String>>();

    void putInterwiki(String lang, String title) {
        if (lang == null || title == null) {
            return;
        }
        Set<String> titles = langToTitlesMap.get(lang);
        if (titles == null) {
            titles = new HashSet<String>();
            langToTitlesMap.put(lang, titles);
        }
        title = unify(lang, title);
        titles.add(title);
    }

    public boolean has(String lang) {
        return langToTitlesMap.containsKey(lang);
    }

    public Set<String> get(String lang) {
        return langToTitlesMap.get(lang);
    }

    static String unify(String lang, String title) {
        if (title.indexOf(":") == -1) {
            String namespace = TemplateInterwikiStorage.getTemplateNamespaceName(lang);
            title = namespace + ":" + title;
        }
        return title;
    }

    @Override
    public String toString() {
        return langToTitlesMap.toString();
    }
}