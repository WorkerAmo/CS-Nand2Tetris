# 7. **Virtual Machine I: Stack Arithmetic**

**第七章 虚拟机1：堆栈技术**



This chapter describes the first steps toward building a compiler for a typical object-based high-level language. We will approach this substantial task in two stages, each spanning two chapters. High-level programs will first be translated into an intermediate code (chapters 10—11), and the intermediate code will then be translated into machine language (chapters 7-8). This two-tier translation model is a rather old idea that goes back to the 1970s. Recently, it made a significant comeback following its adoption by modern languages like Java and C#.

**这个章节描述了通往构建面向对象高级语言编译器的第一步。**

**我们将分2个阶段完成这个重大的任务，每个阶段横跨2个章节。**

**高级程序首先会被翻译成中间代码，中间代码会被翻译成机器语言。这样的2翻译模型是一个可以追溯到1970年代很老的模型。而最近，随着被JAVA和C#采用，这个2层模型大受欢迎。**



The basic idea is as follows: Instead of running on a real platform, the intermediate code is designed to run on a Virtual Machine. The VM is an abstract computer that does not exist for real, but can rather be realized on other computer platforms. There are many reasons why this idea makes sense, one of which being code transportability. Since the VM may be implemented with relative ease on multiple target platforms, VM-based software can run on many processors and operating systems without having to modify the original source code. The VM implementations can be realized in several ways, by software interpreters, by special-purpose hardware, or by translating the VM programs into the machine language of the target platform.

**基础想法如下：中间代码被设计成执行在虚拟机里，而不是在真实地平台上。**

**虚拟机就是并不真实存在的电脑，但是可以在其它电脑平台上被实现。**

**让这个想法有意义的原因有很多，其中一个就是，代码的可移植性。**

**因为虚拟机技术可以轻松的在多个 目标平台被实现，基于虚拟机的软件可以在许多处理器和操作系统上被执行，且无需改动原始代码。**

**虚拟机可以通过一些方法实现。例如：软件翻译，特定的硬件，翻译虚拟机程序到特定平台的机器语言。**

This chapter presents a typical VM architecture, modeled after the Java Virtual Machine (JVM) paradigm. As usual, we focus on two perspectives. First, we motivate and specify the VM abstraction. Next, we implement it over the Hack platform. Our implementation entails writing a program called *VM* translator, designed to translate VM code into Hack assembly code. The software suite that comes with the book illustrates yet another implementation vehicle, called *VM* emulator. This program implements the VM by emulating it on a standard personal computer using Java.

**本章节以JVM范例为模型，展示了一个景点的虚拟机结构。和往常一样，我们专注2个方面。**

**第一，我们会具体说明VM抽象。第二，我们用HACK平台实现它。**

**我们的实现意味着需要写一个叫虚拟机翻译器的程序，作用是将虚拟机代码翻译成HACK汇编代码。**

**随书附带的软件套装提供了另一种实现工具，成为VM仿真器。**

**这个软件通过使用JAVA在标准个人计算机模拟虚拟机来实现。**



A virtual machine model typically has a language, in which one can write *VM* programs. The VM language that we present here consists of four types of commands: arithmetic, memory access, program flow, and subroutine calling commands. We split the implementation of this language into two parts, each covered in a separate chapter and project. In this chapter we build a basic VM translator, capable of translating the VM’s arithmetic and memory access commands into machine language. In the next chapter we extend the basic translator with program flow and subroutine calling functionality. The result is a full-scale virtual machine that will serve as the backend of the compiler that we will build in chapters 10—11.

**虚拟机模型通常都拥有一个可以写虚拟机程序的语言。**

**我们这里展示的虚拟机语言由四个类型的指令组成：**

**数学，存储访问，程序流和子例程调用指令。**

**这个语言的实现我们分2块，每块单独成一个章节和项目。**

**这个章节我们先实现一个基本的VM翻译器，可以将虚拟机的数学运算与内存指令转为机器语言。**

**下个章节扩展这个翻译器得以支持程序流和子程调用。**

**结果是一个完整的虚拟机，作为支撑后续章节编译器的后盾。**



