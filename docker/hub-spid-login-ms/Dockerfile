FROM node:12.18.1

WORKDIR /usr/src/app

RUN git clone https://github.com/pagopa/hub-spid-login-ms.git /usr/src/app && \
         git checkout master && \
         yarn install && \
         yarn generate && \
         yarn build

ENTRYPOINT ["node", "dist/src/server.js"]
