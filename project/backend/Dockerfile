# ============================================
# 后端 Dockerfile - 多阶段构建
# ============================================
# 阶段1: 构建阶段 - 使用 Maven 编译打包
# 阶段2: 运行阶段 - 使用精简的 JRE 运行
# ============================================

# ========== 阶段1: 构建 ==========
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层加速构建
# 只有 pom.xml 变化时才重新下载依赖
COPY pom.xml .

# 下载依赖（离线模式，加速后续构建）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 打包，跳过测试
RUN mvn package -DskipTests -B

# ========== 阶段2: 运行 ==========
FROM eclipse-temurin:17-jre-alpine

# 设置时区为中国
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 创建文件上传目录
RUN mkdir -p /app/uploadFile && chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8919

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
