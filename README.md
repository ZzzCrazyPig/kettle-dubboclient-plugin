# kettle-dubboclient-plugin

## 介绍
  基于Kettle5.3插件体系实现的Dubbo客户端插件,可以使用这个插件来调用由Dubbo Provider基于dubbo协议发布的Service 
  
## 编译使用
* 在项目根目录执行`ant resolve`命令下载插件依赖jar
* 在项目根目录执行`ant dist`命令构建插件发行包
* 在`/dist`目录下找到对应的kettle-dubboclient-plugin-5.3.0.4-364.zip包,将该zip包解压到Kettle发行包`/plugins`下:
  ${Kettle_APP_DIR}/plugins/kettle-dubboclient-plugin

## Dubbo Provider API的存放
  对于Dubbo Provider提供的api jar文件及依赖,放到`kettle-dubboclient-plugin`目录下,结构为:  
${Dubbo_Provider_DIR_NAME}/provider.properties  
${Dubbo_Provider_DIR_NAME}/XXXService.jar(API jar)  
${Dubbo_Provider_DIR_NAME}/lib/....(依赖的第三方jar)  
1. 其中${Dubbo_Provider_DIR_NAME}为自定义的目录名  
2. provider.properties内容说明如下:  
`provider=xxx // 指定provider名称`  
`interfaces=com.xxx.XxxService1,com.xxx.XxxService2 // 指定provider提供的接口,多个接口以','隔开`  

