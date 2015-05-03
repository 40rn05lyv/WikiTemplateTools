package org.wikipedia.templates.list;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wikipedia.api.Constants;
import org.wikipedia.api.db.ConnectionFactory;
import org.wikipedia.api.db.QueryHelper;

import com.mysql.jdbc.StringUtils;

public class ListTemplatesWithoutInterwiki extends HttpServlet {

    private static final long serialVersionUID = -8441332046266659846L;
    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

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
        String langParam = req.getParameter("lang");
        String parentTemplateParam = req.getParameter("parentTemplate");
        String fromParam = req.getParameter("from");
        String limitParam = req.getParameter("limit");
        String allParam = req.getParameter("all");
        String includeSubtemplatesParam = req.getParameter("includeSubtemplates");

        String lang = langParam;
        String parentTemplate = parentTemplateParam;
        Integer from = 0;
        if (!StringUtils.isNullOrEmpty(fromParam)) {
            from = Integer.parseInt(fromParam);
        }
        Integer limit = DEFAULT_LIMIT;
        if (!StringUtils.isNullOrEmpty(limitParam)) {
            limit = Integer.parseInt(limitParam);
        }
        boolean all = false;
        if (!StringUtils.isNullOrEmpty(allParam)) {
            all = Boolean.parseBoolean(allParam);
        }
        boolean includeSubtemplates = false;
        if (!StringUtils.isNullOrEmpty(includeSubtemplatesParam)) {
            if (includeSubtemplatesParam.equals("on")) {
                includeSubtemplates = true;
            }
        }

        List<String> templates = null;
        if (!StringUtils.isNullOrEmpty(lang)) {
            if (all) {
                if (!StringUtils.isNullOrEmpty(parentTemplate)) {
                    templates = QueryHelper.getLinksHereTemplatesWithoutInterwiki(lang, parentTemplate);
                } else {
                    templates = QueryHelper.getAllTemplatesWithoutInterwiki(lang);
                }
            } else {
                if (!StringUtils.isNullOrEmpty(parentTemplate)) {
                    templates = QueryHelper.getLinksHereTemplatesWithoutInterwiki(lang, parentTemplate, from, limit);
                } else {
                    templates = QueryHelper.getAllTemplatesWithoutInterwiki(lang, from, limit);
                }
            }
            
            if (!includeSubtemplates && templates != null) {
                Iterator<String> it = templates.iterator();
                while (it.hasNext()) {
                    if (it.next().contains("/")) {
                        it.remove();
                    }
                }
            }
        } else {
            req.setAttribute("hideTable", true);
        }


        req.setAttribute("supportedLangs", Constants.SUPPORTED_LANGS);
        req.setAttribute("searchLang", langParam);
        req.setAttribute("searchTemplate", parentTemplateParam);
        req.setAttribute("includeSubtemplates", includeSubtemplates);
        req.setAttribute("templates", templates);
        req.getRequestDispatcher("/listwithoutinterwiki.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
        ConnectionFactory.stop();
    }

}
