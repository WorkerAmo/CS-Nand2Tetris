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

**我们用一张从上到下，涵盖完整边一部分和经典高级程序元素的图，去结束第一部分虚拟机描述。**

**图顶部，我们可以看到JAVA程序，由2个Class组成。**

**每个 JACK Class 由一个或者多个方法组成。**

**当JACK编译器应用到了包含几个class文件的文件夹，它就会在同目录下产出对应的几个VM文件。**

**每个yyy文件的 jack 语言的方法 xxx都会在对应的VM文件被翻译到VM函数   yyy.xxx 。**



Next, the figure shows how the *VM translator* can be applied to the directory in which the VM files reside, generating a single assembly program. This assembly program does two main things. First, it emulates the virtual memory segments of each VM function and file, as well as the implicit stack. Second, it effects the VM commands on the target platform. This is done by manipulating the emulated VM data structures using machine language instructions—those translated from the VM commands. If all works well, that is, if the compiler and the VM translator and the assembler are implemented correctly, the target platform will end up effecting the behavior mandated by the original Jack program.

**下面，图片展示了，VM翻译器是如何被引用到包含VM文件的路径的，通过生成一个简单的汇编程序。**

**这个汇编器做2个主要的事情。**

**第一，它模拟每个VM函数和文件的虚拟内存段以及隐式堆栈。**

**第二，它影响目标平台的VM指令。**

**这是通过操控使用机器指令模拟的VM数据结构实现的。**

**如果一切正常，也就是说，如果编译器和虚拟机翻译器，VM转换器和汇编程序都被正确的执行，目标平台最终将影响被原始JACK程序要求的行为。**

（这里翻译的怪怪的，第二轮优化下）



## **7.2.6 VM Programming Examples**

We end this section by illustrating how the VM abstraction can be used to express typical programming tasks found in high-level programs. 

**我们用VM抽象如何被用于表达经典的高级程序任务的描述来结束本章。**

We give three examples: (i) a typical arithmetic task, (ii) typical array handling, and (iii) typical object handling. These examples are irrelevant to the VM implementation, and in fact the entire section 7.2.6 can be skipped without losing the thread of the chapter.

**三个范例：**

**1.经典的算数任务**

**2.经典的数组处理**

**3.经典的对象处理**

**这些案例和VM的实现相关，事实上，整个小节都可以被跳过，且你不会失去章节的主线。**



The main purpose of this section is to illustrate how the compiler developed in chapters 10-11 will use the VM abstraction to translate high-level programs into VM code. Indeed, VM programs are rarely written by human programmers, but rather by compilers. Therefore, it is instructive to begin each example with a high-level code fragment, then show its equivalent representation using VM code. We use a C-style syntax for all the high-level examples.

**本章节的主要目的是说明，10-11章节开发的编译器是如何使用VM抽象去翻译高级语言到虚拟机代码的。**

**实际上，VM程序很少由人类编写，一般都是编译器处理的。**

**因此，每个范例都带着高级语言代码片段然后再展示对应的VM代码表述，更有启发性。**

**所有的高级语言范例，我们都是用C风格语法。**



**A Typical Arithmetic Task** Consider the multiplication algorithm shown at the top of figure 7.9. How should we (or more likely, the compiler) express this algorithm in the VM language? 

First, high-level structures like for and while must be rewritten using the VM’s simple “goto logic.” In a similar fashion, high-level arithmetic and Boolean operations must be expressed using stack-oriented commands. The resulting code is shown in figure 7.9. (The exact semantics of the VM commands function, label, goto, if-goto, and return are described in chapter 8, but their intuitive meaning is self-explanatory.)

**一个经典的算术任务**

**看图展示的乘法算法。我们怎么用VM语言展示这个算法？**

**首先，for 和 while 这样的高级结构必须用VM的goto逻辑重写。**

**以类似的方式，高级算数和布尔操作必须用基于堆栈的指令表述。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnp9ku8f3j30gg0g2dgi.jpg)

**补充，看懂这部分很重要：**

第一步 是从 first approximation 到 pseudo VM Code 

 https://1drv.ms/u/s!An_6yvF4LrWVihqyRgBEOW02oK_Y

