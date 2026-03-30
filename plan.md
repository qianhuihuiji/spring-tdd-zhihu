# 用户头像功能开发计划（遵循 QWEN / TDD 规范）

## 背景

本项目采用 TDD 开发流程与统一约定（见 .qwen/QWEN.md）。用户头像功能需支持文件上传到本地开发目录，并在用户详情返回相对路径供前端访问。

## 目标

- 提供 POST /users/{id}/avatar 接口接收 multipart 文件并保存至 uploads\avatars\
- 在 GET /users/{id} 返回 avatar 字段为相对路径（例如：/uploads/avatars/{filename}）
- 遵循项目的 TDD 流程：先写测试再实现，测试包含单元与集成测试
- 返回遵循 CommonResult<T> 统一封装的 JSON

## TDD 流程（必须遵守）

1. 在单元测试（src/test/java/com/nofirst/spring/tdd/zhihu/unit）编写失败的测试用例（命名与结构遵循 QWEN
   指南：小写下划线、given-when-then）。
2. 运行 mvn test，观察测试失败。
3. 在集成测试（src/test/java/com/nofirst/spring/tdd/zhihu/integration）编写必要的集成测试，继承 BaseContainerTest 并使用
   Testcontainers。
4. 实现最小可行代码（controller/service/storage），使测试通过。
5. 如需数据库变更，先添加 Flyway 迁移脚本（src/main/resources/db/migration），在实现后运行 mvn flyway:migrate
   并更新实体/mapper（如需）。
6. 反复重构并保证所有测试通过。

## 实现要点

- 接口：POST /users/{id}/avatar
    - 字段：file（multipart）
    - 校验：Content-Type（image/png,image/jpeg）、大小上限（建议 2MB）
    - 命名：UUID + 原扩展名，避免冲突
    - 保存目录（开发）：uploads\avatars\（加入 .gitignore）
    - 返回：CommonResult 包装的相对路径数据（data 字段）
- 用户详情：GET /users/{id} 返回 avatar 相对路径或 null/默认头像
- 静态资源：在 Spring Boot 中配置 ResourceHandler 将 uploads 目录映射为 /uploads/**
- 错误处理：参数校验失败返回 400，未授权 401，异常 500，结果封装到 CommonResult

## 测试细节

- 单元测试使用 Mockito/AssertJ，覆盖 service 层、工具类（文件名生成、校验逻辑）
- 集成测试继承 BaseContainerTest，使用 MockMvc 验证完整请求流程（上传、保存、用户详情返回）
- 使用工厂类（AvatarFactory/TestDataFactory）生成测试数据
- 测试命名示例：
    - unit: void unauthenticated_user_cannot_upload_avatar()
    - integration: void authenticated_user_can_upload_avatar_and_get_avatar_path()
- 在 CI/本地运行 mvn test 保证所有测试通过

## 任务列表（对应 todos）

- avatar-spec: 编写接口规范与示例请求/响应（TDD 前置）
- avatar-tests: 遵循 TDD：先写失败测试（单元 + 集成），测试位置与命名遵循 QWEN.md
- avatar-backend: 实现上传控制器、服务与持久化逻辑
- avatar-storage: 创建 uploads\\avatars\\ 目录，实现文件保存、权限与唯一命名
- avatar-return-path: 在用户详情接口返回相对路径并配置静态资源映射
- avatar-flyway: （如需）添加 Flyway 迁移脚本以扩展用户表 avatar 字段
- avatar-docs: 更新 README 与接口文档，确保 uploads 在 .gitignore

## 验收准则

- 使用 TDD 流程：所有新增测试在实现前失败，改动后通过
- 上传接口能保存图片并返回 200/201，用户资料中保存相对路径
- GET 用户详情返回的 avatar 路径可通过静态映射访问到图片
- 非法类型或超大文件返回 4xx 错误并用 CommonResult 封装

## 风险与注意

- 不提交二进制到 Git（uploads/ 应在 .gitignore）
- 并发上传时使用 UUID 避免冲突
- 生产环境建议使用外部文件存储或 CDN，并将该路径作为配置项

