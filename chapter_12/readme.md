In previous chapters of this book, we described and built the hardware architecture of a computer platform, called Hack, and the software hierarchy that makes it usable. In particular, we introduced an object-based language, called Jack, and described how to write a compiler for it. Other high-level programming languages can be specified on top of the Hack platform, each requiring its own compiler.

在本书的前几章中，我们描述并构建了称为Hack的计算机平台的硬件体系结构，以及使之可用的软件层次结构。 特别是，我们引入了一种称为Jack的基于对象的语言，并描述了如何为其编写编译器。 可以在Hack平台上指定其他高级编程语言，每种语言都需要自己的编译器。



The last major interface missing in this puzzle is an operating system (OS). The OS is designed to close gaps between the computer’s hardware and software systems, and to make the overall computer more accessible to programmers and users. For example, in order to render the text “Hello World” on our computer’s screen, several hundred pixels must be drawn at specific screen locations. This can be done by consulting the hardware specification and writing code that puts the necessary bits in the RAM-resident screen memory map. Obviously, high-level programmers expect something better than that. They want to use a command like printString(“Hello World”) and let someone else worry about the details. And that’s where the operating system enters the picture.

Throughout this chapter, the term operating system is used rather loosely. In fact, the OS services that we describe comprise an operating system in a very minimal fashion, aiming at (i) encapsulating various hardware-specific services in a software-friendly way, and (ii) extending high-level languages with various functions and abstract data types. The dividing line between an operating system in this sense and a standard language library is not very clear. Indeed, some modern languages, most notably Java, tend to pack many classic operating system services like GUI management, memory management, and multitasking in its standard software library, along with many language extensions.

这个难题中缺少的最后一个主要接口是操作系统（OS）。该操作系统旨在缩小计算机硬件和软件系统之间的距离，并使整个计算机更易于程序员和用户访问。例如，为了在我们的计算机屏幕上呈现“ Hello World”文本，必须在特定的屏幕位置绘制数百个像素。这可以通过参考硬件规范并编写将必要的位放入驻留于RAM的屏幕存储器映射中的代码来完成。显然，高级程序员期望比这更好的东西。他们想使用诸如printString（“ Hello World”）之类的命令，并让其他人担心这些细节。这就是操作系统输入图片的地方。
在本章中，操作系统一词的使用较为宽松。实际上，我们描述的OS服务以一种非常简单的方式构成了一个操作系统，旨在（i）以软件友好的方式封装各种特定于硬件的服务，以及（ii）扩展具有各种功能的高级语言，以及抽象数据类型。从这个意义上说，操作系统与标准语言库之间的界限不是很清楚。实际上，某些现代语言（最著名的是Java）倾向于在其标准软件库中包含许多经典的操作系统服务，例如GUI管理，内存管理和多任务处理，以及许多语言扩展。

Following this pattern, the collection of services that we specify and build in this chapter can be viewed as a combination of a simple OS and a standard library for the Jack language. This OS is packaged as a collection of Jack classes, each providing a set of related services via Jack subroutine calls. The resulting OS has many features resembling those of industrial strength operating systems, but it still lacks numerous OS features such as process handling, disk management, communications, and more.

Operating systems are usually written in a high-level language and compiled into binary form, just like any other program. Our OS is no exception—it can be written completely in Jack. Yet unlike other programs written in high-level languages, the operating system code must be aware of the hardware platform on which it runs. In other words, in order to hide the gory hardware details from the application programmer, the OS programmer must write code that manipulates these details directly (a task that requires access to the hardware documentation). Conveniently, this can be done using the Jack language. As we observe in this chapter, Jack was defined with sufficient “lowness” in it, permitting an intimate closeness to the hardware when needed.

The chapter starts with a relatively long Background section, describing key algorithms normally used to implement basic operating system services. These include mathematical functions, string operations, memory management, handling text and graphics output to the screen, and handling inputs from the keyboard. This algorithmic introduction is followed by a Specification section, providing the complete API of the Jack OS, and an Implementation section, describing how to build the OS using the classic algorithms presented earlier. As usual, the final Project section provides all the necessary project materials for gradual construction and unit-testing the entire OS presented in the chapter.

The chapter provides two key lessons, one in software engineering and one in computer science. First, we complete the construction of the high-level language, compiler, and operating system trio. Second, since operating system services must execute efficiently, we pay attention to running time considerations. The result is an elegant series of algorithms, each being a computer science gem.

