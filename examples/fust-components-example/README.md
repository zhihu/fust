# Prepare Environment

## install `buf`

We use buff to generate proto java

```shell 
brew install bufbuild/buf/buf
```

## generate grpc proto code

```shell
cd grpc/
buf generate --template buf.gen.yaml
```




