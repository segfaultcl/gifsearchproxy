#!/usr/bin/env bash

api_key=API_KEY

set -x

# Should 502
curl --proxy-insecure -p -x https://127.0.0.1:7427 icanhazip.com

# Should 501
curl --insecure https://127.0.0.1:7427

# Should 502 not https
curl --proxy-insecure -p -x https://127.0.0.1:7427 "http://api.giphy.com"

# Should return 200 and then proxy
curl --proxy-insecure -p -x https://127.0.0.1:7427 "https://api.giphy.com"  
curl --proxy-insecure -p -x https://127.0.0.1:7427 "https://api.giphy.com/v1/gifs/search?q=cat&api_key=$api_key&limit=5"

exit 

# Run multiple processes to ensure rate limiting works
while (true); do
    curl --proxy-insecure -p -x https://127.0.0.1:7427 "https://api.giphy.com/v1/gifs/search?q=cat&api_key=$api_key&limit=5" &
    sleep 0.1
done
