## Introduction
This project is an example of HTTP interactions with Spring Boot.

A movie API project will be taken as example to be called through by the example project.

## Run the project
### Application
Start both movie-api and http-example Spring boot projects, then you can try out the HTTP api.

Get a movie by id
```
curl --request GET \
  --url http://localhost:8090/http-api-example/movies/22
```

Save a movie
```
curl --request POST \
  --url http://localhost:8090/http-api-example/movies \
  --header 'content-type: application/json' \
  --data '{
  "id": 28,
  "title": "Some title",
  "release_date": "2022-02-04"
}'
```
