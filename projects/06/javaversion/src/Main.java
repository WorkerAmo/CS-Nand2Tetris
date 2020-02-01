import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();

        File file = new File("/Users/workeramo/LocalFolders/我的工作间/计算机基础资料/计算机组成原理/Coursera/nandgithubdemo/CS-Nand2Tetris/projects/06/javaversion/src/Max.asm");
        if (!file.exists()) {
            System.out.println("文件不存在!");
            return ;
        }
        try {
            System.out.println(parser.readtFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

