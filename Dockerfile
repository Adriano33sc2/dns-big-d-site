FROM sn0wf1eld/cljs-shadowcljs-lein:latest

WORKDIR /app

COPY docker_temp/dns-big-d-site-standalone.jar ./standalone.jar

# Copy venv from local build
RUN apt install -y python3 python3-venv python3-pip
RUN python3 -m venv /app/venv
RUN /app/venv/bin/pip install spawningtool

EXPOSE 3000

ENV PATH="/app/venv/bin:$PATH"
ENV VIRTUAL_ENV=/app/venv

ENTRYPOINT ["java", "-jar", "standalone.jar"]
