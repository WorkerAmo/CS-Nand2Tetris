import java.util.HashMap;
import java.util.Map;

// 整个表都在处理符号
public class SymbolTable {
    private Map addressMap;

    SymbolTable() {
        this.addressMap = new HashMap();
        this.addressMap.put("SP",0);
        this.addressMap.put("LCL",1);
        this.addressMap.put("ARG",2);
        this.addressMap.put("THIS",3);
        this.addressMap.put("THAT",4);
        this.addressMap.put("R0",0);
        this.addressMap.put("R1",1);
        this.addressMap.put("R2",2);
        this.addressMap.put("R3",3);
        this.addressMap.put("R4",4);
        this.addressMap.put("R5",5);
        this.addressMap.put("R6",6);
        this.addressMap.put("R7",7);
        this.addressMap.put("R8",8);
        this.addressMap.put("R9",9);
        this.addressMap.put("R10",10);
        this.addressMap.put("R11",11);
        this.addressMap.put("R12",12);
        this.addressMap.put("R13",13);
        this.addressMap.put("R14",14);
        this.addressMap.put("R15",15);
        this.addressMap.put("SCREEN",16384);
        this.addressMap.put("KBD",24576);
    }

    // 添加自定义symbol(不在上面构造map中的)
    public void addEntry(String symbol, Integer address){
        this.addressMap.put(symbol, address);
    }

    // 判断是不是包含了指定的Symbol
    public boolean containsSymbol(String symbol){
        return  this.addressMap.containsKey(symbol);
    }

    // 计算地址用于拼接二进制文件
    public  String getSymbolAddress(String symbol){
        // 把A指令@剔除,为了查询MAP用。不剔除不好查
        symbol = symbol.replaceAll("@","");
        // 查询MAP
        if (this.addressMap.containsKey(symbol)) {
            symbol = String.valueOf(this.addressMap.get(symbol));
        }
        Integer symbolValue = Integer.parseInt(symbol);

        String result = "0000000000000000"+Integer.toBinaryString(symbolValue);
        return result.substring(result.length()-16);
    }

}
