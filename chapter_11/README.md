Most programmers take compilers for granted. But if you’ll stop to think about it for a moment, the ability to translate a high-level program into binary code is almost like magic. In this book we demystify this transformation by writing a compiler for Jack—a simple yet modern object-based language. As with Java and C#, the overall Jack compiler is based on two tiers: a virtual machine back-end, developed in chapters 7-8, and a typical front-end module, designed to bridge the gap between the high-level language and the VM language. The compiler’s front-end module consists of a syntax analyzer, developed in chapter 10, and a code generator—the subject of this chapter.

Although the compiler’s front-end comprises two conceptual modules, they are usually combined into a single program, as we will do here. Specifically, in chapter 10 we built a syntax analyzer capable of “understanding”—parsing—source Jack programs. In this chapter we extend the analyzer into a full-scale compiler that converts each “understood” high-level construct into an equivalent series of VM operations. This approach follows the modular analysis-synthesis paradigm underlying the construction of most compilers.



**大多数程序员认为编译器是理所当然的。 但是，如果您暂时不考虑它，那么将高级程序转换为二进制代码的能力几乎就像魔术一样。 在本书中，我们通过为Jack（一种简单而又现代的基于对象的语言）编写编译器来揭开这种转变的神秘面纱。 与Java和C＃一样，整个Jack编译器都基于两层：在第7-8章中开发的虚拟机后端，以及设计用于弥合高级语言和高级语言之间差距的典型前端模块。 VM语言。 编译器的前端模块由第10章开发的语法分析器和本章的主题代码生成器组成。**

**尽管编译器的前端包含两个概念模块，但它们通常被合并为一个程序，就像我们在这里所做的那样。 具体来说，在第10章中，我们构建了一种语法分析器，能够“理解”（解析）源Jack程序。 在本章中，我们将分析器扩展为一个全面的编译器，该编译器将每个“理解的”高级构造转换为等效的一系列VM操作。 这种方法遵循了大多数编译器构建背后的模块化分析-综合范式**



Modern high-level programming languages are rich and powerful. They allow defining and using elaborate abstractions such as objects and functions, implementing algorithms using elegant flow of control statements, and building data structures of unlimited complexity. In contrast, the target platforms on which these programs eventually run are spartan and minimal. Typically, they offer nothing more than a vector of registers for storage and a primitive instruction set for processing. Thus, the translation of programs from high-level to low-level is an interesting brain teaser. If the target platform is a virtual machine, life is somewhat easier, but still the gap between the expressiveness of a high-level language and that of a virtual machine is wide and challenging.

The chapter begins with a Background section covering the minimal set of topics necessary for completing the compiler’s development: managing a symbol table; representing and generating code for variables, objects, and arrays; and translating control flow commands into low-level instructions. The Specification section defines how to map the semantics of Jack programs on the VM platform and language, and the Implementation section proposes an API for a code generation module that performs this transformation. The chapter ends with the usual Project section, providing step-by-step guidelines and test programs for completing the compiler’s construction.

**现代高级编程语言既丰富又强大。 它们允许定义和使用复杂的抽象，例如对象和函数，使用优雅的控制语句流实现算法，以及构建无限复杂的数据结构。 相比之下，这些程序最终在其上运行的目标平台是最小的。 通常，它们只提供用于存储的寄存器向量和用于处理的原始指令集。 因此，程序从高级到低级的转换是一个有趣的脑筋急转弯。 如果目标平台是虚拟机，则生活会更轻松一些，但高级语言的表达能力与虚拟机的表达能力之间的差距仍然很大且具有挑战性。**

**本章从“背景”部分开始，涵盖完成编译器开发所需的最少主题集：管理符号表； 表示并生成变量，对象和数组的代码； 并将控制流命令转换为低级指令。 “规范”部分定义了如何在VM平台和语言上映射Jack程序的语义，“实现”部分为执行此转换的代码生成模块提出了API。 本章以通常的“项目”部分结尾，提供了逐步指南和测试程序，以完成编译器的构造。**

