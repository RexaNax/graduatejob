# 讲解抓手：
# 1. 这个文件说明后端镜像是怎么做出来的。
# 2. 重点看两个 FROM：前一个负责编译打包，后一个负责运行。
# 3. 多阶段构建的目的，是让最终镜像更小、更干净。

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先复制 pom.xml，优先利用 Docker 缓存下载依赖。
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 运行阶段不直接使用 root，属于比较常见的容器安全做法。
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 最终镜像只拿打包后的 jar，不再带上 Maven 和源码。
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploadFile && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8919

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
