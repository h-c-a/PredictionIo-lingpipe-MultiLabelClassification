#!/usr/bin/bash

curl -H "Content-Type: application/json" -d '{"text":"Apple","locale":"DA"}' http://localhost:8000/queries.json
