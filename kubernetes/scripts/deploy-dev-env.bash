#!/usr/bin/env bash

# Print commands to the terminal before execution and stop the script if any error occurs
set -ex

kubectl apply -f kubernetes/namespace.yml
kubectl config set-context $(kubectl config current-context) --namespace=dis

kubectl create configmap config-repo-auth-server       --from-file=config-repo/application.yml --from-file=config-repo/auth-server.yml --save-config
kubectl create configmap config-repo-gateway           --from-file=config-repo/application.yml --from-file=config-repo/gateway.yml --save-config
kubectl create configmap config-repo-hotel-composite   --from-file=config-repo/application.yml --from-file=config-repo/hotel-composite.yml --save-config
kubectl create configmap config-repo-hotel             --from-file=config-repo/application.yml --from-file=config-repo/hotel.yml --save-config
kubectl create configmap config-repo-location          --from-file=config-repo/application.yml --from-file=config-repo/location.yml --save-config
kubectl create configmap config-repo-review            --from-file=config-repo/application.yml --from-file=config-repo/review.yml --save-config
kubectl create configmap config-repo-room              --from-file=config-repo/application.yml --from-file=config-repo/room.yml --save-config

kubectl create secret generic rabbitmq-server-credentials \
    --from-literal=RABBITMQ_DEFAULT_USER=rabbit-user-dev \
    --from-literal=RABBITMQ_DEFAULT_PASS=rabbit-pwd-dev \
    --save-config

kubectl create secret generic rabbitmq-credentials \
    --from-literal=SPRING_RABBITMQ_USERNAME=rabbit-user-dev \
    --from-literal=SPRING_RABBITMQ_PASSWORD=rabbit-pwd-dev \
    --save-config

kubectl create secret generic rabbitmq-zipkin-credentials \
    --from-literal=RABBIT_USER=rabbit-user-dev \
    --from-literal=RABBIT_PASSWORD=rabbit-pwd-dev \
    --save-config

kubectl create secret generic mongodb-credentials \
    --from-literal=SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
    --from-literal=SPRING_DATA_MONGODB_USERNAME=mongodb-user-dev \
    --from-literal=SPRING_DATA_MONGODB_PASSWORD=mongodb-pwd-dev \
    --save-config

kubectl create secret generic mysql-server-credentials \
    --from-literal=MYSQL_ROOT_PASSWORD=rootpwd \
    --from-literal=MYSQL_DATABASE=review-db \
    --from-literal=MYSQL_USER=mysql-user-dev \
    --from-literal=MYSQL_PASSWORD=mysql-pwd-dev \
    --save-config

kubectl create secret generic mysql-credentials \
    --from-literal=SPRING_DATASOURCE_USERNAME=mysql-user-dev \
    --from-literal=SPRING_DATASOURCE_PASSWORD=mysql-pwd-dev \
    --save-config

kubectl create secret tls tls-certificate --key kubernetes/cert/tls.key --cert kubernetes/cert/tls.crt --namespace=dis

# First deploy the resource managers and wait for their pods to become ready
kubectl apply -f kubernetes/services/overlays/rabbitmq-dev.yml
kubectl apply -f kubernetes/services/overlays/mongodb-set.yml
kubectl apply -f kubernetes/services/overlays/mysql-set.yml
kubectl wait --timeout=600s --for=condition=ready pod --all --namespace=dis

# Next deploy the microservices and wait for their pods to become ready
kubectl apply -k kubernetes/services/overlays
kubectl wait --timeout=600s --for=condition=ready pod --all --namespace=dis

set +ex