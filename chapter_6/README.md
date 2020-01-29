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

**机器语言通常以2种形式指定：符号和二进制。**

 The binary codes—for example, 110000101000000110000000000000111—represent actual machine instructions, as understood by the underlying hardware. 

例如110000101000000110000000000000111的二进制代码，代表实际的机器指令，可以被底层硬件理解。



For example, the instruction’s leftmost 8 bits can represent an operation code, say LOAD, the next 8 bits a register, say R3, and the remaining 16 bits an address, say 7. 

**例如，指令的最左侧8位可以被理解为操作码，LOAD。后面的8位表示寄存器R3。剩余的16位表示地址，7.**

**LOAD R3 7**



 Depending on the hardware’s logic design and the agreed-upon machine language, the overall 32-bit pattern can thus cause the hardware to effect the operation “load the contents of Memory[7] into register R3.” 

依赖硬件的逻辑设计和协定的计算机语言。整个32位模式可以导致硬件实现“加载内存地址为7的内容到R3寄存器”操作。

Modern computer platforms support dozens if not hundreds of such elementary operations. 

现代计算机平台支持数十种甚至上百种此类基本操作。

Thus, machine languages can be rather complex, involving many operation codes, different memory addressing modes, and various instruction formats

**因此，机器语言可能非常复杂，涉及许多的操作代码，不同的存储地址模式和多种指令格式。**



One way to cope with this complexity is to document machine instructions using an agreed-upon syntax, say LOAD R3,7 rather than 110000101000000110000000000000111.

**一个应对这个复杂性的方法就是使用商定好的语法来记录机器指令。例如用LOAD R3,7 ，而不是110000101000000110000000000000111。**

And since the translation from symbolic notation to binary code is straightforward, it makes sense to allow low-level programs to be written in symbolic notation and to have a computer program translate them into binary code. 

**而且，将符号表示法转化为二进制代码是直接的。因此，允许符号表示法编写低级程序，并让计算机程序将其转化为二进制代码是有意义的。**



The symbolic language is called assembly, and the translator program assembler. The assembler parses each assembly command into its underlying fields, translates each field into its equivalent binary code, and assembles the generated codes into a binary instruction that can be actually executed by the hardware.

**这里的符号语言称为汇编语言。翻译器称为汇编器。**

**汇编器解析每个汇编指令为其基础字段。将每个字段翻译成等效的二进制代码，并且将生成的代码组合为硬件可以实际执行的二进制指令。**



### **Symbols** 符号

Binary instructions are represented in binary code.

**二进制指令是以二进制代码形式展现的。**

 By definition, they refer to memory addresses using actual numbers. 

**根据定义，他们使用实际的数字来引用内存地址。**

For example, consider a program that uses a variable to represent the weight of various things, and suppose that this variable has been mapped on location 7 in the computer’s memory.

**例如，想象一个使用变量去表示不同事物重量的程序。并且假设这个变量已经映射到内存的位置7.**



 At the binary code level, instructions that manipulate the weight variable must refer to it using the explicit address 7. 

**在二进制代码的级别，控制重量变量的指令必须x显式引用地址7.**

Yet once we step up to the assembly level, we can allow writing commands like LOAD R3,weight instead of LOAD R3,7. 

**一旦我们提高到汇编层级，我们就可以允许写雷系LOAD R3 weight 这样的指令而不是LOAD R3,7。**

**注释：这部分的意思就是地址7保存的变量的值，在二进制代码层面，就必须用实际地址表述，但是在汇编层面，因为更接近人类层面，所以我们可以使用weight替代地址7。**



In both cases, the command will effect the same operation: “set R3 to the contents of Memory[7].”

**两种情况下，指令都会实现同样的指令：“将内存地址为7的内容设为R3”。**

 In a similar fashion, rather than using commands like goto 250, assembly languages allow commands like goto loop, assuming that somewhere in the program the symbol loop is made to refer to address 250. 

**以类似的方式，而不是使用类似Goto250的指令，汇编语言允许使用goto loop这样的指令。假设程序中的某个地方，符号loop被引用到地址250.**

In general then, symbols are introduced into assembly programs from two sources:

**一般来说，符号是从2个源头引入到汇编程序里的。**（分别是变量和标签）

■ *Variables:* The programmer can use symbolic variable names, and the translator will “automatically” assign them to memory addresses. Note that the actual values of these addresses are insignificant, so long as each symbol is resolved to the same address throughout the program’s translation.

**变量：程序员可以使用符号化的变量名，然后翻译器会自动的把它们分配到内存地址。记住，实际变量的地址不重要，只要每个符号通过程序翻译之后可以指向同样的地址。**

