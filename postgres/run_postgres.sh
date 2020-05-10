#!/usr/bin/env sh

docker_runnable_name="postgres_forum_api_runnable"

if [ ! "$(docker ps -q -f name=${docker_runnable_name})" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=${docker_runnable_name})" ]; then
        docker rm ${docker_runnable_name}
    fi

    docker run \
    --name ${docker_runnable_name} \
    -it \
    -p 5432:5432 \
    -e POSTGRES_USER=api \
    -e POSTGRES_PASSWORD=password \
    -e POSTGRES_DB=forum \
    -v /home/igor/IdeaProjects/Forum-API/postgres/volumes/init.sql:/docker-entrypoint-initdb.d/init.sql \
    postgres
fi
