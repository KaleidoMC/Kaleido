package moe.mmf.csscolors.keywords;

import java.util.HashMap;
import java.util.Map;

public abstract class CSSLevel1 {
    public static final Map<String, String> colors = new HashMap<String, String>() {{
        put("black", black);
        put("silver", silver);
        put("gray", gray);
        put("white", white);
        put("maroon", maroon);
        put("red", red);
        put("purple", purple);
        put("fuchsia", fuchsia);
        put("green", green);
        put("lime", lime);
        put("olive", olive);
        put("yellow", yellow);
        put("navy", navy);
        put("blue", blue);
        put("teal", teal);
        put("aqua", aqua);
    }};

    public static final String black = "#000000";
    public static final String silver = "#c0c0c0";
    public static final String gray = "#808080";
    public static final String white = "#ffffff";
    public static final String maroon = "#800000";
    public static final String red = "#ff0000";
    public static final String purple = "#800080";
    public static final String fuchsia = "#ff00ff";
    public static final String green = "#008000";
    public static final String lime = "#00ff00";
    public static final String olive = "#808000";
    public static final String yellow = "#ffff00";
    public static final String navy = "#000080";
    public static final String blue = "#0000ff";
    public static final String teal = "#008080";
    public static final String aqua = "#00fff";
}