解释&重点：

1.程序员为了操作方便可以给地址命名，也就是我们理解的变量名。翻译器会自动翻译到对应的地址。重要的是，这些名字，可以被正确的翻译。

2.执行人类使用的名字和地址一一对应需求的是翻译器：translator。

■ *Labels:* The programmer can mark various locations in the program with symbols. For example, one can declare the label loop to refer to the beginning of a certain code segment. Other commands in the program can then goto loop, either conditionally or unconditionally.

**程序员可以在程序的各种地方打标签。例如，我们可以声明loop标签引用到特定代码段的开头。程序中的其它指令就可以直接去loop位置。无论是有条件的还是没有条件的。**



The introduction of symbols into assembly languages suggests that assemblers must be more sophisticated than dumb text processing programs.

**汇编语言引入符号表明，汇编器一定比哑文本处理要复杂的多。**

 Granted, translating agreed-upon symbols into agreed-upon binary codes is not a complicated task. 

**当然，将符号翻译成二进制代码不是一个复杂的任务。**

At the same time, the mapping of user-defined variable names and symbolic labels on actual memory addresses is not trivial. In fact, this symbol resolution task is the first nontrivial translation challenge in our ascent up the software hierarchy from the hardware level. The following example illustrates the challenge and the common way to address it.

**与此同时，将用户定义的变量名和符号标签映射到实际物理地址并不简单。实际上，符号解析的任务是我们从硬件层级到软件层级的第一个重要翻译挑战。下面的示例说明了挑战和一般的方案。**



**Symbol Resolution** Consider figure 6.1, showing a program written in some self-explanatory low-level language. The program contains four user-defined symbols: two variable names (i and sum) and two labels (loop and end). How can we systematically convert this program into a symbol-less code?

**符号解析： 考虑下图6.1 。下图为用某种不言自明的底层语言展示的程序。此程序包含了四个用户自定义的符号：2个变量名，i和sum。2个标签，loop和end。我们改如何系统性的将程序转换成无符号的代码？**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbbf1mhs4yj30iq07ndgm.jpg)

**Figure 6.1** Symbol resolution using a symbol table. The line numbers are not part of the program—they simply count all the lines in the program that represent real instructions, namely, neither comments nor label declarations. Note that once we have the symbol table in place, the symbol resolution task is straightforward.

**符号解析依赖符号表。每行的number序号，并不是程序的一部分。他们只是标记程序行的数字，不包含标签（也就是说就标注程序）。记住，一旦我们有了符号表，符号的解析任务就是直接了当的了。**



We start by making two arbitrary game rules: The translated code will be stored in the computer’s memory starting at address 0, and variables will be allocated to memory locations starting at address 1024 (these rules depend on the specific target hardware platform).

**我们从制定2个规则开始：转化的代码将会被保存在电脑的内存地址为0开始的地方。变量将会被保存在地址为1024开始的地方。这些规则都依赖具体的硬件平台。**



 Next, we build a symbol table, as follows. For each new symbol xxx encountered in the source code, we add a line (*xxx*, n) to the symbol table, where n is the memory address associated with the symbol according to the game rules. After completing the construction of the symbol table, we use it to translate the program into its symbol-less version.

**下一步，我们将建立一个符号表。每在源代码中遇到新符号XXX，我们都会在符号表中添加一行(xxx,n)。n就是根据游戏规则中规定的和符号关联的内存地址。**



Note that according to the assumed game rules, variables i and sum are allocated to addresses 1024 and 1025, respectively. Of course any other two addresses will be just as good, so long as all references to i and sum in the program resolve to the same physical addresses, as indeed is the case. The remaining code is self-explanatory, except perhaps for instruction 6. This instruction terminates the program’s execution by putting the computer in an infinite loop.

**注意，根据假定的规则，变量i和sum都会被分别分配到内存地址为1024和1025的地方。只要程序中对i和sum的引用解析都指向同样的物理地址，任何其他2个地址都是可以的。（意思是只要解析对就可以，地址规则不重要）**

**剩余的代码都是不言自明的，除了指令6.这个指令通过将电脑搞进一个无限循环状态来结束程序的执行。**



Three comments are in order here. First, note that the variable allocation assumption implies that the largest program that we can run is 1,024 instructions long. Since realistic programs (like the operating system) are obviously much larger, the base address for storing variables will normally be much farther. Second, the assumption that each source command is mapped on one word may be naïve. Typically, some assembly commands (e.g., if i=101 goto end) may translate into several machine instructions and thus will end up occupying several memory locations. The translator can deal with this variance by keeping track of how many words each source command generates, then updating its “instruction memory counter” accordingly.

