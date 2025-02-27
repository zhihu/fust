# install docker

## MacOS

```bash
brew install --cask docker
```

## Windows

see [Install Docker Desktop on Windows
](https://docs.docker.com/desktop/setup/install/windows-install/)

# create docker image

- x86

```bash
docker-compose -f docker-compose.yml up
```

- arm64

```bash
docker-compose -f docker-compose-arm64.yml up
```

# check docker start log

```text
apollo-quick-start    | ==== starting service ====
apollo-quick-start    | Service logging file is ./service/apollo-service.log
apollo-quick-start    | Started [45]
apollo-quick-start    | Waiting for config service startup.......
apollo-quick-start    | Config service started. You may visit http://localhost:8080 for service status now!
apollo-quick-start    | Waiting for admin service startup......
apollo-quick-start    | Admin service started
apollo-quick-start    | ==== starting portal ====
apollo-quick-start    | Portal logging file is ./portal/apollo-portal.log
apollo-quick-start    | Started [254]
apollo-quick-start    | Waiting for portal startup.......
apollo-quick-start    | Portal started. You can visit http://localhost:8070 now!
```

# login to check log

```bash
docker exec -it apollo-quick-start bash
```

check log file in `/apollo-quick-start/service`and `/apollo-quick-start/portal`
