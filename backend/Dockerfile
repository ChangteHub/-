# ===== 构建阶段 =====
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存依赖
COPY pom.xml .
# 国内构建加速：阿里云 Maven 镜像（本地/CI 构建同样可用）
COPY settings-aliyun.xml /root/.m2/settings.xml
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== 运行阶段 =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# curl 供容器 healthcheck 使用
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar

# 非 root 用户运行（安全）
RUN groupadd -r appuser && useradd -r -g appuser appuser \
    && mkdir -p /app/uploads && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# JVM 参数：容器感知内存 + GC 优化
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
ENV UPLOAD_PATH=/app/uploads/

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
