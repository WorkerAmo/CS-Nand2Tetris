import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();
        ArrayList commandsList;

        //File file = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/06/javaversion/src/Rect.asm");
        //File outputFile = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/06/javaversion/src/Rect.hack"); // Test passed.

        File file = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/06/pong/Pong.asm");
        File outputFile = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/06/pong/PongMyOut.hack");

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
                    // 注意点：SymbolTable我第一次写的时候创建了2个，一定记得要保持是在一个里面。
                    parser.table.addEntry(currentCommand, commandPC);
                }else {
                    commandPC +=1;
                }
            }

            // 第二步：正式翻译为二进制并写入文件

            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            BufferedWriter bw = new BufferedWriter(writer);
            for (int index=0; index<commandsList.size(); index++) {
                String currentCommand = commandsList.get(index).toString();
                //System.out.println(currentCommand);
                System.out.println();
                String binaryCodeLine = parser.getBinaryCode(currentCommand);
                if (binaryCodeLine.length()>0){
                    bw.write(binaryCodeLine);
                    bw.newLine();
                }else {
                    System.out.println("有空格");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

