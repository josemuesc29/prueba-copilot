package com.imaginamos.farmatodo.model.home

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia
import com.imaginamos.farmatodo.model.customer.CustomerOnlyData
import com.imaginamos.farmatodo.model.provider.LandingPagesRequest
import com.imaginamos.farmatodo.model.provider.ProviderResponse

class HomeInfoConfig {
    var homeConfigAlgolia: HomeConfigAlgolia? = null
    var customerOnlyData: CustomerOnlyData? = null
    var idStoreGroup: Int? = 0
    var homeRequest: HomeRequest? = null
    var landingPagesRequest: LandingPagesRequest? = null
    var providerResponse: ProviderResponse? = null
    var asyncBannersDTFRes : BannersDTFRes? = null
    var asyncBannersDTFResponseMinLeft: BannersDTFRes? = null
    var carrouselItemListAsync: ItemCarrouselAsync? = null

    fun isValid(): Boolean {
        return homeConfigAlgolia != null && customerOnlyData !=null && idStoreGroup != null
                && homeRequest != null
    }

    fun isValidLandingPages(): Boolean {
        return homeConfigAlgolia != null && customerOnlyData !=null && idStoreGroup != null
                && landingPagesRequest != null && providerResponse != null
    }
}