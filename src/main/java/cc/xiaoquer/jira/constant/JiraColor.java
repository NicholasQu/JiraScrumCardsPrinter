package cc.xiaoquer.jira.constant;

import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.storage.PropertiesCache;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nicholas on 2017/9/21.
 *
 * 原则1：因为Jira任务是持续性录入的，必须保证在同一Sprint里的再次打印新任务的时候颜色是一致的，否则无法寻找血缘关系。
 */
public enum JiraColor {
    STORY       ("#FFFF00","纯黄"),
    TASK        ("#808000","橄榄"),
    WHITE       ("#ffffff", "白色"),   //白
    SUBTASK     ("#7fa4d3", "子任务色"),   //
    ;

    private String hex;
    private String desc;

//    private static final List<Colors> leftColors =  new ArrayList<>(); //剩余可选的颜色
    /**
     * Key：BoardId_SprintId_owner_colors
     * Value:
     *       K = ownerName
     *       V = color
     */
    private static String CARD_COLOR_TYPE_OWNER = "owner";
    private static String CARD_COLOR_TYPE_BLOOD = "blood";
    private static Map<String, Map<String, String>> CARD_COLOR_MAP = new ConcurrentHashMap<>();
    /**
     * Key：BoardId_SprintId_blood_colors
     * Value:
     *       K = bloodKey最后的数字
     *       V = color
     */
//    private static Map<String, Map<String, String>> BLOOD_COLOR_MAP = new ConcurrentHashMap<>();

    static{
//        Collections.addAll(leftColors, Colors.values());
    }

    private JiraColor(String cssHex, String desc) {
        this.hex  = cssHex;
        this.desc = desc;
    }


    public String getHex() {
        return hex;
    }

    private static String getCardColorCacheKey(JiraIssue jiraIssue, String colorType) {
        return jiraIssue.getBoardId() + "_" + jiraIssue.getSprintId() + "_" + colorType + "_colors";
    }

    private static Map<String, String> _getCardColorMapCached(JiraIssue jiraIssue, String colorType) {
        String unionKey = getCardColorCacheKey(jiraIssue, colorType);
        Map<String, String> cachedColorsInSprint = CARD_COLOR_MAP.get(unionKey);
        if (cachedColorsInSprint == null) {
            cachedColorsInSprint = new LinkedHashMap<String, String>();
            CARD_COLOR_MAP.put(unionKey, cachedColorsInSprint);
        }
        return cachedColorsInSprint;
    }

    // colorType: owner / blood
    // key = ownerName / blookKey最后的数字
    private static String _getCardColorCachedByKey(JiraIssue jiraIssue, String colorType, String key) {
        Map<String, String> cachedColorsInSprint = _getCardColorMapCached(jiraIssue, colorType);
        if (cachedColorsInSprint == null) {
            return null;
        }
        return cachedColorsInSprint.get(key);
    }

    private static void _putCardColorToCache(JiraIssue jiraIssue, String colorType, String key, String color) {
        String unionKey = getCardColorCacheKey(jiraIssue, colorType);
        Map<String, String> cachedColorsInSprint = CARD_COLOR_MAP.get(unionKey);
        if(cachedColorsInSprint == null) {
            cachedColorsInSprint = new LinkedHashMap<String, String>();
        }
        cachedColorsInSprint.put(key, color);
        CARD_COLOR_MAP.put(unionKey, cachedColorsInSprint);

        //持久化缓存
        PropertiesCache.setProp(unionKey, JSON.toJSONString(cachedColorsInSprint));
        PropertiesCache.flush();
    }

    //从缓存中加载颜色信息
    private synchronized static void _loadCache(JiraIssue jiraIssue) {
        if (CARD_COLOR_MAP.size() == 0) {
            CARD_COLOR_MAP.put(
                    getCardColorCacheKey(jiraIssue, CARD_COLOR_TYPE_OWNER),
                    _loadSingleCache(jiraIssue, CARD_COLOR_TYPE_OWNER));

            CARD_COLOR_MAP.put(
                    getCardColorCacheKey(jiraIssue, CARD_COLOR_TYPE_BLOOD),
                    _loadSingleCache(jiraIssue, CARD_COLOR_TYPE_BLOOD));
        }
    }

