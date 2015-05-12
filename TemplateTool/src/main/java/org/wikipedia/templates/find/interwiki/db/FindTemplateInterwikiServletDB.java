package org.wikipedia.templates.find.interwiki.db;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.wikipedia.api.PageUtils;
import org.wikipedia.api.db.ConnectionFactory;

public class FindTemplateInterwikiServletDB extends HttpServlet {

    private static final long serialVersionUID = -1893098398192062889L;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ConnectionFactory.init();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: add start-end time as separate filter
        String templateLang = req.getParameter("templateLang");
        String templateNameParam = req.getParameter("templateName");
        String searchLangsParam = req.getParameter("searchLangs");
        String freezeParam = req.getParameter("freeze");

        // Convertations
        Set<String> searchLangs = null;
        if (searchLangsParam != null) {
            searchLangs = new HashSet<String>();
            String[] splittedLangs = searchLangsParam.split(",");
            for (int i = 0; i < splittedLangs.length; i++) {
                String trimmed = splittedLangs[i].trim();
                if (!trimmed.isEmpty()) {
                    searchLangs.add(trimmed);
                }
            }
            searchLangs.remove(templateLang);
        }
        String templateName = null;
        if (templateNameParam != null) {
            templateName = PageUtils.removeNamespace(templateNameParam);
            templateName = PageUtils.toNormalView(templateName);
        }
        boolean freeze = Boolean.valueOf(freezeParam);

        // Validations
        boolean templateLangValid = !StringUtils.isEmpty(templateLang);
        boolean templateNameValid = !StringUtils.isEmpty(templateName);
        boolean searchLangsValid = !CollectionUtils.isEmpty(searchLangs);

        if (!templateLangValid || !templateNameValid || !searchLangsValid) {
            freeze = true;
        }

        // Processing
        FindTemplateInterwikiBean bean = new FindTemplateInterwikiBean();
        bean.setTemplateLang(templateLang);
        bean.setTemplateName(templateName);
        bean.setSearchLangs(searchLangs);
        bean.setFreeze(freeze);

        if (!freeze) {
            ITemplateInterwikiFinder processor = new TemplateInterwikiFinderViaContent(templateLang, templateName, searchLangs);
            TemplateInterwikiFinderResult result = processor.process();
            bean.setResult(result);
            // TODO: show number of transclusion
        }
        
        req.setAttribute("bean", bean);
        req.getRequestDispatcher("/findinterwikidb.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
        ConnectionFactory.stop();
    }

}
