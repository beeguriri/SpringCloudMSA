package wendy.study.orderservice.vo;

import lombok.Data;

@Data
public class RequestOrder {
    private String productId;
    private Integer qty;
    //TODO: 카탈로그에서 가져와야하는거 아닐까?
    private Integer unitPrice;
}
