"use strict";
exports.__esModule = true;
var parser_1 = require("./parser");
// import code from './code'
var symbolTable_1 = require("./symbolTable");
var fs = require("fs");
var fileName = process.argv[2];
var newFile = '';
var data = fs.readFileSync(fileName + ".asm", "utf-8");
var lines = data.split("\n");
lines = lines.map(function (line) { return parser_1["default"].clear(line); }).filter(function (e) { return e; });
// 第一轮预处理（自定义符号处理）
// 该阶段主要是在符号表中简历每条命令以及其对应的地址,逐行处理整个汇编程序
// 构建符号表,每一行得用数字记录ROM地址,当命令最终被加载到地址中,这个数字从0开始
// 他就是PC计数器,他遇到注释行代码不自增,或者(XXX)这种 L-COMMAND 不自增,并且在符号表中将他们相关联
for (var pc = 0; pc < lines.length;) { // PC用来记录当前第几行指令
    var line = lines[pc]; // 获取当前指令
    var type = parser_1["default"].commandType(line.trim()); // 判断指令类型
    if (type === "L_COMMAND") { // 如果是L指令的话，直接在符号表添加，并把符号的地址也记录进去
        symbolTable_1["default"].addEntry(line.replace(/[\(\)]/g, ""), pc);
        lines.splice(pc, 1);
    }
    else {
        pc++; // L不算在PC计数范畴内。
    }
    //综合来说，这个步骤就是针对用户自定义的，不在符号表中的符号。简单来说就是自定义符号处理步骤。
}
;
// 第二轮真正处理
// 现在对整个程序进行处理,对每一行进行语法树分析
// 每次遇到符号变化A-指令时候,就对@xxx分析他是不是符号,如果能在符号表中查询到,则替换,
// 如果查询不到,则他就代表变量.为了处理这个变量
lines.forEach(function (line) { // 对每一行指令进行操作
    line = parser_1["default"].advance(line); // 根据对应的指令类型（A，C，L）返回二进制代码
    line && (newFile += line + "\r\n"); // 文件添加数据
});
fs.writeFileSync(fileName + ".hack", newFile); // 文件读写，输出二进制文件
