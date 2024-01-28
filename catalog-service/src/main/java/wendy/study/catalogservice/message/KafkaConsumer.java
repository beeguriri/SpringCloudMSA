package wendy.study.catalogservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import wendy.study.catalogservice.entity.CatalogEntity;
import wendy.study.catalogservice.repository.CatalogRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final CatalogRepository catalogRepository;

    /**
     * kafka topic에 메시지가 전달되면
     * 해당 메서드가 실행 됨
     */
    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage) {
        log.info("kafka Message -> {}", kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        /*
        kafka message (json) 를 object 형태로 변환
         */
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex) {
            log.error("json processing Exception!!!");
        }

        CatalogEntity entity = catalogRepository.findByProductId((String) map.get("productId"));

        if(entity == null) {
            log.error("no product!!!");
            return;
        }

        entity.setStock(entity.getStock() - (Integer)map.get("qty"));
        catalogRepository.save(entity);
    }
}
