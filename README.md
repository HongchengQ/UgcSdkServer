## 目前已实现功能
- 反序列化 gil 文件
- 反序列化 gia 文件

## TODO
- 序列化
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
1. 将要反序列化的文件放入`./input`目录中
2. 确保已创建`./output`目录
3. 运行程序
4. 查看`./output`目录