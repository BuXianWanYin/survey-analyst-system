# 在线问卷调查与数据分析系统 - 后端服务

## 项目简介

在线问卷调查与数据分析系统后端服务，基于Spring Boot框架开发，提供完整的问卷设计、发布、数据收集、统计分析和数据导出等功能。系统采用前后端分离架构，通过RESTful API与前端进行交互，支持用户管理、问卷管理、数据统计、系统管理等核心功能。

## 技术栈

### 核心框架
- **Spring Boot** 2.6.13 - 应用框架
- **Spring Security** - 安全认证框架
- **MyBatis Plus** 3.5.3.1 - 持久层框架
- **MySQL** 8.0+ - 关系型数据库
- **Redis** 6.0+ - 缓存数据库

### 工具库
- **JWT** (jjwt 0.11.5) - 身份认证
- **Knife4j** 3.0.3 - API文档（Swagger增强）
- **Lombok** - 简化代码
- **Hutool** 5.8.22 - Java工具类库
- **EasyExcel** 3.3.2 - Excel处理
- **Apache POI** 5.2.4 - Office文档处理
- **Druid** 1.2.18 - 数据库连接池

### 开发环境
- **JDK** 1.8+
- **Maven** 3.6+
- **IDE** IntelliJ IDEA / Eclipse

## 项目结构

```
survey-analyst-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/server/surveyanalystserver/
│   │   │       ├── SurveyAnalystServerApplication.java
│   │   │       ├── aspect/
│   │   │       │   └── OperationLogAspect.java
│   │   │       ├── common/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── Result.java
│   │   │       │   └── ResultCode.java
│   │   │       ├── config/
│   │   │       │   ├── EncodingFilterConfig.java
│   │   │       │   ├── FileConfig.java
│   │   │       │   ├── JacksonConfig.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── JwtConfig.java
│   │   │       │   ├── MybatisPlusConfig.java
│   │   │       │   ├── MyMetaObjectHandler.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── SwaggerConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── AdminDataController.java
│   │   │       │   ├── AdminSurveyController.java
│   │   │       │   ├── AdminSystemController.java
│   │   │       │   ├── AdminTemplateController.java
│   │   │       │   ├── AdminUserController.java
│   │   │       │   ├── AnalysisController.java
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── ExportController.java
│   │   │       │   ├── FileController.java
│   │   │       │   ├── FormController.java
│   │   │       │   ├── FormDataController.java
│   │   │       │   ├── FormLogicController.java
│   │   │       │   ├── FormSettingController.java
│   │   │       │   ├── FormTemplateController.java
│   │   │       │   ├── FormThemeController.java
│   │   │       │   ├── ResponseController.java
│   │   │       │   ├── StatisticsController.java
│   │   │       │   ├── SurveyController.java
│   │   │       │   ├── SurveyPublishController.java
│   │   │       │   └── UserController.java
│   │   │       ├── entity/
│   │   │       │   ├── FormConfig.java
│   │   │       │   ├── FormData.java
│   │   │       │   ├── FormItem.java
│   │   │       │   ├── FormLogic.java
│   │   │       │   ├── FormSetting.java
│   │   │       │   ├── FormTemplate.java
│   │   │       │   ├── FormTemplateCategory.java
│   │   │       │   ├── FormTheme.java
│   │   │       │   ├── OperationLog.java
│   │   │       │   ├── Response.java
│   │   │       │   ├── Survey.java
│   │   │       │   ├── User.java
│   │   │       │   └── dto/
│   │   │       │       ├── OperationLogVO.java
│   │   │       │       ├── ResponseVO.java
│   │   │       │       ├── SurveyVO.java
│   │   │       │       ├── UserLoginDTO.java
│   │   │       │       └── UserRegisterDTO.java
│   │   │       ├── mapper/
│   │   │       │   ├── FormConfigMapper.java
│   │   │       │   ├── FormDataMapper.java
│   │   │       │   ├── FormItemMapper.java
│   │   │       │   ├── FormLogicMapper.java
│   │   │       │   ├── FormSettingMapper.java
│   │   │       │   ├── FormTemplateCategoryMapper.java
│   │   │       │   ├── FormTemplateMapper.java
│   │   │       │   ├── FormThemeMapper.java
│   │   │       │   ├── OperationLogMapper.java
│   │   │       │   ├── ResponseMapper.java
│   │   │       │   ├── SurveyMapper.java
│   │   │       │   └── UserMapper.java
│   │   │       ├── service/
│   │   │       │   ├── AnalysisService.java
│   │   │       │   ├── EmailService.java
│   │   │       │   ├── ExportService.java
│   │   │       │   ├── FileService.java
│   │   │       │   ├── FormConfigService.java
│   │   │       │   ├── FormDataService.java
│   │   │       │   ├── FormItemService.java
│   │   │       │   ├── FormLogicService.java
│   │   │       │   ├── FormSettingService.java
│   │   │       │   ├── FormTemplateCategoryService.java
│   │   │       │   ├── FormTemplateService.java
│   │   │       │   ├── FormThemeService.java
│   │   │       │   ├── OperationLogService.java
│   │   │       │   ├── RedisCacheService.java
│   │   │       │   ├── ResponseService.java
│   │   │       │   ├── StatisticsService.java
│   │   │       │   ├── SurveyPublishService.java
│   │   │       │   ├── SurveyService.java
│   │   │       │   ├── SystemService.java
│   │   │       │   ├── UserService.java
│   │   │       │   └── impl/
│   │   │       │       ├── AnalysisServiceImpl.java
│   │   │       │       ├── EmailServiceImpl.java
│   │   │       │       ├── ExportServiceImpl.java
│   │   │       │       ├── FileServiceImpl.java
│   │   │       │       ├── FormConfigServiceImpl.java
│   │   │       │       ├── FormDataServiceImpl.java
│   │   │       │       ├── FormItemServiceImpl.java
│   │   │       │       ├── FormLogicServiceImpl.java
│   │   │       │       ├── FormSettingServiceImpl.java
│   │   │       │       ├── FormTemplateCategoryServiceImpl.java
│   │   │       │       ├── FormTemplateServiceImpl.java
│   │   │       │       ├── FormThemeServiceImpl.java
│   │   │       │       ├── OperationLogServiceImpl.java
│   │   │       │       ├── RedisCacheServiceImpl.java
│   │   │       │       ├── ResponseServiceImpl.java
│   │   │       │       ├── StatisticsServiceImpl.java
│   │   │       │       ├── SurveyPublishServiceImpl.java
│   │   │       │       ├── SurveyServiceImpl.java
│   │   │       │       ├── SystemServiceImpl.java
│   │   │       │       └── UserServiceImpl.java
│   │   │       └── utils/
│   │   │           ├── DataCleaningUtils.java
│   │   │           ├── DataEncryptionUtils.java
│   │   │           ├── ImageExcelWriteHandler.java
│   │   │           ├── IpUtils.java
│   │   │           ├── JwtUtils.java
│   │   │           └── UserAgentUtils.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
│           └── com/server/surveyanalystserver/
│               ├── SurveyAnalystServerApplicationTests.java
│               └── UserTestData.java
├── upload/
├── pom.xml
└── README.md
```

