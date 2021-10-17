package moe.mmf.csscolors;

import moe.mmf.csscolors.keywords.CSSLevel1;
import moe.mmf.csscolors.keywords.CSSLevel2;
import moe.mmf.csscolors.keywords.CSSLevel3;
import moe.mmf.csscolors.keywords.CSSLevel4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("^([0-9\\\\.e%]+) *(?:[, ]) *([0-9\\\\.e%]+) *(?:[, ]) *([0-9\\\\.e%]+) *(?:(?:[,/]) *([0-9\\\\.e%]+))?$");

    private final byte r;
    private final byte g;
    private final byte b;
    private final byte a;

    private Color(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int R() {
        return this.r & 0xff;
    }

    public int G() {
        return this.g & 0xff;
    }

    public int B() {
        return this.b & 0xff;
    }

    public int A() {
        return this.a & 0xff;
    }

    public String toHex(boolean withHash, boolean alpha) {
        return String.format("%s%02X%02X%02X", withHash ? "#" : "", this.r, this.g, this.b)
                + (alpha ? String.format("%02X", this.a) : "");
    }

    public String toHex() {
        return toHex(true, false);
    }

    public String toHex(boolean alpha) {
        return toHex(true, alpha);
    }

    public Color withAlpha(byte alpha) {
        return Color.fromRGB(this.r, this.g, this.b, alpha);
    }

    public int toInt() {
        return ((this.r & 0xff) << 16) + ((this.g & 0xff) << 8) + (this.b & 0xff);
    }

    public int[] toIntArray() {
        return new int[]{this.r & 0xff, this.g & 0xff, this.b & 0xff, this.a & 0xff};
    }

    public static Color fromString(String color) {
        color = color.trim();
        // Hex
        if (color.charAt(0) == '#') {
            return fromHex(color);
        }

        if (color.charAt(color.length() - 1) == ')') {
            boolean isRGB, alpha = false;
            if (color.length() < 10) {
                // rgb(r,g,b)
                return null;
            }
            if (color.startsWith("rgb")) {
                isRGB = true;
            } else if (color.startsWith("hsl")) {
                isRGB = false;
            } else {
                return null;
            }
            if (color.charAt(4) == 'a') {
                alpha = true;
            }
            color = color.substring(color.indexOf("(") + 1, color.indexOf(")"));
            if (isRGB) {
                return fromRGB(color, alpha);
            } else {
                return fromHSL(color, alpha);
            }
        }

        // Keywords
        if (CSSLevel1.colors.containsKey(color)) {
            return fromHex(CSSLevel1.colors.get(color));
        }
        if (CSSLevel2.colors.containsKey(color)) {
            return fromHex(CSSLevel2.colors.get(color));
        }
        if (CSSLevel3.colors.containsKey(color)) {
            return fromHex(CSSLevel3.colors.get(color));
        }
        if (CSSLevel4.colors.containsKey(color)) {
            return fromHex(CSSLevel4.colors.get(color));
        }

        return null;
    }

    public static Color fromHex(String hex) {
        byte r, g, b, a = 0;
        switch (hex.length()) {
            case 4:
            case 5:
                // #RGB(A)
                r = (byte) (Integer.parseInt(String.valueOf(hex.charAt(1)), 16) * 0x11);
                g = (byte) (Integer.parseInt(String.valueOf(hex.charAt(2)), 16) * 0x11);
                b = (byte) (Integer.parseInt(String.valueOf(hex.charAt(3)), 16) * 0x11);
                if (hex.length() == 5) {
                    a = (byte) (Integer.parseInt(String.valueOf(hex.charAt(4)), 16) * 0x11);
                }
                break;
            case 7:
            case 9:
                // #RRGGBB(AA)
                r = (byte) Integer.parseInt(hex.substring(1, 3), 16);
                g = (byte) Integer.parseInt(hex.substring(3, 5), 16);
                b = (byte) Integer.parseInt(hex.substring(5, 7), 16);
                if (hex.length() == 9) {
                    a = (byte) Integer.parseInt(hex.substring(7, 9), 16);
                }
                break;
            default:
                return null;
        }
        return fromRGB(r, g, b, a);
    }

    public static Color fromRGB(String parameters, boolean alpha) {
        Matcher matcher = PARAMETER_PATTERN.matcher(parameters);
        if (!matcher.matches() || matcher.groupCount() < (3 + (alpha ? 1 : 0))) {
            return null;
        }
        byte r, g, b, a = 0;
        r = paramToByte(matcher.group(1));
        g = paramToByte(matcher.group(2));
        b = paramToByte(matcher.group(3));
        if (alpha) {
            a = paramToByte(matcher.group(4));
        }
        return Color.fromRGB(r, g, b, a);
    }

    public static Color fromRGB(byte r, byte g, byte b) {
        return new Color(r, g, b, (byte) 0xff);
    }

    public static Color fromRGB(byte r, byte g, byte b, byte a) {
        return new Color(r, g, b, a);
    }

    public static Color fromHSL(String parameters, boolean alpha) {
        Matcher matcher = PARAMETER_PATTERN.matcher(parameters);
        if (!matcher.matches() || matcher.groupCount() < (3 + (alpha ? 1 : 0))) {
            return null;
        }
        double h, s, l;
        h = (byte) paramToDouble(matcher.group(1));
        s = (byte) paramToDouble(matcher.group(2));
        l = (byte) paramToDouble(matcher.group(3));

        byte r, g, b, a = 0;
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double x = c * (1 - Math.abs(h / 60d % 2 - 1));
        double m = l - c / 2;
        if (h < 60) {
            r = (byte) c;
            g = (byte) x;
            b = (byte) 0;
        } else if (h < 120) {
            r = (byte) x;
            g = (byte) c;
            b = (byte) 0;
        } else if (h < 180) {
            r = (byte) 0;
            g = (byte) c;
            b = (byte) x;
        } else if (h < 240) {
            r = (byte) 0;
            g = (byte) x;
            b = (byte) c;
        } else if (h < 300) {
            r = (byte) x;
            g = (byte) 0;
            b = (byte) c;
        } else {
            r = (byte) c;
            g = (byte) 0;
            b = (byte) x;
        }
        if (alpha) {
            a = paramToByte(matcher.group(4));
        }
        return Color.fromRGB(r, g, b, a);
    }

    public static double paramToDouble(String param) throws NumberFormatException {
        return Double.parseDouble(param);
    }

    public static byte doubleToByte(double d) {
        if (d < 1) {
            d *= 255;
        } else if (d > 255) {
            d %= 256;
        }
        return (byte) d;
    }

    public static byte paramToByte(String param) throws NumberFormatException {
        return doubleToByte(paramToDouble(param));
    }
}
