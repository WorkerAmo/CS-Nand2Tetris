import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private Integer tableIndex =16;
    public SymbolTable table = new SymbolTable();
    Code code = new Code();

    Parser() {}

    // 清除所有注释
    public String clear(String commandLine) {
        String regex = "\\s+//[\\s\\S]+";
        return commandLine.replaceAll(regex, "");
    }

    // 判断指令类型
    public String commandType(String commandLine) {
        Pattern typeAPattern = Pattern.compile("@");
        Matcher aMatcher = typeAPattern.matcher(commandLine);
        if (aMatcher.find()) {
            return "A_COMMAND";
        }

        // 一开始我这里写的时候出了点问题，小括号没有用\\处理，导致匹配出错。
        Pattern typeLPattern = Pattern.compile("\\([\\w\\W]+\\)");
        Matcher lMatcher = typeLPattern.matcher(commandLine);
        if (lMatcher.find()) {
            return "L_COMMAND";
        }

        Pattern typeCPattern = Pattern.compile("[\\w\\d]+(=[\\w\\d+]+){0,1}");
        Matcher cMatcher = typeCPattern.matcher(commandLine);
        if (cMatcher.find()) {
            return "C_COMMAND";
        }

        return "";
    }

    // A L 指令转二进制地址
    public String symbolAddress(String commandLine){
        return table.getSymbolAddress(commandLine);
    }

    // C指令转二进制代码 dest 部分
    public String getDestBinaryCode(String commandLine) {
        return code.getDestCommand(commandLine);
    }

    // C指令转二进制代码 comp 部分
    public String getCompBinaryCode(String commandLine) {
        return code.getCompCommand(commandLine);
    }

    // C指令转二进制代码 jump 部分
    public String getJumpBinaryCode(String commandLine) {
        return code.getJumpCommand(commandLine);
    }

    // 综合函数：分解C指令组合上面函数，同时处理A指令
    public String getBinaryCode(String commandLine) {
        // 先判断指令是什么类型的
        String typeStr = this.commandType(commandLine);

        // 如果是A指令
        if (typeStr=="A_COMMAND"){
            commandLine=commandLine.replaceAll("@","");

            // Integer.valueOf(commandLine) == null && 这里要处理纯数字的情况
            // 无法适用于Rect.asm的BUG出自这里，没有处理好纯数字的情况。这里纯数字应该直接不执行的。
            boolean isNumber = commandLine.matches("[0-9]+");
            if (!table.containsSymbol(commandLine) && !isNumber) {
                // 条件都不满足意味着 table 内是没有的，得加
                table.addEntry(commandLine, tableIndex);
                this.tableIndex++;
            }
            // ACommand处理完成
            return this.symbolAddress(commandLine);
        }

        if (typeStr=="C_COMMAND") {
            String dest = "0";
            String comp = commandLine;
            String jump = "0";

            // 开始切割,以=作为分界线,先得到dest
            if (commandLine.contains("=")) {
                dest = commandLine.split("=")[0];
                comp = commandLine.split("=")[1];
            }

            // 再处理comp,通过分割 ; 实现
            if (commandLine.contains(";")) {
                String[] containor;
                containor = comp.split(";");
                comp = containor[0];
                jump = containor[1];
            }
            String binaryCode = "111"+this.getCompBinaryCode(comp)+this.getDestBinaryCode(dest)+this.getJumpBinaryCode(jump);
            return binaryCode;
        }
        // L-command不在这里处理。在main程序处理
        return "";
    }

    // 文件读取
    public ArrayList readtFile(File file) throws IOException, ParseException {
        ArrayList linesList = new ArrayList<>();
        InputStreamReader read = null;// 考虑到编码格式
        try {
            read = new InputStreamReader(new FileInputStream(file), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt = null;
        while ((lineTxt = bufferedReader.readLine()) != null) {
            // 1.剔除每行注释部分内容
                String newString = this.clear(lineTxt);
                lineTxt = lineTxt.replaceAll("//.*","");
                lineTxt = lineTxt.trim();
                if (lineTxt.length()!=0){
                    linesList.add(lineTxt);
                }
        }
        read.close();
        return linesList;
    }
}