**按照这种模式，我们在本章中指定和构建的服务集合可以看作是简单的操作系统和杰克语言的标准库的组合。该操作系统打包为Jack类的集合，每个类通过Jack子例程调用提供一组相关服务。最终的OS具有许多与工业级操作系统类似的功能，但仍缺少许多OS功能，例如流程处理，磁盘管理，通信等。**
**就像任何其他程序一样，操作系统通常用高级语言编写并编译为二进制形式。我们的操作系统也不例外，它可以完全用Jack编写。但是，与其他用高级语言编写的程序不同，操作系统代码必须知道其运行所在的硬件平台。换句话说，为了对应用程序程序员隐藏复杂的硬件细节，OS程序员必须编写直接处理这些细节的代码（此任务需要访问硬件文档）。方便地，这可以使用杰克语言来完成。正如我们在本章中所观察到的，Jack被定义为具有足够的“低度”，允许在需要时与硬件紧密贴合。**
**本章从相对较长的背景部分开始，描述通常用于实现基本操作系统服务的关键算法。这些功能包括数学函数，字符串操作，内存管理，处理文本和图形输出到屏幕以及处理键盘输入。在此算法简介之后，提供了Jack OS的完整API的“规范”部分和“实现”部分，描述了如何使用前面介绍的经典算法构建操作系统。与往常一样，最后的项目部分提供了所有必要的项目材料，用于逐步构建和对本章中介绍的整个OS进行单元测试。**
**本章提供了两个关键课程，一个是软件工程课程，另一个是计算机科学课程。首先，我们完成高级语言，编译器和操作系统三重奏的构建。其次，由于操作系统服务必须高效执行，因此我们要注意运行时间注意事项。结果是一系列优雅的算法，每个算法都是计算机科学的瑰宝。**



## **12.1.1 Mathematical Operations**

数学运算



Computer systems must support mathematical operations like addition, multiplication, and division. Normally, addition is implemented in hardware, at the ALU level, as we have done in chapter 3. Other operations like multiplication and division can be handled by either hardware or software, depending on the computer’s cost/performance requirements. This section shows how multiplication, division, and square root operations can be implemented efficiently in software, at the OS level. We note in passing that hardware implementations of these mathematical operations can be based on the same algorithms presented here.

**计算机系统必须支持数学运算，例如加法，乘法和除法。 通常，加法是在硬件上在ALU级别上实现的，就像我们在第3章中所做的那样。乘法和除法等其他操作可以由硬件或软件来处理，具体取决于计算机的成本/性能要求。 本节说明如何在OS级别的软件中有效地实现乘法，除法和平方根运算。 我们注意到，这些数学运算的硬件实现可以基于此处介绍的相同算法。**



**Efficiency First** Mathematical algorithms operate on *n*-bit binary numbers, with typical computer architectures having *n* = 16, 32, or 64. As a rule, we seek algorithms whose running time is proportional (or at least polynomial) in this parameter n. Algorithms whose running time is proportional to the value of *n*-bit numbers are unacceptable, since these values are exponential in n. For example, suppose we implement the multiplication operation *x* · *y* using the repeated addition algorithm for i = 1 ... y {*result* = result + *x*}. Well, the problem is that in a 64-bit computer, *y* can be greater than 18,000,000,000,000,000,000, implying that this naïve algorithm may run for years even on the fastest computers. In sharp contrast, the running time of the multiplication algorithm that we present below is proportional not to the multiplicands’ value, which may be as large as 2*n*, but rather to n. Therefore, it will require only *c·n*elementary operations for any pair of multiplicands, where c is a small constant representing the number of elementary operations performed in each loop iteration.

We use the standard “Big-Oh” notation, *O*(*n*), to describe the running time of algorithms. Readers who are not familiar with this notation can simply read *O*(*n*) as “in the order of magnitude of *n*.” With that in mind, we now turn to present an efficient multiplication *x · y* algorithm for *n*-bit numbers whose running time is *O*(*n*) rather than *O*(*x*) or *O*(*y*), which are exponentially larger.

**效率优先数学算法对n位二进制数进行操作，典型的计算机体系结构具有n = 16、32或64。通常，我们寻找运行时间与该参数n成比例（或至少为多项式）的算法。运行时间与n位数字的值成比例的算法是不可接受的，因为这些值在n中是指数级的。例如，假设我们对i = 1 ... y {结果=结果+ x}使用重复加法算法来实现乘法运算x·y。好吧，问题在于，在64位计算机上，y可能大于18,000,000,000,000,000,000，这意味着即使在最快的计算机上，这种简单的算法也可能会运行数年。与之形成鲜明对比的是，我们在下面介绍的乘法算法的运行时间与被乘数的值不成正比，被乘数的值可能最大为2n，而是与n成正比。因此，对于任何被乘数对，仅需要c·n个基本运算，其中c是一个小常数，表示每个循环迭代中执行的基本运算的数量。**
**我们使用标准的“ Big-Oh”符号O（n）来描述算法的运行时间。不熟悉此表示法的读者可以简单地将O（n）读为“按n的数量级”。考虑到这一点，我们现在转向为运行n位数字提供有效的x x y乘法算法。时间是O（n），而不是O（x）或O（y），后者呈指数增长。**

