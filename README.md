# springai-ops-copilot

Spring AI 运维 Copilot 脚手架：Java 17 + Spring Boot 3.5.x + Spring AI 1.1.x + PostgreSQL/pgvector + Redis + Vue 3 + TypeScript + Ant Design Vue。

## 结构

- `backend`: Spring Boot API 服务
- `frontend`: Vue 3 管理端
- `docker-compose.yml`: 本地 PostgreSQL/pgvector 与 Redis

## 后端技术栈

- Java 17
- Spring Boot 3.5.13
- Spring AI 1.1.2
- Spring Web / Validation / Security / JPA / Actuator / Redis
- PostgreSQL Driver + pgvector starter
- Lombok + MapStruct
- springdoc-openapi

## 本地启动

```bash
docker compose up -d
cd backend
mvn spring-boot:run
```

默认接口：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Chat API: `POST http://localhost:8080/api/chat`

如需真实调用 OpenAI：

```bash
export OPENAI_API_KEY=你的Key
```

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认代理后端 `http://localhost:8080`。
