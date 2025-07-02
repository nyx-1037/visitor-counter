# 🚀 网站访问量统计系统

一个**轻量级、高性能**的网站访问量统计解决方案，基于现代Java技术栈（Spring Boot, Redis, MySQL）构建。它支持**多网站/多页面独立统计**，提供**实时数据缓存**与**持久化存储**，并具备**灵活的跨域集成能力**，助您轻松掌握网站流量动态。

## ✨ 功能亮点

- **多维度统计**：支持对不同网站、页面或自定义目标进行独立访问量统计。
- **高性能缓存**：利用Redis实现数据高速读写，确保统计数据实时更新与响应。
- **数据持久化**：通过MySQL安全存储历史访问数据，支持数据分析与报表生成。
- **无缝集成**：提供灵活的API和JS库，轻松嵌入任何前端项目，支持跨域请求。
- **直观管理界面**：内置简洁的管理后台，方便查看、管理访问记录和系统配置。
- **安全可靠**：采用JWT认证机制，保障管理接口的访问安全。

## 🛠️ 技术栈

- **后端**：Spring Boot, Spring Security, MyBatis-Plus
- **数据存储**：Redis (高性能缓存), MySQL (数据持久化)
- **前端**：Bootstrap 5, jQuery (用于管理界面)
- **认证**：JWT (JSON Web Token)

## 🚀 快速开始

### 📋 系统要求

在开始之前，请确保您的系统满足以下要求：

- **JDK**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **Redis**: 5.0 或更高版本
- **MySQL**: 5.7 或更高版本

### ⚙️ 配置指南

1.  **数据库准备**：
    -   创建一个新的MySQL数据库（例如：`visitor_counter_db`）。
    -   导入项目提供的SQL文件以初始化数据库结构：`src/main/resources/visitor_counter_db.sql`。

2.  **应用配置**：
    -   编辑 `src/main/resources/application.yml` 文件（如果不存在，请创建）。
    -   根据您的实际环境，配置MySQL和Redis的连接信息：

    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/visitor_counter_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
        username: your_mysql_username
        password: your_mysql_password
      redis:
        host: localhost
        port: 6379
        password: your_redis_password # 如果Redis设置了密码，请填写；否则可留空或删除此行
    ```

### 🚀 构建与运行

1.  **构建项目**：

    ```bash
    mvn clean package
    ```

2.  **运行应用**：

    ```bash
    java -jar target/visitor-counter-1.0.0.jar
    ```

    应用启动后，您可以通过浏览器访问：[http://localhost:7777](http://localhost:7777)。

## 💡 使用指南

### 🖥️ 管理界面

1.  **访问地址**：在浏览器中打开 [http://localhost:7777](http://localhost:7777) 进入登录页面。
2.  **默认凭证**：使用以下默认管理员账号登录：
    -   **用户名**：`admin`
    -   **密码**：`admin123`
3.  **功能概览**：登录后，您可以方便地管理访问量记录、用户账户以及查看详细的访问日志。

### 🌐 嵌入到您的网站

本系统提供两种灵活的方式将访问量统计功能集成到您的网站中：

#### 方法一：直接调用API

适用于需要完全自定义前端展示或后端直接获取统计数据的场景。

```html
<div id="visitor-counter">加载中...</div>
<script>
    (function() {
        var target = "your-page-identifier"; // 为您的页面或内容设置一个唯一标识符
        var xhr = new XMLHttpRequest();
        // 请将 yourdomain.com 替换为您的实际部署域名
        xhr.open("GET", "https://yourdomain.com/api/visitor/increment?target=" + encodeURIComponent(target), true);
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                try {
                    var response = JSON.parse(xhr.responseText);
                    document.getElementById("visitor-counter").innerHTML = "访问量: " + response.count;
                } catch (e) {
                    console.error("解析访问量数据失败:", e);
                    document.getElementById("visitor-counter").innerHTML = "访问量统计出错";
                }
            }
        };
        
        xhr.send();
    })();
