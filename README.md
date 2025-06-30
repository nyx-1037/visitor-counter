# 访问量统计系统

一个简单、高效的网站访问量统计系统，基于Spring Boot和Redis开发，支持跨域请求，可以轻松集成到任何网站。

## 功能特点

- 支持多网站/多页面独立统计
- 基于Redis的高性能缓存
- 数据持久化到MySQL数据库
- 跨域支持，可嵌入任何网站
- 简单易用的管理界面
- 提供多种嵌入方式

## 技术栈

- 后端：Spring Boot、Spring Security、MyBatis-Plus
- 数据存储：Redis (缓存)、MySQL (持久化)
- 前端：Bootstrap 5、jQuery
- 安全：JWT认证

## 快速开始

### 系统要求

- JDK 1.8+
- Maven 3.6+
- Redis 5.0+
- MySQL 5.7+

### 配置数据库

1. 创建MySQL数据库
2. 导入`src/main/resources/visitor_counter_db.sql`初始化数据库结构

### 配置应用

编辑`src/main/resources/application.yml`文件，配置数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/visitor_counter_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password # 如果有的话
```

### 构建和运行

```bash
# 构建项目
mvn clean package

# 运行应用
java -jar target/visitor-counter-1.0.0.jar
```

应用将在 http://localhost:7777 启动。

## 使用方法

### 管理界面

1. 访问 http://localhost:7777 进入登录页面
2. 使用默认管理员账号登录：
   - 用户名：admin
   - 密码：admin123
3. 登录后可以管理访问量记录、用户和查看访问日志

### 嵌入到网站

#### 方法1：直接使用API

```html
<div id="visitor-counter">访问量统计中...</div>
<script>
    (function() {
        var target = "your-unique-id";
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "https://yourdomain.com/api/visitor/increment?target=" + encodeURIComponent(target), true);
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                try {
                    var response = JSON.parse(xhr.responseText);
                    document.getElementById("visitor-counter").innerHTML = "访问量: " + response.count;
                } catch (e) {
                    document.getElementById("visitor-counter").innerHTML = "访问量统计出错";
                }
            }
        };
        
        xhr.send();
    })();
</script>
```

#### 方法2：使用提供的JS库

```html
<!-- 1. 引入JS文件 -->
<script src="https://yourdomain.com/js/visitor-counter.js"></script>

<!-- 2. 添加带有data-visitor-target属性的元素 -->
<span data-visitor-target="your-unique-id"></span>

<!-- 3. 使用内置图标功能 -->
<!-- 使用默认图标 -->
<span data-visitor-target="your-site-with-icon" data-show-icon="true">统计中...</span>

<!-- 自定义图标 -->
<span 
  data-visitor-target="your-custom-icon" 
  data-show-icon="true"
  data-icon-url="/images/visitor-counter-icon.svg"
  data-icon-size="24px"
  data-icon-alt="访客统计"
  data-template="已有 {{count}} 位访客">
  统计中...
</span>
```

## 示例页面

系统提供了两个示例页面，展示如何使用访问量统计功能：

1. **基本用法示例**：http://localhost:7777/usage_example.html
2. **嵌入代码示例**：http://localhost:7777/embed_example.html

## API文档

### 增加访问量

```
GET /api/visitor/increment?target={target}
```

- `target`: 目标标识符，用于区分不同的统计对象
- 返回：JSON格式的访问量计数，例如 `{"count": 42}`

## 管理API

以下API需要JWT认证：

### 获取所有访问量记录

```
GET /api/admin/visitors
```

### 分页获取访问量记录

```
GET /api/admin/visitors/page?pageNum={pageNum}&pageSize={pageSize}
```

### 创建访问量记录

```
POST /api/admin/visitors
```

### 更新访问量记录

```
PUT /api/admin/visitors
```

### 删除访问量记录

```
DELETE /api/admin/visitors/{id}
```

### 切换访问量记录状态

```
PUT /api/admin/visitors/{id}/status
```

## 数据同步

系统会自动将Redis中的数据定期同步到MySQL数据库，也可以通过管理界面手动触发同步。

## 许可证

[MIT](LICENSE)