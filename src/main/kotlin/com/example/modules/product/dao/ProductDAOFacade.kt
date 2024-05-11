package com.example.modules.product.dao

import com.example.modules.product.model.Product

interface ProductDAOFacade {
    suspend fun allProducts(): List<Product>
    suspend fun product(id: Int): Product?
    suspend fun addNewProduct(product: Product): Product?
    suspend fun editProduct(product: Product): Boolean
    suspend fun deleteProduct(id: Int): Boolean
}