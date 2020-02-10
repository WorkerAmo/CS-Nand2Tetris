# 第 8 章 虚拟机 II : 程序控制

前面一章引入了VM的概念。这章节我们继续开发VM。我们设计了基于堆栈的调用处理（程序，函数，方法）。这章节我们扩展之前构建的虚拟机实现，给个完整的虚拟机翻译器。这个翻译器将作为10~11章节的编译器的基础。

在任何CS竞赛里面，堆栈处理都是不可忽略的。之前的章节描述了堆栈操作如何处理算数和布尔表达式。

这个章节我们继续展示这个数据结构是如何给复杂的任务提供支持的，例如子程调用，参数传递，递归，内存分配技术。这些能力大多数程序员都已经认为是理所应当了。我们现在就去打开这个黑盒子，看看如何用堆栈技术的虚拟机实现的。

本章节致力于介绍堆栈的设计原理,以及它是如何处理过程化语言,或者面向对象的语言的嵌套子程序

## 背景知识

高级语言允许描述这样的表达式`x=-b+sqrt(power(b,2)-4*a*c)`来表示,他几乎和实际表达式差不多,高级语言支持这种表达式来源于它的三种语言原则.

- 高级语言允许在程序中根据自由定义入 sqrt 和 power 这样的高级操作(函数)
- 高级语言允许自由调用这些子函数,就像使用一些如+/-这样的基本操作
- 高级语言允许假设每个被调用的子程序都被执行,并且随着子程序的结束,程序的控制权顺利返回

高级语言所具有的这些能够将表达式自由组合的能力,使得我们可以编写更加抽象的代码,让我们能把主要精力放在算法的思想上,而不是机器上,当然高级的语言抽象级别越高,在底层要做的工作就越多,特别的是,必须在底层控制子程序和子程序调用者(执行注入 sqrt 和 power 的系统定义的用户定义操作的程序单元)之间的微妙的相互影响,对于在运行期的每个子程序调用,底层必须处理下面的一些细节

- 将参数从调用者传递给被调用者,
- 在跳转并执行被调用之前,必须保存调用者的状态/
- 为被调用者使用局部变量分配空间
- 跳转并执行被调用者
- 被调用者的运行结果返回给调用者.
- 在从被调用者的运行之前,回收其使用的内存空间
- 恢复调用者的状态
- 返回调用语句之后的下一条语句继续执行.

要考虑这些繁琐的事情本身就很头疼疼好在编译器把高级语言从程序员从中解放出来,事实上**堆栈机**就很适合处理这些任务.

本章节剩余内容将是描述**程序控制流**命令（if while）和**子程序调用**（函数调用）是如何在堆栈机上面实现的,我们先介绍控制流命令的实现.然后再介绍复杂的子程序调用.



## **8.1.1 Program Flow**

The default execution of computer programs is linear, one command after the other. This sequential flow is occasionally broken by branching commands, for example, embarking on a new iteration in a loop. In low-level programming, the branching logic is accomplished by instructing the machine to continue execution at some destination in the program other than the next instruction, using a goto destination command. The destination specification can take several forms, the most primitive being the physical address of the instruction that should be executed next. A slightly more abstract redirection command is established by describing the jump destination using a symbolic label. This variation requires that the language be equipped with some labeling directive, designed to assign symbols to selected points in the code.

This basic *goto* mechanism can easily be altered to effect conditional branching as well. For example, an if-goto destination command can instruct the machine to take the jump only if a given Boolean condition is true; if the condition is false, the regular program flow should continue, executing the next command in the code. How should we introduce the Boolean condition into the language? **In a stack machine paradigm, the most natural approach is conditioning the jump on the value of the stack’s topmost element: if it’s not zero, jump to the specified destination; otherwise, execute the next command in the program.**

In chapter 7 we saw how primitive VM operations can be used to compute any Boolean expression, leaving its truth-value at the stack’s topmost element. This power of expression, combined with the goto and if-goto commands just described, can be used to express any flow of control structure found in any programming language. Two typical examples appear in figure 8.1.

The low-level implementation of the VM commands label, goto label, and if-goto label is straightforward. All programming languages, including the “lowest” ones, feature branching commands of some sort. For example, if our low-level implementation is based on translating the VM commands into assembly code, all we have to do is reexpress these goto commands using the branching logic of the assembly language.