第二部是结合内存分配，看FinalVMCode







Let us focus on the virtual segments depicted at the bottom of figure 7.9. We see that when a VM function starts running, it assumes that (i) the stack is empty, (ii) the argument values on which it is supposed to operate are located in the argument segment, and (iii) the local variables that it is supposed to use are initialized to 0 and located in the local segment.

**让我们专注于图示的虚拟片段。当VM函数开始运行的时候，它会假定堆栈是空的、要被操作的参数被保存在了参数段、要被使用的本地变量被初始化为0且被保存在本地段。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnpn7oglbj30dr04ewei.jpg)

Let us now focus on the VM representation of the algorithm. Recall that VM commands cannot use symbolic argument and variable names—they are limited to making 〈*segment index*〉 references only. However, the translation from the former to the latter is straightforward. All we have to do is map x, y, sum and j on argument 0, argument 1, local 0 and local 1, respectively, and replace all their symbolic occurrences in the pseudo code with corresponding 〈*segment index*〉 references.

**下面我们看VM表述的算法。回想一下，VM指令不可以使用符号参数和变量名。他们仅限于使用段索引。**

**但是，翻译过程是直接的。我们要做的就是定位X，Y，SUM，参数0的J，参数1，本地0，1，然后分别把他们在伪代码中的符号表现替换为对应的段索引。**



To sum up, when a VM function starts running, it assumes that it is surrounded by a private world, all of its own, consisting of initialized argument and local segments and an empty stack, waiting to be manipulated by its commands. The agent responsible for staging this virtual worldview for every VM function just before it starts running is the VM implementation, as we will see in the next chapter.

**总结一下，当VM函数开始运行，他会假设在一个私有的世界里。组成这个世界的是初始化过后的参数和本地段，与空栈，等到指令的操作。**

**在程序运行之前，负责为每个VM函数暂存这个虚拟世界的代理人，就是虚拟机的实现。**

（人话就是暂存这个私有空间的代理就是虚拟机。）



**Array Handling** An array is an indexed collection of objects. Suppose that a high-level program has created an array of ten integers called bar and filled it with some ten numbers. Let us assume that the array’s base has been mapped (behind the scene) on RAM address 4315. Suppose now that the high-level program wants to execute the command bar[2]=19. How can we implement this operation at the VM level?

**数组处理**

**数组是序列化的一组对象。假设一个高级从横须已经创建了由10个整数组成的数组。然后我们假设数组的基址被定在了RAM地址4315。再假设，很高级程序希望之星指令 bar[2]=19。我们怎么用VM实现？**

In the C language, such an operation can be also specified as *(bar+2)=19, meaning “*set* the RAM location whose address is (bar+2) to 19.” As shown in figure 7.10, this operation lends itself perfectly well to the VM language.

**C语言里，这样的操作可以被指明为 (bar+2)=19。**

**意思是将RAM地址为bar+2的值设为10。看下图有解释。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnprznk3ej30gg0ft3za.jpg)



It remains to be seen, of course, how a high-level command like bar [2]= 19 is translated in the first place into the VM code shown in figure 7.10. This transformation is described in section 11.1.1, when we discuss the code generation features of the compiler.



![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnpuw2orij30gg0ef3zd.jpg)

It follows that the VM designer can principally let programmers implement the VM on target platforms in any way they see fit. However, it is usually recommended that some guidelines be provided as to how the VM should map on the target platform, rather than leaving these decisions completely to the implementer’s discretion. These guidelines, called standard mapping, are provided for two reasons. First, they entail a public contract that regulates how VM-based programs can interact with programs produced by compilers that don’t use this VM (e.g., compilers that produce binary code directly). Second, we wish to allow the developers of the VM implementation to run standardized tests, namely, tests that conform to the standard mapping. This way, the tests and the software can be written by different people, which is always recommended. With that in mind, the remainder of this section specifies the standard mapping of the VM on a familiar hardware platform: the Hack computer.

**因此，VM的设计者在原则上可以让程序员在目标平台上以他们认为合适的方式实现VM。**

**但是，通常建议提供一些准则去描述VM应该如何映射到目标平台，而不是都交给开发者去实现。**

**这些准则成为标准映射。有2个原因需要他**

