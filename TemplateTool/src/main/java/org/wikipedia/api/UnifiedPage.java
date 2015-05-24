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

    public String getOne(String lang) {
        Set<String> set = langToTitlesMap.get(lang);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return (String) set.toArray()[0];
    }

    public Set<String> getLangs() {
        return langToTitlesMap.keySet();
    }

    public int getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return langToTitlesMap.toString();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }
}