## 快速开始

### 环境要求

- JDK 1.8 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- Redis 6.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd survey-analyst-system/survey-analyst-server
   ```

2. **创建数据库**
   ```sql
   CREATE DATABASE survey_analyst CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **导入数据库脚本**
   - 执行数据库脚本创建表结构（参考 `doc/数据库说明文档.md`）

4. **配置数据库连接**
   
   编辑 `src/main/resources/application-dev.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/survey_analyst?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root
       password: your_password
   ```

5. **配置Redis**
   
   编辑 `src/main/resources/application-dev.yml`：
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
         password: your_redis_password  # 如果有密码
         database: 0
   ```

6. **配置邮件服务（可选）**
   
   编辑 `src/main/resources/application.yml`：
   ```yaml
   spring:
     mail:
       username: your_email@example.com
       password: your_email_auth_code
   ```

7. **编译项目**
   ```bash
   mvn clean install
   ```

8. **运行项目**
   ```bash
   mvn spring-boot:run
   ```
   
   或者直接运行 `SurveyAnalystServerApplication.java` 的 main 方法

9. **访问API文档**
   
   启动成功后，访问：http://localhost:8080/doc.html

## 配置说明

### 主要配置项

#### 数据库配置 (`application-dev.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/survey_analyst
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
```

#### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

#### 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 500MB
```

#### JWT配置 (`application-dev.yml`)
```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

#### 文件存储配置 (`application-dev.yml`)
```yaml
file:
  path: ./upload
  max-size: 10485760  # 10MB
