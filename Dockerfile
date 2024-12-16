# Ubuntu 22.04 기반 이미지 사용
FROM ubuntu:22.04

# 1. 시스템 업데이트 및 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y wget curl gnupg2 lsb-release \
    libx11-6 libxcomposite1 libxrandr2 libxi6 libxtst6 \
    libnss3 libgdk-pixbuf2.0-0 libgtk-3-0 libxss1 \
    libasound2 libxtst6 \
    # Xvfb 및 기타 X11 관련 패키지 설치
    x11-apps xvfb certbot openssl \
    # OpenJDK 설치
    openjdk-21-jdk \
    # 타임존 설정을 위한 tzdata
    tzdata && \
    # KST로 타임존 변경
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata

# 2. Google Chrome 다운로드 및 설치
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb

# 3. JAR 파일 추가
ADD ./build/libs/*.jar app.jar

# 4. 포트 80, 443 노출
EXPOSE 80 443

# 5. Xvfb 실행 및 애플리케이션 실행
CMD ["bash", "-c", "Xvfb :99 -screen 0 1024x768x16 & export DISPLAY=:99 && java -jar /app.jar"]
