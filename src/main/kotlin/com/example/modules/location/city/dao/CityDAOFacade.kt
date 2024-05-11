package com.example.modules.location.city.dao

import com.example.modules.location.city.model.City

interface CityDAOFacade {
    suspend fun city(id: Int): City?
    suspend fun addNewCity(city: City): City?
    suspend fun deleteCity(id: Int): Boolean
}