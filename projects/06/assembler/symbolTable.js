"use strict";
exports.__esModule = true;
var Map = {
    SP: 0,
    LCL: 1,
    ARG: 2,
    THIS: 3,
    THAT: 4,
    R0: 0,
    R1: 1,
    R2: 2,
    R3: 3,
    R4: 4,
    R5: 5,
    R6: 6,
    R7: 7,
    R8: 8,
    R9: 9,
    R10: 10,
    R11: 11,
    R12: 12,
    R13: 13,
    R14: 14,
    R15: 15,
    'SCREEN': 16384,
    'KBD': 24576
};
var SymbolTable = /** @class */ (function () {
    function SymbolTable() {
    }
    // 将 symbol和 address 配对加入字符表
    SymbolTable.addEntry = function (symbol, address) {
        Map[symbol] = address;
    };
    // 符号表是否包含了指定的symbol
    SymbolTable.contains = function (symbol) {
        return Map.hasOwnProperty(symbol);
    };
    // 获取与 sybmol 相关的地址
    SymbolTable.getAddress = function (symbol) {
        symbol = symbol.replace(/@/, ""); // A和L指令进来后剥离@
        if (this.contains(symbol)) {
            symbol = Map[symbol];// 如果是LCL等内置名称那么symbol就会被替换为1等MAP内对应的值。
        }
        var res = "0000000000000000" + parseInt(symbol).toString(2);
        // LCL结果为1，结果为 '00000000000000001'
        // THAT的结果是4 结果为 '0000000000000000100'
        return res.substring(res.length - 16); // 保证长度始终为16，剔除前面多余的0。THAT结果为0000000000000100。前面的3个0被剔除了。
    };
    return SymbolTable;
}());
exports["default"] = SymbolTable;
