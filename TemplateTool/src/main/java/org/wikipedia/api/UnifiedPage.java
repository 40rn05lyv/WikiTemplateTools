package org.wikipedia.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnifiedPage {

    private final int namespace;

    public UnifiedPage(int namespace) {
        this.namespace = namespace;
    }

    private Map<String, Set<String>> langToTitlesMap = new HashMap<String, Set<String>>();

    public void putInterwiki(String lang, String title) {
        if (lang == null || title == null) {
            return;
        }
        Set<String> titles = langToTitlesMap.get(lang);
        if (titles == null) {
            titles = new HashSet<String>();
            langToTitlesMap.put(lang, titles);
        }
        title = PageUtils.addNamespace(lang, title, namespace);
        titles.add(title);
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