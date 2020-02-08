import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;

public class Parser {

    Parser() {}

    // 清除所有注释
    public String clear(String commandLine) {
        String regex = "\\s+//[\\s\\S]+";
        return commandLine.replaceAll(regex, "");
    }

    // 判断指令是不是为空
    public boolean hasMoreCommands(String commandLine) {
        if (commandLine!=null) {
            return  true;
        }
        return  false;
    }

    // 判断指令是不是POP PUSH类型的
    public String commandType(String commandLine) {
        if (commandLine.contains("pop")) {
            return "C_POP";
        }
        if (commandLine.contains("push")) {
            return "C_PUSH";
        }
        return "C_ARITHMETIC";
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