Finally, the assumption that each variable is represented by a single memory location is also naïve. Programming languages feature variables of different types, and these occupy different memory spaces on the target computer. For example, the C language data types short and double represent 16-bit and 64-bit numbers, respectively. When a C program is run on a 16-bit machine, these variables will occupy a single memory address and a block of four consecutive addresses, respectively. Thus, when allocating memory space for variables, the translator must take into account both their data types and the word width of the target hardware.

**这里有三条点：**

**1.记住变量分配假设意味着我们可以运行的最长的程序长度是1024个指令（1024条是极限，简单，没啥）当然现实的程序长度，例如操作系统，会很长。保存变量的内存基址也会远的多；**

**2.每个源指令映射到1个word的假设是不准确的。典型如，一些汇编指令会被翻译成几个机器指令( if i=101 goto end) 并因此占据几个内存地址。翻译器会通过追踪每个源指令将会生成多少word并且相应的更新它的“指令内存计数器”来解决这种变动（也就是每个汇编指令可能会生成好几个机器指令，不一定是1个。）；**

**3.每个变量都由单个的内存地址表示的假设也是不成熟的。编程语言有不同类型的变量。每种变量占据的空间大小是不一样的。例如，C语言的数据类型short和double类型分别占据16位和64位。当C程序运行在一个16位的机器上，这些变量将会分别占据单个内存地址和4个连续地址。因此，当给变量分配内存空间的时候。这些翻译器必须同时考虑目标硬件设备的数据类型和字宽。**



**The Assembler** Before an assembly program can be executed on a computer, it must be translated into the computer’s binary machine language. The translation task is done by a program called the assembler. The assembler takes as input a stream of assembly commands and generates as output a stream of equivalent binary instructions. The resulting code can be loaded as is into the computer’s memory and executed by the hardware.

**编译器**

**在一个汇编程序可以被电脑执行之前，它必须被翻译为电脑的二进制机器语言。编译器会完成这项任务。**

**编译器会将输入的汇编指令翻译成对应的二进制指令。翻译后的结果代码可以被计算机加载进内存，并被计算及硬件执行。**



We see that the assembler is essentially a text-processing program, designed to provide translation services. The programmer who is commissioned to write the assembler must be given the full documentation of the assembly syntax, on the one hand, and the respective binary codes, on the other. Following this contract—typically called machine language *specification*—it is not difficult to write a program that, for each symbolic command, carries out the following tasks (not necessarily in that order):

**我们可以看出来，编译器本质上就是一个被设计出来提供翻译服务的文本处理程序。**

**被委托写编译器的程序员，必须提供完整的汇编语法文件和相应的二进制代码。**

**根据这个规定，通常称为机器语言规范，编写针对每个符号指令执行下面任务的程序并不困难。任务见下：**



■ Parse the symbolic command into its underlying fields.

■ For each field, generate the corresponding bits in the machine language.

■ Replace all symbolic references (if any) with numeric addresses of memory locations.

■ Assemble the binary codes into a complete machine instruction.

**把符号命令解析为底层字段。**

**每个字段，生成对应的机器语言位。**

**存储位置的数字地址替换所有的符号引用。**

**二进制代码汇编为完整的机器指令。**



Three of the above tasks (parsing, code generation, and final assembly) are rather easy to implement. The fourth task—symbols handling—is more challenging, and considered one of the main functions of the assembler. This function was described in the previous section. The next two sections specify the Hack assembly language and propose an assembler implementation for it, respectively.

**上面的三个任务，（解析，生成代码，汇编），非常容易实现。第四个任务，符号处理，则更有挑战性。但是第四个任务也被认为是编译器的主要功能之一。这个功能在之前的部分已经被描述过了。下面的2个小节分别指明了HACK汇编语言并提出了一种汇编器的实现。**



# ***\*6.2 Hack Assembly-to-Binary Translation Specification\****

The Hack assembly language and its equivalent binary representation were specified in chapter 4.A compact and formal version of this language specification is repeated here, for ease of reference. This specification can be viewed as the contract that Hack assemblers must implement, one way or another.

**HACK编译器翻译规范**

**HACK汇编语言和对应的二进制表示在第四章节就指定了。**

**在此，我们重复一次这个语言规范的紧凑正式版。这个规范可以被视为HACK汇编器必须用一种或者另一种方式实现的规范。**



## **6.2.1 Syntax Conventions and File Formats**

**6.2.1 语法约定和文件格式**



**File Names** By convention, programs in binary machine code and in assembly code are stored in text files with “hack” and “asm” extensions, respectively. Thus, a Prog.asm file is translated by the assembler into a Prog.hack file.

