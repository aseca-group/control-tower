package com.example.modules.location.address.dao

import com.example.modules.location.address.model.Address

interface AddressDAOFacade {
    suspend fun address(id: Int): Address?
    suspend fun addNewAddress(address: Address): Address?
    suspend fun deleteAddress(id: Int): Boolean
}