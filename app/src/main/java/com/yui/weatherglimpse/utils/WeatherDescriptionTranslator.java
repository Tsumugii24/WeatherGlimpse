package com.yui.weatherglimpse.utils;
import java.util.HashMap;
import java.util.Map;

public class WeatherDescriptionTranslator {
    private static final Map<String, String> translations = new HashMap<>();

    static {
        initTranslations();
    }

    private static void initTranslations() {
        translations.put("Clear", "晴天");
        translations.put("Clouds", "多云");
        translations.put("Few clouds", "少云");
        translations.put("Scattered clouds", "疏云");
        translations.put("Broken clouds", "碎云");
        translations.put("Overcast clouds", "阴天");
        translations.put("Rain", "雨");
        translations.put("Light rain", "小雨");
        translations.put("Moderate rain", "中雨");
        translations.put("Heavy rain", "大雨");
        translations.put("Thunderstorm", "雷雨");
        translations.put("Snow", "雪");
        translations.put("Light snow", "小雪");
        translations.put("Heavy snow", "大雪");
        translations.put("Sleet", "雨夹雪");
        translations.put("Mist", "薄雾");
        translations.put("Fog", "雾");
        translations.put("Haze", "霾");
        translations.put("Drizzle", "毛毛雨");
        translations.put("Shower rain", "阵雨");
        translations.put("Freezing rain", "冻雨");
        translations.put("Dust", "浮尘");
        translations.put("Sand", "沙尘");
        translations.put("Smoke", "烟雾");
        translations.put("Tornado", "龙卷风");
        translations.put("Tropical storm", "热带风暴");
        translations.put("Hurricane", "飓风");
        translations.put("Cold", "寒冷");
        translations.put("Hot", "炎热");
        translations.put("Windy", "大风");
        translations.put("Hail", "冰雹");
    }

    public static String translate(String englishDescription) {
        if (englishDescription == null || englishDescription.isEmpty()) {
            return "";
        }

        // 转换为小写并去除首尾空格，以增加匹配的可能性
        String normalizedDescription = englishDescription.toLowerCase().trim();

        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (normalizedDescription.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // 如果没有找到匹配的翻译，返回原始描述
        return englishDescription;
    }
}