So what’s in it for you? Typically, students who don’t take a formal compilation course don’t have an opportunity to develop a full-scale compiler. Thus readers who follow our instructions and build the Jack compiler from scratch will gain an important lesson for a relatively small effort (of course, their knowledge of compilation theory will remain limited unless they take a course on the subject). Further, some of the tricks and techniques used in the code generation part of the compiler are rather clever. Seeing these tricks in action leads one to marvel, once again, at how human ingenuity can dress up a primitive switching machine to look like something approaching magic.

**那有什么用呢？ 通常，不参加正规编译课程的学生没有机会开发全面的编译器。 因此，遵循我们的说明并从头开始构建Jack编译器的读者将以相对较小的努力获得重要的教训（当然，除非他们学习该主题的课程，否则他们对编译理论的了解将仍然有限）。 此外，在编译器的代码生成部分中使用的一些技巧和技术也很聪明。 看到这些动作的技巧，人们再次惊叹于人类的创造力如何打扮原始的开关机器，使其看起来像某种接近魔法的东西。**



# ***\*11.1 Background\****

A program is essentially a series of operations that manipulate data. Thus, the compilation of high-level programs into a low-level language focuses on two main issues: data translation and command translation.

The overall compilation task entails translation all the way to binary code. However, since we are focusing on a two-tier compiler architecture, we assume throughout this chapter that the compiler generates VM code. Therefore, we do not touch low-level issues that have already been dealt with at the Virtual Machine level (chapters 7 and 8).

**程序本质上是一系列操作数据的操作。 因此，将高级程序编译为低级语言的重点是两个主要问题：数据转换和命令转换。**

**整个编译任务需要一直转换为二进制代码。 但是，由于我们专注于两层编译器体系结构，因此在本章中，我们始终假定编译器生成VM代码。 因此，我们不会涉及虚拟机级别已解决的低级问题（第7章和第8章）。**



## **11.1.1 Data Translation**

Programs manipulate many types of variables, including simple types like integers and booleans and complex types like arrays and objects. Another dimension of interest is the variables’ kind of life cycle and scope—namely, whether it is local, global, an argument, an object field, and so forth.

For each variable encountered in the program, the compiler must map the variable on an equivalent representation suitable to accommodate its type in the target platform. In addition, the compiler must manage the variable’s life cycle and scope, as implied by its kind. This section describes how compilers handle these tasks, beginning with the notion of a symbol table.

 

**程序可以处理多种类型的变量，包括简单类型（如整数和布尔值）和复杂类型（如数组和对象）。 另一个有趣的方面是变量的生命周期和范围类型，即变量是局部变量，全局变量，自变量，对象字段等。**

**对于程序中遇到的每个变量，编译器必须将变量映射到适合于在目标平台中容纳其类型的等效表示形式。 此外，编译器必须根据变量的种类来管理变量的生命周期和范围。 本节从符号表的概念开始介绍编译器如何处理这些任务。**



**Symbol Table** High-level programs introduce and manipulate many identifiers. Whenever the compiler encounters an identifier, say xxx, it needs to know what xxx stands for. Is it a variable name, a class name, or a function name? If it’s a variable, is xxx a field of an object, or an argument of a function? What type of variable is it—an integer, a boolean, a char, or perhaps some class type? The compiler must resolve these questions before it can represent xxx’s semantics in the target language. Further, all these questions must be answered (for code generation) each time xxx is encountered in the source code.

Clearly, there is a need to keep track of all the identifiers introduced by the program, and, for each one, to record what the identifier stands for in the source program and on which construct it is mapped in the target language. Most compilers maintain this information using a symbol table abstraction. Whenever a new identifier is encountered in the source code for the first time (e.g., in a variable declaration), the compiler adds its description to the table. Whenever an identifier is encountered elsewhere in the code, the compiler looks it up in the symbol table and gets all the necessary information about it. Here is a typical example:



