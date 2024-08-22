FROM node:14.16.0@sha256:f6b9ff4caca9d4f0a331a882e560df242eb332b7bbbed2f426784de208fcd7bd

WORKDIR /usr/src/app

RUN git clone https://github.com/pagopa/ade-aa-ms-mock.git /usr/src/app && \
         yarn install && \
         yarn generate && \
         yarn build

CMD ["yarn", "start"]
