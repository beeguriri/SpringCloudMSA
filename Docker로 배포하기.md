## ⭐ Docker로 배포하기
- [https://www.docker.com/get-started/](https://www.docker.com/get-started/)
### ✨ 자주 사용하는 명령어
```shell
# docker login
$ docker login -u beeguri

# docker 설치 확인
$ docker info

# docker image 목록 확인
$ docker image ls

# 현재 실행중인 container 목록 확인
$ docker container ls

# 이떄까지 실행 한 container 목록 확인
$ docker container ls -a

# 실행중이지 않은 container 삭제 [CONTAINER ID]
$ docker container rm 2a551f95a09f 

# image 다운
$ docker pull ubuntu:16.04

# https://hub.docker.com/ 에서 image 검색
$ docker run [OPTIONS] IMAGE[:TAG|@DIGEST] [COMMAND] [ARG...]
# $ docker run ubuntu:16.04
# -d : 백그라운드 모드
# -p : 호스트와 컨테이너의 포트를 연결
# -v : 호스트와 컨테이너의 디렉토리를 연결
# -e : 컨테이너 내에서 사용할 환경변수 설정
# --name : 컨테이너 이름 설정
# --rm : 프로세스 종료시 컨테이너 자동 제거

# 상태 확인
$ docker logs [CONTAINER ID]

# 사용되지 않는 컨테이너, 네트워크 모두 삭제
$ docker system prune
```
### ✨ 네트워크 설정
- bridge Network
  - 호스트 pc와 별도의 가상의 network 만들어 놓고 사용
- host Network
  - 호스트 pc과 가상의 pc가 같은 환경을 사용
```shell
$ docker network ls
NETWORK ID     NAME      DRIVER    SCOPE
c2b0060cbcd1   bridge    bridge    local
7b7caf14345a   host      host      local
f6797ac8e8b6   none      null      local
  
# network 만들기 (같은 네트워크를 사용하도록 설정)
# 유레카에 설정 된 container 이름으로 찾도록 하기 위해서!
$ docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 ecommerce-network

# 네트워크 상세내용 확인
$ docker network inspect ecommerce-network
```
### ✨ RabbitMQ 실행
```shell
# docker run -d \ background mode로 실행 
#              --name rabbitmq \ 네트워크에서 구분하기 위한 이름
#              --network ecommerce-network \ 네트워크 설정
#              -p 15672:15672 -p 15671:15671 -p 5672:5672\ 포트포워팅 정보
#               -e RABBITMQ_DEFAULT_USER=guest 
#               -e RABBITMQ_DEFAULT_PASS=guest \ 환경변수
#               호출하려는 컨테이너 이름 

# rabbitmq 기동
# 도커에 이미지 없어도 run 명령어로 알아서 설치 후 실행 해줌
$ docker run -d --name rabbitmq --network ecommerce-network -p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:management

# 네트워크 안에 rabbitmq 포함되어있는지 확인 할 것
$ docker network inspect ecommerce-network
```
### ✨ Configuration server 실행
- API Key 관련 파일을 컨테이너로 복사해 주는 작업 필요함
- 다른 서비스의 IP를 명시할 때는 yml 파일을 고치기 보다는 cmd에서 지정하는게 좋음
  - IP가 바뀔수도 있기 때문에...!!
```shell
# 0. api key를 config-service 폴더 최상단에 복사
# 0. yml 파일 수정
# 1. Docker file 작성

# 2. mvn build
$ mvn clean compile package -DskipTests=true

# 3. docker build
$ docker build -t beeguri/config-service:1.0 .

# 4. config-service 실행
# 이때 rabbitMQ container 이름을 써줌!
$ docker run -d -p 8888:8888 --network ecommerce-network -e "spring.rabbitmq.host=rabbitmq" -e "spring.profiles.active=default" --name config-service beeguri/config-service:1.0

# 5. log 확인
$ docker logs config-service
```
### ✨ Discovery service 실행
```shell
# 1. Docker file 작성

# 2. mvn build
$ mvn clean compile package -DskipTests=true

# 3. docker build
$ docker build -t beeguri/discovery-service:1.0 .

# 4. discovery-service 실행
# 이때 config server 를 container 이름으로 써줌
# 같은 network에 묶여있기 때문에 이름으로 찾을 수 있다!!!
$ docker run -d -p 8761:8761 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" --name discovery-service beeguri/discovery-service:1.0

# 5. log 확인
$ docker logs discovery-service
```
### ✨ Gateway service 실행
- config service, rabbit mq, eureka 등록을 위한 환경변수 설정
```shell
# 1. Docker file 작성
# 2. mvn build
# 3. docker build
# 4 . 실행
$ docker run -d -p 8000:8000 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" -e "spring.rabbitmq.host=rabbitmq" -e "eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka" --name gateway-service beeguri/gateway-service:1.0
```

### ✨ mariaDB 실행
- mariaDB test용 설치 및 실행
```shell
# mariadb 다운 및 실행 
# port : [host요청 : container 응답]
# name : container name
# image name
$ docker pull mariadb:latest
$ docker run -d -p 13306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=true --name mariadb mariadb:latest

# 확인
$ docker container ls
CONTAINER ID   IMAGE            COMMAND                  CREATED          STATUS          PORTS                    NAMES
dbdceb6fba43   mariadb:latest   "docker-entrypoint.s…"   24 seconds ago   Up 23 seconds   0.0.0.0:3306->3306/tcp   mariadb

# 컨테이너에 추가적인 작업을 하고자 할때
$ docker exec -it mariadb /bin/bash
root@dbdceb6fba43:/# mariadb -u root -p
Enter password:
Welcome to the MariaDB monitor.  Commands end with ; or \g.

# 컨테이너를 삭제하고 나면, db에 만들었던 데이터가 삭제됨!!!
```
- 실제 서비스에 이용할 mariadb 설정
```shell
# 0. 기존에 사용했던 db의 데이터 복사해오기
# 1. docker file 생성 및 빌드 (/etc/docker-files)
# 2. 실행
$ docker run -d -p 3306:3306 --network ecommerce-network --name mariadb beeguri/my_mariadb:1.0

# 3. 접속
$ docker exec -it mariadb /bin/bash
root@dbdceb6fba43:/# mariadb -uroot -p
# password : test1357

# db에 데이터 복제 된거 확인
> show databases;
> use mydb;
> show tables;
> select * from orders;

# 사용권한 모든 접근 허용
> grant all privileges on *.* to 'root'@'%' identified by 'test1357';
> flush privileges;
```

### ✨ Kafka 실행
- zookeeper + kafka standalone
- docker-compose로 실행
- github.com/wurstmeister/kafka-docker
- kafka 사용하는 서비스의 zookeeper와 kafka의 ip를 변경
```shell
# 1. docker-compse-single-broker.yml 파일 수정(/etc/docker-files/kafka-docker)
# 2. 실행
$ docker-compose -f docker-compose-single-broker.yml up -d
```

### ✨ Zipkin 실행
```shell
$ docker run -d -p 9411:9411 --network ecommerce-network --name zipkin openzipkin/zipkin
```

### ✨ Monitoring 실행
```shell
# prometheus 실행
# prometheus.yml 파일 수정 및 복사 (etc/prometheus)
# -v : volume mount option에 추가
$ docker run -d -p 9090:9090 --network ecommerce-network --name prometheus -v D:\programs\prometheus-2.49.1.windows-amd64\prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus

# grafana 실행
$ docker run -d -p 3000:3000 --network ecommerce-network --name grafana grafana/grafana
```

### ✨ service 실행
- user-service build 및 실행
  ```shell
  # 1. DockerFile 작성
  
  # 2. jar build
  $ mvn clean compile package -DskipTests=true
  
  # 3. docker build (hubsite 계정명 써야됨)
  $ docker build --tag beeguri/userservice:1.0 .
  
  # 4. datahub에 push
  $ docker push beeguri/userservice:1.0
  
  # 5. repository에서 pull (test)
  # 기존 이미지파일 삭제하고 pull
  $ docker rmi [ID]
  $ docker pull beeguri/userservice:1.0
  
  # 6. 실행
  $ docker run beeguri/userservice:1.0
  ```
- dd