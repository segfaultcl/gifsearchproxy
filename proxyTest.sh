#!/bin/bash -x 

api_key=API_KEY

curl --proxy-insecure -p -x https://127.0.0.1:7427 icanhazip.com
curl --insecure https://127.0.0.1:7427
curl --proxy-insecure -p -x https://127.0.0.1:7427 "https://api.giphy.com/v1/gifs/search?q=cat&api_key=$api_key&limit=5"