</script>
```

#### 方法二：使用提供的JavaScript库 (推荐)

此方法更为便捷，只需引入JS文件并添加特定属性即可。

```html
<!-- 1. 引入JS文件 -->
<!-- 请将 yourdomain.com 替换为您的实际部署域名 -->
<script src="https://yourdomain.com/js/visitor-counter.js"></script>

<!-- 2. 添加带有data-visitor-target属性的元素 -->
<!-- 系统会自动在此元素内显示访问量 -->
<span data-visitor-target="your-unique-page-id"></span>

<!-- 3. 使用内置图标功能 (可选) -->
<!-- 默认图标 -->
<span data-visitor-target="your-site-with-icon" data-show-icon="true">统计中...</span>

<!-- 自定义图标 -->
<span 
  data-visitor-target="your-custom-icon-example" 
  data-show-icon="true"
  data-icon-url="/images/visitor-counter-icon.svg" <!-- 自定义图标路径 -->
  data-icon-size="24px" <!-- 图标大小 -->
  data-icon-alt="访客统计" <!-- 图片alt文本 -->
  data-template="已有 {{count}} 位访客"> <!-- 自定义显示模板 -->
  加载访客统计...
</span>
```

## 📄 示例页面

为了帮助您更好地理解和使用本系统，我们提供了两个示例页面：

1.  **基本用法示例**：[http://localhost:7777/usage_example.html](http://localhost:7777/usage_example.html)
    -   展示了如何通过最简单的方式集成访问量统计。
2.  **嵌入代码示例**：[http://localhost:7777/embed_example.html](http://localhost:7777/embed_example.html)
    -   演示了如何使用JS库以及自定义图标等高级功能。

## 📚 API 文档

### 📈 增加访问量 (公共API)

-   **URL**：`/api/visitor/increment`
-   **方法**：`GET`
-   **参数**：
    -   `target` (String, **必需**): 唯一标识符，用于区分不同的统计对象（例如：页面URL、文章ID等）。
-   **响应**：
    ```json
    {
      "count": 42 // 当前目标的总访问量
    }
    ```
-   **示例**：`GET /api/visitor/increment?target=homepage`

## 🔒 管理API (需要JWT认证)

以下API用于管理后台操作，所有请求都需要在 `Authorization` 头中携带有效的JWT。

### 📊 获取所有访问量记录

-   **URL**：`/api/admin/visitors`
-   **方法**：`GET`
-   **响应**：返回所有访问量记录的列表。

### 📄 分页获取访问量记录

-   **URL**：`/api/admin/visitors/page`
-   **方法**：`GET`
-   **参数**：
    -   `pageNum` (Integer, **必需**): 当前页码，从1开始。
    -   `pageSize` (Integer, **必需**): 每页记录数。
-   **响应**：返回分页的访问量记录数据。

### ➕ 创建访问量记录

-   **URL**：`/api/admin/visitors`
-   **方法**：`POST`
-   **请求体**：JSON格式的访问量记录对象。
-   **响应**：创建成功的访问量记录对象。

### ✏️ 更新访问量记录

-   **URL**：`/api/admin/visitors`
-   **方法**：`PUT`
-   **请求体**：JSON格式的访问量记录对象（需包含ID）。
-   **响应**：更新成功的访问量记录对象。

### 🗑️ 删除访问量记录

-   **URL**：`/api/admin/visitors/{id}`
-   **方法**：`DELETE`
-   **参数**：
    -   `id` (Long, **必需**): 要删除的访问量记录ID。
-   **响应**：无内容或成功消息。

### 🔄 切换访问量记录状态

-   **URL**：`/api/admin/visitors/{id}/status`
-   **方法**：`PUT`
-   **参数**：
    -   `id` (Long, **必需**): 要切换状态的访问量记录ID。
-   **响应**：更新后的访问量记录对象。

## 🔄 数据同步

本系统实现了Redis缓存与MySQL数据库之间的数据自动同步机制，确保数据的最终一致性。

-   **自动同步**：系统会定期将Redis中的访问量数据同步到MySQL数据库。
-   **手动触发**：您也可以通过管理界面手动触发数据同步操作。

## 📜 许可证

本项目采用 [MIT 许可证](LICENSE) 发布，您可以自由使用、修改和分发。