​		**首先，达成一个协议。协议规范了基于VM的程序如何与非此VM编译器生成的程序进行交流。（一些编译器直接生成二进制代码）**

**第二，我们希望允许开发者运行标准化测试，即，符合标准化映射的测试。**

**考虑到这一点，本节的其余部分指定了VM在熟悉的硬件平台上的标准映射：Hack计算机。**



**VM to Hack Translation** Recall that a VM program is a collection of one or more .vm files, each containing one or more VM functions, each being a sequence of VM commands. The VM translator takes a collection of .vm files as input and produces a single Hack assembly language .asm file as output (see figure 7.7). Each VM command is translated by the VM translator into Hack assembly code. The order of the functions within the .vm files does not matter.

**回想一下，VM程序是一个或者多个vm文件的集合。每个包含有一个或者多个vm函数，每个都是vm指令的序列。**

**vm翻译器将一组vm文件作为输入，然后输出单独的HACK汇编语言.asm文件作为输出。**

**每个VM指令都会被VM编译器翻译为HACK汇编代码。vm文件函数的顺序不重要。**



**RAM Usage** The data memory of the Hack computer consists of 32K 16-bit words. The first 16K serve as general-purpose RAM. The next 16K contain memory maps of I/O devices. The VM implementation should use this space as follows:

**HACK计算机的数据内存有32K。前16K作为一般RAM。后面的16K作为I/O映射。VM实现遵循下面的图**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnq963n1kj30dh03074a.jpg)

Recall that according to the Hack Machine Language Specification, RAM addresses 0 to 15 can be referred to by any assembly program using the symbols R0 to R15, respectively. In addition, the specification states that assembly programs can refer to RAM addresses 0 to 4 (i.e., R0 to R4) using the symbols SP, LCL, ARG, THIS, and THAT. This convention was introduced into the assembly language with foresight, in order to promote readable VM implementations. The expected use of these registers in the VM context is described as follows:

**根据HACK机器语言规范。RAM的0 ~ 15都可以被任何汇编程序分别使用符号 R0 ~ R15**

**此外，规范说了，汇编程序可以使用SP，LCL等指向R 0~R4**

**为了促进可读的VM实现，这个惯例被有预见性地引入了**

**这些寄存器再VM上下文预期用途描述如下：**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnq9m7833j30dk06zdg6.jpg)

**Memory Segments Mapping**

local, argument, this, that: Each one of these segments is mapped directly on the RAM, and its location is maintained by keeping its physical base address in a dedicated register (LCL, ARG, THIS, and THAT, respectively). Thus any access to the *i*th entry of any one of these segments should be translated to assembly code that accesses address (*base* + i) in the RAM, where base is the current value stored in the register dedicated to the respective segment.

**local(LCL)，THIS等都被直接映射到RAM中，通过在专用寄存器（LCLTHISARGTAHT）里保存物理地址来维护。**

**任何试图访问i片段的操作都应该被翻译成基于RAM中对应地址的汇编码(base + i)**



pointer, temp: These segments are each mapped directly onto a fixed area in the RAM. The pointer segment is mapped on RAM locations 3-4 (also called THIS and THAT) and the temp segment on locations 5-12 (also called R5, R6,..., R12). Thus access to pointer i should be translated to assembly code that accesses RAM location 3 + *i*, and access to temp i should be translated to assembly code that accesses RAM location 5 + i. 

**pointer, temp这样的片段都被直接直接映射到了RAM的区域。**

**pointer被映射到RAM地址为3~4的地方。（THIS和THAT）**

**TEMP被映射到R5~R12。这意味着如果你要访问pointer只要base+3+i如果是temp就是base+5+i**



constant: This segment is truly virtual, as it does not occupy any physical space on the target architecture. Instead, the VM implementation handles any VM access to 〈*constant i*〉 by simply supplying the constant i.

**关于constant。这个是完全虚拟的。不占用目标架构上的任何物理空间。**

**事实上，VM实现的方式是直接使用i来提供访问。（可能要看代码才能懂）**

