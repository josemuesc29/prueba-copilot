package com.imaginamos.farmatodo.backend.stock

import com.google.api.server.spi.response.ConflictException
import com.imaginamos.farmatodo.backend.OfyService
import com.imaginamos.farmatodo.backend.product.ProductsMethods
import com.imaginamos.farmatodo.model.product.Item
import com.imaginamos.farmatodo.model.product.StoreInformation
import com.imaginamos.farmatodo.model.stock.DeleteOfferItem
import com.imaginamos.farmatodo.model.stock.StoreStock
import com.imaginamos.farmatodo.model.stock.TotalStockRequest
import com.imaginamos.farmatodo.model.stock.TotalStockResponse
import com.imaginamos.farmatodo.model.util.Constants
import com.imaginamos.farmatodo.networking.services.ShoppingCartService
import java.util.logging.Logger

class StockMethods {

    private val productsMethods = ProductsMethods()
    private val logger: Logger = Logger.getLogger(StockMethods::class.java.name)

    companion object {
        private const val MIN_VALID_SCAN_AND_GO_STOCK = 1
        private const val DEFAULT_SCAN_AND_GO_STOCK = 1000
    }

    fun updateStoreInformation(
        listStoreInformation: List<StoreInformation>?,
        storeStock: StoreStock
    ): List<StoreInformation> {
        val auxList: MutableList<StoreInformation> = ArrayList()
        auxList.addAll(listStoreInformation!!)

        val storeInformation = auxList.stream().filter { p: StoreInformation -> p.storeGroupId == storeStock.store }
            .findFirst().get()

        if (storeStock.offerText != null) storeInformation.offerText = storeStock.offerText

        if (storeStock.offerDescription != null) storeInformation.offerDescription = storeStock.offerDescription

        if (storeStock.offerPrice >= 0) storeInformation.offerPrice = storeStock.offerPrice

        if (storeStock.fullPrice > 0) storeInformation.fullPrice = storeStock.fullPrice

        if (storeStock.stock >= 0) storeInformation.stock = storeStock.stock.toLong()

        return auxList
    }

    fun deleteStoreInformation(itemSaved: Item, deleteOfferItem: DeleteOfferItem): Boolean {
        if (itemSaved.storeInformation != null && itemSaved.storeInformation.size > 0) {
            val auxList = itemSaved.storeInformation
            for (st in auxList) {
                st.offerPrice = 0.0
                st.offerText = null
                st.offerDescription = null
                st.fullPrice = deleteOfferItem.fullPrice
            }

            val itemToUpdate = OfyService.ofy().load().type(
                Item::class.java
            ).filter("id", itemSaved.id).first().now()
            itemToUpdate.storeInformation = auxList
            OfyService.ofy().save().entity(itemToUpdate).now()
            return true
        }
        return false
    }

    fun validateStockItem(
        item: Item?,
        nearbyStores: List<Int>,
        id: Int,
        isScanAndGo: Boolean
    ): Item {
        val validItem = getValidItem(item, id, isScanAndGo)
        val totalStockResponse = getTotalStockResponse(validItem.id, nearbyStores)
        validItem.totalStock = totalStockResponse.data.totalStock
        validateStock(validItem, isScanAndGo)
        adjustStockForScanAndGo(validItem, isScanAndGo)
        return validItem
    }

    /**
     * Get valid item from algolia
     */
    private fun getValidItem(item: Item?, id: Int, isScanAndGo: Boolean): Item {
        return if (isScanAndGo && item == null) {
            productsMethods.setFindInformationToAlgoliaByIdItemisScanAndGo(id.toString(), Constants.DEFAULT_STORE_CO)
        } else {
            item
        } ?: throw ConflictException(Constants.PRODUCT_NOT_EXISTS).also {
            logger.severe("Error getting item: item is null")
        }
    }

    /**
     * Get total stock response from shopping cart service
     */
    private fun getTotalStockResponse(itemId: Long, nearbyStores: List<Int>): TotalStockResponse {
        val totalStockRequest = TotalStockRequest(itemId, nearbyStores)
        return ShoppingCartService.getInstance().getTotalStock(totalStockRequest)
            .orElseThrow { ConflictException("Error retrieving total stock response") }
    }

    /**
     * if the item is not found in the stock, throw an exception
     */
    private fun validateStock(item: Item, isScanAndGo: Boolean) {
        if (!isScanAndGo && item.totalStock < 1) {
            logger.severe("No cuenta con stock el item " + item.itemId)
            throw ConflictException(Constants.PRODUCT_OUT_OF_STOCK+" productId: "+ item.itemId?.toString() ?: "");
        }
    }

    private fun adjustStockForScanAndGo(item: Item, isScanAndGo: Boolean) {
        if (isScanAndGo && item.totalStock < MIN_VALID_SCAN_AND_GO_STOCK) {
            item.totalStock = DEFAULT_SCAN_AND_GO_STOCK
        }
    }
}
