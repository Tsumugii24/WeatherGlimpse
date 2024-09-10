// coverage test for URL.java

package com.yui.weatherglimpse.url;

import com.yui.weatherglimpse.location.LocationCord;

public class URLTest {

    private URL url;

    public void setUp() {
        LocationCord.lat = "26.9124";
        LocationCord.lon = "75.7873";
        url = new URL();
    }

    public void testGetLink() {
        String expectedLink = "https://api.openweathermap.org/data/3.0/onecall?exclude=minutely&lat=26.9124&lon=75.7873&appid=93394a87f27af7b7e7d3bece79e96a6d";
        String actualLink = url.getLink();
        assertEquals(expectedLink, actualLink, "getLink测试失败");
    }

    public void testGetCityUrl() {
        URL.setCity_url("SHANGHAI");
        String expectedCityUrl = "https://api.openweathermap.org/data/2.5/weather?&q=SHANGHAI&appid=93394a87f27af7b7e7d3bece79e96a6d";
        String actualCityUrl = URL.getCity_url();
        assertEquals(expectedCityUrl, actualCityUrl, "getCityUrl测试失败");
    }

    private void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "\n期望: " + expected + "\n实际: " + actual);
        }
    }

    public static void main(String[] args) {
        URLTest test = new URLTest();
        test.setUp();
        test.testGetLink();
        test.testGetCityUrl();
        System.out.println("所有测试通过！");
    }
}