package com.orderflow.infrastructure.adapter.in.rest;

import com.orderflow.domain.model.Product;
import com.orderflow.domain.port.out.ProductRepositoryPort;
import com.orderflow.infrastructure.adapter.in.rest.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductRepositoryPort productRepository;

    public ProductController(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productRepository.findAll()
                .stream()
                .map(ProductResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(products);
    }
}