@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.product.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.product.model.Product
import com.example.modules.product.model.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductDAOImpl : ProductDAOFacade {
    private fun resultRowToProduct(resultRow: ResultRow) =
        Product(
            id = resultRow[Products.id].value,
            price = resultRow[Products.price],
            name = resultRow[Products.name],
        )

    override suspend fun allProducts(): List<Product> =
        dbQuery {
            Products.selectAll().map(::resultRowToProduct)
        }

    override suspend fun product(id: Int): Product? =
        dbQuery {
            Products
                .select(Products.id eq id)
                .map(::resultRowToProduct)
                .singleOrNull()
        }

    override suspend fun addNewProduct(product: Product): Product? =
        dbQuery {
            val insertStatement =
                Products.insert {
                    it[price] = price
                    it[name] = product.name
                }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToProduct)
        }

    override suspend fun editProduct(product: Product): Boolean =
        dbQuery {
            Products.update({ Products.id eq product.id }) {
                it[price] = price
                it[name] = name
            } > 0
        }

    override suspend fun deleteProduct(id: Int): Boolean =
        dbQuery {
            Products.deleteWhere { Products.id eq id } > 0
        }
}

val productDao = ProductDAOImpl()