The virtual machine that emerges from this effort illustrates many important ideas in computer science. First, the notion of having one computer emulating another is a fundamental idea in the field, tracing back to Alan Turing in the 1930s. Over the years it had many practical implications, for example, using an emulator of an old generation computer running on a new platform in order to achieve upward code compatibility. More recently, the virtual machine model became the centerpiece of two competing mainstreams—the Java architecture and the .NET infrastructure. These software environments are rather complex, and one way to gain an inside view of their underlying structure is to build a simple version of their VM cores, as we do here.

**这种努力得到的虚拟机说明了计算机科学中的许多思想。**

**第一，用一台计算机模拟另外一台计算机的概念可以追溯到1930年代。**

**这么多年来，它有了许多实际意义。例如，使用新平台来运行老一代机器的虚拟机去获得向上的代码兼容性。**

**最近，虚拟机模型成为了2个互相竞争的主流，JAVA和.NET基础设施，的核心。**

**这些软件环境非常复杂，要想一窥他们底层架构的究竟，我们就得做现在在做的事情，构建一个虚拟机的简单版本。**



Another important topic embedded in this chapter is stack processing. The stack is a fundamental and elegant data structure that comes to play in many computer systems and algorithms. Since the VM presented in this chapter is stack-based, it provides a working example of this remarkably versatile data structure.

**蕴含在这个章节另一个很重要的话题就是堆栈处理。**

**堆栈是一种在很多计算机系统、算法中被使用的，基本且优雅的数据结构。**

**因为这个章节实现的虚拟机是基于堆栈的，自然也就提供了这个非常通用的数据结构的工作实例。**



## 7.1Background

## **7.1.1 The Virtual Machine Paradigm**

## 虚拟机范例

Before a high-level program can run on a target computer, it must be translated into the computer’s machine language. This translation—known as *compilation*—is a rather complex process. Normally, a separate compiler is written specifically for any given pair of high-level language and target machine language. This leads to a proliferation of many different compilers, each depending on every detail of both its source and destination languages. One way to decouple this dependency is to break the overall compilation process into two nearly separate stages. In the first stage, the high-level program is parsed and its commands are translated into intermediate processing steps—steps that are neither “high” nor “low.” In the second stage, the intermediate steps are translated further into the machine language of the target hardware.

**高级程序想要运行在目标电脑，就必须先被翻译成机器语言。这种翻译，尝叫汇编，是一种非常复杂的操作。**

**一般情况下，会给一对高级语言与目标机器语言写一个单独的编译器。这就导致了许多不同编译器的增殖，每个都单独依赖特定的徐姐。打破这种依赖的方法就是将整个编译过程分为2个几乎独立的阶段。**

**第一阶段就是高级语言到中间步骤，第二阶段就是中间步骤到机器码。**



This decomposition is very appealing from a software engineering perspective: The first stage depends only on the specifics of the source high-level language, and the second stage only on the specifics of the target machine language. Of course, the interface between the two compilation stages—the exact definition of the intermediate processing steps—must be carefully designed. In fact, this interface is sufficiently important to merit its own definition as a stand-alone language of an abstract machine. Specifically, one can formulate a virtual machine whose instructions are the intermediate processing steps into which high-level commands are decomposed. The compiler that was formerly a single monolithic program is now split into two separate programs. The first program, still termed compiler, translates the high-level code into intermediate VM instructions, while the second program translates this VM code into the machine language of the target platform.

**这种分解从软工的角度来看很吸引人。**

**第一步只和源头的高级语言细节有关，第二阶段只和机器语言细节有关。**

**当然，2个步骤之间的接口，也就是中间步骤的确切定义需要被小心的定义。**

**实际上，这个接口已经足够重要到可以将自己的定义作为一个独立的抽象机语言。**

**具体来说，可以制定一个虚拟机，其指令是高级指令分解得到的中间处理步骤。**

**以前是单个程序的编译器现在可以被拆分为2个独立的程序。**

**第一个程序将高级代码转为中间虚拟机指令，第二个程序将中间虚拟机指令转为平台机器语言。**



This two-stage compilation model has been used—one way or another—in many compiler construction projects. Some developers went as far as defining a formal and stand-alone virtual machine language, most notably the p-code generated by several Pascal compilers in the 1970s. Java compilers are also two-tiered, generating a bytecode language that runs on the JVM virtual machine (also called the Java Runtime *Environment*). More recently, the approach has been adopted by the .NET infrastructure. In particular, .NET requires compilers to generate code written in an intermediate language (IL) that runs on a virtual machine called CLR (*Common* Language *Runtime*).