**符号表高级程序引入并操纵了许多标识符。每当编译器遇到标识符（例如xxx）时，它都需要知道xxx代表什么。它是变量名，类名还是函数名？如果是变量，xxx是对象的字段还是函数的自变量？它是什么类型的变量-整数，布尔值，字符或某种类类型？编译器必须解决这些问题，才能以目标语言表示xxx的语义。此外，每次在源代码中遇到xxx时，都必须回答所有这些问题（用于代码生成）。**

**显然，需要跟踪该程序引入的所有标识符，并分别记录每个标识符在源程序中的含义以及将其映射到目标语言的构造上。大多数编译器使用符号表抽象来维护此信息。每当源代码中首次遇到新标识符时（例如，在变量声明中），编译器都会将其描述添加到表中。每当在代码的其他地方遇到标识符时，编译器都会在符号表中查找该标识符，并获取有关它的所有必要信息。这是一个典型的例子：**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtt9jx1mmj307x04ft8m.jpg)

**Symbol table** (of some hypothetical subroutine) 

The symbol table is the “Rosetta stone” that the compiler uses when translating high-level code involving identifiers. For example, consider the statement balance= balance+sum. Using the symbol table, the compiler can translate this statement into code reflecting the facts that balance is field number 2 of the current object, while sum is argument number 0 of the running subroutine. Other details of this translation will depend on the target language.

The basic symbol table abstraction is complicated slightly due to the fact that most languages permit different program units to use the same identifiers to represent completely different things. In order to enable this freedom of expression, each identifier is implicitly associated with a scope, namely, the region of the program in which the identifier is recognized. The scopes are typically nested, the convention being that inner-scoped definitions hide outer ones. For example, if the statement x++ appears in some C function, the C compiler first checks whether the identifier x is declared locally in the current function, and if so, generates code that increments the local variable. Otherwise, the compiler checks whether x is declared globally in the file, and if so, generates code that increments the global variable. The depth of this scoping convention is potentially unlimited, since some languages permit defining variables which are local only to the block of code in which they are declared.

Thus, we see that in addition to all the relevant information that must be kept about each identifier, the symbol table must also record in some way the identifier’s scope. The classic data structure for this purpose is a list of hash tables, each reflecting a single scope nested within the next one in the list. When the compiler fails to find the identifier in the table associated with the current scope, it looks it up in the next table in the list, from inner scopes outward. Thus if x appears undeclared in a certain code segment (e.g., a method), it may be that x is declared in the code segment that owns the current segment (e.g., a class), and so on.



**符号表（某些假设子程序）**

**符号表是编译器在翻译涉及标识符的高级代码时使用的“罗塞塔石”。例如，考虑语句balance = balance + sum。使用符号表，编译器可以将此语句转换为反映以下事实的代码：balance是当前对象的字段编号2，而sum是正在运行的子例程的参数编号0。此翻译的其他细节将取决于目标语言。**
**由于大多数语言都允许不同的程序单元使用相同的标识符来表示完全不同的事物，因此基本符号表的抽象稍微有些复杂。为了实现这种表达自由，每个标识符都与范围（即在其中识别标识符的程序区域）隐式关联。作用域通常是嵌套的，惯例是内部作用域的定义隐藏外部作用域的定义。例如，如果语句x ++出现在某些C函数中，则C编译器首先检查标识符x是否在当前函数中本地声明，如果是，则生成递增局部变量的代码。否则，编译器将检查x是否在文件中全局声明，如果是，则生成递增全局变量的代码。该范围约定的深度可能是无限的，因为某些语言允许定义仅在声明它们的代码块本地的变量。**
**因此，我们看到，除了必须保留的每个标识符的所有相关信息之外，符号表还必须以某种方式记录标识符的范围。用于此目的的经典数据结构是哈希表的列表，每个哈希表反映了嵌套在列表中下一个范围内的单个作用域。当编译器无法在与当前作用域关联的表中找到标识符时，它将从内部作用域向外查找列表中的下一个表。因此，如果x在某个代码段（例如，方法）中似乎未声明，则可能是x在拥有当前段（例如，类）的代码段中声明的，依此类推。**



