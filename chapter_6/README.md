前半本书都是描述构建一个计算机硬件平台。第二部分专注于计算机软件层级。最终目的是开发一个简单的，基于面向对象语言的编译器和基础的计算机系统。

整个章节最基础和重要的模组就是编译器。

在之前的第四章从汇编和二进制表述展示了机器语言。这个章节描述了编译器是如何系统性的将程序从之前的状态翻译成后面状态的。（从汇编到二进制）。

随着这个章节的展开，我们解释了如何开发一个HACK编译器。这个编译器可以生成可以在之前第五章中构建的硬件平台上运行的二进制代码。

因为汇编指令和对应的二进制代码的对应关系很直接，这就意味着写一个使用高级语言的汇编器不是什么难事。

允许汇编程序使用对内存地址的符号引用会引起复杂的问题。汇编器会被赋予管理这些用户定义的符号的期望，还要把他们解析到对应的物理存储地址。

通常会使用符号表去解决这个问题。**符号表**是解决许多软件翻译项目要用到的经典的数据结构。

和往常一样，HACK汇编器并不是终点。它简单且精确的演示了构建任何编译器需要用到的关键的软件工程原则。

此外，编写一个编译器是这本书剩余的七个软件开发项目的第一个。并不像之前用HDL描述的硬件项目。构建翻译程序的软件项目可以用任何编程语言实现。在每个项目中，我们都会提供自然语言的API，和每一步测试计划。这些项目的每一个都会以编译器开头，却都是独立的模组，都可以独立测试。（孤岛）。



从本章节开始讨论软件阶层体系.6-12 章节致力于构建能够编译简单**面向对象语言开发的编译器**和基本的**操作系统**为止.在软件体系中最基本的模块就是**编译器(assembler)**,前面说过机器语言的两种表现形式,**汇编形式**和**二进制形式**,

本章节用来构建一个汇编编译器.目的在于将

```asm
@1024
D=A
```

转换成 16 位的二进制编码

```bash
0000 0010 0000 0000 // a指令 instruction[14] => 1024
1110 xxxx xx00 0000 // c指令
```

[汇编编译器跳转链接](../projects/06/assembler)



## 6.1 背景

机器语言通常以2种形式指定：符号和二进制。

 The binary codes—for example, 110000101000000110000000000000111—represent actual machine instructions, as understood by the underlying hardware. 

例如110000101000000110000000000000111的二进制代码，代表实际的机器指令，可以被底层硬件理解。



For example, the instruction’s leftmost 8 bits can represent an operation code, say LOAD, the next 8 bits a register, say R3, and the remaining 16 bits an address, say 7. 

例如，指令的最左侧8位可以被理解为操作码，LOAD。后面的8位表示寄存器R3。剩余的16位表示地址，7.

LOAD R3 7



 Depending on the hardware’s logic design and the agreed-upon machine language, the overall 32-bit pattern can thus cause the hardware to effect the operation “load the contents of Memory[7] into register R3.” 

依赖硬件的逻辑设计和协定的计算机语言。整个32位模式可以导致硬件实现“加载内存地址为7的内容到R3寄存器”操作。

Modern computer platforms support dozens if not hundreds of such elementary operations. 

现代计算机平台支持数十种甚至上百种此类基本操作。

Thus, machine languages can be rather complex, involving many operation codes, different memory addressing modes, and various instruction formats

因此，机器语言可能非常复杂，涉及许多的操作代码，不同的存储地址模式和多种指令格式。



One way to cope with this complexity is to document machine instructions using an agreed-upon syntax, say LOAD R3,7 rather than 110000101000000110000000000000111.

一个应对这个复杂性的方法就是使用商定好的语法来记录机器指令。例如用LOAD R3,7 ，而不是110000101000000110000000000000111。

And since the translation from symbolic notation to binary code is straightforward, it makes sense to allow low-level programs to be written in symbolic notation and to have a computer program translate them into binary code. 

而且，将符号表示法转化为二进制代码是直接的。因此，允许符号表示法编写低级程序，并让计算机程序将其转化为二进制代码是有意义的。



The symbolic language is called assembly, and the translator program assembler. The assembler parses each assembly command into its underlying fields, translates each field into its equivalent binary code, and assembles the generated codes into a binary instruction that can be actually executed by the hardware.

这里的符号语言称为汇编语言。翻译器称为汇编器。

汇编器解析每个汇编指令为其基础字段。将每个字段翻译成等效的二进制代码，并且将生成的代码组合为硬件可以实际执行的二进制指令。



### **Symbols** 符号

Binary instructions are represented in binary code.

二进制指令是以二进制代码形式展现的。

 By definition, they refer to memory addresses using actual numbers. 

根据定义，他们使用实际的数字来引用内存地址。

For example, consider a program that uses a variable to represent the weight of various things, and suppose that this variable has been mapped on location 7 in the computer’s memory.

例如，想象一个使用变量去表示不同事物重量的程序。并且假设这个变量已经映射到内存的位置7.



 At the binary code level, instructions that manipulate the weight variable must refer to it using the explicit address 7. 



Yet once we step up to the assembly level, we can allow writing commands like LOAD R3,weight instead of LOAD R3,7. In both cases, the command will effect the same operation: “set R3 to the contents of Memory[7].” In a similar fashion, rather than using commands like goto 250, assembly languages allow commands like goto loop, assuming that somewhere in the program the symbol loop is made to refer to address 250. In general then, symbols are introduced into assembly programs from two sources:

■ *Variables:* The programmer can use symbolic variable names, and the translator will “automatically” assign them to memory addresses. Note that the actual values of these addresses are insignificant, so long as each symbol is resolved to the same address throughout the program’s translation.

■ *Labels:* The programmer can mark various locations in the program with symbols. For example, one can declare the label loop to refer to the beginning of a certain code segment. Other commands in the program can then goto loop, either conditionally or unconditionally.







