package example.my.servlet;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ReplaceTests {

    @Test
    public void test1() {
        String result = GeneralHelper.replaceWikiLink("[[Some text]] Some text", "Some text", "Another text");
        Assert.assertEquals("[[Another text]] Some text", result);
    }
    
    @Test
    public void test2() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Some", "Another");
        map.put("text", "sprint");
        String result = GeneralHelper.replaceWikiLinks("[[Some text]]", map);
        Assert.assertEquals("[[Some text]]", result);
    }

}