默认的程序执行是线性的，一个一个执行。顺序流偶尔会被分支指令打断，比如，循环里会开启新的迭代。低级编程中，分支逻辑是通过goto等指令命令跳转到特定位置来实现的。目标可以有很多形式，最原始的就是下面要执行指令的物理地址。通过使用符号标签描述跳转目标，可以建立稍微抽象一些的重定向命令。这种变化要求该语言配备一些标签指令，旨在将符号分配给代码中的选定点。

基本的goto也可以通过轻松的更改来实现条件分支。 例如，仅当给定的布尔条件为true时，if-goto destination命令才能指示机器执行跳转。 如果条件为假，则常规程序流程应继续，执行代码中的下一个命令。 

我们应该如何在语言中引入布尔条件？ （布尔条件控制的实现原理）

**在堆栈机范例中，最自然的方法是限制堆栈最顶部元素的值的跳跃：如果不为零，则跳转至指定的目标； 否则，在程序中执行下一个命令。**

在第7章中，我们了解了如何使用原始VM操作来计算任何布尔表达式，并将其真值保留在堆栈的最顶层元素。 这种表达能力与刚刚描述的goto和if-goto命令结合在一起，可用于表达在任何编程语言中发现的任何控制结构流。 图8.1中显示了两个典型示例。

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbr4spxn5tj30dp08p3yl.jpg)





VM命令标签，goto标签和if-goto标签的底层实现非常简单。 所有编程语言，包括“最低”的语言，都具有某种形式的分支命令。 例如，如果我们的底层实现是基于将VM命令转换为汇编代码的，那么我们要做的就是使用汇编语言的分支逻辑重新表达这些goto命令。



## **8.1.2 Subroutine Calling**

Each programming language is characterized by a fixed set of built-in commands. The key abstraction mechanism provided by modern languages is the freedom to extend this basic repertoire with high-level, programmer-defined operations. In procedural languages, the high-level operations are called subroutines, procedures, or functions, and in object-oriented languages they are usually called methods. Throughout this chapter, all these high-level program units are referred to as subroutines.

In well-designed programming languages, the use of a high-level operation (implemented by a subroutine) has the same “look and feel” as that of built-in commands. For example, consider the functions add and raise to a power. Most languages feature the former as a built-in operation, while the latter may be written as a subroutine. In spite of these different implementations, both functions should ideally look alike from the caller’s perspective. This would allow the caller to weave the two operations together naturally, yielding consistent and readable code. A stack language implementation of this principle is illustrated in figure 8.2.

We see that the only difference between invoking a built-in command and calling a user-defined subroutine is the keyword call preceding the latter. Everything else is exactly the same: Both operations require the caller to set up their arguments, both operations are expected to remove their arguments from the stack, and both operations are expected to return a value which becomes the topmost stack element. The uniformity of this protocol has a subtle elegance that, we hope, is not lost on the reader.

Subroutines like power usually use local variables for temporary storage. These local variables must be represented in memory during the subroutine’s lifetime, namely, from the point the subroutine starts executing until a return command is encountered. At this point, the memory space occupied by the subroutine’s local variables can be freed. This scheme is complicated by allowing subroutines to be arbitrarily nested: One subroutine may call another subroutine, which may then call another one, and so on. Further, subroutines should be allowed to call themselves recursively; each recursive call must be executed independently of all the other calls and maintain its own set of local and argument variables. How can we implement this nesting mechanism and the memory management tasks implied by it?

The property that makes this housekeeping task tractable is the hierarchical nature of the call-and-return logic. Although the subroutine calling chain may be arbitrarily deep as well as recursive, at any given point in time only one subroutine executes at the top of the chain, while all the other subroutines down the calling chain are waiting for it to terminate. This *Last-In-First-Out* (LIFO) processing model lends itself perfectly well to a stack data structure, which is also LIFO. When subroutine xxx calls subroutine yyy, we can push (save) xxx’s world on the stack and branch to execute yyy. When yyy returns, we can pop (reinstate) xxx’s world off the stack, and continue executing xxx as if nothing happened. This execution model is illustrated in figure 8.3.

We use the term *frame* to refer, conceptually, to the subroutine’s local variables, the arguments on which it operates, its working stack, and the other memory segments that support its operation. In chapter 7, the term stack referred to the working memory that supports operations like pop, push, add, and so on. From now on, when we say *stack* we mean *global stack—*the memory area containing the frames of the current subroutine and all the subroutines waiting for it to return. These two stack notions are closely related, since the working stack of the current subroutine is located at the very tip of the global stack.

