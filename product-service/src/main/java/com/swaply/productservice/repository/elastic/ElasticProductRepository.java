package com.swaply.productservice.repository.elastic;

import com.swaply.productservice.document.ProductDocument;
import com.swaply.productservice.utils.enums.ProductStatus;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticProductRepository extends ElasticsearchRepository<ProductDocument, String> {

    @Query("{\"bool\": {\"filter\": [{\"term\": {\"status\": \"ACTIVE\"}}], \"must\": [{\"query_string\": {\"query\": \"*?0*\", \"fields\": [\"title\", \"description\"], \"default_operator\": \"AND\"}}]}}")
    List<ProductDocument> searchActiveProducts(String query);

}