static: According to the Hack machine language specification, when a new symbol is encountered for the first time in an assembly program, the assembler allocates a new RAM address to it, starting at address 16. This convention can be exploited to represent each static variable number j in a VM file f as the assembly language symbol f.j. For example, suppose that the file Xxx.vm contains the command push static 3. This command can be translated to the Hack assembly commands@Xxx.3 and D=M, followed by additional assembly code that pushes D’s value to the stack. This implementation of the static segment is somewhat tricky, but it works.

**静态部分**

**根据HACK机器语言规范。在汇编成语遇到新符号的时候，汇编器分配新的RAM地址给它，从地址16开始。**

**这个协议可以被利用于表现VM文件f每个静态变量j作为汇编语言符号f.j**

**例如，假设文件Xxx.vm包含指令，PUSH静态3.**

**这个指令可以被翻译成HACK汇编指令@Xxx.3 D=M，紧接着是额外的汇编代码PUSH D值到堆栈。**

**这个静态段的实现有点trick意味，就是抖机灵那种，但是可行。**



**Assembly Language Symbols** We recap all the assembly language symbols used by VM implementations that conform to the standard mapping.

**汇编语言符号**

**我们概括了符合映射标准的所有使用VM实现的汇编语言符号。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnqwjwb9oj30c906tq3b.jpg)



## **7.3.2 Design Suggestion for the VM Implementation**

**设计建议**

The VM translator should accept a single command line parameter, as follows:

prompt> VMtranslator source

**VM翻译器应该支持 命令行参数**

`prompt> VMtranslator source`

Where *source* is either a file name of the form Xxx.vm (the extension is mandatory) or a directory name containing one or more .vm files (in which case there is no extension). The result of the translation is always a single assembly language file named Xxx.asm, created in the same directory as the input Xxx. The translated code must conform to the standard VM mapping on the Hack platform.

**source可以是后缀带有.vm的文件名或者是保存有多个vm文件的路径（这情况下是不带后缀的）。**

**翻译的结果永远是一个单一的汇编语言文件Xxx.asm，和输入文件在一个路径下。翻译代码必须符合标准HACK平台VM映射标准**



## **7.3.3 Program Structure**

**程序架构**

We propose implementing the VM translator using a main program and two modules: parser and *code writer.*

**只要2个模组，parser和code writer。**

**The \*Parser\* Module**

**Parser:** Handles the parsing of a single .vm file, and encapsulates access to the input code. It reads VM commands, parses them, and provides convenient access to their components. In addition, it removes all white space and comments.

**分析模组处理分析单个vm文件的工作。并且封装输入代码访问。**

**读代码指令，解析他们，提供访问他们元素入口。此外，删除所有的注释和空格。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnr167689j30dp0eidgg.jpg)



**The** **Code Writer** **Module**

**CodeWriter:** Translates VM commands into Hack assembly code.

**写码器的作用是吧VM代码转为HACK汇编代码（第六章是吧汇编码写成二进制）**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnr3i711yj30dp0a8mxm.jpg)

**Main Program** The main program should construct a Parser to parse the VM input file and a CodeWriter to generate code into the corresponding output file. It should then march through the VM commands in the input file and generate assembly code for each one of them.

If the program’s argument is a directory name rather than a file name, the main program should process all the .vm files in this directory. In doing so, it should use a separate Parser for handling each input file and a single CodeWriter for handling the output.

**主程序构造解析器去解析。生成代码到对应的输出文件。遍历VM文件指令，输出为对应的汇编代码。**

**如果程序参数不止一个文件，主程序应该处理所有的vm文件。这样的话，应该使用分别独立的解析器处理每个输入文件，然后用一个单独的写码器处理输出。**



# ***\*7.4 Perspective\****

In this chapter we began the process of developing a compiler for a high-level language. Following modern software engineering practices, we have chosen to base the compiler on a two-tier compilation model. In the frontend tier, covered in chapters 10 and 11, the high-level code is translated into an intermediate code, running on a virtual machine. In the backend tier, covered in this and in the next chapter, the intermediate code is translated into the machine language of a target hardware platform (see figures 7.1 and 7.9).

