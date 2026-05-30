# Spring Boot 3.x（Spring 6.x）保存CGLIB代理字节码的完整方案

## 重要前置说明

Spring 6.0对CGLIB做了**彻底的重构**：

1. 将CGLIB完全内嵌到`spring-core`中，不再作为第三方依赖
2. 重命名了所有CGLIB的包名（从`net.sf.cglib`改为`org.springframework.cglib`）
3. **废弃了原来的`-Dcglib.debugLocation`系统属性**
4. 新增了Spring官方的调试配置

---

## Arthas实时导出字节码文件

这是**所有Java版本通用、最可靠、不需要修改任何代码或配置**的方法，可以直接在运行时导出任何动态生成的字节码。

### 步骤1：下载并启动Arthas

```bash
# 下载Arthas
curl -O https://arthas.aliyun.com/arthas-boot.jar

# 默认启动启动命令
java -jar arthas-boot.jar
# 推荐指定端口方式启动
java -jar arthas-boot.jar --telnet-port 9998 --http-port 9999
# 或者使用随机端口，让系统自动选择可用端口
java -jar arthas-boot.jar --telnet-port 0 --http-port 0
```

> Arthas 默认会使用 3658 端口，而在 Windows 中，这个端口很有可能被系统内核保留，通过
`netsh int ipv4 show excludedportrange protocol=tcp`命令可以进行确认
> 从而导致
`ERROR c.t.a.c.s.t.i.h.HttpTelnetTermServer -Error listening to port 3658 java.net.BindException: Address already in use: bind`
> 报错
> 可从 `~/logs/arthas/arthas.log`日志文件查看

### 步骤2：选择你的Spring Boot进程

Arthas会列出所有运行中的Java进程，输入你的Spring Boot应用对应的序号，按回车连接。

### 步骤3：查找CGLIB代理类的全名

```bash
# 搜索所有包含UserService的类
sc *UserService*
```

你会看到类似这样的输出：

```
com.example.service.UserService
com.example.service.UserService$$EnhancerBySpringCGLIB$$7a8b9c0d
```

第二个就是CGLIB生成的代理类。

### 步骤4：导出字节码到磁盘

```bash
# 导出代理类的字节码文件
dump com.example.service.UserService$$EnhancerBySpringCGLIB$$7a8b9c0d
```

Arthas会自动将字节码保存到当前目录下的`arthas-output`文件夹中，路径类似：

```
./arthas-output/com/example/service/UserService$$EnhancerBySpringCGLIB$$7a8b9c0d.class
```

### 步骤5：反编译查看字节码

```bash
# 直接在Arthas中反编译（最方便）
jad com.example.service.UserService$$EnhancerBySpringCGLIB$$7a8b9c0d

# 或者用javap反编译导出的class文件
javap -c -p ./arthas-output/com/example/service/UserService$$EnhancerBySpringCGLIB$$7a8b9c0d.class
```