**Handling Variables** One of the basic challenges faced by every compiler is how to map the various types of variables declared in the source program onto the memory of the target platform. This is not a trivial task. First, different types of variables require different sizes of memory chunks, so the mapping is not one-to-one. Second, different kinds of variables have different life cycles. For example, a single copy of each static variable should be kept alive during the complete duration of the program’s run-time. In contrast, each object instance of a class should have a different copy of all its instance variables (fields), and, when disposed, the object’s memory should be recycled. Also, each time a subroutine is being called, new copies of its local and argument variables must be created—a need that is clearly seen in recursion.

That’s the bad news. The good news is that we have already handled all these difficulties. In our two-tier compiler architecture, memory allocation of variables was delegated to the VM back-end. In particular, the virtual machine that we built in chapters 7-8 includes built-in mechanisms for accommodating the standard kinds of variables needed by most high-level languages: static, local, and argument variables, as well as fields of objects. All the allocation and de-allocation details of these variables were already handled at the VM level, using the global stack and the virtual memory segments.

Recall that this functionality was not achieved easily. In fact, we had to work rather hard to build a VM implementation that maps the global stack and the virtual memory segments on the ultimate hardware platform. Yet this effort was worth our while: For any given language L, any L-to-VM compiler is now completely relieved from low-level memory management. The only thing required from the compiler is mapping the variables found in the source program on the virtual memory segments and expressing the high-level commands that manipulate them using VM commands—a rather simple translation task.

**处理变量每个编译器面临的基本挑战之一是如何将源程序中声明的各种类型的变量映射到目标平台的内存中。这不是一件简单的任务。首先，不同类型的变量需要不同大小的内存块，因此映射不是一对一的。其次，不同种类的变量具有不同的生命周期。例如，在程序运行的整个过程中，每个静态变量的单个副本应保持活动状态。相反，一个类的每个对象实例应具有其所有实例变量（字段）的不同副本，并且在处置对象时，应回收对象的内存。另外，每次调用子例程时，都必须创建其局部变量和参数变量的新副本，这种需求可以在递归中清楚地看到。**
**那是个坏消息。好消息是我们已经解决了所有这些困难。在我们的两层编译器体系结构中，变量的内存分配被委派给VM后端。特别是，我们在第7-8章中构建的虚拟机包括用于适应大多数高级语言所需的标准种类变量的内置机制：静态变量，局部变量和参数变量，以及对象字段。这些变量的所有分配和取消分配细节已经在VM级别使用全局堆栈和虚拟内存段进行了处理。**
**回想一下，该功能并非易事。实际上，我们必须付出相当大的努力才能构建一个VM实现，该实现在最终的硬件平台上映射全局堆栈和虚拟内存段。然而，这项工作值得我们花点时间：对于任何给定的语言L，任何L-to-VM编译器现在都完全摆脱了低级内存管理。编译器唯一需要做的就是将源程序中找到的变量映射到虚拟内存段上，并表达使用VM命令操纵它们的高级命令，这是一个相当简单的转换任务。**

**Handling Arrays** *Arrays* are almost always stored as sequences of consecutive memory locations (multi-dimensional arrays are flattened into one-dimensional ones). The array name is usually treated as a pointer to the base address of the RAM block allocated to store the array in memory. In some languages like Pascal, the entire memory space necessary to represent the array is allocated when the array is declared. In other languages like Java, the array declaration results in the allocation of a single pointer only, which, eventually, may point to the array’s base address. The array proper is created in memory later, if and when the array is actually constructed at run-time. This type of dynamic memory allocation is done from the heap, using the memory management services of the operating system. Typically, the OS has an alloc(size) function that knows how to find an available memory block of size size and return its base address to the caller. Thus, when compiling a high-level statement like bar=new int [10], the compiler generates low-level code that effects the operation bar=alloc(10). This results in assigning the base-address of the array’s memory block to bar, which is exactly what we want. Figure 11.1 offers a snapshot of this practice.

