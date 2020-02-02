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