    private static Map<String, String> _loadSingleCache(JiraIssue jiraIssue, String colorType) {
        String cardColorJson = PropertiesCache.getProp(getCardColorCacheKey(jiraIssue, colorType));
        if (cardColorJson == null || cardColorJson.trim().length() == 0) {
            return new LinkedHashMap<String, String>();
        }

        Map<String, String> map = JSON.parseObject(cardColorJson, Map.class);
        if (map == null) {
            return new LinkedHashMap<String, String>();
        }
        return map;
    }

    /**
     * 获取卡片上的所有颜色
     * @param jiraIssue
     * @param ownerName      员工姓名
     * @param bloodKey       血缘关系依赖的Key(issueKey后面的数字）
     * @return
     *      [0] cardColor 卡片的整体颜色-以人为准
     *      [1] bloodColor 血缘关系的线索Key
     */
    public static String[] getCardColor(JiraIssue jiraIssue, String ownerName, String bloodKey) {
        String needColorfulIssue = PropertiesCache.getProp("colorful");

        //只有colorful显性设置了不等于1，才不会打印五颜六色的, 否则确认统一一种颜色
        if (needColorfulIssue != null && !"1".equalsIgnoreCase(needColorfulIssue)) {
            return new String[]{SUBTASK.hex, Colors.COLOR_CRIMSON.hex};
        }

        _loadCache(jiraIssue);

        String ownerColor = getOneColor(jiraIssue, CARD_COLOR_TYPE_OWNER, ownerName);
        String bloodColor = getOneColor(jiraIssue, CARD_COLOR_TYPE_BLOOD, bloodKey);

        return new String[]{ownerColor, bloodColor};
    }

    //获取卡片上的一种颜色
    private static String getOneColor(JiraIssue jiraIssue, String colorType, String colorKey) {
        final Colors[] colorArray    = Colors.values();
        final int      totalColorCnt = colorArray.length;

        Map<String, String> cachedColorMap = _getCardColorMapCached(jiraIssue, colorType);;
        String oneColor = _getCardColorCachedByKey(jiraIssue, colorType, colorKey);
        if (oneColor == null) {
            int oneHashCode = Math.abs(DigestUtils.md5Hex(colorKey).hashCode());
            int offset = oneHashCode % totalColorCnt;

            //第一步按照hashCode命中一个颜色
            oneColor = colorArray[offset].hex;

            //第二步如果该颜色已经被占用 或者 与颜色相似！，就+1往下找'
            int loopTimes = 0;
            while (hasSimilarColorInMap(cachedColorMap, oneColor)) {
                //防止死循环
                if (loopTimes++ > Colors.values().length) {
                    break;
                }
                offset += 1;
                oneColor = colorArray[offset % totalColorCnt].hex;
            }
            //最终生成的颜色，放入缓存
            _putCardColorToCache(jiraIssue, colorType, colorKey, oneColor);
        }

        return oneColor;
    }

    public static boolean isColorDark(String hexColor){
        String r = hexColor.substring(1, 3);
        String g = hexColor.substring(3, 5);
        String b = hexColor.substring(5, 7);

        int red = Integer.parseInt(r, 16);
        int green = Integer.parseInt(g, 16);
        int blue = Integer.parseInt(b, 16);

        String rawFontColor = hexColor.substring(1, hexColor.length());
        int rgb = Integer.parseInt(rawFontColor, 16);
        Color c = new Color(rgb);
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float brightness = hsb[2];

        //Not accurate
//        if (brightness < 0.5) {
//            return false;
//        } else {
//            return true;
//        }

        //good one
//        return red + green + blue <= 0xff * 3 / 2;

        return 0.2126*red + 0.7152*green + 0.0722*blue < 128;
    }

    public static boolean hasSimilarColorInMap(Map<String, String> colorMap, String hexColor) {
        for (String color : colorMap.values()) {
            if (isColorSimilar(color, hexColor, 40)) {
                return true;
            }
        }
        return false;
    }

    //暂停77位分割线
    public static boolean isColorSimilar(String aHexColor, String bHexColor) {
        return isColorSimilar(aHexColor, bHexColor, 77);
    }
    public static boolean isColorSimilar(String aHexColor, String bHexColor, int line) {
        if (getColorDiff(aHexColor, bHexColor) < line) {
            return true;
        } else {
            return false;
        }
    }