Let us consider how the compiler translates the statement bar[k]=19. Since the symbol bar points to the array’s base-address, this statement can be also expressed using the C-language notation *(bar+k)=19, that is, “store 19 in the memory cell whose address is bar+k.” In order to implement this operation, the target language must be equipped with some sort of an indirect addressing mechanism. Specifically, instead of storing a value in some memory location y, we need to be able to store the value in the memory location whose address is the current contents of y. Different languages have different means to carry out this pointer arithmetic, and figure 11.2 shows two possibilities.

**处理数组数组几乎总是存储为连续存储位置的序列（多维数组被展平为一维数组）。阵列名称通常被视为指向分配用于将阵列存储在内存中的RAM块基地址的指针。在某些语言（例如Pascal）中，在声明数组时会分配表示数组所需的全部内存空间。在其他语言（如Java）中，数组声明仅导致分配单个指针，该指针最终可能指向数组的基地址。如果在运行时实际构造了数组，则稍后会在内存中创建适当的数组。这种类型的动态内存分配是使用操作系统的内存管理服务从堆中完成的。通常，操作系统具有alloc（size）函数，该函数知道如何查找大小为大小的可用内存块并将其基址返回给调用者。因此，当编译诸如bar = new int [10]之类的高级语句时，编译器会生成影响操作bar = alloc（10）的低级代码。这导致将数组存储块的基地址分配给bar，这正是我们想要的。图11.1提供了这种做法的快照。**
**让我们考虑编译器如何翻译语句bar [k] = 19。由于符号条指向数组的基地址，因此该语句也可以使用C语言符号*（bar + k）= 19来表示，即“将19存储在地址为bar + k的存储单元中”。为了实现此操作，目标语言必须配备某种间接寻址机制。具体而言，我们需要能够将值存储在地址为y的当前内容的存储位置中，而不是在某个存储位置y中存储值。不同的语言具有不同的方法来执行此指针算术，图11.2显示了两种可能性。**



