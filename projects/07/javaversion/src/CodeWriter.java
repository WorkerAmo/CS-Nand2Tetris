// VM指令翻译成HACK汇编指令
// 生成的汇编指令写入相应的输出文件 .asm

import java.util.HashMap;
import java.util.Map;

public class CodeWriter {

    private Map operationSymbol;
    private Map segmentSymbol;

    // 构造函数中初始化表
    CodeWriter(){
        this.operationSymbol = new HashMap();
        this.operationSymbol.put("add","+");
        this.operationSymbol.put("sub","-");
        this.operationSymbol.put("not","!");
        this.operationSymbol.put("neg","-");
        this.operationSymbol.put("and","&");
        this.operationSymbol.put("or","|");

        this.segmentSymbol = new HashMap();
        this.segmentSymbol.put("local","LCL");
        this.segmentSymbol.put("argument","ARG");
        this.segmentSymbol.put("this","THIS");
        this.segmentSymbol.put("pointer0","THIS");
        this.segmentSymbol.put("pointer1","THAT");
        this.segmentSymbol.put("temp0","5");
        this.segmentSymbol.put("temp0","6");
        this.segmentSymbol.put("temp0","7");
        this.segmentSymbol.put("temp0","8");
        this.segmentSymbol.put("temp0","9");
        this.segmentSymbol.put("temp0","10");
        this.segmentSymbol.put("temp0","11");
        this.segmentSymbol.put("temp0","12");
        this.segmentSymbol.put("static","16");
    }

    // VM的算术符号和指令转逻辑代码
    public String writeArithmetic(String commandLine){

        if (commandLine.contains("add") || commandLine.contains("sub") || commandLine.contains("or") || commandLine.contains("and"))
        {
            return "\n@SP\n" +
                    "M=M-1\n" +
                    "A=M\n" +
                    "D=M\n" +
                    "@SP\n" +
                    "M=M-1\n" +
                    "A=M\n" +
                    "M=M" + this.operationSymbol.get(commandLine) + "D\n" +
                    "@SP\n" +
                    "M=M+1\n";
        }
        if (commandLine.contentEquals("neg")||commandLine.contentEquals("not")){
            return "\n@SP\n" +
                    "A=M-1\n" +
                    "M=" + this.operationSymbol.get(commandLine) +
                    "M\n";
        }

        if (commandLine.contentEquals("eq")){
            // 生成随机三位数
            int rn = (int)Math.round((Math.random()*1000));// 反正就三位

            return  "\n@SP\n" +
                    "A=M-1\n" +
                    "D=M\n" +
                    "@SP\n" +
                    "M=M-1\n" +
                    "A=M-1\n" +
                    "D=M-D\n" +
                    "@GOTO_TRUE" + rn +
                    "\nD;J"
                    + commandLine.toUpperCase() +
                    "\n@GOTO_FALSE" + rn
                    + "\n0;JMP\n" +
                    "(GOTO_TRUE" + rn + ")\n" +
                    "@SP\n" +
                    "A=M-1\n" +
                    "M=-1\n" +
                    "@OUT" + rn +
                    "\n0;JMP" +
                    "\n(GOTO_FALSE" + rn + ")\n" +
                    "@SP\n" +
                    "A=M-1\n" +
                    "M=0\n" +
                    "(OUT" + rn + ")\n";
        }
        return "ERROR";
    }

    // VM的指令转汇编
    public String writePushPop(String commandLine, String segment, int index){

        // 确定是PUSH或POP指令
        if (commandLine.contentEquals("C_PUSH")){

            // PUSH的是constant
            if (segment.contentEquals("constant")){
                // 将 index 压入栈中 256-2047
                return "\n@" + index + "\n" +
                        "D=A\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n";
            }

            // PUSH的是that this local argument
            if (    segment.contentEquals("that")  ||
                    segment.contentEquals("this")  ||
                    segment.contentEquals("local") ||
                    segment.contentEquals("argument")){

                String loopString = "A=A+1\n";
                for (;index>0;index--){
                    loopString += "A=A+1\n";
                }

                return "\n@" + this.segmentSymbol.get(segment)+ "\n" +
                        "A=M\n" + loopString + "\n" + //  结合这里可以看出来，等于从THIS或者THAT等后面延续第几个开始
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n";
            }

            // PUSH的是TEMP和POINTER
            if (    segment.contentEquals("temp")  ||
                    segment.contentEquals("pointer")  ){
                return "\n@" + this.segmentSymbol.get(segment+index)+ "\n" + // 处理temp5~12、pointer0~1的
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n " +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n";
            }

            // PUSH 的是Static
            if (    segment.contentEquals("static")){
                // segmentSymbol中的static是从16开始的
                int staticNum = Integer.valueOf(this.segmentSymbol.get("static").toString());
                return "\n@" + String.valueOf(staticNum+index) + "\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n";
            }
        }

        // 处理POP
        if (commandLine.contentEquals("C_POP")){

            // POP 的是TEMP和POINTER
            if (    segment.contentEquals("temp")  ||
                    segment.contentEquals("pointer")  ){
                return "\n@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@" + this.segmentSymbol.get(segment+index)+ "\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M-1\n";
            }

            // POP 的是that this local argument
            if (    segment.contentEquals("that")  ||
                    segment.contentEquals("this")  ||
                    segment.contentEquals("local") ||
                    segment.contentEquals("argument")) {
                // 栈顶推出到local.0中
                String loopString = "A=A+1\n";
                for (;index>0;index--){
                    loopString += "A=A+1\n";
                }

                return  "\n@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@" +this.segmentSymbol.get(segment) + "\n" +
                        "A=M\n" +
                        loopString + "\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M-1\n";
            }

            // POP 的是Static
            if (    segment.contentEquals("static")){
                // pop 从16 开始, 将当前栈顶数据push到static
                int staticNum = Integer.valueOf(this.segmentSymbol.get("static").toString());
                return "\n@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@" + String.valueOf(staticNum+index) + "\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M-1\n";
            }

        }
        return  "ERROR";
    }
}