To recap, the low-level implementation of the call xxx operation entails saving the caller’s frame on the stack, allocating stack space for the local variables of the called subroutine (xxx), then jumping to execute its code. This last “mega jump” is not hard to implement. Since the name of the target subroutine is specified in the call command, the implementation can resolve the symbolic name to a memory address, then jump to execute the code starting at that address. Returning from the called subroutine via a return command is trickier, since the command specifies no return address. Indeed, the caller’s anonymity is inherent in the very notion of a subroutine call. For example, subroutines like power(x,y) or sqrt(x) are designed to serve any caller, implying that the return address cannot be part of their code. Instead, a return command should be interpreted as follows: Redirect the program’s execution to the command following the call command that called the current subroutine, wherever this command may be. The memory location of this command is called *return address.*

A glance at figure 8.3 suggests a stack-based solution to implementing this return logic. When we encounter a call xxx operation, we know exactly what the return address should be: It’s the address of the next command in the caller’s code. Thus, we can push this return address on the stack and proceed to execute the code of the called subroutine. When we later encounter a return command, we can pop the saved return address and simply goto it. In other words, the return address can also be placed in the caller’s frame.

## 子程序调用

每种编程语言都有一组固定的内置命令。 现代语言提供的关键抽象机制是可以自由地通过程序员定义的高级操作来扩展此基本库。 在过程语言中，高级操作称为子例程，过程或函数，而在面向对象的语言中，它们通常称为方法。 在本章中，所有这些高级程序单元都称为子例程。

在设计良好的编程语言中，使用高级操作（由子例程实现）具有与内置命令相同的“外观”。 例如，考虑功能加和乘幂。 大多数语言都将前者作为内置操作来使用，而将后者作为子例程来编写。 尽管实现方式不同，但从调用者的角度来看，两个功能在理想情况下应该看起来相似。 这将允许调用者自然地将两个操作编织在一起，从而产生一致且可读的代码。 图8.2说明了此原理的堆栈语言实现。![](https://tva1.sinaimg.cn/large/0082zybpgy1gbr5c6vmrtj30dp04njrf.jpg)



我们看到，调用内置命令和调用用户定义的子例程之间的唯一区别是在后者之前的关键字调用。 其他所有操作都完全相同：这两个操作都要求调用者设置其参数，这两个操作均应从堆栈中删除其参数，并且这两个操作均应返回一个成为最高堆栈元素的值。 该协议的一致性具有微妙的优雅，我们希望读者不要对此感到迷惑。

诸如power之类的子例程通常使用局部变量进行临时存储。 这些局部变量必须在子例程的生命周期内（即，从子例程开始执行直到遇到返回命令）在内存中表示。 此时，可以释放子例程的局部变量所占用的内存空间。 通过允许任意嵌套子例程，此方案变得很复杂：一个子例程可以调用另一个子例程，然后可以再调用另一个子例程，依此类推。 此外，应该允许子例程递归调用它们； 每个递归调用必须独立于所有其他调用执行，并且必须维护自己的一组局部变量和参数变量。 我们如何实现这种嵌套机制及其隐含的内存管理任务？

使此内务处理易于处理的属性是调用和返回逻辑的分层性质。 尽管子例程调用链可能是任意深度，也可能是递归的，但在任何给定的时间点，只有一个子例程在链的顶部执行，而调用链下游的所有其他子例程都在等待其终止。 这种后进先出（LIFO）处理模型非常适合于堆栈数据结构，它也是LIFO。 当子例程xxx调用子例程yyy时，我们可以将xxx的环境压入（保存）在堆栈上并分支以执行yyy。 当yyy返回时，我们可以将xxx的世界弹出（恢复）状态，然后继续执行xxx，就好像什么都没发生一样。 该执行模型如图8.3所示。

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbr5rb7j8aj30b907h3ym.jpg)

我们使用“框架”一词在概念上指代子例程的局部变量，子例程在其上运行的参数，其工作堆栈以及支持该子例程的其他内存段。 在第7章中，术语堆栈指的是支持诸如pop，push，add等操作的工作内存。 从现在开始，当我们说堆栈时，我们指的是全局堆栈，即包含当前子例程和所有等待其返回的所有子例程的帧的存储区。 这两个堆栈概念紧密相关，因为当前子例程的工作堆栈位于全局堆栈的最顶端。

