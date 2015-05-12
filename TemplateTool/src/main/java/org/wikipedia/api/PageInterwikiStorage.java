package org.wikipedia.api;

import java.util.ArrayList;
import java.util.List;

public class PageInterwikiStorage {

    private final int namespace;
    private List<UnifiedPage> pages = new ArrayList<UnifiedPage>();

    public PageInterwikiStorage(int namespace) {
        this.namespace = namespace;
    }

    public void addInterwiki(String fromLang, String fromTitle, String toLang, String toTitle) {
        UnifiedPage page = findUnifiedPage(fromLang, fromTitle);
        if (page == null) {
            page = findUnifiedPage(toLang, toTitle);
        }
        if (page == null) {
            page = new UnifiedPage(namespace);
            pages.add(page);
        }
        page.putInterwiki(fromLang, fromTitle);
        page.putInterwiki(toLang, toTitle);
    }

    public UnifiedPage findUnifiedPage(String lang, String title) {
        if (lang == null || title == null) {
            return null;
        }
        title = PageUtils.addNamespace(lang, title, namespace);
        for (UnifiedPage page : pages) {
            if (page.has(lang) && page.get(lang).contains(title)) {
                return page;
            }
        }
        return null;
    }
    
    public List<UnifiedPage> getPages() {
        return pages;
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    public boolean addStorage(PageInterwikiStorage storage) {
        if (namespace != storage.namespace) {
            return false;
        }
        // TODO: not correct:
        pages.addAll(storage.getPages());
        return true;
    }
    
}
