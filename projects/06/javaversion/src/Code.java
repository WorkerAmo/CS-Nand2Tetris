import java.util.HashMap;
import java.util.Map;

public class Code {
    private Map compTable;
    private Map destTable;
    private Map jumpTable;

    Code(){
        this.compTable = new HashMap();
        this.compTable.put("","0001100");
        this.compTable.put("0","0101010");
        this.compTable.put("1","0111111");
        this.compTable.put("-1","0111010");
        this.compTable.put("D","0001100");
        this.compTable.put("A","0110000");
        this.compTable.put("M","1110000");
        this.compTable.put("!D","0001101");
        this.compTable.put("!A","0110001");
        this.compTable.put("!M","1110001");
        this.compTable.put("-D","0001111");
        this.compTable.put("-A","0110011");
        this.compTable.put("-M","1110011");
        this.compTable.put("D+1","0011111");
        this.compTable.put("A+1","0110111");
        this.compTable.put("M+1","1110111");
        this.compTable.put("D-1","0001110");
        this.compTable.put("A-1","0110010");
        this.compTable.put("M-1","1110010");
        this.compTable.put("D+A","0000010");
        this.compTable.put("D+M","1000010");
        this.compTable.put("D-A","0010011");
        this.compTable.put("D-M","1010011");
        this.compTable.put("A-D","0000111");
        this.compTable.put("M-D","1000111");
        this.compTable.put("D&A","0000000");
        this.compTable.put("D&M","1000000");
        this.compTable.put("D|A","0010101");
        this.compTable.put("D|M","1010101");

        this.destTable = new HashMap();
        this.destTable.put("0","000");
        this.destTable.put("M","001");
        this.destTable.put("D","010");
        this.destTable.put("MD","011");
        this.destTable.put("A","100");
        this.destTable.put("AM","101");
        this.destTable.put("AD","110");
        this.destTable.put("ADM","111");

        this.jumpTable = new HashMap();
        this.jumpTable.put("000","000");
        this.jumpTable.put("0","000");
        this.jumpTable.put("JGT","001");
        this.jumpTable.put("JEQ","010");
        this.jumpTable.put("JGE","011");
        this.jumpTable.put("JLT","100");
        this.jumpTable.put("JNE","101");
        this.jumpTable.put("JLE","110");
        this.jumpTable.put("JMP","111");
    }

    // 获得dest的二进制数据 3bit
    public String getDestCommand(String commandLine) {
        return this.destTable.get(commandLine).toString();
    }

    // 获得comp的二进制数据 7bit
    public String getCompCommand(String commandLine) {
        return this.compTable.get(commandLine).toString();
    }

    // 获得jump的二进制数据 3bit
    public String getJumpCommand(String commandLine) {
        return this.jumpTable.get(commandLine).toString();
    }
}