**许多编译器项目架构都使用了这种2步编译模型。**

**一些开发人员甚至定义了一种正式的独立虚拟机语言，注明的是1970年代的PASCAL编译器得到的P代码。**

**JAVA编译器也是两步的。生成一个可以在JVM虚拟机上运行的字节代码语言。**

**最近，.NET基础架构也用了这个方法。特别的，.net要求编译器生成以IL中间语言编写的代码，这个代码可以在CLR虚拟机上运行。**



Indeed, the notion of an explicit and formal virtual machine language has several practical advantages. First, compilers for different target platforms can be obtained with relative ease by replacing only the virtual machine implementation (sometimes called the compiler’s *backend*). This, in turn, allows the VM code to become transportable across different hardware platforms, permitting a range of implementation trade-offs among code efficiency, hardware cost, and programming effort. Second, compilers for many languages can share the same VM backend, allowing code sharing and language interoperability. For example, one high-level language may be good at scientific calculations, while another may excel in handling the user interface. If both languages compile into a common VM layer, it is rather natural to have routines in one language call routines in the other, using an agreed-upon invocation syntax.

**实际上，显式，正式的虚拟机语言有多个实际优势。**

**第一，不同平台的编译器可以相对轻松的获得，只要通过替换虚拟机代码的实现。**

**这样也就允许虚拟机代码在不同硬件平台变得很容易迁移。允许在代码效率，硬件成本，编程工作之间制作权衡。**

**第二，不同语言的编译器可以共享一个相同的虚拟机后端，允许代码和语言的互通性。**

**例如，一个高级语言也许擅长科学计算，但是其他的也许擅长表格和处理UI。如果所有语言都编译到通用虚拟机层，那么使用规定的调用语法在一个语言例程里面调用另一个语言例程就会很自然了。**



Another benefit of the virtual machine approach is modularity. Every improvement in the efficiency of the VM implementation is immediately inherited by all the compilers above it. Likewise, every new digital device or appliance that is equipped with a VM implementation can immediately benefit from a huge base of available software, as seen in figure 7.1.

**虚拟机的另一个好处就是模块化。VM效率的每个改进都立即被所有编译器继承。**

**配备了虚拟机的每个数字设备都可以立即收益。**



## **7.1.2 The Stack Machine Model**

Like most programming languages, the VM language consists of arithmetic, memory access, program flow, and subroutine calling operations. There are several possible software paradigms on which to base such a language implementation. One of the key questions regarding this choice is where will the operands and the results of the *VM operations reside?* Perhaps the cleanest solution is to put them on a stack data structure.

**和大部分的语言类似，虚拟机使用的语言由数字，存储方案程序流，子程调用等操作组成。**

**有几种可行的软件范式基于这样的语言实现。**

**关于这个选择关键问题是，操作数和VM操作的结果放在那里？**

**也许最干净的解决方案是把他们放在堆栈数据结构中。**



In a *stack machine* model, arithmetic commands pop their operands from the top of the stack and push their results back onto the top of the stack. Other commands transfer data items from the stack’s top to designated memory locations, and vice versa. 

As it turns out, these simple stack operations can be used to implement the evaluation of any arithmetic or logical expression. Further, any program, written in any programming language, can be translated into an equivalent stack machine program. One such stack machine model is used in the Java Virtual Machine as well as in the VM described and built in what follows.

**在堆栈机模型中，算术指令使用POP指令把操作数从栈顶POP出去，然后把结果在PUSH进去。**

**其他的指令把数据从栈顶传递到设计的预定内存地址。**

**这些简单的栈操作可以被使用去实现任何数学和逻辑操作。**

**任何语言编写的程序，可以被转换成等价的栈机器程序。**

**这样的机器模型被用在了JVM里面。**



![](https://tva1.sinaimg.cn/large/006tNbRwgy1gblt6nsdqsj30gh0caaao.jpg)



**Elementary Stack Operations** A stack is an abstract data structure that supports two basic operations: push and pop. The push operation adds an element to the top of the stack; the element that was previously on top is pushed below the newly added element. The pop operation retrieves and removes the top element; the element just below it moves up to the top position. Thus the stack implements a last-in-first-out (LIFO) storage model, illustrated in figure 7.2.

**栈是支持POP和PUSH2个操作的抽象数据结构。**

**PUSH操作从栈顶添加新元素,新加的会覆盖在旧的上面。**

**POP操作获取并移除顶部的元素。然后原来顶部下面的物体就会变成新的栈顶。**

**因此，栈的实现是一个后进先出的模型。在7.2图会展示。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbltf42famj30gh08lglu.jpg)

