package net.alcaris.plugin.items.lib;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaterialScoreData {
    public static final List<String> SCORE_KEYS = List.of(
            "red",
            "orange",
            "yellow",
            "lime",
            "green",
            "cyan",
            "aqua",
            "blue",
            "pink",
            "purple",
            "black",
            "gray",
            "white"
    );

    private Integer red;
    private Integer orange;
    private Integer yellow;
    private Integer lime;
    private Integer green;
    private Integer cyan;
    private Integer aqua;
    private Integer blue;
    private Integer pink;
    private Integer purple;
    private Integer black;
    private Integer gray;
    private Integer white;

    public int getScore(String key) {
        return switch (key) {
            case "red" -> valueOrZero(red);
            case "orange" -> valueOrZero(orange);
            case "yellow" -> valueOrZero(yellow);
            case "lime" -> valueOrZero(lime);
            case "green" -> valueOrZero(green);
            case "cyan" -> valueOrZero(cyan);
            case "aqua" -> valueOrZero(aqua);
            case "blue" -> valueOrZero(blue);
            case "pink" -> valueOrZero(pink);
            case "purple" -> valueOrZero(purple);
            case "black" -> valueOrZero(black);
            case "gray" -> valueOrZero(gray);
            case "white" -> valueOrZero(white);
            default -> 0;
        };
    }

    public Map<String, Integer> toOrderedMap() {
        Map<String, Integer> scores = new LinkedHashMap<>();

        for (String key : SCORE_KEYS) {
            scores.put(key, getScore(key));
        }

        return scores;
    }

    public boolean hasAnyScore() {
        return toOrderedMap().values().stream().anyMatch(value -> value != 0);
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
