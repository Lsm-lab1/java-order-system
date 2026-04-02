# 电商订单系统 - 多道程序设计项目

## 项目说明

这是一个基于Spring Boot的电商订单系统，实现了生产者-消费者模型的多道程序设计。

### 核心功能
1. **商品管理**：添加、编辑、删除商品
2. **订单管理**：创建订单，多线程并发处理
3. **生产者-消费者模型**：商品操作和订单处理均使用队列和线程池
4. **同步互斥机制**：使用BlockingQueue和synchronized实现线程安全

### 技术栈
- Spring Boot 2.7.14
- Java 17
- MySQL
- Thymeleaf（前端）
- JPA（数据访问）

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE order_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 创建表结构

**商品表 (tb_product)**
```sql
CREATE TABLE `tb_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` VARCHAR(50) NOT NULL COMMENT '商品名称',
  `product_type` VARCHAR(30) NOT NULL COMMENT '商品类型',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

**订单表 (tb_order)**
```sql
CREATE TABLE `tb_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(50) NOT NULL COMMENT '商品名称',
  `product_type` VARCHAR(30) NOT NULL COMMENT '商品类型',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
  `generator_id` INT NOT NULL COMMENT '生成线程ID',
  `shipper_id` INT DEFAULT -1 COMMENT '发货线程ID',
  `fail_reason` VARCHAR(100) COMMENT '失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

## 运行项目

### 方法1：使用Maven（推荐）

1. **安装Maven**
   - 下载Maven 3.8+：https://maven.apache.org/download.cgi
   - 配置环境变量：MAVEN_HOME 和 PATH

2. **修改数据库配置**
   - 编辑 `src/main/resources/application.properties`
   - 修改数据库连接信息：
     ```properties
     spring.datasource.username=root
     spring.datasource.password=123456
     ```

3. **下载依赖**
   ```bash
   # 下载所有依赖
   mvn dependency:resolve
   ```

4. **编译和运行**
   ```bash
   # 编译项目
   mvn clean compile
   
   # 运行项目
   mvn spring-boot:run
   ```

### 方法2：使用Maven Wrapper

如果没有安装Maven，可以使用Maven Wrapper：

1. **下载Maven Wrapper**
   ```bash
   # 在项目根目录执行
   mvn -N io.takari:maven-wrapper:wrapper
   ```

2. **使用Maven Wrapper运行**
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```

### 方法2：使用IDEA/Eclipse

1. **导入项目**
   - 在IDE中选择「Import Project」
   - 选择项目根目录
   - 选择「Maven」作为项目类型

2. **配置JDK**
   - 确保使用JDK 17

3. **运行**
   - 右键点击 `OrderSystemApplication` 类
   - 选择「Run OrderSystemApplication」

## 访问地址

- **首页**：http://localhost:8080
- **商品管理**：http://localhost:8080/product/list
- **创建订单**：http://localhost:8080/order/create
- **订单列表**：http://localhost:8080/order/list

## 多线程演示

1. **添加商品**
   - 进入商品管理页面，添加几个不同类型的商品

2. **批量创建订单**
   - 进入创建订单页面
   - 选择一个商品
   - 设置批量生成数量（如10个）
   - 点击「批量生成订单」

3. **观察多线程效果**
   - 查看控制台输出，会看到3个消费者线程并发处理订单
   - 查看订单列表，不同订单会显示不同的处理线程ID（通过颜色区分）

## 项目结构

```
order--system/
├── pom.xml                          # Maven配置文件
├── src/main/
│   ├── java/com/example/ordersystem/
│   │   ├── OrderSystemApplication.java    # 启动类
│   │   ├── concurrent/                    # 多线程核心包
│   │   │   ├── ProductTask.java          # 商品任务类
│   │   │   ├── ProductConsumer.java      # 商品消费者线程
│   │   │   ├── OrderTask.java            # 订单任务类
│   │   │   └── OrderConsumer.java        # 订单消费者线程（3个）
│   │   ├── entity/                        # 实体类
│   │   │   ├── Product.java              # 商品实体
│   │   │   └── Order.java                # 订单实体
│   │   ├── repository/                    # 数据访问层
│   │   │   ├── ProductRepository.java
│   │   │   └── OrderRepository.java
│   │   ├── service/                       # 业务逻辑层
│   │   │   ├── ProductService.java
│   │   │   └── OrderService.java
│   │   └── controller/                    # 控制层
│   │       ├── IndexController.java
│   │       ├── ProductController.java
│   │       └── OrderController.java
│   └── resources/
│       ├── application.properties         # 配置文件
│       └── templates/                     # 前端页面
│           ├── index.html
│           ├── product/list.html
│           └── order/create.html
│           └── order/list.html
```

## 关键技术点

1. **生产者-消费者模型**
   - 使用 `BlockingQueue` 实现线程安全的任务队列
   - 商品操作：1个消费者线程
   - 订单处理：3个消费者线程并发处理

2. **同步互斥机制**
   - `synchronized` 关键字保护共享资源
   - `AtomicInteger` 线程安全计数器
   - 阻塞队列的 `put()` 和 `take()` 方法

3. **订单号生成**
   - 规则：商品类型前2位 + 年月日时分秒 + 4位序号
   - 例如：DZ202403151430250001

4. **多线程并发**
   - 3个订单消费者线程并行处理
   - 线程ID显示在订单列表中

## 注意事项

1. **数据库连接**：确保MySQL服务正在运行
2. **JDK版本**：使用JDK 17
3. **端口冲突**：默认端口8080，如有冲突可在application.properties中修改
4. **日志查看**：控制台会输出线程运行情况，可观察多线程效果

## 扩展功能

- **库存管理**：下单时检查库存
- **订单状态跟踪**：添加发货、完成等状态
- **用户管理**：添加用户登录功能
- **支付功能**：集成支付接口

## 总结

本项目成功实现了基于生产者-消费者模型的多道程序设计，通过多线程并发处理订单，展示了线程同步与互斥的核心概念。系统结构清晰，功能完整，适合作为多道程序设计的学习案例。
