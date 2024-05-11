package com.example.modules.price.dao

import com.example.modules.price.model.Price

interface PriceDAOFacade {
    suspend fun allPrices(): List<Price>
    suspend fun price(id: Int): Price?
    suspend fun addNewPrice(price: Price): Price?
    suspend fun editPrice(price: Price): Boolean
    suspend fun deletePrice(id: Int): Boolean
}