**Multiplication** Consider the standard multiplication method taught in elementary school. To compute 356 times 27, we line up the two numbers one on top of the other. Next, we multiply each digit of 356 by 7. Next, we “shift to the left” one position, and multiply each digit of 356 by 2. Finally, we sum up the columns and obtain the result. The binary version of this technique—figure 12.1—follows exactly the same logic.

The algorithm in figure 12.1 performs *O*(*n*) addition operations on *n*-bit numbers, where n is the number of bits in *x*and y. Note that *shiftedX* * 2 can be efficiently obtained by either left-shifting its bit representation or by adding *shiftedX* to itself. Both operations can be easily performed using primitive ALU operations. Thus this algorithm lends itself naturally to both software and hardware implementations.

**乘法考虑小学教授的标准乘法方法。 为了计算356乘以27，我们将两个数字一个接一个地排列。 接下来，我们将356的每个数字乘以7。接下来，我们将“左移”一个位置，然后将356的每个数字乘以2。最后，对列进行求和并得到结果。 该技术的二进制版本（图12.1）遵循完全相同的逻辑。**
**图12.1中的算法对n位数字执行O（n）加法运算，其中n是x和y中的位数。 请注意，可以通过左移其位移位表示或向其自身添加shiftX来有效地获取shiftedX * 2。 两种操作都可以使用原始ALU操作轻松执行。 因此，该算法自然适用于软件和硬件实现。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtunq05akj30dp08i0sq.jpg)





**A Comment about Notation** The algorithms in this chapter are written using a self-explanatory pseudocode syntax. The only non-obvious convention is that we use indentation to represent blocks of code (avoiding curly brackets or begin/end keywords). For example, in figure 12.1, *sum* = sum + *shiftedX* belongs to the single-statement body of the *if* statement whereas *shiftedX* = *shiftedX* * 2 ends the two-statement body of the *for* statement.

**关于符号的注释本章中的算法使用不言自明的伪代码语法编写。 唯一不明显的约定是，我们使用缩进来表示代码块（避免使用花括号或begin / end关键字）。 例如，在图12.1中，sum = sum + shiftedX属于if语句的单语句主体，而shiftedX = shiftedX * 2终止for语句的双语句主体。**



**Division** The naïve way to compute the division of two *n*-bit numbers *x/y* is to repeatedly subtract *y* from *x* until it is impossible to continue (i.e., until *x* < *y*). The running time of this algorithm is clearly proportional to the quotient, and may be as large as *O*(*x*), that is, exponential in the number of bits n. To speed up this algorithm, we can try to subtract large chunks of *y*’s from *x* in each iteration. For example, if *x* = 891 and *y* = 5, we can tell right away that we can deduct a hundred 5’s from *x* and the remainder will still be greater than 5, thus shaving 100 iterations from the naïve approach. Indeed, this is the rationale behind the school method for long division *x*/*y*. Formally, in each iteration we try to subtract from *x* the largest possible shift of y, namely, *y* · *T* where T is the largest power of 10 such that *y* · *T*≤ x. The binary version of this opportunistic algorithm is identical, except that T is a power of 2 instead of 10.

**除法计算两个n位数字x / y的除法的简单方法是从x重复减去y，直到不可能继续（即直到x <y）为止。 该算法的运行时间显然与商成正比，并且可能与O（x）一样大，也就是说，位数为n的指数。 为了加快该算法的速度，我们可以尝试在每次迭代中从x减去y的大块。 例如，如果x = 891且y = 5，我们可以立即告诉我们可以从x减去100，而余数仍大于5，因此从幼稚的方法中减少了100次迭代。 的确，这是长除法x / y的学校方法背后的原理。 形式上，在每次迭代中，我们尝试从x中减去y的最大可能偏移量，即y·T，其中T是10的最大幂，从而y·T≤x。 此机会算法的二进制版本是相同的，除了T是2的幂而不是10的幂。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtuqthpzvj30dp048a9x.jpg)















