package example.find.template.interwiki.utils;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.json.JSONObject;
import org.junit.Test;

public class JsonUtilsTest {

    @Test
    public void test() throws ParseException {
        JSONObject obj1= new JSONObject("{ \"key1\" : [{ \"*\" : \"uk\" }] }");
        JSONObject obj2 = new JSONObject("{ \"key1\" : [{ \"*\" : \"ru\" }] }");
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        net.minidev.json.JSONObject o1 = (net.minidev.json.JSONObject)parser.parse(obj1.toString());
        net.minidev.json.JSONObject o2 = (net.minidev.json.JSONObject)parser.parse(obj2.toString());
        o1.merge(o2);
        System.out.println(o1.toJSONString());
    }

}
