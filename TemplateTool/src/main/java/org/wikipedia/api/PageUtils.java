package org.wikipedia.api;

import java.util.HashMap;
import java.util.Map;

import org.wikipedia.api.http.ApiHelper;

public class PageUtils {

    /***
     * Map<NamespaceNumber, Map<Language, LocalizedNamespaceName>>
     */
    private static Map<Integer, Map<String, String>> globalNamespaceMap = new HashMap<Integer, Map<String, String>>();

    public static String getNamespaceName(String lang, int namespace) {
        Map<String, String> namespaceMap = globalNamespaceMap.get(namespace);
        if (namespaceMap == null) {
            namespaceMap = new HashMap<String, String>();
            globalNamespaceMap.put(namespace, namespaceMap);
        }
        String namespaceName = namespaceMap.get(lang);
        if (namespaceName == null) {
            namespaceName = ApiHelper.findNamespaceName(lang, Constants.NAMESPACE_TEMPLATE);
            namespaceMap.put(lang, namespaceName);
        }
        return namespaceName;
    }

    public static String addNamespace(String lang, String page, int namespace) {
        if (page.indexOf(":") == -1) {
            String namespaceName = getNamespaceName(lang, namespace);
            page = namespaceName + ":" + page;
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