概括地说，低级调用xxx操作的实现需要将调用者的帧保存在堆栈中，为被调用子例程（xxx）的局部变量分配堆栈空间，然后跳转以执行其代码。最后的“大跃进”并不难实现。由于在调用命令中指定了目标子例程的名称，因此实现可以将符号名称解析为内存地址，然后跳转到该地址处执行代码。通过返回命令从调用的子例程返回比较麻烦，因为该命令未指定返回地址。确实，在子例程调用的概念中，调用者的匿名性是固有的。例如，诸如power（x，y）或sqrt（x）之类的子例程旨在服务于任何调用者，这意味着返回地址不能成为其代码的一部分。相反，返回命令的解释应如下：将程序的执行重定向到调用当前子例程的调用命令之后的命令，无论该命令位于何处。该命令的存储位置称为返回地址。

一览图8.3可以看出实现此返回逻辑的基于堆栈的解决方案。 当我们遇到呼叫xxx操作时，我们确切知道返回地址应该是：这是调用者代码中下一个命令的地址。 因此，我们可以将此返回地址压入堆栈，然后继续执行被调用子例程的代码。 以后遇到返回命令时，我们可以弹出已保存的返回地址，然后直接转到它。 换句话说，寄信人地址也可以放在呼叫者的框架中。



# ***\*8.2 VM Specification, Part II\****

his section extends the basic VM specification from chapter 7 with program flow and *function calling* commands, thereby completing the overall VM specification.

**他的部分从程序的流程和函数调用命令扩展了第7章中的基本VM规范，从而完成了整个VM规范。**



## **8.2.1 Program Flow Commands**

The VM language features three program flow commands:

■ label *label* This command labels the current location in the function’s code.

Only labeled locations can be jumped to from other parts of the program. The scope of the label is the function in which it is defined. The label is an arbitrary string composed of any sequence of letters, digits, underscore (_), dot (.), and colon (:) that does not begin with a digit.

■ goto *label* This command effects an unconditional goto operation, causing execution to continue from the location marked by the label. The jump destination must be located in the same function.

■ if-goto *label* This command effects a conditional goto operation. The stack’s topmost value is popped; if the value is not zero, execution continues from the location marked by the label; otherwise, execution continues from the next command in the program. The jump destination must be located in the same function.

**VM语言具有三个程序流命令：**

**■label label此命令在功能代码中标记当前位置。**

**只能从程序其他部分跳转到带有标签的位置。 标签的范围是定义标签的功能。 标签是一个任意字符串，由不以数字开头的字母，数字，下划线（_），点（。）和冒号（:)的任何序列组成。**

**■goto label该命令将执行无条件的goto操作，从而使执行从标签标记的位置继续。 跳转目标必须位于同一功能中。**

**■if-goto标签此命令影响条件的goto操作。 堆栈的最高值被弹出； 如果该值不为零，则从标签标记的位置继续执行； 否则，将从程序中的下一个命令继续执行。 跳转目标必须位于同一功能中。**

## **8.2.2 Function Calling Commands**

Different high-level languages have different names for program units including functions, procedures, methods, and subroutines. In our overall compilation model (elaborated in chapters 10-11), each such high-level program unit is translated into a low-level program unit called *VM function*, or simply function.

A function has a symbolic name that is used globally to call it. The function name is an arbitrary string composed of any sequence of letters, digits, underscore (_), dot (.), and colon (:) that does not begin with a digit. (We expect that a method bar in class Foo in some high-level language will be translated by the compiler to a VM function named Foo.bar). The scope of the function name is global: All functions in all files are seen by each other and may call each other using the function name.

The VM language features three function-related commands:

■ function *f n* Here starts the code of a function named f that has n local variables;

■ call *f m* Call function *f*, stating that *m* arguments have already been pushed onto the stack by the caller;

■ return Return to the calling function.

**不同的高级语言对程序单元具有不同的名称，包括功能，过程，方法和子例程。在我们的总体编译模型（在第10-11章中进行了详细介绍）中，每个这样的高级程序单元都转换为称为VM函数或简称为函数的低级程序单元。**

**函数具有一个全局使用的符号名称。函数名称是一个任意字符串，由不以数字开头的字母，数字，下划线（_），点（。）和冒号（:)的任何序列组成。 （我们期望编译器将Foo类中某种高级语言的方法栏翻译成名为Foo.bar的VM函数）。函数名称的范围是全局的：所有文件中的所有函数彼此可见，并且可以使用函数名称相互调用。**