We see that stack access differs from conventional memory access in several respects. First, the stack is accessible only from the top, one item at a time. Second, reading the stack is a lossy operation: The only way to retrieve the top value is to remove it from the stack.

 In contrast, the act of reading a value from a regular memory location has no impact on the memory’s state. Finally, writing an item onto the stack adds it to the stack’s top, without changing the rest of the stack. In contrast, writing an item into a regular memory location is a lossy operation, since it overrides the location’s previous value.

**我们可以看到，栈访问和常规内存访问在某些方面有不同。**

**首先，栈只能从顶部访问，一次一个对象。**

**第二，读栈是一个有损耗的操作。读取栈元素的唯一途径就是把它从栈顶溢出。**

**相反，从存储地址读取值的操作不会有任何影响。**

**最终，往栈添加元素，不影响栈其余部分。相反，写元素进常规的存储地址就会覆盖之前的值。**



The stack data structure can be implemented in several different ways. The simplest approach is to keep an array, say stack, and a stack pointer variable, say sp, that points to the available location just above the topmost element. The *push x* command is then implemented by storing x at the array entry pointed by *sp* and then incrementing *sp* (i.e., stack [sp]=x; sp=sp+1). The *pop* operation is implemented by first decrementing *sp* and then returning the value stored in the top position (i.e., sp=sp-1; return stack [sp]).

**要实现栈数据结构有一些不同的方法。最简单的方法就是持有一个Array数组，就说是栈。然后再来个栈指针变量，sp。栈指针指向Array顶部元素。**

**pushx指令然后就被实现了，通过在数组入口sp指向的地方保存x，然后给sp+1。**

 **pop的实现步骤是，先sp减一，然后将保存在顶部位置的值return。**



As usual in computer science, simplicity and elegance imply power of expression. The simple stack model is a versatile data structure that comes to play in many computer systems and algorithms. In the virtual machine architecture that we build here, it serves two key purposes. First, it is used for handling all the arithmetic and logical operations of the VM. Second, it facilitates subroutine calls and the associated memory allocation—the subjects of the next chapter.

**和计算机科学一样，简明和优雅意味着表达的力量。**

**简单的栈模型在许多计算机系统和算法李，是一个多才多艺的数据结构。**

**我们这里构建的虚拟机器架构有2个目的：第一，处理虚拟机的所有的算数和逻辑操作。第二，它简化了子例程调用和相关的内存分配-下一张的主题。**



Stack-based arithmetic is a simple matter: the operands are popped from the stack, the required operation is performed on them, and the result is pushed back onto the stack. For example, here is how addition is handled:

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gblunwy3i0j308r027mwy.jpg)



**基于堆栈的算法很简单。堆栈顶部弹出操作数，对他们执行所需的操作，然后把结果推回堆栈。**

The stack version of other operations (subtract, multiply, etc.) are precisely the same. For example, consider the expression d=(2-x)*(y+5), taken from some high-level program. The stack-based evaluation of this expression is shown in figure 7.3.

**其他操作的堆栈版本完全相同。例如，考虑表达式 d=(2-x)*(y+5)。**

**图7.3展示了这个表达式的等价实现。**

**先算加减乘除再做乘法。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbluzfcvu3j30dr0dkdg0.jpg)

Stack-based evaluation of Boolean expressions has precisely the same flavor. For example, consider the high-level command if (x<7) or (y=8) then.... The stack-based evaluation of this expression is shown in figure 7.4.

**基于堆栈的表达式有完全相同的特点。**

**例如，想一个高级的指令 if(x<7)or(x=8) then ... 实现见下方。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbluzyu5bej30dr0bg3ym.jpg)

The previous examples illustrate a general observation: any arithmetic and Boolean expression—no matter how complex—can be systematically converted into, and evaluated by, a sequence of simple operations on a stack.

 Thus, one can write a compiler that translates high-level arithmetic and Boolean expressions into sequences of stack commands, as we will do in chapters 10-11. We now turn to specify these commands (section 7.2), and describe their implementation on the Hack platform (section 7.3).

