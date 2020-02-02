import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();
        ArrayList commandsList;

        File file = new File("/Users/workeramo/LocalFolders/我的工作间/计算机基础资料/计算机组成原理/Coursera/nandgithubdemo/CS-Nand2Tetris/projects/06/javaversion/src/Rect.asm");
        if (!file.exists()) {
            System.out.println("文件不存在!");
            return ;
        }
        try {
            commandsList = parser.readtFile(file);
            // 第一步：处理自定义符号
            int commandPC = 0;
            for (int pc=0; pc<commandsList.size(); pc++) {
                //System.out.println(commandsList.get(pc));
                // 获取当前指令
                String currentCommand = commandsList.get(pc).toString();
                String type = parser.commandType(currentCommand);
                // 处理 L-command
                if (type=="L_COMMAND") {
                    // 再提前判断下，如果是L指令，去除下括号
                    if (parser.commandType(currentCommand)=="L_COMMAND") {
                        currentCommand = currentCommand.replaceAll("\\(","");
                        currentCommand = currentCommand.replaceAll("\\)","");
                    }
                    // 坑：这里很坑很坑的是PC作为地址的计数器，你在处理不止一个的自定义 symbol 的时候，之前的序列是不能算进来的。所以之前的(XXX)得删除来计算PC
                    parser.table.addEntry(currentCommand, commandPC);
                }else {
                    commandPC +=1;
                }
            }

            // 第二步：正式翻译为二进制
            for (int index=0; index<commandsList.size(); index++) {
                String currentCommand = commandsList.get(index).toString();
                System.out.println(currentCommand);
                System.out.println(parser.getBinaryCode(currentCommand));

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