**VM语言具有三个与功能相关的命令：**

**■函数f n从此处开始具有n个局部变量的名为f的函数的代码；**

**■call f m调用函数f，表明调用者已将m个参数压入堆栈；**

**■return返回到调用函数。**



## **8.2.3 The Function Calling Protocol**

The events of calling a function and returning from a function can be viewed from two different perspectives: that of the calling function and that of the called function.

**调用函数和从函数返回的事件可以从两个不同的角度来看：调用函数的事件和被调用函数的事件。**

*The calling function view:*■ Before calling the function, the caller must push as many arguments as necessary onto the stack;■ Next, the caller invokes the function using the call command;■ After the called function returns, the arguments that the caller has pushed before the call have disappeared from the stack, and a return value (that always exists) appears at the top of the stack;■ After the called function returns, the caller’s memory segments argument, local, static, this, that, and pointer are the same as before the call, and the temp segment is undefined.*The called function view:*■ When the called function starts executing, its argument segment has been initialized with actual argument values passed by the caller and its local variables segment has been allocated and initialized to zeros. The static segment that the called function sees has been set to the static segment of the VM file to which it belongs, and the working stack that it sees is empty. The segments this, that, pointer, and temp are undefined upon entry.■ Before returning, the called function must push a value onto the stack.

**在调用函数视图中：**

**■在调用函数之前，调用者必须将所需数量的参数压入堆栈;**

**■接下来，调用者使用call命令调用该函数;**

**■被调用函数返回后，调用者具有的参数在调用从堆栈中消失之前被按入，并且返回值（始终存在）出现在堆栈顶部;**

**■被调用函数返回后，调用者的内存段参数，局部，静态，this，that和指针与调用之前相同，并且temp段未定义。被调用函数视图：**

**■当被调用函数开始执行时，其参数段已使用调用方传递的实际参数值进行了初始化，并且已分配了其局部变量段并初始化为零。被调用函数看到的静态段已设置为它所属的VM文件的静态段，并且它看到的工作堆栈为空。在输入时未定义this，that，pointer和temp的段。**

**■返回之前，被调用的函数必须将一个值压入堆栈。**

To repeat an observation made in the previous chapter, we see that when a VM function starts running (or resumes its previous execution), it assumes that it is surrounded by a private world, all of its own, consisting of its memory segments and stack, waiting to be manipulated by its commands. The agent responsible for building this virtual worldview for every VM function is the VM implementation, as we elaborate in section 8.3.

**重复上一章中的观察，我们看到，当VM函数开始运行（或恢复其先前的执行）时，它假定它被私有世界包围着，私有世界由其内存段和堆栈组成 ，等待被其命令操纵。 正如我们在第8.3节中详述的那样，负责为每个VM功能构建此虚拟世界观的代理是VM实现。**



## **8.2.4 Initialization**

A VM program is a collection of related VM functions, typically resulting from the compilation of some high-level program. When the VM implementation starts running (or is reset), the convention is that it always executes an argument-less VM function called Sys.init. Typically, this function then calls the main function in the user’s program. Thus, compilers that generate VM code must ensure that each translated program will have one such Sys.init function.

**VM程序是相关VM功能的集合，通常是由一些高级程序的编译产生的。 当VM实现开始运行（或重置）时，约定是它始终执行称为Sys.init的无参数VM函数。 通常，此函数然后在用户程序中调用main函数。 因此，生成VM代码的编译器必须确保每个翻译的程序都具有一个这样的Sys.init函数。**



# ***\*8.3 Implementation\****

This section describes how to complete the VM implementation that we started building in chapter 7, leading to a full-scale virtual machine implementation. Section 8.3.1 describes the stack structure that must be maintained, along with its standard mapping over the Hack platform. Section 8.3.2 gives an example, and section 8.3.3 provides design suggestions and a proposed API for actually building the VM implementation.

**本节介绍如何完成我们在第7章中开始构建的VM实施，从而实现全面的虚拟机实施。 第8.3.1节描述了必须维护的堆栈结构，以及在Hack平台上的标准映射。 第8.3.2节提供了一个示例，而第8.3.3节提供了用于实际构建VM实现的设计建议和建议的API。**



Some of the implementation details are rather technical, and dwelling on them may distract attention from the overall VM operation. This big picture is restored in section 8.3.2, which illustrates the VM implementation in action. Therefore, one may want to consult 8.3.2 for motivation while reading 8.3.1.

