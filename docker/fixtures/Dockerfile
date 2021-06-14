FROM node:10.14.2-alpine

WORKDIR /opt/fixtures

RUN npm install -g ts-node && \
    npm install typescript fp-ts@1.17.4 azure-storage @types/node

COPY index.ts /opt/fixtures

CMD ["sh", "-c", "ts-node index"]