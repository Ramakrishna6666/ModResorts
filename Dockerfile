# =============================================================================
# Stage 1: Builder - Compile and package the WAR
# =============================================================================
FROM maven:3.9.4-eclipse-temurin-8 AS builder

WORKDIR /workspace

# Copy the entire project structure for build
COPY . .

# Download dependencies first (layer caching optimization)
RUN mvn dependency:go-offline -DskipTests --no-transfer-progress || true

# Build the WAR artifact
RUN mvn clean package -DskipTests --no-transfer-progress

# =============================================================================
# Stage 2: Runtime - Apache Tomcat on eclipse-temurin:8-jre
# =============================================================================
FROM eclipse-temurin:8-jre

# Install Tomcat
ENV TOMCAT_VERSION=9.0.85
ENV CATALINA_HOME=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser -d /opt/tomcat -s /sbin/nologin appuser

# Download and install Tomcat
RUN apt-get update && apt-get install -y --no-install-recommends wget ca-certificates \
    && wget -q "https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
         -O /tmp/tomcat.tar.gz \
    && mkdir -p ${CATALINA_HOME} \
    && tar -xzf /tmp/tomcat.tar.gz -C ${CATALINA_HOME} --strip-components=1 \
    && rm /tmp/tomcat.tar.gz \
    && rm -rf ${CATALINA_HOME}/webapps/ROOT \
    && rm -rf ${CATALINA_HOME}/webapps/examples \
    && rm -rf ${CATALINA_HOME}/webapps/docs \
    && rm -rf ${CATALINA_HOME}/webapps/host-manager \
    && rm -rf ${CATALINA_HOME}/webapps/manager \
    && apt-get remove -y wget \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

# Set timezone
ENV TZ=UTC

# JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UnlockExperimentalVMOptions \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=UTC"

# Copy the WAR from builder stage
COPY --from=builder /workspace/target/modresorts-2.0.0.war ${CATALINA_HOME}/webapps/resorts.war

# Set ownership
RUN chown -R appuser:appuser ${CATALINA_HOME}

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Graceful shutdown support
STOPSIGNAL SIGTERM

# Start Tomcat
CMD ["catalina.sh", "run"]
