#!/usr/bin/env sh

curl -d @./json/post.json -H "Content-Type: application/json" -X POST "http://localhost:8888/addNewPost"