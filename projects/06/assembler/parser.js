"use strict"; // js里不放在第一行就无效了
// 功能语法分析器 parse 模块
// 用于对.asm汇编文件进行语法分析
// 将汇编命令分解成其所表达的内在含义
exports.__esModule = true;
var code_1 = require("./code"); // Code module
var symbolTable_1 = require("./symbolTable"); // sumboltable module
var varityIndex = 16; // 配合SymbolTable使用，传输给addEntry作为参数，确定地址的。因为0~15都已经被占用，后面被占用的是16384，所以这里需要从16开始计数。
// C-instruction:
// 1  1  1  a  c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3
// 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
// dest=comp;jump
var Parser = /** @class */ (function () {
    
    function Parser() {
    }
    
    // 清除注释后面的字符
    Parser.clear = function (line) {
        return line.replace(/\/\/[\s\S]+/, "").trim(); // 清空“//”后面所有的空格和字符
    };
    
    // 当前命令 返回机器码
    Parser.advance = function (line) {
        var _a;
        var type = this.commandType(line); // 这里直接跳到52行看。
        // @xxx 是符号, 或者 十进制数字
        if (type === "A_COMMAND") {
            line = line.replace(/@/, ""); // 如果是@xxx，要的就是xxx，去除@即可。
            if (isNaN(Number(line)) && !symbolTable_1["default"].contains(line)) { // 如果这个字符不是纯数字，并且，符号表里面也不包含的话，它就是变量了。
                // 所以这段处理的是用户的变量
                symbolTable_1["default"].addEntry(line, varityIndex);
                varityIndex++;
            }
            return this.symbol(line); // 看下方67行Parser.symbol
        }
        if (type === "C_COMMAND") { // 如果是计算指令，初始化值，
            var dest = "0";
            var comp = line;
            var jump = "0";
            if (line.match(/=/)) { // 然后看代码是不是包含=号。包含等号就将等号左侧的内容复制为dest，右侧内容赋值为comp。
                dest = line.split("=")[0];
                comp = line.split("=")[1];
            }
            if (line.match(/;/)) { // 如果有分号，继续分离得到jump。因为正常指令如果灭有后面的jump的话是不会有分号的。
                _a = comp.split(";"), comp = _a[0], jump = _a[1];
            }
            return "111" + this.comp(comp) + this.dest(dest) + this.jump(jump);// 按照顺序排列好二进制代码（二进制代码怎么来要靠code类了。）
        }
    };
    // ============== 命令类型 ===============
    Parser.commandType = function (line) {
        if (/@/.test(line)) {
            return "A_COMMAND";
        }
        // 伪命令 (xxx)
        if (/\([\w\W]+\)/.test(line)) {
            return "L_COMMAND";
        }
        // dest = comp;jump
        if (/[\w\d]+(\=[\w\d\+]+){0,1}/.test(line)) {
            return "C_COMMAND";
        }
    };
    // ============== 命令类型 ===============
    // A,L 命令时候调用
    Parser.symbol = function (line) {
        return symbolTable_1["default"].getAddress(line); // 见symbolTable的方法
    };
    // C 命令调用处理dest
    Parser.dest = function (command) {
        return code_1["default"].dest(command);
    };
    // C 命令调用处理comp
    Parser.comp = function (command) {
        return code_1["default"].comp(command);
    };
    // C 命令调用处理jump
    Parser.jump = function (command) {
        return code_1["default"].jump(command);
    };
    return Parser;
}());
exports["default"] = Parser;