**一些实施细节是相当技术性的，并且过多地关注它们可能会分散整个VM操作的注意力。 在8.3.2节中还原了此全局图，该节说明了正在运行的VM实现。 因此，阅读8.3.1时可能要参考8.3.2的动机。**



## **8.3.1 Standard VM Mapping on the Hack Platform, Part II**

**The Global Stack** The memory resources of the VM are implemented by maintaining a global stack. Each time a function is called, a new block is added to the global stack. The block consists of the arguments that were set for the called function, a set of pointers used to save the state of the calling function, the local variables of the called function (initialized to 0), and an empty working stack for the called function. Figure 8.4 shows this generic stack structure.

**全局堆栈VM的内存资源是通过维护全局堆栈来实现的。 每次调用函数时，都会向全局堆栈中添加一个新块。 该块由为调用函数设置的参数，用于保存调用函数状态的一组指针，被调用函数的局部变量（初始化为0）以及被调用函数的空工作堆栈组成 。 图8.4显示了这种通用堆栈结构。**







______________________________________________________________________________________________________________________________________________________________________________________________________________________________

子程序通常使用局部变量 local variables 进行临时存储,在子程序的生命周期中,也就是从子程序开始执行到 return 命令位置,必须为这些局部变量分配内存.子程序返回时候,被这些局部变量占用的内存将被释放.当子程序被任意嵌套时候,子程序可以调用另一个子程序,这将会变得十分复杂,同时还有递归调用自身.因此子程序实现机制是后进先出的堆模式.因此当子程序 xxx 调用子程序 yyy 的时候,可以将 xxx 的环境变量压入堆栈中,然后执行 yyy,yyy 返回后,再将 xxx 的环境变量从堆栈中弹出(恢复),如果没有特殊的情况下,就执行 xxx.

我们在概念上使用帧(frame)来代表子程序的局部变量的集合,它包括子程序的参数,工作堆栈和运行过程中所使用的内存段,现在开始,堆栈(stack)开始指代**全局堆栈**,这是由当前子程序的帧,和所有正在等待该子程序返回其他子程序的帧所构成的内存区,这两个不同的堆栈概念是紧密相关的,因为当前子程序的工作堆栈位于全局堆栈的顶部.

在 call xxx 操作的底层实现中,先将调用者的帧保存到堆栈中,然后为子程序 xxx 的局部变量分配堆栈空间,最后跳到子程序 xx 开始执行其代码,最后一部大跳转,并不难实现,因为目标子程序的名字在 call 命令中已经被指定了.这样的调用过程中可以将这个目标程序名解析成一个内存地址,然后跳转到地址为基址的代码段,(该代码段就是子程序的代码段)开始执行.而通过 return 命令从子程序中返回的过程却暗藏玄机,因为命令中并未指定返回地址,事实上,在所有调用子程序的过程中,调用者的返回地址并没有显式的给出,比如因为想`power(x,y)`或者`sqrt(x)`这样的子程序可以被任何调用者调用,那么它的代码中就不可能给出具体的返回地址,所以,return 命令应该按如下方式来解释:它将程序的执行重定向到调用语句 call 命令的下一条命令所在的内存地址,(无论这条命令的内存地址位置在哪),即称之为**返回地址**(return address).

基于堆栈调用的返回实现过程如图 8.3 所示,我们调用 call xxx 指令执行调用操作时, 应该知道准确的返回地址:指令 call xxx 的下一条指令的内存地址,因此,我们将该指令内存地址作为返回地址压入堆栈保存,然后去执行子程序.当我门最终遇到 return 命令时候,我们就将先前保存在对炸某种的返回的地址弹出来,然后只用简单的 goto 命令跳转到这个地址就可以了,换句话说,这就是返回地址也可以保存在调用者的帧里面.

## VM 规范详述 II

### 程序控制命令

VM 语言的三种形式的程序控制命令流:

- **label _label_** 该命令标记程序中某条指令的位置,在程序中的跳转指令就只能跳转到被 **label _label_** 所标记的位置,label 标签所指示的代码段范围就是程序中定义的函数体,label 可以是任意字母,数字,下划线和冒号:组成的组付出,但不能是数字开头.
- **goto _label_** 该命令执行**条件跳转**操作.首先,将布尔表达式的运算结果从堆栈顶段弹出,如果该值非 0,那么程序就跳转到 label 标志的位置继续进行;否则,继续执行程序中的下一条命令.跳转的目的地址必须是位域同一个函数内.