**之前的案例说明了一般性观察。**

**任何算数和布尔表达式，无论多复杂，都可以被系统性的转换为对应的一系列简单的栈的操作。**

**因此，我们可以写一个编译器，将高级的算数和逻辑表达式翻译成一系列的栈指令，我们会在10~11章实现。**

**我们现在指明这些指令，描述在HACK平台的实现。**



# 7.2 VM Specification, Part I

## **7.2.1 General**

The virtual machine is stack-based: all operations are done on a stack. It is also function-based: a complete VM program is organized in program units called functions, written in the VM language. Each function has its own stand-alone code and is separately handled. The VM language has a single 16-bit data type that can be used as an integer, a Boolean, or a pointer. The language consists of four types of commands:

**虚拟机基于堆栈：所有的操作都在堆栈完成。它同样基于函数：一个完整的虚拟机程序都被组织到了一个程序单元，也就是用虚拟机语言写的函数里。**

**每个函数都有自己的独立的大妈，并且是单独处理的。**

**虚拟机程序有单独的16位数据类型，可以被当成整型使用，指针，布尔类型。这个语言由四个类型组成：**

■ *Arithmetic commands* perform arithmetic and logical operations on the stack.

**算数指令在堆栈的算数和逻辑操作。**

■ *Memory access commands* transfer data between the stack and virtual memory segments.

**存储访问指令将数据在堆栈和虚拟内存片之间来回转移。**

■ *Program flow commands* facilitate conditional and unconditional branching operations.

**程序流指令有助于有条件和无条件的分支操作。**

■ *Function calling commands* call functions and return from them.

**函数调用指令调用函数并从中返回。**



Building a virtual machine is a complex undertaking, and so we divide it into two stages. In this chapter we specify the *arithmetic* and *memory access* commands and build a basic VM translator that implements them only. The next chapter specifies the program flow and function calling commands and extends the basic translator into a full-scale virtual machine implementation.

**建造虚拟机是一个复杂的工作，所以我们把他们拆分成两步。**

**这个章节我们指明算数和存储访问，构造一个基础的虚拟机翻译器。**

**下一张，实现程序流和函数调用，来扩展这个基础的翻译器到尺寸虚拟机实现。**



**Program and Command Structure** A VM *program* is a collection of one or more files with a .vm extension, each consisting of one or more functions. From a compilation standpoint, these constructs correspond, respectively, to the notions of *program, class,* and method in an object-oriented language.

**程序和指令架构**

**一个虚拟机程序是一个或者多个.vm文件的集合。每个文件都包含一个或者多个函数。**

**从编译的角度来看，这些结构分别对应面向对象语言的程序，类和方法的概念。**



Within a .vm file, each VM command appears in a separate line, and in one of the following formats: *command* (e.g., add), *command arg* (e.g., goto loop), or command *arg1 arg2* (e.g., push local 3). The arguments are separated from each other and from the *command*part by an arbitrary number of spaces. “//” comments can appear at the end of any line and are ignored. Blank lines are permitted and ignored.

**在vm文件内，每个虚拟机指令在分开的行出现，并且遵循下面的格式：**

`指令 参数 参数`

**参数各自分开。//符号可以在任何行位出现，并且编译的时候是忽略的。空行是允许的，也会被忽略。**



## **7.2.2 Arithmetic and Logical Commands**

**算数和逻辑指令。**

The VM language features nine stack-oriented arithmetic and logical commands. Seven of these commands are binary: They pop two items off the stack, compute a binary function on them, and push the result back onto the stack. The remaining two commands are unary: they pop a single item off the stack, compute a unary function on it, and push the result back onto the stack. We see that each command has the net impact of replacing its operand(s) with the command’s result, without affecting the rest of the stack. Figure 7.5 gives the details.

**虚拟机语言有九个基于堆栈的算数和逻辑指令。**

**其中七个是二进制的：从堆栈POP元素，计算，PUSH回栈。**

**剩余的2个指令是医院的，POP并计算，PUSH结果回栈**

**我们可以发现，每个指令都有实际影响，用指令结果替换操作数而不影响堆栈的其余部分。**

