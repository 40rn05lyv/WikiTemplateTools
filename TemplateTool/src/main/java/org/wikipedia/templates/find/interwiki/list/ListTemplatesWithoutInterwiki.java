package org.wikipedia.templates.find.interwiki.list;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wikipedia.api.db.ConnectionFactory;
import org.wikipedia.api.db.QueryHelper;

public class ListTemplatesWithoutInterwiki extends HttpServlet {

    private static final long serialVersionUID = -8441332046266659846L;
    
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
        String lang = req.getParameter("lang");
        String parentTemplate = req.getParameter("parentTemplate");
        List<String> templates = QueryHelper.getLinksHereTemplatesWithLangLinks(lang, parentTemplate);
        req.setAttribute("searchLang", "uk");
        req.setAttribute("templates", templates);
        req.getRequestDispatcher("/listwithoutinterwiki.jsp").forward(req, resp);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        ConnectionFactory.stop();
    }

}
