# =====================================================
# AŞAMA 1: BUILD (Derleme)
# =====================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1. Gradle wrapper ve konfigürasyon dosyalarını kopyala
#    (Bağımlılıkları önbelleğe almak için önce bunları kopyalıyoruz)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# gradlew dosyasını çalıştırılabilir yap
RUN chmod +x ./gradlew

# 2. Bağımlılıkları indir (Sadece bağımlılıklar değişirse bu adım tekrar çalışır)
RUN ./gradlew dependencies || true

# 3. Tüm kaynak kodları kopyala
COPY src src

# 4. Projeyi derle ve JAR dosyasını oluştur (Testleri atlayarak hızlandırıyoruz)
RUN ./gradlew bootJar -x test


# =====================================================
# AŞAMA 2: RUNTIME (Çalıştırma)
# =====================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache wget
# Build aşamasından oluşan JAR dosyasını al
COPY --from=builder /app/build/libs/*.jar app.jar

# Uygulamanın dinlediği port (Spring Boot varsayılan: 8080)
EXPOSE 8080

# JVM ayarları ile uygulamayı başlat
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]