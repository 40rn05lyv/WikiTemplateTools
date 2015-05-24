package example.my.servlet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Pages {
    
    Map<Integer, Page> pagesMap = new HashMap<Integer, Page>();
    
    public Pages() {
    }

    public void addTitle(int id, String title) {
        Page page = pagesMap.get(id);
        if (page == null) {
            page = new Page();
            page.setPageId(id);
            pagesMap.put(id, page);
        }
        page.setPageTitle(title);
    }

    public void addLang(int id, String lang) {
        Page page = pagesMap.get(id);
        if (page == null) {
            page = new Page();
            page.setPageId(id);
            pagesMap.put(id, page);
        }
        page.addLang(lang);
    }
    
    public void addLink(int id, String link) {
        Page page = pagesMap.get(id);
        if (page == null) {
            page = new Page();
            page.setPageId(id);
            pagesMap.put(id, page);
        }
        page.addLink(link);
    }
    
    public Collection<Page> getList() {
        return pagesMap.values();
    }

    public void add(Page page) {
        pagesMap.put(page.getPageId(), page);
    }
    
    public Set<Integer> getPageIds() {
        return pagesMap.keySet();
    }

}
