FROM node:12.18.1@sha256:2b85f4981f92ee034b51a3c8bb22dbb451d650d5c12b6439a169f8adc750e4b6

WORKDIR /usr/src/app

RUN git clone https://github.com/pagopa/hub-spid-login-ms.git /usr/src/app && \
         git checkout master && \
         yarn install && \
         yarn generate && \
         yarn build

ENTRYPOINT ["node", "dist/src/server.js"]