**根据约定，二进制机器码和汇编码分别被存放在后缀为hack和asm的文本文件内。也就是说，Prog.asm文件会被编译器翻译成Prog.hack文件。**



**Binary Code (.hack) Files** A binary code file is composed of text lines. Each line is a sequence of 16 “0” and “1” ASCII characters, coding a single 16-bit machine language instruction. Taken together, all the lines in the file represent a machine language program. When a machine language program is loaded into the computer’s instruction memory, the binary code represented by the file’s *n*th line is stored in address n of the instruction memory (the count of both program lines and memory addresses starts at 0).

**关于hack文件**

**一个二进制代码文件由字符行组成。每行都由16个0和1组成的ASCII字符来编码单个的16位机器指令。**

**总而言之，文件的每一行都代表机器语言程序。**

**当机器语言程序被加载到指令内存中之后，二进制文件中第N行的指令会被保存在地址为n的指令内存中。（程序行数和指令内存地址的起始位都是0）**



**Assembly Language (.asm) Files** An assembly language file is composed of text lines, each representing either an instruction or a symbol declaration:

■ *Instruction:* an *A*-instruction or a *C*-instruction, described in section 6.2.2.

■ (Symbol): This pseudo-command binds the Symbol to the memory location into which the next command in the program will be stored. It is called “pseudocommand” since it generates no machine code.

(The remaining conventions in this section pertain to assembly programs only.)

**asm文件**

**没和汇编语言文件也都由字符行组成，每一行都代表一个指令或者符号申明**：

**指令：A指令和C指令，在6.2.2有描述**

**符号：这个伪指令会将符号绑定到下一条程序指令将要保存的内存位置中。**

**这个章节剩余的约定都仅属于汇编程序。**



**Constants and Symbols** *Constants* must be non-negative and are written in decimal notation. A user-defined symbol can be any sequence of letters, digits, underscore (_), dot (.), dollar sign ($), and colon (:) that does not begin with a digit. 

**Comments** Text beginning with two slashes (//) and ending at the end of the line is considered a comment and is ignored.

**White Space** Space characters are ignored. Empty lines are ignored.

**Case Conventions** All the assembly mnemonics must be written in uppercase. The rest (user-defined labels and variable names) is case sensitive. The convention is to use uppercase for labels and lowercase for variable names.

**常数和符号**

**常数必须是非负数并且要用十进制表示法。用户定义的符号可以是字符数字下划线，点，美金符和冒号，但是并不能以数字开头。**

**注释**

**文本以//开头并且在这行结束的都视为注释，编译器会忽略。**

**空格**

**空格会被忽略，空行也被忽略。**

**案例惯例**

**所有的汇编助记符都必须用大写书写。其余的，例如用户定义的标签和变量名都是区分大小写的。**

**规范是对标签都用大写，变量名都是用小写。**



## **6.2.2 Instructions**

The Hack machine language consists of two instruction types called addressing instruction (*A*-instruction) and compute instruction (*C*-instruction). The instruction format is as follows.



## 6.2.2 指令

**HACK机器语言由两种指令类型组成。**

**一种是Addressing Instruction，也就是A指令，是针对地址的指令。**

**另一种是Compute Instruction，也就是计算指令，C指令。**

**指令格式如下。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbc6nfups8j30f00793z1.jpg)

**补充：从图可以看出来，A指令和C指令都是由16bit组成。**

**注释：@value是A指令的一个示例。value既可以是非负的十进制数字，又可以使这个数字的符号引用。**

**16位的数据，第一位是0，后面的15个位表示值。**

**C指令的范例是dest=comp;jump**

**前三位为1，后面10位分为三部分。**



The translation of each of the three fields comp, dest, jump to their binary forms is specified in the following three tables.

**comp，dest，jump三个字段到二进制形式的翻译标准在下面三个表格中被制定。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbcimdeyzpj30b40gqt9e.jpg)

注释：这个怎么看呢？我们配合下图：

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbclbvqg4ij30ek01ut8o.jpg)

**comp包含2个部分，一个是a，一个是指令代码部分。**

**a的作用是什么呢？确定到底使用左边的还是右边的。这样只要通过a作为条件，一组c1~c6，就可以有2个选择。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbclmjps3dj30cq0bb3z3.jpg)



## **6.2.3 Symbols**

### 符号



**Predefined Symbols** Any Hack assembly program is allowed to use the following predefined symbols.

**预定义符号**

**任何HACK汇编语言程序都可以使用如下的预定义符号。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbcm7x2531j308805nglo.jpg)



Hack assembly commands can refer to memory locations (addresses) using either constants or symbols. Symbols in assembly programs arise from three sources.

