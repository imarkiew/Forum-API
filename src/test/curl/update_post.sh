#!/usr/bin/env bash

curl -d @./json/updated_post.json -H "Content-Type: application/json" -X PATCH "http://localhost:8888/updatePost"