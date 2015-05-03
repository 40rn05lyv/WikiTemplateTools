package org.wikipedia.templates.find.interwiki.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TemplateInterwikiStorage {

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
            titles.add(title);
            //System.out.println("INFO: Added interwiki (" + lang + ", " + title + ").");
        }

        public boolean has(String lang) {
            return langToTitlesMap.containsKey(lang);
        }

        public Set<String> get(String lang) {
            return langToTitlesMap.get(lang);
        }
        
        @Override
        public String toString() {
            return langToTitlesMap.toString();
        }
    }

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

}
