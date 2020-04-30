#!/usr/bin/env sh

docker_runnable_name="postgres_forum_api_runnable"

if [ "$(docker ps -q -f name=${docker_runnable_name})" ]; then
    docker exec -it ${docker_runnable_name}  psql -U postgres
fi