```

### 环境配置

项目支持多环境配置，通过 `spring.profiles.active` 指定：
- `dev` - 开发环境（默认）
- `prod` - 生产环境

## API文档

### 访问地址

- **Knife4j文档**：http://localhost:8080/doc.html
- **Swagger文档**：http://localhost:8080/swagger-ui.html

### 主要API模块

- **认证模块** (`/api/auth`) - 用户注册、登录、密码重置
- **用户模块** (`/api/user`) - 用户信息管理
- **问卷模块** (`/api/survey`) - 问卷创建、编辑、发布
- **表单模块** (`/api/form`) - 表单设计、表单项管理
- **数据模块** (`/api/form/data`) - 数据提交、查询
- **统计模块** (`/api/statistics`) - 数据统计
- **分析模块** (`/api/analysis`) - 数据分析
- **导出模块** (`/api/export`) - 数据导出（Excel格式）
- **管理员模块** (`/api/admin/*`) - 管理员功能

## 安全认证

系统使用JWT进行身份认证：

1. **登录获取Token**
   ```http
   POST /api/auth/login
   Content-Type: application/json
   
   {
     "loginName": "user@example.com",
     "password": "password123"
   }
   ```

2. **使用Token访问接口**
   ```http
   GET /api/user/info
   Authorization: Bearer <your-token>
   ```

## 核心功能模块

### 1. 用户管理模块
- 用户注册（支持验证码）
- 用户登录（账号/邮箱登录）
- 密码重置（邮件/验证码）
- 用户信息管理
- 管理员用户管理

### 2. 问卷管理模块
- 问卷创建、编辑、删除
- 问卷发布、取消发布
- 问卷状态管理
- 问卷访问权限控制

### 3. 表单设计模块
- 表单配置管理
- 表单项管理（多种题型）
- 表单逻辑规则
- 表单样式设置
- 表单主题配置

### 4. 数据收集模块
- 数据提交与保存
- 填写前校验（IP限制、设备限制、用户限制等）
- 数据查询与管理

### 5. 统计分析模块
- 问卷统计概览
- 题目统计分析
- 交叉分析
- 趋势分析
- 样本画像分析

### 6. 数据导出模块
- Excel导出（支持图片）
- CSV导出

### 7. 系统管理模块（管理员）
- 用户管理
- 问卷管理
- 数据管理
- 系统统计
- 操作日志

## 数据库设计

### 主要数据表

- `user` - 用户表
- `survey` - 问卷表
- `form_config` - 表单配置表
- `form_item` - 表单项表
- `form_data` - 表单数据表
- `response` - 填写记录表
- `form_template` - 表单模板表
- `operation_log` - 操作日志表

详细数据库设计请参考：`doc/数据库说明文档.md`

## 开发规范

### 代码规范

项目严格遵循后端开发规范，详细规范请参考：`doc/后端开发规范文档.md`

#### 主要规范要点：

1. **分层架构**
   - Controller层：只负责接收请求和返回结果，不包含业务逻辑
   - Service层：所有业务逻辑在此层实现
   - Mapper层：数据访问层，使用MyBatis Plus

2. **注释规范**
   - 所有类、方法必须添加JavaDoc注释
   - 注释使用中文，不包含日期、作者、HTML标签
   - 重要方法需要详细说明业务逻辑

3. **导入规范**
   - 禁止使用完整类名，必须使用import语句
   - 禁止使用通配符导入（如 `import java.util.*`）

4. **命名规范**
   - 类名：大驼峰命名（如 `UserService`）
   - 方法名：小驼峰命名（如 `getUserById`）
   - 常量：全大写下划线分隔（如 `MAX_SIZE`）

### 包结构规范

```
com.server.surveyanalystserver
├── common          # 公共模块
├── config          # 配置类
├── entity          # 实体类
│   └── dto         # DTO类
├── mapper          # 数据访问层
├── service         # 业务逻辑层接口
│   └── impl        # 业务逻辑层实现
├── controller      # 控制器层
├── utils           # 工具类
└── aspect          # 切面
```

## 部署说明

### 开发环境部署

1. 确保MySQL和Redis服务已启动
2. 配置数据库连接信息
3. 运行 `mvn spring-boot:run` 或直接运行启动类

### 生产环境部署

1. **打包项目**
   ```bash
   mvn clean package -DskipTests
   ```

2. **运行JAR包**
   ```bash
   java -jar target/survey-analyst-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

3. **使用Docker部署（可选）**
   ```dockerfile
   FROM openjdk:8-jre
   COPY target/survey-analyst-server-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

### 文件存储

- 所有上传的文件存储在项目根目录下的 `upload/` 文件夹
- 文件按日期分类存储：`upload/yyyy/MM/dd/`
- 确保 `upload/` 目录有写入权限

## 常见问题

### 1. 数据库连接失败

**问题**：启动时提示数据库连接失败

**解决方案**：
- 检查MySQL服务是否启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限

### 2. Redis连接失败

**问题**：Redis相关功能不可用

**解决方案**：
- 检查Redis服务是否启动
- 检查Redis连接配置
- 如果不需要Redis功能，可以暂时禁用相关配置

### 3. 文件上传失败

**问题**：文件上传时提示错误

**解决方案**：
- 检查 `upload/` 目录是否存在
- 检查目录写入权限
- 检查文件大小是否超过限制

### 4. JWT Token无效

**问题**：接口返回401未授权

**解决方案**：
- 检查Token是否过期
- 检查Token格式是否正确（Bearer token）
- 重新登录获取新Token
