FROM sn0wf1eld/cljs-shadowcljs-lein:2025.38.1

WORKDIR /app

COPY project.clj ./
RUN lein deps

COPY package.json package-lock.json ./
RUN npm install

COPY src/ src/
COPY backend/ backend/
COPY public/ public/
COPY shadow-cljs.edn ./

RUN ./node_modules/.bin/shadow-cljs release app && \
    lein uberjar

FROM bellsoft/hardened-liberica-runtime-container:jdk-21-crac-cds-musl

WORKDIR /app

COPY --from=0 /app/target/dns-big-d-site-0.1.0-SNAPSHOT-standalone.jar ./standalone.jar
COPY --from=0 /app/public/ ./public/

EXPOSE 3000

ENV DATABASE_URL=jdbc:postgresql://db:5432/dns_coaching
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

ENTRYPOINT ["java", "-jar", "standalone.jar"]
