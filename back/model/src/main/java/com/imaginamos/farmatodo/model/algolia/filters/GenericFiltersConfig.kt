package com.imaginamos.farmatodo.model.algolia.filters

class GenericFiltersConfig {
    var filtersConfig: List<FiltersConfig>? = null
}

class FiltersConfig {
    var filterDescription: String? = null
    var configFilters: String? = null
    var itemsWithFilters: List<String>? = null
}