The idea of formulating the intermediate code as the explicit language of a virtual machine goes back to the late 1970s, when it was used by several popular Pascal compilers. These compilers generated an intermediate “p-code” that could execute on any computer that implemented it. Following the wide spread use of the World Wide Web in the mid-1990s, cross-platform compatibility became a universally vexing issue. In order to address the problem, the Sun Microsystems company sought to develop a new programming language that could potentially run on any computer and digital device connected to the Internet. The language that emerged from this initiative—*Java*—is also founded on an intermediate code execution model called the Java Virtual Machine, on JVM.

The JVM is a specification that describes an intermediate language called *byte-code*—the target language of Java compilers. Files written in bytecode are then used for dynamic code distribution of Java programs over the Internet, most notably as applets embedded in web pages. Of course in order to execute these programs, the client computers must be equipped with suitable JVM implementations. These programs, also called Java Run-time Environments (JREs), are widely available for numerous processor/OS combinations, including game consoles and cell phones.

In the early 2000s, Microsoft entered the fray with its .NET infrastructure. The centerpiece of .NET is a virtual machine model called Common Language Runtime (CLR). According to the Microsoft vision, many programming languages (including C++, C#, Visual Basic, and J#—a Java variant) could be compiled into intermediate code running on the CLR. This enables code written in different languages to interoperate and share the software libraries of a common run-time environment.

We note in closing that a crucial ingredient that must be added to the virtual machine model before its full potential of interoperability is unleashed is a common software library. Indeed the Java virtual machine comes with the standard Java libraries, and the Microsoft virtual machine comes with the Common Language Runtime. These software libraries can be viewed as small operating systems, providing the languages that run on top of the VM with unified services like memory management, GUI utilities, string functions, math functions, and so on. One such library will be described and built in chapter 12.

**这段概览太长，我仔细看完后翻大意如下。**

**1.这个章节我们开始开发高级语言编译器了，我们选择了2层编译模型。这个章节完成了第一个。**

**2.中间代码这个思想1970年代就开始了。随着90年代互联网兴起，跨平台兼容变的棘手。**

**由此导致了SUN公司开始开发新的语言，可以运行在互联网设备上。**

**3.JVM是啥？JVM本质就是一个描述中间语言的标准。这个中间语言叫字节码。目标客户端需要装备有JVM实现，也叫JRES也就是JAVA的运行时环境。**

**4.2000年早期的时候偶，微软开始玩.NET，他目标就是把c++C等语言编译成CLR，通用语言运行时。思路和SUN是一样的。这样的目的是不同语言编写的库可以共用，也就是interoperate地方的库。**

**5.我们意识到，虚拟机器模型的关键要素就是通用软件库。JAM带有JAVA库，微软虚拟机带的是CLR。**

**这些通用软件库可以被看成是操作系统。因为他们运行在VM上，有统一的服务例如内存管理，GUI，字符串函数，数学函数等。**

**这样的库会在12章描述。**



# ***\*7.5 Project\****

This section describes how to build the VM translator presented in the chapter. In the next chapter we will extend this basic translator with additional functionality, leading to a full-scale VM implementation. Before you get started, two comments are in order. First, section 7.2.6 is irrelevant to this project. Second, since the VM translator is designed to generate Hack assembly code, it is recommended to refresh your memory about the Hack assembly language rules (section 4.2).

 

**Objective** Build the first part of the VM translator (the second part is implemented in Project 8), focusing on the implementation of the stack arithmetic and memory access commands of the VM language.

**目标：** 

**构造虚拟机翻译器，本次主要目的就是实现堆栈算数和内存访问指令。**

**Resources** You will need two tools: the programming language in which you will implement your VM translator, and the CPU emulator supplied with the book. This emulator will allow you to execute the machine code generated by your VM translator—an indirect way to test the correctness of the latter. Another tool that may come in handy in this project is the visual VM emulator supplied with the book. This program allows experimenting with a working VM implementation before you set out to build one yourself. For more information about this tool, refer to the VM emulator tutorial.

 **资源：**

**2个工具。**

**CPU模拟器。帮你执行你VM翻译器翻译出来的VM文件，也可以用于测试。**

**视觉化VM模拟器。用于测试你的VM实现。**

**Contract** Write a VM-to-Hack translator, conforming to the VM Specification, Part I (section 7.2) and to the Standard VM Mapping on the Hack Platform, Part I (section 7.3.1). Use it to translate the test VM programs supplied here, yielding corresponding programs written in the Hack assembly language. When executed on the supplied CPU emulator, the assembly programs generated by your translator should deliver the results mandated by the supplied test scripts and compare files.

 

**Proposed Implementation Stages**

We recommend building the translator in two stages. This will allow you to unit-test your implementation incrementally, using the test programs supplied here.

 **建议2步走**

**Stage I: Stack Arithmetic Commands** The first version of your VM translator should implement the nine stack arithmetic and logical commands of the VM language as well as the push constant x command (which, among other things, will help in testing the nine former commands). Note that the latter is the generic push command for the special case where the first argument is constant and the second argument is some decimal constant.

 **第一步，实现堆栈的算术指令。**

**第一步你要实现的是VM语言的九个算术逻辑指令。**

**Stage II: Memory Access Commands** The next version of your translator should include a full implementation of the VM language’s push and pop commands, handling all eight memory segments. We suggest breaking this stage into the following substages:

\0. You have already handled the constant segment.

\1. Next, handle the segments local, argument, this, and that.

\2. Next, handle the pointer and temp segments, in particular allowing modification of the bases of the this and that segments.

\3. Finally, handle the static segment.

**第二步是内存访问。**

 **让翻译器支持PUSH和POP。处理所有的九个内存片段。我们建议这一步分成四个小步骤**

处理常量

处理本地，参数thisthat

处理指针和temp

最后处理静态部分，



**Test Programs**

 

The five VM programs listed here are designed to unit-test the proposed implementation stages just described.

 

**Stage I: Stack Arithmetic**

■ SimpleAdd: Pushes and adds two constants.

■ StackTest: Executes a sequence of arithmetic and logical operations on the stack.

 

**Stage II: Memory Access**

 

■ BasicTest: Executes pop and push operations using the virtual memory segments.

■ PointerTest: Executes pop and push operations using the pointer, this, and that segments.

■ StaticTest: Executes pop and push operations using the static segment.

 

For each program Xxx we supply four files, beginning with the program’s code in Xxx.vm. The XxxVME.tst script allows running the program on the supplied VM emulator, so that you can gain familiarity with the program’s intended operation. After translating the program using your VM translator, the supplied Xxx.tst and Xxx.cmp scripts allow testing the translated assembly code on the CPU emulator.

 

**Tips**

 

**Initialization** In order for any translated VM program to start running, it must include a preamble startup code that forces the VM implementation to start executing it on the host platform. In addition, in order for any VM code to operate properly, the VM implementation must anchor the base addresses of the virtual segments in selected RAM locations. Both issues—startup code and segments initializations—are implemented in the next project. The difficulty of course is that we need these initializations in place in order to execute the test programs given in this project. The good news is that you should not worry about these issues at all, since the supplied test scripts carry out all the necessary initializations in a manual fashion (for the purpose of this project only).

 

**Testing/Debugging** For each one of the five test programs, follow these steps:

\1. Run the Xxx.vm program on the supplied VM emulator, using the XxxVME.tst test script, to get acquainted with the intended program’s behavior.

\2. Use your partial translator to translate the .vm file. The result should be a text file containing a translated .asm program, written in the Hack assembly language.

\3. Inspect the translated .asm program. If there are visible syntax (or any other) errors, debug and fix your translator.

\4. Use the supplied .tst and .cmp files to run your translated .asm program on the CPU emulator. If there are run-time errors, debug and fix your translator.

 

The supplied test programs were carefully planned to test the specific features of each stage in your VM implementation. Therefore, it’s important to implement your translator in the proposed order and to test it using the appropriate test programs at each stage. Implementing a later stage before an early one may cause the test programs to fail.![](https://tva1.sinaimg.cn/large/0082zybpgy1gbnrsyg0itj30gi0bxgmf.jpg)

**Tools**

 

**The VM Emulator** The book’s software suite includes a Java-based VM implementation. This VM emulator allows executing VM programs directly, without having to translate them first into machine language. This practice enables experimentation with the VM environment before you set out to implement one yourself. Figure 7.12 is a typical screen shot of the VM emulator in action.



