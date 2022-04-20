FROM node:14.16.0

WORKDIR /usr/src/app

RUN git clone https://github.com/pagopa/ade-aa-ms-mock.git /usr/src/app && \
         yarn install && \
         yarn generate && \
         yarn build

CMD ["yarn", "start"]