    public static double getColorDiff(String aHexColor, String bHexColor) {
        int r1, g1, b1, r2, g2, b2;
        r1 = Integer.parseInt(aHexColor.substring(1, 3), 16);
        g1 = Integer.parseInt(aHexColor.substring(3, 5), 16);
        b1 = Integer.parseInt(aHexColor.substring(5, 7), 16);

        r2 = Integer.parseInt(bHexColor.substring(1, 3), 16);
        g2 = Integer.parseInt(bHexColor.substring(3, 5), 16);
        b2 = Integer.parseInt(bHexColor.substring(5, 7), 16);

        int[] lab1 = rgb2lab(r1, g1, b1);
        int[] lab2 = rgb2lab(r2, g2, b2);
        return Math.sqrt(Math.pow(lab2[0] - lab1[0], 2) + Math.pow(lab2[1] - lab1[1], 2) + Math.pow(lab2[2] - lab1[2], 2));
    }

    public static int[] rgb2lab(int R, int G, int B) {
        //http://www.brucelindbloom.com
        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; //R 0..1
        g = G / 255.f; //G 0..1
        b = B / 255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);


        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        int[] lab = new int[3];
        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
        return lab;
    }

    public static void main(String[] args) {
        System.out.println(getColorDiff("#FFB6C1", "#FFC0CB"));
        System.out.println(getColorDiff("#FFB6C1", "#DC143C"));
        System.out.println(getColorDiff("#FFC0CB", "#DC143C"));

        System.out.println("----------------------------");

        System.out.println(getColorDiff("#FFB6C1", "#EE82EE"));
        System.out.println(getColorDiff("#FFB6C1", "#4B0082"));

        System.out.println("----------------------------");
        System.out.println(getColorDiff("#ADFF2F", "#98FB98"));
        System.out.println(getColorDiff("#E1FFFF", "#F0F8FF"));


    }
    /**
     * 重复颜色，Dark深色，移除
     */
    enum Colors {
        COLOR_LIGHTPINK ("#FFB6C1","浅粉红"),
        COLOR_PINK ("#FFC0CB","粉红"),
        COLOR_CRIMSON ("#DC143C","猩红"),
        COLOR_LAVENDERBLUSH ("#FFF0F5","脸红的淡紫色"),
        COLOR_PALEVIOLETRED ("#DB7093","苍白的紫罗兰红色"),
        COLOR_HOTPINK ("#FF69B4","热情的粉红"),
        COLOR_DEEPPINK ("#FF1493","深粉色"),
        COLOR_MEDIUMVIOLETRED ("#C71585","适中的紫罗兰红色"),
        COLOR_ORCHID ("#DA70D6","兰花的紫色"),
        COLOR_THISTLE ("#D8BFD8","蓟"),
        COLOR_PLUM ("#DDA0DD","李子"),
        COLOR_VIOLET ("#EE82EE","紫罗兰"),
        COLOR_MAGENTA ("#FF00FF","洋红"),
//        COLOR_FUCHSIA ("#FF00FF","灯笼海棠(紫红色)"),
//        COLOR_DARKMAGENTA ("#8B008B","深洋红色"),
        COLOR_PURPLE ("#800080","紫色"),
        COLOR_MEDIUMORCHID ("#BA55D3","适中的兰花紫"),
//        COLOR_DARKVOILET ("#9400D3","深紫罗兰色"),
//        COLOR_DARKORCHID ("#9932CC","深兰花紫"),
        COLOR_INDIGO ("#4B0082","靛青"),
        COLOR_BLUEVIOLET ("#8A2BE2","深紫罗兰的蓝色"),
        COLOR_MEDIUMPURPLE ("#9370DB","适中的紫色"),
        COLOR_MEDIUMSLATEBLUE ("#7B68EE","适中的板岩暗蓝灰色"),
        COLOR_SLATEBLUE ("#6A5ACD","板岩暗蓝灰色"),
//        COLOR_DARKSLATEBLUE ("#483D8B","深岩暗蓝灰色"),
        COLOR_LAVENDER ("#E6E6FA","熏衣草花的淡紫色"),
        COLOR_GHOSTWHITE ("#F8F8FF","幽灵的白色"),
        COLOR_BLUE ("#0000FF","纯蓝"),
        COLOR_MEDIUMBLUE ("#0000CD","适中的蓝色"),
//        COLOR_MIDNIGHTBLUE ("#191970","午夜的蓝色"),
//        COLOR_DARKBLUE ("#00008B","深蓝色"),
        COLOR_NAVY ("#000080","海军蓝"),
        COLOR_ROYALBLUE ("#4169E1","皇军蓝"),
        COLOR_CORNFLOWERBLUE ("#6495ED","矢车菊的蓝色"),
        COLOR_LIGHTSTEELBLUE ("#B0C4DE","淡钢蓝"),
//        COLOR_LIGHTSLATEGRAY ("#778899","浅石板灰"),
        COLOR_SLATEGRAY ("#708090","石板灰"),
        COLOR_DODERBLUE ("#1E90FF","道奇蓝"),
        COLOR_ALICEBLUE ("#F0F8FF","爱丽丝蓝"),
        COLOR_STEELBLUE ("#4682B4","钢蓝"),
        COLOR_LIGHTSKYBLUE ("#87CEFA","淡蓝色"),
        COLOR_SKYBLUE ("#87CEEB","天蓝色"),
        COLOR_DEEPSKYBLUE ("#00BFFF","深天蓝"),
        COLOR_LIGHTBLUE ("#ADD8E6","淡蓝"),
        COLOR_POWDERBLUE ("#B0E0E6","火药蓝"),
        COLOR_CADETBLUE ("#5F9EA0","军校蓝"),
        COLOR_AZURE ("#F0FFFF","蔚蓝色"),
        COLOR_LIGHTCYAN ("#E1FFFF","淡青色"),
        COLOR_PALETURQUOISE ("#AFEEEE","苍白的绿宝石"),
        COLOR_CYAN ("#00FFFF","青色"),
//        COLOR_AQUA ("#00FFFF","水绿色"),
        COLOR_DARKTURQUOISE ("#00CED1","深绿宝石"),
//        COLOR_DARKSLATEGRAY ("#2F4F4F","深石板灰"),
        COLOR_DARKCYAN ("#008B8B","深青色"),
        COLOR_TEAL ("#008080","水鸭色"),
        COLOR_MEDIUMTURQUOISE ("#48D1CC","适中的绿宝石"),
        COLOR_LIGHTSEAGREEN ("#20B2AA","浅海洋绿"),
        COLOR_TURQUOISE ("#40E0D0","绿宝石"),
        COLOR_AUQAMARIN ("#7FFFAA","绿玉|碧绿色"),
        COLOR_MEDIUMAQUAMARINE ("#00FA9A","适中的碧绿色"),
        COLOR_MEDIUMSPRINGGREEN ("#00FF7F","适中的春天的绿色"),
        COLOR_MINTCREAM ("#F5FFFA","薄荷奶油"),
        COLOR_SPRINGGREEN ("#3CB371","春天的绿色"),
        COLOR_SEAGREEN ("#2E8B57","海洋绿"),
        COLOR_HONEYDEW ("#F0FFF0","蜂蜜"),
        COLOR_LIGHTGREEN ("#90EE90","淡绿色"),
        COLOR_PALEGREEN ("#98FB98","苍白的绿色"),
        COLOR_DARKSEAGREEN ("#8FBC8F","深海洋绿"),
        COLOR_LIMEGREEN ("#32CD32","酸橙绿"),
        COLOR_LIME ("#00FF00","酸橙色"),
        COLOR_FORESTGREEN ("#228B22","森林绿"),
        COLOR_GREEN ("#008000","纯绿"),
        COLOR_DARKGREEN ("#006400","深绿色"),
        COLOR_CHARTREUSE ("#7FFF00","查特酒绿"),
        COLOR_LAWNGREEN ("#7CFC00","草坪绿"),
        COLOR_GREENYELLOW ("#ADFF2F","绿黄色"),
        COLOR_OLIVEDRAB ("#556B2F","橄榄土褐色"),
        COLOR_BEIGE ("#F5F5DC","米色(浅褐色)"),
        COLOR_LIGHTGOLDENRODYELLOW ("#FAFAD2","浅秋麒麟黄"),
        COLOR_IVORY ("#FFFFF0","象牙"),
        COLOR_LIGHTYELLOW ("#FFFFE0","浅黄色"),
//        COLOR_YELLOW ("#FFFF00","纯黄"),    //给故事用
//        COLOR_OLIVE ("#808000","橄榄"),     //给任务用
        COLOR_DARKKHAKI ("#BDB76B","深卡其布"),
        COLOR_LEMONCHIFFON ("#FFFACD","柠檬薄纱"),
        COLOR_PALEGODENROD ("#EEE8AA","灰秋麒麟"),
        COLOR_KHAKI ("#F0E68C","卡其布"),
        COLOR_GOLD ("#FFD700","金"),
        COLOR_CORNISLK ("#FFF8DC","玉米色"),
        COLOR_GOLDENROD ("#DAA520","秋麒麟"),
        COLOR_FLORALWHITE ("#FFFAF0","花的白色"),
        COLOR_OLDLACE ("#FDF5E6","老饰带"),
        COLOR_WHEAT ("#F5DEB3","小麦色"),
        COLOR_MOCCASIN ("#FFE4B5","鹿皮鞋"),
        COLOR_ORANGE ("#FFA500","橙色"),
        COLOR_PAPAYAWHIP ("#FFEFD5","番木瓜"),
        COLOR_BLANCHEDALMOND ("#FFEBCD","漂白的杏仁"),
        COLOR_NAVAJOWHITE ("#FFDEAD","纳瓦霍白"),
        COLOR_ANTIQUEWHITE ("#FAEBD7","古代的白色"),
        COLOR_TAN ("#D2B48C","晒黑"),
        COLOR_BRULYWOOD ("#DEB887","结实的树"),
        COLOR_BISQUE ("#FFE4C4","(浓汤)乳脂,番茄等"),
        COLOR_DARKORANGE ("#FF8C00","深橙色"),
        COLOR_LINEN ("#FAF0E6","亚麻布"),
        COLOR_PERU ("#CD853F","秘鲁"),
        COLOR_PEACHPUFF ("#FFDAB9","桃色"),
        COLOR_SANDYBROWN ("#F4A460","沙棕色"),
        COLOR_CHOCOLATE ("#D2691E","巧克力"),
        COLOR_SADDLEBROWN ("#8B4513","马鞍棕色"),
        COLOR_SEASHELL ("#FFF5EE","海贝壳"),
        COLOR_SIENNA ("#A0522D","黄土赭色"),
        COLOR_LIGHTSALMON ("#FFA07A","浅鲜肉(鲑鱼)色"),
        COLOR_CORAL ("#FF7F50","珊瑚"),
        COLOR_ORANGERED ("#FF4500","橙红色"),
        COLOR_DARKSALMON ("#E9967A","深鲜肉(鲑鱼)色"),
        COLOR_TOMATO ("#FF6347","番茄"),
        COLOR_MISTYROSE ("#FFE4E1","薄雾玫瑰"),
        COLOR_SALMON ("#FA8072","鲜肉(鲑鱼)色"),
        COLOR_SNOW ("#FFFAFA","雪"),
        COLOR_LIGHTCORAL ("#F08080","淡珊瑚色"),
        COLOR_ROSYBROWN ("#BC8F8F","玫瑰棕色"),
        COLOR_INDIANRED ("#CD5C5C","印度红"),
//        COLOR_RED ("#FF0000","纯红"),
//        COLOR_BROWN ("#A52A2A","棕色"),
//        COLOR_FIREBRICK ("#B22222","耐火砖"),
//        COLOR_DARKRED ("#8B0000","深红色"),
//        COLOR_MAROON ("#800000","栗色"),
        COLOR_WHITE ("#FFFFFF","纯白"),
        COLOR_WHITESMOKE ("#F5F5F5","白烟"),
//        COLOR_GAINSBORO ("#DCDCDC","亮灰色"),
//        COLOR_LIGHTGREY ("#D3D3D3","浅灰色"),
        COLOR_SILVER ("#C0C0C0","银白色"),
//        COLOR_DARKGRAY ("#A9A9A9","深灰色"),
//        COLOR_GRAY ("#808080","灰色"),
//        COLOR_DIMGRAY ("#696969","暗淡的灰色"),
//        COLOR_BLACK ("#000000","纯黑"),
        ;

        private String hex;
        private String desc;

        private Colors(String hex, String desc) {
            this.hex = hex;
            this.desc = desc;
        }
    }
}
