.PHONY: build db dev openapi test

build:
	./gradlew clean build

db:
	docker compose up -d

dev:
	./gradlew run

openapi:
	curl -fsS http://localhost:8080/openapi | jq .

test:
	./gradlew test
