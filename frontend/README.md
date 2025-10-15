```bash
    docker stop avatar-client
    docker rm avatar-client
    docker build -t avatar-frontend .
    docker run -d -p 8080:80 --name avatar-client avatar-frontend
```
