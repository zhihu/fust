## prepare env for grpc

### install buf

```shell
brew install bufbuild/buf/buf
```

### generate proto code

```shell
buf generate --template buf.gen.yaml
```

### run grpc server

```shell
mvn package
sh build.sh
./run services/
```
