FROM node:10.14.2-alpine@sha256:46b2488c02dc82c801eaebc775019262e100af290dd06d61201a8dd53f78c022

WORKDIR /opt/fixtures

RUN npm install -g ts-node && \
    npm install typescript fp-ts@1.17.4 azure-storage @types/node

COPY index.ts /opt/fixtures

CMD ["sh", "-c", "ts-node index"]