#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=hotel-service \
--package-name=microservices.core.hotel \
--groupId=microservices.core.hotel \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
hotel-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=location-service \
--package-name=microservices.core.location \
--groupId=microservices.core.location \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
location-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=room-service \
--package-name=microservices.core.room \
--groupId=microservices.core.room \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
room-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=review-service \
--package-name=microservices.core.review \
--groupId=microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=hotel-composite-service \
--package-name=microservices.composite.hotel \
--groupId=microservices.composite.hotel \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
hotel-composite-service

cd ..