Three of the commands listed in figure 7.5 (eq, gt, lt) return Boolean values. The VM represents *true* and *false* as -1 (minus one, 0xFFFF) and 0 (zero, 0x0000), respectively.

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbmrwpav0bj30dq05k3yo.jpg)

上图展示的指令中中间三行返回的是布尔类型。虚拟机将true和false分别表示为-1和0



## **7.2.3 Memory Access Commands**

So far in the chapter, memory access commands were illustrated using the pseudo-commands *pop* and *push x*, where the symbol *x* referred to an individual location in some global memory. Yet formally, our VM manipulates eight separate virtual memory segments, listed in figure 7.6.

**到目前为止，存储访问指令我们使用了POP和PUSH X这样的伪指令展示过了。push X的符号X指向全局存储器中的某个位置。**

**我们的虚拟机操作8个独立的存储器段，下图展示了。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbmv1a5qpyj30dp0cu0tr.jpg)



**Memory Access Commands** All the memory segments are accessed by the same two commands:

> • push *segment index* Push the value of *segment*[*index*] onto the stack.

> • pop *segment index* Pop the top stack value and store it in *segment*[*index*].

**内存访问指令**

**所有的存储段都依靠2个指令访问： PUSH和POP。**

**push会将段落[index]的数据push到栈里。**

**pop指令会将值pop出去，然后保存到segment[index]里面**



Where *segment* is one of the eight segment names and index is a non-negative integer. For example, push argument 2 followed by pop local 1 will store the value of the function’s third argument in the function’s second local variable (each segment’s index starts at 0).

**segment就是8个segment的名字，index是非负的整型。**

**例如，push参数2，随后pop1，将会在函数第二个本地变量保存函数第三个参数的值。**

The relationship among VM files, VM functions, and their respective virtual memory segments is depicted in figure 7.7.

**虚拟机文件，函数和对应的虚拟存储段的关系将会在下图展示。**

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbmv2xwkbuj30dh0b93yv.jpg)

In addition to the eight memory segments, which are managed explicitly by VM push and pop commands, the VM implementation manages two implicit data structures called *stack* and *heap*. These data structures are never mentioned directly, but their states change in the background, as a side effect of VM commands.

**除了明确被虚拟机PUSH和POP等指令管理的八个内存段，虚拟器实现管理2个隐式数据结构堆和栈。**

**这些数据结构从来没有被直接提到，但是他们后台状态改变是使用虚拟机指令的产物。**



**The Stack** Consider the commands sequence push argument 2 and pop local 1, mentioned before. The working memory of such VM operations is the stack. The data value did not simply jump from one segment to another—it went through the stack. Yet in spite of its central role in the VM architecture, the stack proper is never mentioned in the VM language.

**之前提到的，PUSH2POP本地1。这些虚拟机操作都是在栈进行的。**

**数据并没有简单的从一个段跳到另一个，它直接去了栈。**

**尽管它在虚拟机架构起核心作用，虚拟机语言并没有像样的提及。**

 

**The Heap** Another memory element that exists in the VM’s background is the *heap*. The heap is the name of the RAM area dedicated for storing objects and arrays data. These objects and arrays can be manipulated by VM commands, as we will see shortly.

**堆。另外一个存在虚拟机后台的存储元素就是堆。**

**堆是RAM区域专用于存储对象和数组的数据结构。**

**这些对象和数组可以被虚拟机指令操控，我们很快就可以看到。**



## **7.2.4 Program Flow and Function Calling Commands**

The VM features six additional commands that are discussed at length in the next chapter. For completeness, these commands are listed here.

**虚拟机的6个额外的指令会在下个章节讨论到。为了完整，指令列在下方。**

label

goto

if-go



符号流指令有

function 

call

return



## **7.2.5 Program Elements in the Jack-VM-Hack Platform**

**JACK虚拟机的程序元素**

We end the first part of the VM specification with a top-down view of all the program elements that emerge from the full compilation of a typical high-level program. At the top of figure 7.8 we see a Jack program, consisting of two classes (Jack, a simple Java-like language, is described in chapter 9). Each Jack class consists of one or more methods. When the Jack compiler is applied to a directory that includes n class files, it produces n VM files (in the same directory). Each Jack method xxx within a class Yyy is translated into one *VM* *function* called Yyy.xxx within the corresponding VM file.

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbn2khaqjbj30dp0ab0sz.jpg)