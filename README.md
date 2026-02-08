## 目前已实现功能
- 序列化与反序列化 `gil` `gia` 文件
  - `gil/gia` 与 `json/pb` 文件互相转换

## TODO
- web - sdk

## 安装与运行

通过源码构建：
-  Java 25
-  maven

```bash
# 运行应用
mvn compile
mvn spring-boot:run
```

```bash
# 构建jar文件
mvn package
```
输出目录在`/target/`

## 使用说明
- 反序列化
  - 本地目录
    1. 将要反序列化的文件放入`./input`目录中
    2. 确保已创建`./output`目录
    3. 运行后端程序
    4. 查看`./output`目录
    - 程序会在初始化时解码一次文件，如果启动后还想对目录解码可以访问`http://127.0.0.1:1696/decode/reload`
  - 通过网络
    - 浏览器打开`./frontend/index.html`文件 按照指引进行操作

- 序列化
  - 浏览器打开`./frontend/index.html`文件 按照指引进行操作