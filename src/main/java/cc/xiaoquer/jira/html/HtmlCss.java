package cc.xiaoquer.jira.html;

/**
 * Created by Nicholas on 2017/9/6.
 */
public class HtmlCss {
    public static final String COMMON = "\n" +
            " body  { margin: 0; font-family: Arial, Helvetica, sans-serif; font-size:18px; } \n" +
            " .kanban_name {font-style:italic;} \n" +
            " .parent_name {font-style:bold;} \n" +
            " .issue_name {font-style:bold; font-size:26px} \n" +
            " .owner_name {font-style:bold; font-size:26px} \n" +
//            " table { page-break-inside:auto } \n" +
//            " tr    { page-break-inside:avoid; page-break-after:auto } \n" +
//            " td    { page-break-inside:avoid; page-break-after:auto } \n" +
//            " thead { display:table-header-group } \n" +
//            " tfoot { display:table-header-group } \n" +
//            " tbody { display: block; page-break-after: always; page-break-before: avoid; page-break-inside:avoid; } \n" +
            " \n";

    private static final String CARD = "\n" +
            "#cardview  \n" +
            "{  " +
            "    font-family: Lucida Sans Unicode, Lucida Grande, Sans-Serif;  " +
            "    font-size: 18px;  " +
//            "    margin: 10px;  " +
            "    width: 500px;  " +
            "    color: @COLOR_ON_BACKGROUND@;  " +
//            "    text-align: left;  " +
            "    border-collapse: collapse;  " +
            "    border-color: black;" +
            "    border-style: solid;" +
            "    border-width: 5px;" +
            "    page-break-inside:avoid; " +
            "}  \n" +
            "#cardview thead tr  \n" +
            "{  " +
//            "    background: url('table-images/pattern-head.png');  " +
            "}  \n" +
            "#cardview th  \n" +
            "{  " +
            "    font-size: 18px;  " +
            "    font-weight: normal;  " +
            "    padding: 0px;  " +
//            "    border-bottom: 2px solid #fff;  " +
//            "    color: #039;  " +
            "    border-color: #000;" +
            "    border-style: none;" +
            "    border-width: 0px;" +
            "}  \n" +
            "#cardview td \n " +
            "{  " +
            "    font-size: 18px;  " +
            "    padding: 0px;   " +
//            "    border-bottom: 1px solid #fff;  " +
            "    color: ;  " +
//            "    border-top: 1px solid transparent;  " +
            "    border-color: black;" +
            "    border-style: solid;" +
            "    border-width: 5px;" +
            "} \n " +
//            "#cardview tbody tr:hover td  " +
//            "{  " +
//            "    color: #339;  " +
//            "    background: #fff;" +
//            "} " +
            "\n";

    //浅色卡片，黑色字体
    public static final String CARD_LIGHT = CARD
            .replaceAll("#cardview", "#cardview_light")
            .replaceAll("@COLOR_ON_BACKGROUND@", "black");
    //深色卡片，白色字体
    public static final String CARD_DARK = CARD
            .replaceAll("#cardview", "#cardview_dark")
            .replaceAll("@COLOR_ON_BACKGROUND@", "white");

    /**
     * https://github.com/cognitom/paper-css/blob/master/examples/a4.html
     * https://cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.css
     * https://cdnjs.cloudflare.com/ajax/libs/paper-css/0.2.3/paper.css
     *
     **/
    public static final String PAPER =
            ".sheet {\n" +
            "    margin: 0;\n" +
            "    overflow: hidden;\n" +
            "    position: relative;\n" +
            "    box-sizing: border-box;\n" +
            "    -moz-box-sizing: border-box;\n" +      /* Firefox */
            "    -webkit-box-sizing: border-box;\n" +   /* Safari */
            "    display: inline-table;\n" +            //最最关键的配置！！！！！！！！！！！！！！不能用block
            "    page-break-inside: avoid;\n" +
            "    page-break-after: always;\n" +
            "    vertical-align: top;\n" +
            "}\n" +
            "\n" +
            "/** Paper sizes **/\n" +
            "body.A3           .sheet { width: 297mm; height: 419mm }\n" +
            "body.A3.landscape .sheet { width: 420mm; height: 296mm }\n" +
            "body.A4           .sheet { width: 210mm; height: 296mm }\n" +
            "body.A4.landscape .sheet { width: 297mm; height: 209mm }\n" +
            "body.A5           .sheet { width: 148mm; height: 209mm }\n" +
            "body.A5.landscape .sheet { width: 210mm; height: 147mm }\n" +
            "\n" +
            "/** Padding area **/\n" +
            ".sheet.padding-10mm { padding: 10mm }\n" +
            ".sheet.padding-15mm { padding: 15mm }\n" +
            ".sheet.padding-20mm { padding: 20mm }\n" +
            ".sheet.padding-25mm { padding: 25mm }\n" +
            "\n" +
            "/** For screen preview **/\n" +
            "@media screen {\n" +
            "    body { background: #e0e0e0 }\n" + //#e0e0e0 }\n" +
            "    .sheet {\n" +
            "       background: white;\n" +
            "       box-shadow: 0.5mm 2mm rgba(75, 74, 75, 1.000);\n" +
            "       margin: 5mm;\n" +
            "       vertical-align: top;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "/** Fix for Chrome issue #273306 **/\n" +
            "@media print {\n" +
            "             body.A3.landscape { width: 420mm }\n" +
            "    body.A3, body.A4.landscape { width: 297mm }\n" +
            "    body.A4, body.A5.landscape { width: 210mm }\n" +
            "    body.A5                    { width: 148mm }\n" +
            "}";

}
