package com.example.modules.location.country.dao

import com.example.modules.location.country.model.Country

interface CountryDAOFacade {
    suspend fun country(id: Int): Country?
    suspend fun addNewCountry(country: Country): Country?
    suspend fun deleteCountry(id: Int): Boolean
}