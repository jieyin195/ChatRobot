## 此项目用于微信机器人聊天后端代码

### ✅ 环境要求

* **JDK**：17 及以上版本

---

### 🌱 启动你的专属智能助手

只需简单配置，即可启用基于 OpenAI 的智能助手：

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key
      base-url: https://api.openai.com（记住不要加/v1）
      chat:
        options:
          model: gpt-4o-mini
```

> 📌 注意事项：
>
> * 请确保 `base-url` 填写为 **正确的 OpenAI API 地址**（例如：`https://api.openai.com`）。
> * 请将 `your-api-key` 替换为你自己的 OpenAI 密钥。

---

### 🧩 数据支持

你可以参考项目中的实体类，让大模型为你自动生成 SQL 语句，快速接入结构化数据。

---

### 📦 Maven依赖

请使用官方 Maven 仓库拉取 Spring AI 依赖，**阿里云镜像暂不支持**：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

### 💬 它将倾听你的每一句话语，回应你的每一个疑问。

融合 AI 智能问答与后端系统，从此构建更聪明的企业应用，触手可及！

