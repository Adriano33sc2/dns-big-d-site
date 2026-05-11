FROM bellsoft/liberica-openjdk-alpine-musl:21

WORKDIR /app

# Copy pre-built jar and frontend assets
COPY docker_temp/dns-big-d-site-standalone.jar ./standalone.jar
COPY public/ ./public/

# Copy venv from local build
RUN apk add --no-cache python3 py3-pip
RUN python3 -m venv /app/venv
RUN /app/venv/bin/pip install spawningtool

EXPOSE 3000

ENV PATH="/app/venv/bin:$PATH"
ENV VIRTUAL_ENV=/app/venv

ENTRYPOINT ["java", "-jar", "standalone.jar"]
