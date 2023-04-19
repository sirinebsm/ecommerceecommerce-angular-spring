package com.davi.shop.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.davi.shop.dto.controller.ProductDTO;
import com.davi.shop.dto.product.RegisterProductDTO;
import com.davi.shop.entities.product.Product;
import com.davi.shop.entities.product.ProductCategory;
import com.davi.shop.exceptions.DataNotFoundException;
import com.davi.shop.repositories.product.ProductCategoryRepository;
import com.davi.shop.repositories.product.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    private ProductRepository repository;

    private ProductCategoryRepository productCategoryRepository;

    public ProductService(ProductRepository repository,
	    ProductCategoryRepository productCategoryRepository) {
	this.repository = repository;
	this.productCategoryRepository = productCategoryRepository;
    }

    public Page<ProductDTO> findAllPaged(Pageable pageable) {
	Page<Product> entity = repository.findAll(pageable);
	return entity.map(ProductDTO::new);
    }

    public Page<Product> findByCategoryId(Long id,
	    Pageable pageable) {
	Page<Product> entity;
	if (checkIfNullOrNegative(id)) {
	    entity = repository.findAll(pageable);
	} else {
	    entity = repository.findByCategoryId(id, pageable);
	}
	;
	return entity;
    }

    private boolean checkIfNullOrNegative(Long entity) {
	return entity == null || entity < 0;
    }

    @Transactional
    public Page<ProductDTO> findByName(String name,
	    Pageable pageable) {
	return repository
		.findByNameContainingIgnoreCase(name, pageable)
		.map(ProductDTO::from);
    }

    @Transactional
    public ProductDTO findById(Long id) {
	Product entity = getProduct(id);
	return new ProductDTO(entity);
    }

    @Transactional
    public Product saveProductJson(RegisterProductDTO productDto) {
	ProductCategory category = getProductCategory(productDto);
	Product entity = Product.create(category, productDto.getSku(),
		productDto.getName(), productDto.getDescription(),
		productDto.getUnitPrice(), productDto.getImageUrl(),
		productDto.getActive(), productDto.getUnitsInStock());
	return repository.save(entity);
    }

    @Transactional
    public Boolean deleteById(Long id) {
	
	if (repository.existsById(id)) {
	    repository.deleteById(id);
	}
	
	return true;
    }

    @Transactional
    public Product updateProductJson(Long id,
	    RegisterProductDTO productDto) {
	Product entity = getProduct(id);
	ProductCategory category = getProductCategory(productDto);
	entity.update(category, productDto.getSku(),
		productDto.getName(), productDto.getDescription(),
		productDto.getUnitPrice(), productDto.getImageUrl(),
		productDto.getActive(), productDto.getUnitsInStock());

	repository.save(entity);
	return entity;
    }

    private ProductCategory getProductCategory(
	    RegisterProductDTO productDto) {
	return productCategoryRepository
		.findByCategoryName(productDto.getCategoryName())
		.orElseThrow(() -> new DataNotFoundException(
			"'categoryName' not found with name: %s"
				.formatted(productDto
					.getCategoryName())));
    }

    private Product getProduct(Long id) {
	return repository.findById(id)
		.orElseThrow(() -> new DataNotFoundException(
			"'product' not found with id: %s"
				.formatted(id)));
    }
}
