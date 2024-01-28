### ✨ Apache Kafka 사용
- [Apache Kafka 다운로드](https://kafka.apache.org/downloads) `2.13-3.6.1`
```shell
# zookeeper-server 기동
$ .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
 
# kafka-server 기동
$ .\bin\windows\kafka-server-start.bat .\config\server.properties

# topic 생성
$ .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic quikstart-events --partitions 1

# topic list 확인
$ .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list

# topic 상세 보기
$ .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic quikstart-events

# procude
$ .\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic quickstart-events

# consumer
$ .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic quickstart-events --from-beginning
```
### ✨ Apache Kafka Connect 사용 준비
- Restful API를 통해 지원
- Stream 또는 Batch 형태로 데이터 전송 가능
- Connect Source -> Kafka Cluster -> Connect Sink
    - `users` DB에 값이 입력 되면
    - source connector가 my_topic_users 로 pub
    - sink connector가 consume해서 `my_topic_users` DB에 저장
- source로 maria db 사용 `2.7.11`
- [kafka connect 다운로드](https://www.confluent.io/installation/) `7.5.3`
- [JDBC Connector 설치](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc) `10.7.4`
- JDBC Connector 설정
```shell
# jdbc connector 다운로드 받은 주소 추가
# confluent-7.5.3\etc\kafka\connect-distributed.properties
plugin.path=\D:\\programs\\confluentinc-kafka-connect-jdbc-10.7.4\\lib

# mariadb 드라이버 복사
# mariadb-java-client-2.7.11.jar
> \confluent-7.5.3\share\java\kafka
```
- 실행
```shell
# kafka connect 실행
$ .\bin\windows\connect-distributed.bat .\etc\kafka\connect-distributed.properties

# topic 목록 확인
$ .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
> connect-configs
> connect-offsets
> connect-status
> quickstart-events
```
- 실행 시 오류 발생 할 경우 kafka-run-class.bat 수정해주기
```
# 추가
# confluent-7.5.3\bin\windows\kafka-run-class.bat
rem Classpath addition for LSB style path
if exist %BASE_DIR%\share\java\kafka\* (
	call:concat %BASE_DIR%\share\java\kafka\*
)

rem Classpath addition for core
for %%i in ("%BASE_DIR%\core\build\libs\kafka_%SCALA_BINARY_VERSION%*.jar") do (
	call :concat "%%i"
)
```
### ✨ Source Connectors 추가
- source-connector 생성 (POST localhost:8083/connectors/)
  ![](/images/create_connect.png)
- connector 확인
  ![](/images/status_connect.png)
- topic 생성 및 produce 확인
    - => DB에 새로운 데이터가 입력되면 topic이 생성 됨
      ![](/images/create_topic.png)
      ![](/images/create_topic_consume.png)
### ✨ Sink Connectors 추가
- sink-connector 생성 (POST localhost:8083/connectors/)
  ![](/images/create_sink_connect.png)
- 기존 topic에 있던 데이터가 새로운 table 생성 및 저장되어 있음 확인
  
  ![](/images/source_sink_db.png)
- console-producer를 이용하여 db 입력
  ```shell
    $ .\bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic my_topic_users
  ```
  ```json
  {
    "schema":{
      "type":"struct",
      "fields":[
        {
          "type":"int32",
          "optional":false,
          "field":"id"
        },{
          "type":"string",
          "optional":true,
          "field":"user_id"
        }, ... ],
      "optional":false,
      "name":"users"
    },
    "payload":{
      "id":5,
      "user_id":"prod1",
      "password":"prod1234",
      "name":"prod1"
    }
  }
  ```
- connect 된 db에만 반영 된 것 확인

  ![](/images/source_sink_db2.png)

