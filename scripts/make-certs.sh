#!/usr/bin/env bash

mkdir -p docker/certs

# TLS spidtest-env2
openssl req -nodes -new -x509 -sha256 -days 365 -newkey rsa:2048 \
    -config scripts/tls.conf \
    -keyout docker/certs/certificate.pem -out docker/certs/certificate.crt

# TLS METADATA ENDPOINT hub-spid-login-ms
openssl req -nodes -new -x509 -sha256 -days 365 -newkey rsa:2048 \
    -subj "/C=IT/ST=State/L=City/O=Acme Inc. /OU=IT Department/CN=hub-spid-login-ms" \
    -keyout docker/certs/key.pem -out docker/certs/cert.pem
