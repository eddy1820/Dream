# 使用 Maven 和 JDK 21 進行構建
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# 複製 pom.xml 並下載依賴（利用 Docker 緩存層）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 複製源代碼並打包
COPY src ./src
RUN mvn clean package -DskipTests

# 使用 JRE 21 作為運行時鏡像
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 從構建階段複製 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露應用端口
EXPOSE 8080

# 運行應用
ENTRYPOINT ["java", "-jar", "app.jar"]

