package example.find.template.interwiki.utils;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.json.JSONObject;


public class JsonUtils {
    
    public static JSONObject merge(JSONObject obj1, JSONObject obj2) throws ParseException {
        JSONParser p = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        net.minidev.json.JSONObject o1 = (net.minidev.json.JSONObject)p.parse(obj1.toString());
        net.minidev.json.JSONObject o2 = (net.minidev.json.JSONObject)p.parse(obj2.toString());
        o1.merge(o2);
        return new JSONObject(o1.toJSONString());
    }

}
