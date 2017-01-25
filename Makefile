docker-build:
	docker pull debian:jessie
	docker pull dinkel/openldap 
	docker pull postgres:9.4
	docker pull tianon/apache2
	docker-compose build
