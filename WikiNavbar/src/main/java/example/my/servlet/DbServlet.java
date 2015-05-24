package example.my.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mashape.unirest.http.exceptions.UnirestException;

import example.api.ApiHelper;

public class DbServlet extends HttpServlet {

    private static final long serialVersionUID = -9122125709896866661L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Pages linksHereTemplates = QueryHelper.getLinksHereTemplatesWithLangLinks("Navbox", 10);
            Pages filteredTemplates = GeneralHelper.filterByLangLink(linksHereTemplates, "uk");
            Pages filteredTemplatesWithLinks = QueryHelper.getLinksForPages(filteredTemplates);
            Pages filteredTemplatesWithDoubleLinks = ApiHelper.getLinksForPages(filteredTemplatesWithLinks);
            Pages filteredTemplatesWithDoubleLinksAndStats = GeneralHelper.generateStats(filteredTemplatesWithDoubleLinks);
            ArrayList<InfoBean> list = new ArrayList<InfoBean>();
            req.setAttribute("list", list);
            req.getRequestDispatcher("/navbar.jsp").forward(req, resp);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

}
