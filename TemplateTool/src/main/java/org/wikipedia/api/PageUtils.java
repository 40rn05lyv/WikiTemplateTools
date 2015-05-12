package org.wikipedia.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.api.http.ApiHelper;

public class PageUtils {

    /***
     * Map<NamespaceNumber, Map<Language, LocalizedNamespaceName>>
     */
    private static Map<Integer, Map<String, String>> globalNamespaceMap = new HashMap<Integer, Map<String, String>>();

    public static synchronized String getNamespaceName(String lang, int namespace) {
        Map<String, String> namespaceMap = globalNamespaceMap.get(namespace);
        if (namespaceMap == null) {
            namespaceMap = new HashMap<String, String>();
            globalNamespaceMap.put(namespace, namespaceMap);
        }
        String namespaceName = namespaceMap.get(lang);
        if (namespaceName == null) {
            namespaceName = ApiHelper.findNamespaceName(lang, namespace);
            namespaceMap.put(lang, namespaceName);
        }
        return namespaceName;
    }

    public static String addNamespace(String lang, String page, int namespace) {
        if (page.indexOf(":") == -1) {
            String namespaceName = getNamespaceName(lang, namespace);
            if (!StringUtils.isEmpty(namespaceName)) {
                page = namespaceName + ":" + page;
            }
        }
        return page;
    }

    public static String removeNamespace(String page) {
        if (page == null) {
            throw new NullPointerException();
        }
        int colonIndex = page.indexOf(":");
        if (colonIndex == -1) {
            return page;
        }
        return page.substring(colonIndex + 1);
    }

    public static String toDBView(String page) {
        return page.replaceAll(" ", "_");
    }

    public static String toNormalView(String page) {
        return page.replaceAll("_", " ");
    }

}
