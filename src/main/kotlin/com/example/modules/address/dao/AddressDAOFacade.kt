package com.example.modules.address.dao

import com.example.modules.address.model.Address
import com.example.modules.address.model.CreateAddressDTO

interface AddressDAOFacade {
    suspend fun address(id: Int): Address?
    suspend fun addNewAddress(address: CreateAddressDTO): Address?
    suspend fun deleteAddress(id: Int): Boolean
}