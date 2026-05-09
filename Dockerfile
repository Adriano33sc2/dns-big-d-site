FROM sn0wf1eld/cljs-shadowcljs-lein:2025.38.1 AS builder
WORKDIR /app
COPY project.clj ./
RUN lein deps
COPY package.json package-lock.json ./
RUN npm install
COPY src/ src/
COPY backend/ backend/
COPY public/ public/
COPY shadow-cljs.edn ./
RUN ./node_modules/.bin/shadow-cljs release app && lein uberjar

# Install Python and spawningtool in builder (has package manager)
RUN python3 -m venv /app/venv
RUN /app/venv/bin/pip install spawningtool

FROM bellsoft/hardened-liberica-runtime-container:jdk-21-crac-cds-musl

WORKDIR /app

# Copy venv from builder stage
COPY --from=builder /app/venv /app/venv
COPY --from=builder /app/target/dns-big-d-site-0.1.0-SNAPSHOT-standalone.jar ./standalone.jar
COPY --from=builder /app/public/ ./public/

EXPOSE 3000

ENV PATH="/app/venv/bin:$PATH"
ENV VIRTUAL_ENV=/app/venv

ENTRYPOINT ["java", "-jar", "standalone.jar"]