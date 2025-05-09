# 🔧 베이스 이미지 설정 (OpenJDK 17)
FROM openjdk:17-jdk-slim

# 🗂 작업 디렉토리 설정 (선택적이지만 권장)
WORKDIR /app

# 📦 JAR 파일을 컨테이너에 복사
COPY build/libs/be12-fin-5verdose-WMTHIS-BE-0.0.1-SNAPSHOT.jar app.jar

# 🚀 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
