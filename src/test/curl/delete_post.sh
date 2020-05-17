#!/usr/bin/env sh

curl -d @./json/deleted_post_data.json -H "Content-Type: application/json" -X DELETE "http://localhost:8888/deletePost"