![](https://tva1.sinaimg.cn/large/0082zybpgy1gbttlh3sfej30gg08ht8y.jpg)



**Handling Objects** Object instances of a certain class, say Employee, are said to encapsulate data items like name and salary, as well as a set of operations (methods) that manipulate them. The data and the operations are handled quite differently by the compiler. Let’s start with the data.

The low-level handling of object data is quite similar to that of arrays, storing the fields of each object instance in consecutive memory locations. In most object-oriented languages, when a class-type variable is declared, the compiler only allocates a pointer variable. The memory space for the object proper is allocated later, if and when the object is actually created via a call to a class constructor. Thus, when compiling a constructor of some class Xxx, the compiler first uses the number and type of the class fields to determine how many words—say n—are necessary to represent an object instance of type Xxx on the host RAM. Next, the compiler generates the code necessary for allocating memory for the newly constructed object, for example, this=alloc(n). This operation sets the this pointer to the base address of the memory block that represents the new object, which is exactly what we want. Figure 11.3 illustrates these operations in a Java context.



**处理对象据说某个类的对象实例（例如Employee）封装了数据项，例如名称和薪水，以及用于操作它们的一组操作（方法）。数据和操作由编译器完全不同地处理。让我们从数据开始。**
**对象数据的低级处理与数组非常相似，将每个对象实例的字段存储在连续的内存位置中。在大多数面向对象的语言中，声明类类型变量时，编译器仅分配一个指针变量。如果并且当通过调用类构造函数实际创建对象时，则会分配适当的对象的内存空间。因此，在编译某个类Xxx的构造函数时，编译器首先使用类字段的数量和类型来确定在主机RAM上表示Xxx类型的对象实例所需的字数（例如n）。接下来，编译器生成为新构造的对象分配内存所需的代码，例如this = alloc（n）。此操作将this指针设置为代表新对象的内存块的基地址，这正是我们想要的。图11.3说明了Java上下文中的这些操作。**

 ![](https://tva1.sinaimg.cn/large/0082zybpgy1gbttnohdogj30gg04nwek.jpg)





Since each object is represented by a pointer variable that contains its base-address, the data encapsulated by the object can be accessed linearly, using an index relative to its base. For example, suppose that the Complex class includes the following method:



**由于每个对象都由包含其基地址的指针变量表示，因此可以使用相对于其基址的索引来线性访问对象封装的数据。 例如，假设Complex类包含以下方法：**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbttpe02hwj305b01sa9u.jpg)

How should the compiler handle the statement im = im * c? Well, an inspection of the symbol table will tell the compiler that im is the second field of this object and that c is the first argument of the mult method. Using this information, the compiler can translate im = im * c into code effecting the operation *(this + 1) = *(this + 1) times (argument 0). Of course, the generated code will have to accomplish this operation using the target language.

Suppose now that we wish to apply the mult method to the b object, using a method call like b . mult(5). How should the compiler handle this method call? Unlike the fields data (e.g., re and im), of which different copies are kept for each object instance, only one copy of each method (e.g., mult) is actually kept at the target code level for all the object instances derived from this class. In order to make it look as if each object encapsulates its own code, the compiler must force this single method to always operate on the desired object. The standard compilation trick that accomplishes this abstraction is to pass a reference to the manipulated object as a hidden argument of the called method, compiling b . mult(5) as if it were written as mult(b, 5). In general then, each object-based method call foo . bar(v1, v2, ...) is translated into the VM code push foo, push v1, push v2, ... , call bar. This way, the compiler can force the same method to operate on any desired object for instance, creating the high-level perception that each object encapsulates its own code.

**编译器应如何处理语句im = im * c？好吧，检查符号表将告诉编译器im是该对象的第二个字段，而c是mult方法的第一个参数。使用此信息，编译器可以将im = im * c转换为代码，从而执行*（this + 1）= *（this + 1）次运算（参数0）。当然，生成的代码将必须使用目标语言来完成此操作。**
**现在假设我们希望使用像b这样的方法调用将mult方法应用于b对象。多（5）。编译器应如何处理此方法调用？与为每个对象实例保留不同副本的字段数据（例如re和im）不同，对于从该对象派生的所有对象实例，每种方法（例如，mult）的每个副本实际上仅保留在目标代码级别类。为了使其看起来好像每个对象都封装了自己的代码，编译器必须强制此单个方法始终对所需的对象进行操作。实现此抽象的标准编译技巧是将对操作对象的引用作为被调用方法的隐藏参数传递给编译b。 mult（5）就像写为mult（b，5）一样。通常，每个基于对象的方法都将调用foo。 bar（v1，v2，...）转换为VM代码push foo，push v1，push v2，...，调用栏。这样，编译器可以强制同一方法对任何所需对象进行操作，例如，使每个对象封装自己的代码，从而形成高级感知。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtu6but46j30gf0bk0ss.jpg)

However, the compiler’s job is not done yet. Since the language allows different methods in different classes to have the same name, the compiler must ensure that the right method is applied to the right object. Further, due to the possibility of method overriding in a subclass, compilers of object-oriented languages must do this determination at run-time. When run-time typing is out of the picture, for example, in languages like Jack, this determination can be done at compile-time. Specifically, in each method call like x . m(y), the compiler must ensure that the called method m() belongs to the class from which the x object was derived.

**但是，编译器的工作尚未完成。 由于该语言允许不同类中的不同方法具有相同的名称，因此编译器必须确保将正确的方法应用于正确的对象。 此外，由于可能会在子类中覆盖方法，因此面向对象语言的编译器必须在运行时进行此确定。 当无法进行运行时键入时（例如，使用Jack之类的语言），可以在编译时进行此确定。 具体来说，在每个方法中，像x一样调用。 m（y），编译器必须确保所调用的方法m（）属于x对象所源自的类。**



