package com.mmert.base.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Integer id;
    private String name;
    private Integer productId;
    private Integer customerId;
    private Integer salesCount;
    private Integer price;
    private String status;
    private String source;

}