### 函数调用命令 Function Calling Commands

不同的高级语言对于程序单元(program units)概念采用不同的名称, 包括 函数 function, 过程 procedure, 方法 method, 以及子程序 subroutine.在整个便已模型中, 每个高级程序单元都被翻译成 VM 函数.或者简称**函数**.

函数的名称是一个全局量,可以是任意字母,数字,下划线,点,以及冒号组成的字符串, 但不能以数字开头,(如果在高级语言的 Foo 类中定义函数 bar,那么该函数应该经过编译翻译成为对应的名为 Foo.bar 的 function).函数的名称的使用范围是全局的,即所有文件中的所有函数都可以通过这个全局名称相互调用.

VM 语言的是那种函数相关的命令:

- function _f_ _n_ 一段函数名为 f 的代码,该函数有 n 个参数
- call _f_ _n_ 调用函数 f,其中 m 个参数已经被调用者压入调用栈;
- return 返回到调用者.

### 函数调用协议 The Function Calling Protocol

调用函数和从函数返回这两个操作可以用两个不同的角度来看. 即**调用者**和**被调用者**

#### 调用者角度

- 在调用函数之前,调用者必须将必要的参数压入堆栈;
- 接着,调用者使用 call 命令来调用函数;
- 被调用函数返回后,调用者先前压入堆栈的参数将被删除,并且函数的返回值的返回值将出现在栈顶;
- 被调用函数返回后,调用者的各个内存段(入 argument,local,static,this,that 和 pointer)跟调用之前一样,temp 未被定义

#### 被调用者角度

- 当被调用的 函数开始执行,其 argument,segment 段被初始化为调用者所传递的参数,为其 local segment 段分配内存空间初始化为 0,它的 static segment 段被置为其所属 vm 文件中的 static segment, 工作堆栈为空,this,that,pointer 和 temp 四个指针均为初始化.
- 返回前,被调用函数必须将某个值压入堆栈.

### 初始化 initialization

VM 程序是一组相关的 VM 函数集合,一般来自于某种高级程序的编译,当 VM 实现开始运行(或者重启),按照惯例,他总是执行名称为 Sys.init 的无参数 VM 函数.接着该函数调用用户程序中的主函数.因此,生成 VM 的编译器必须保证每个翻译后的程序都有这个`Sys.init`函数.

## 实现 Implementation

这一章节介绍了如何完成从第七章开始构建的 VM 实现机制,最终实现整个虚拟机. 其中一些细节还是相当复杂的,深究这些技术细节会使得我们的注意力偏离整个 VM 操作. 因此不必纠结细节.

**全局堆栈** VM 的内存资源是通过维护一个全局的堆栈来得到的,每当调用一个函数时候,该函数对应的帧(frame)就会被压入全局堆栈,该帧包括被调用函数将要用到的参数,一组 用于保存调用者状态的指针(pointer);被调用函数的局部变量(被初始化为 0);以及一个被调用函数将要使用的工作堆栈(当前为空).

[image](./G_STACK.png)
全局堆栈结构

值得注意的是图中的阴影区以及 ARG,LCL 和 SP 指针对于 VM 函数是不可见的(无法感知他们的存在).这三个指针 是更加底层的 VM 实现函数调用与返回协会中使用的.

我们直到如何才能在 Hack 平台上面实现这种模型呢?前面介绍过,标准映射协议制定了堆栈的内存地址应该为 256,意味着 VM 的实现机制可以从生成将 sp 指针设置为 256 的汇编代码(SP=256)开始,于是 每当 VM 实现遇到诸如 pop,push,add 等命令时,就会生成相应的汇编代码,这些代通过操作 sp 和内存中对应的存储单元来实现对应的 pop,push,add 命令.所有这些工作已经在第七章完成,同样的,当 VM 实现遇到诸如 call,function,return 的命令的时候,他也将生成如图 8.4 所示的堆栈结构的汇编代码.

函数调用协议的实现 函数调用协议以及其对应的全局堆栈结构能够执行如图所示.的伪指令.

