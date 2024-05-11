package com.example.modules.product.dao

import com.example.modules.product.model.Product
import com.example.modules.product.model.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductDAOImpl : ProductDAOFacade {
    private fun resultRowToProduct(resultRow: ResultRow) = Product (
        id = resultRow[Products.id],
        priceId = resultRow[Products.priceId],
        costId = resultRow[Products.costId],
        name = resultRow[Products.name],
    )

    override suspend fun allProducts(): List<Product> {
        return Products.selectAll().map(::resultRowToProduct)
    }

    override suspend fun product(id: Int): Product? {
        return Products
            .select(Products.id eq id)
            .map(::resultRowToProduct)
            .singleOrNull()
    }

    override suspend fun addNewProduct(product: Product): Product? {
        val insertStatement = Products.insert {
            it[priceId] = priceId
            it[costId] = product.costId
            it[name] = product.name
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToProduct)
    }

    override suspend fun editProduct(product: Product): Boolean {
        return Products.update({ Products.id eq product.id}) {
            it[priceId] = priceId
            it[costId] = costId
            it[name] = name
        } > 0
    }

    override suspend fun deleteProduct(id: Int): Boolean {
        return Products.deleteWhere { Products.id eq id } > 0
    }
}