## 基本介绍

kettle-dubboclient-plugin是基于Kettle5.3插件体系实现的Dubbo客户端插件,属于Kettle的**转换步骤插件**(StepPlugin), 通过使用这个步骤插件,可以调用由Dubbo Provider基于**dubbo协议**发布的服务。
  
## 插件构建

该插件追随Kettle源码的构建方式，使用 ***Ant + Ivy*** 来完成构建和解决依赖。编译构建插件仅需要执行两个Ant命令:

(1) 通过命令行窗口进入项目根目录并执行如下命令来下载插件所需要的依赖jar包:

	ant resolve

(2) 通过命令行窗口进入项目根目录并执行以下命令来构建插件发布包
 
	ant dist

## 集成到Kettle发布包

插件构建成功后, 在 ***/dist*** 目录下找到如下zip包:

	kettle-dubboclient-plugin-5.3.0.4-364.zip

将该zip包解压到Kettle发布包 ***/plugins*** 下,解压后的位置如下所示:
  
	${Kettle_APP_DIR}/plugins/kettle-dubboclient-plugin

其中 ***${Kettle\_APP\_DIR}*** 表示你的Kettle发布包所在目录

## Dubbo Provider API的存放

需要将Dubbo服务提供者提供的服务接口相关jar放置到该插件的某个位置以便于该插件能够检索到哪些服务可以使用。在这里我们基于约定来存放Dubbo Provider API以及相关配置。对于Dubbo Provider提供的api jar包及依赖,放到 ***kettle-dubboclient-plugin*** 插件根目录下,每个Dubbo Provider存放的目录层次结构为:  

	${Dubbo_Provider_DIR_NAME}/provider.properties  
	${Dubbo_Provider_DIR_NAME}/XXXService.jar(API jar)  
	${Dubbo_Provider_DIR_NAME}/lib/....(provider依赖的第三方jar)  

其中 ***${Dubbo\_Provider\_DIR\_NAME}*** 可以为任意自定义的目录名,表示某个特定的Dubbo Provider。

### provider.properties

每个Dubbo Provider所在目录都必须提供一个 **provider.properties** 配置文件,配置文件里面必须提供的配置项有: 

(1) **provider**

用于指定特定provider名称。

eg:

	provider=example

(2) **interfaces**

指定provider提供的接口,多个接口以**逗号(,)**隔开。

eg:

	interfaces=com.example.ExampleService1,com.example.ExampleService2 

### XXXService.jar

表示Dubbo Provider提供出来的服务接口jar包。

### lib子目录

lib子目录用于存放Dubbo Provider提供出来的服务接口必须依赖的第三方jar包。

## FAQ

### 有新的服务提供者要加入该插件该如何操作？

对于新的服务提供者要加入该插件, 只需要按照上面所讲的配置方式存放新的服务提供者API包到插件对应位置。如果你使用的是Spoon工具, 通过重启Spoon工具就可以让Kettle在加载该插件的时候识别到新的服务提供者。

### Spoon工具在哪里可以找到该插件

因为该插件是属于转换步骤插件(StepPlugin)的, 所以在新建转换之后, 可以在**"核心对象"**里面找到该插件, 对应的组件名称是**"Dubbo Client"**, 可以在**"查询"**分类下找到该组件。

### 对于Dubbo服务接口方法调用的入参如何传递和返回值如何处理?

对于入参, 即方法参数, 如果不是基本数据类型或者其包装类(***Integer***, ***String***, ...etc), 那么要求以 ***json*** 格式来传递入参。对于返回值, 与入参采取同样的方式, 如果不是基本数据类型或者包装类, 那么会将返回值序列化成 ***json*** 格式串返回。对于基本数据类型或者包装类, 统一返回 ***String*** 字符串值。