**HACK汇编指令可以通过使用常量与符号引用内存地址。**

**汇编程序的符号有三个来源。**



**Label Symbols** The pseudo-command (Xxx) defines the symbol Xxx to refer to the instruction memory location holding the next command in the program. A label can be defined only once and can be used anywhere in the assembly program, even before the line in which it is defined.

 **标签符号**

**伪指令定义了符号XX指向指令内存地址中程序下个指令的地址。标签只可以被定义一次并且可以被汇编程序的任何地方使用，甚至是之前已经定义好的代码行。**



**Variable Symbols** Any symbol Xxx appearing in an assembly program that is not predefined and is not defined elsewhere using the (Xxx) command is treated as a variable. Variables are mapped to consecutive memory locations as they are first encountered, starting at RAM address 16 (0x0010).

**变量符号**

**汇编程序里面出现的任何不是预定义并且没有在其它地方使用指令定义的符号都被视为变量。**

**变量会被映射到内存地址中连续的地址，从RAM的0x0010（也就是16，从图中没看错的话R0~15都是寄存器？）开始计数。**



## **6.2.4 Example**

Chapter 4 presented a program that sums up the integers 1 to 100. Figure 6.2 repeats this example, showing both its assembly and binary versions.

**第四章展示了一个从1+到100的程序。图6.2重复了这个案例，展示了它的汇编码和机器码。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbcn96r0xtj30hp0eftam.jpg)



# ***\*6.3 Implementation\****

## 实现

The Hack assembler reads as input a text file named Prog.asm, containing a Hack assembly program, and produces as output a text file named Prog.hack, containing the translated Hack machine code. The name of the input file is supplied to the assembler as a command line argument:

**HACK汇编语言输入的是Prob.asm文本文件，包含HACK汇编程序，输出的文本文件叫prog.hack，包含被翻译的HACK机器代码。输入文件的名字会被提供给编译器作为指令行的参数。**

The translation of each individual assembly command to its equivalent binary instruction is direct and one-to-one. Each command is translated separately. In particular, each mnemonic component (field) of the assembly command is translated into its corresponding bit code according to the tables in section 6.2.2, and each symbol in the command is resolved to its numeric address as specified in section 6.2.3.

**每个独立汇编指令到相应的二进制指令的翻译过程是直接且一对一的。**

**每个指令是被分别翻译的。汇编指令中的助记符会根据6.2.2节的表被翻译到对应的字节码。**

**每个指令中的符号都会被解析到6.2.3制定的，其对应的数字化地址。**



We propose an assembler implementation based on four modules: a Parser module that parses the input, a *Code* module that provides the binary codes of all the assembly mnemonics, a *SymbolTable* module that handles symbols, and a main program that drives the entire translation process.

**我们提出一个由四个模组组成的汇编器实现。**

**Parser,解析模组解析输入。**

**Code,代码模组，提供汇编助记符的二进制代码。**

**SymbolTable，符号表模组，用来处理符号。**

**MainProgram，主程序，用来驱动整个翻译过程。**



**A Note about API Notation** The assembler development is the first in a series of five software construction projects that build our hierarchy of translators (*assembler*, *virtual machine, and compiler*). Since readers can develop these projects in the programming language of their choice, we base our proposed implementation guidelines on language independent APIs. A typical project API describes several modules, each containing one or more routines. In object-oriented languages like Java, C++, and C#, a module usually corresponds to a class, and a routine usually corresponds to a method. In procedural languages, routines correspond to functions, subroutines, or procedures, and modules correspond to collections of routines that handle related data. In some languages (e.g., Modula-2) a module may be expressed explicitly, in others implicitly (e.g., a *file* in the C language), and in others (e.g., Pascal) it will have no corresponding language construct, and will just be a conceptual grouping of routines.

**API表示法的注释**

**汇编器的开发是我们构建翻译器体系项目里5个软件的第一个。（汇编器，虚拟机，编译器）。**

**因为读者可以使用他们自己选择的语言开发西安航母，我们就基于我们提议的实现指南给出独立于语言的API。**（理解为语言们的协议吧，随你用JS还是JAVA）

**一个典型的项目API描述了下面几个模组，每个模组都包含一个或者多个事务。**

**面向对象语言，例如JAVA，C++和C#，一个模组经常对应到一个类。一个事务到一个方法。**

**在面向过程语言里，事务对应函数，子事务或者过程。模组对应一组处理相关数据的事务。**

**在有些语言里，一个模组可能被表述的很明确，在其他语言里，例如C语言，就会很模糊，而Pascal语言里，不会有对应的语言结构，只有事物在概念上的组合。**











