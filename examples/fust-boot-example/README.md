# Env

## MYSQL DB

Connect your local mysql, create database `db1` and table `yd_user`.

```sql
create database db1;
CREATE TABLE `yd_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `birthday` date NOT NULL,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

## redis

start your local redis, use default port.

## grpc proto

* install buf

```shell
brew install bufbuild/buf/buf
```

* Generate proto code

```shell
cd proto
buf mod update
cd ..
# will generate proto code in felis-boot-example-grpc/gen-src
buf generate proto
```

# gRPC service

## build

```shell
mvn package
bash build.sh
```

## run

```shell
bash run.sh services/felis-boot-example-grpc
```

## local debug

* open http://127.0.0.1:8888/_docs
* Find `hello` method in `HelloService`, click `Debug`
* short link: http://127.0.0.1:8888/_docs/#/methods/hello.HelloService/Hello/POST?debug_form_is_open=true

