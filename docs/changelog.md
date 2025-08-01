# 更新日志

## 1.0.2
- 引入OrmLite库，以支持通过ORM方式使用SQLite数据库。
- 添加`DatabaseHelper`与`BaseDao`对OrmLite进行封装，通过继承`BaseDao`类，并传入一个实体类的`Class`对象的方式，即可快速创建基于该实体类的一个数据库访问对象，不必考虑数据表的创建、升级和降级。

## 1.0.1
- 支持快速创建和启动内部HTTP服务器，根据请求URL匹配和获取`[assets]/web`目录下的静态资源，也支持获取应用私有目录下的图片。
- 支持异步JavaScript Interface，避免WebView中的JS代码调用Android层方法时产生线程阻塞和UI无响应。
- `GlobalData`更名为`GlobalComponents`。

## 1.0.0
- 初始版本。包含`GlobalData`与`java.beans.Transient`。