| 符号                    | 用法                                                                                                                                                                                           |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| SP,LCL, ARG, THIS, THAT | 这些预定义符号分别指向栈顶和各虚拟段 local,argument,this,that 的基地址                                                                                                                         |
| R13-R15                 | 这些预定义符号可以用于任何用途                                                                                                                                                                 |
| xxx.h                   | 在 VM 文件中 xxx.vm 中每个静态变量 j 被翻译成汇编符号 xxx.j.在随后的汇编过程中,Hack 汇编编译器会为这些编译器分配 RAM 空间                                                                      |
| functionName\$label     | 在 VM 函数 f 中的每个 label b 命令应该生成唯一的全局符号`f\$b`,这里 f 是函数名,b 是 VM 函数体内的标记符号. 在将 VM 命令 goto b 和 if-goto b 翻译成目标语言时,应该使用完整的符号`f\$b`,而不是 b |
| (FunctionName)          | 每个 VM 函数 f 应该生成一个符号 f,以指代函数 f 在目标计算机指令内存中的地址入口                                                                                                                |
| return-address          | 每个 VM 函数调用应该 生成 并在翻译后的代码中插入唯一的标号,它代表被调用函数(called function)的返回地址,即 内存单元的地址(目标计算机的内存).该 地址就是紧接着调用命令之后的指令地址             |

**引导程序代码** 当 VM-Hack 翻译器编译 VM 程序时,会产生一个用 Hack 汇编语言编写的`.asm`文件.他必须符合规范

1. VM 堆栈的初始地址必须被映射到 RAM[256];
2. 经过编译后的 VM 程序所执行的第一个 VM 函数必须是`Sys.init`

如何在 VM 翻译器生成的`.asm`文件中执行这个初始化呢,在第五章构建 Hack 计算机引荐时候,我们设计得失:在重置 reset 时候获取 ROM[0]的位置并且执行,因此起始于 ROM 的 0 的代码段,称之为`引导程序码`,是计算机 `启动`时候要执行的第一段代码,于是,前面介绍的,计算机的引导程序代码应该执行一下操作

```bash
SP=256          // 将堆栈指针初始化0x0100
call Sys.init   // 开始执行(翻译后的)Sys.init
```

`Sys.init`将调用主程序中的主函数,然后进入无限循环,这样翻译后的 VM 程序就进入运行状态.

程序 `program` ,主程序 `mian program` 和主函数`main function`的概念与编译过程有关,在不同的高级语言中他们的概念并不相同,例如,在 jack 语言中,默认的是自动开始运行的第一个程序单元就是 Main 类中的 main 方法.同样让 JVM 去执行给定的类,JVM 会去寻找并执行 Foo.main 方法,通过正确的编写 Sys.init,每种语言都能执行这样的自动启动程序

### 范例

正数的 n 的阶乘可以用迭代方程`n!=1*2*3...*(n-1)*n`,图 8.7 给出了算法的实现.

现在用重点分析下面图片中的 call mult 命令,

### VM 实现的设计建议

项目 7 中,构建了基本的 VM 翻译模块:Parser 和 CodeWriter. 通过扩展这两个人模块的功能可以实现 2 完整的 VM.\

Parser 模块:项目 7 中构建的语法分析器还不能分析本章介绍的 6 个 VM 命令,这里就把他们追加进来.需要确认的是:项目 7 中开发的 commandType 方法需要返回 6 个 VM 命令对应的常数: `C_LABEL`,`C_TOTO`,`C_IF`,`C_FUNCTION`, `C_RETURN`, `C_CALL`.

CodeWriter 模块:

### SimplerFunction.vm

抽象函数帧在内存中的表现形式

[image](./G_STACK.png)

|        | 调用链中所有函数的调用帧 |                                                                    |
| :----: | :----------------------: | :----------------------------------------------------------------: |
| ARG--> |         argment0         |                                                                    |
|        |         argment1         |                      当前函数的参数被压入堆栈                      |
|        |           ...            |                                                                    |
|        |       argment n-1        |                                                                    |
|        |      return address      |                                                                    |
|        |        saved LCL         |                                                                    |
|        |        saved ARG         | 保存调用函数的状态,以便于在当前被调用函数返回时候,回复调用函数的帧 |
|        |        saved THIS        |                                                                    |
|        |        saved THAT        |                                                                    |
| LCL->  |         local 0          |                                                                    |
|        |         local 1          |                      当前被调用函数的局部变量                      |
|        |           ...            |                                                                    |
|        |        local k-1         |                                                                    |
|  SP->  |                          |                      当前被调用函数的工作堆栈                      |
|        |        🔽🔽🔽🔽🔽        |                                                                    |
