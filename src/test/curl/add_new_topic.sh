#!/usr/bin/env sh

curl -d @./json/topic.json -H "Content-Type: application/json" -X POST "http://localhost:8888/addNewTopic"