## **11.1.2 Commands Translation**

We now describe how high-level commands are translated into the target language. Since we have already discussed the handling of variables, objects, and arrays, there are only two more issues to consider: expression evaluation and flow control.

 

**Evaluating Expressions** How should we generate code for evaluating high-level expressions like x+g(2,y,-z)*5? First, we must “understand” the syntactic structure of the expression, for example, convert it into a parse tree like the one depicted in figure 11.4. This parsing was already handled by the syntax analyzer described in chapter 10. Next, as seen in the figure, we can traverse the parse tree and generate from it the equivalent VM code.

The choice of the code generation algorithm depends on the target language into which we are translating. For a stack-based target platform, we simply need to print the tree in postfix notation, also known as Right Polish Notation (RPN). In RPN syntax, an operation like f(x, y) is expressed as x, y, f (or, in the VM language syntax, push x, push y, call f). Likewise, an operation like x + y, which is +(x, y) in prefix notation, is stated as x, y, + (i.e., push x, push y, add). The strategy for translating expressions into stack-based VM code is straightforward and is based on recursive post-order traversal of the underlying parse tree, as follows:

**11.1.2命令翻译**
**现在，我们描述如何将高级命令翻译成目标语言。由于我们已经讨论了变量，对象和数组的处理，因此只需要考虑两个问题：表达式求值和流控制。**

**评估表达式我们应该如何生成代码来评估x + g（2，y，-z）* 5之类的高级表达式？首先，我们必须“理解”表达式的句法结构，例如，将其转换为图11.4所示的解析树。解析已由第10章中描述的语法分析器处理。接下来，如图所示，我们可以遍历解析树并从中生成等效的VM代码。**
**代码生成算法的选择取决于我们要翻译成的目标语言。对于基于堆栈的目标平台，我们只需要以后缀表示法（也称为右波兰表示法（RPN））打印树。在RPN语法中，像f（x，y）这样的运算被表示为x，y，f（或者在VM语言语法中，推x，推y，调用f）。同样，x + y之类的运算符也用前缀x（y，y）表示为x，y，+（即，按x，按y，加）。将表达式转换为基于堆栈的VM代码的策略非常简单，并且基于基础解析树的递归后序遍历，如下所示：**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtub2ikbuj30g106zdfw.jpg)





**Translating Flow Control** High-level programming languages are equipped with a variety of control flow structures like if, while, for, switch, and so on. In contrast, low-level languages typically offer two basic control primitives: conditional goto and unconditional goto. Therefore, one of the challenges faced by the compiler writer is to translate structured code segments into target code utilizing these primitives only. As shown in figure 11.5, the translation logic is rather simple.

Two features of high-level languages make the compilation of control structures slightly more challenging than that shown in figure 11.5. First, a program normally contains multiple instances of if and while statements. The compiler can handle this multiplicity by generating and using unique label names. Second, control structures can be nested, for example, if within while within another while and so on. This complexity can be dealt with easily using a recursive compilation strategy.

**转换流程控制高级编程语言配备了多种控制流程结构，例如if，while，for，switch等。 相反，低级语言通常提供两个基本的控制原语：有条件的goto和无条件的goto。 因此，编译器编写者面临的挑战之一是仅利用这些原语将结构化代码段转换为目标代码。 如图11.5所示，转换逻辑非常简单。**
**高级语言的两个功能使控制结构的编译比图11.5中所示更具挑战性。 首先，程序通常包含if和while语句的多个实例。 编译器可以通过生成和使用唯一的标签名称来处理这种多样性。 其次，控制结构可以嵌套，例如，如果在一个循环内另一个循环内，依此类推。 使用递归编译策略可以轻松解决这种复杂性。**

![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtuh3wikjj30dp0453yj.jpg)



![](https://tva1.sinaimg.cn/large/0082zybpgy1gbtuh9vnf6j30dp08odfy.jpg)

