package org.wikipedia.templates.find.interwiki.db;

import java.util.Set;

import org.wikipedia.api.PageUtils;
import org.wikipedia.api.UnifiedPage;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class InterwikiCandidate {

    Multiset<String> langs = HashMultiset.create();
    UnifiedPage page;

    public InterwikiCandidate(UnifiedPage page) {
        super();
        this.page = page;
    }

    public void add(String lang) {
        langs.add(lang);
    }

    public Integer size() {
        return langs.size();
    }

    public Multiset<String> getLangs() {
        return langs;
    }

    public String get(String lang) {
        return page.getOne(lang);
    }

    public String getForDisplay(String lang) {
        String page = get(lang);
        if (page == null) {
            return null;
        }
        page = PageUtils.removeNamespace(lang, page, this.page.getNamespace());
        page = PageUtils.toNormalView(page);
        return page;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(langs.toString());
        sb.append("\n");
        sb.append(page.toString());
        sb.append("\n");
        return sb.toString();
    }

}
