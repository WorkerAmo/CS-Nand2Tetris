import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();
        ArrayList commandsList;
        ArrayList outputList = new ArrayList();
        CodeWriter writer = new CodeWriter();

        File file = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/07/javaversion/src/SimpleAdd.vm");
        //File outputFile = new File("/Users/workeramo/LocalFolders/我的工作间/MyKnowledge/CS-Nand2Tetris/projects/06/pong/PongMyOut.hack");

        if (!file.exists()) {
            System.out.println("文件不存在!");
            return ;
        }

        try{
            commandsList = parser.readtFile(file);

            int commandPC = 0;
            for (int pc=0; pc<commandsList.size(); pc++) {
                String currentCommand = commandsList.get(pc).toString();
                String commandType = parser.commandType(currentCommand);

                String newLine = "";

                // 处理PUSH和POP
                if (commandType.contentEquals("C_PUSH") || commandType.contentEquals("C_POP")){
                    newLine = writer.writePushPop(commandType, currentCommand.split(" ")[1], Integer.valueOf(currentCommand.split(" ")[2].toString()));
                    outputList.add(newLine);
                    System.out.println(newLine);
                }

                // 处理Arithmetic
                if (commandType.contentEquals("C_ARITHMETIC")){
                    newLine = writer.writeArithmetic(currentCommand);
                    outputList.add(newLine);
                    System.out.println(newLine);
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
