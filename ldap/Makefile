# Docker related targets

DOCKER-COMPOSE-BIN=docker-compose
all: docker-build

docker-pull:
	docker pull debian:bullseye

docker-build: docker-pull
	$(DOCKER-COMPOSE-BIN) build

run: docker-build
	$(DOCKER-COMPOSE-BIN) up
