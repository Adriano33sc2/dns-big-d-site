FROM sn0wf1eld/cljs-shadowcljs-lein:2025.38.1

COPY . /usr/src/app
WORKDIR /usr/src/app

RUN npx tailwindcss -i public/css/styles.css -o resources/public/assets/css/output.css --minify

RUN npm i

RUN npx shadow-cljs release app

CMD [ "npm", "run", "server" ]
