.PHONY: build run

build:
	mvn compile -q

run: build
	mvn spring-boot:run -Dspring-boot.run.profiles=dev