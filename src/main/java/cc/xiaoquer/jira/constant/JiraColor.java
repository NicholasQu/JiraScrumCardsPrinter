package cc.xiaoquer.jira.constant;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nicholas on 2017/9/21.
 */
public enum JiraColor {
    STORY       ("#FFFF00","纯黄"),
    TASK        ("#808000","橄榄"),
    WHITE       ("#ffffff", "白色"),   //白
    SUBTASK     ("#7fa4d3", "子任务色"),   //
    ;

    private String hex;
    private String desc;

    private static final List<Colors> leftColors =  new ArrayList<>(); //剩余可选的颜色
    private static final Map<String, Colors> ownerColorMap = new ConcurrentHashMap<>();


    static{
        Collections.addAll(leftColors, Colors.values());
    }

    private JiraColor(String cssHex, String desc) {
        this.hex  = cssHex;
        this.desc = desc;
    }


    public String getHex() {
        return hex;
    }

    /**
     * 根据不同的人员设定不同的颜色
     * @param name 任务责任人
     * @return
     */
    public static String getColorByName(String name) {
        String needColorfulIssue1 = System.getenv("colorful");
        String needColorfulIssue2 = System.getProperty("colorful");

        //显性的注明colorful=1启动参数才会打印五颜六色的, 否则确认统一一种颜色
        if (!"1".equalsIgnoreCase(needColorfulIssue1) && !"1".equalsIgnoreCase(needColorfulIssue2)) {
            return SUBTASK.hex;
        }

        if (name == null || name.trim().length() == 0) {
            return WHITE.hex;
        }

        if (ownerColorMap.containsKey(name)) {
            return ownerColorMap.get(name).hex;
        }

        int leftColorCnt = leftColors.size();
        if (leftColorCnt == 0) {
            return WHITE.hex;
        }
        int rand = new Random().nextInt(leftColorCnt);
        Colors pickedColor = leftColors.get(rand);

        ownerColorMap.put(name, pickedColor);

        leftColors.remove(pickedColor);

//        Iterator<Colors> iterator = leftColors.iterator();
//        while (iterator.hasNext()) {
//            Colors c = iterator.next();
//            if (pickedColor.equals(c)) {
//                iterator.remove();
//                break;
//            }
//        }

        return pickedColor.hex;
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

    public static void main(String[] args) {
        for (Colors c : Colors.values()) {
            System.out.println(c.desc + " is " + (JiraColor.isColorDark(c.hex) ? "Dark" : "Light"));
        }
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
