package com.swaply.productservice.document;

import com.swaply.productservice.utils.enums.City;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Boolean)
    private Boolean isNew;

    @Field(type = FieldType.Keyword)
    private ProductCategory category;

    @Field(type = FieldType.Keyword)
    private City city;

    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    @Field(type = FieldType.Long)
    private Long createdAt;

    @Field(type = FieldType.Keyword)
    private java.util.List<String> imageUrls;

    @Field(type = FieldType.Boolean)
    private Boolean isDelivery;
}
