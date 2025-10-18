```bash
    docker stop avatar-client
    docker rm avatar-client
    docker build -t avatar-client .
    docker run -d -p 3000:80 --name avatar-client avatar-client
```
