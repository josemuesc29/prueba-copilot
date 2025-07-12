package com.imaginamos.farmatodo.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Clase encargada de exponer datos estructurados en Google
 */
public class ItemSeo implements Serializable {

    public final static String IN_STOCK = "http://schema.org/InStock";
    public final static String OUT_OF_STOCK = "http://schema.org/OutOfStock";

    @JsonProperty("@context")
    private String context = "http://schema.org/";
    @JsonProperty("@type")
    private String type = "Product";
    private String name;
    private String image;
    private String description;
    private String sku;
    private Offers offers;
    private AggregateRating aggregateRating;

    public ItemSeo() {
    }

    public ItemSeo(String name, String image, String description, String sku, Offers offers) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.sku = sku;
        this.offers = offers;
        //this.aggregateRating = new AggregateRating();
    }

    public AggregateRating getAggregateRating() {
        return aggregateRating;
    }

    public void setAggregateRating(AggregateRating aggregateRating) {
        this.aggregateRating = aggregateRating;
    }

    public String getContext() {
        return context;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Offers getOffers() {
        return offers;
    }

    public void setOffers(Offers offers) {
        this.offers = offers;
    }

    static class Offers {
        private final String TYPE_AGREGATE_OFFER ="AggregateOffer";
        private final String TYPE_OFFER ="Offer";

        @JsonProperty("@type")
        private String type = "Offer";
        private String priceCurrency = "COP";
        private String price;
        private String lowPrice;
        private String priceValidUntil;
        private String itemCondition = "http://schema.org/NewCondition";
        private String availability;
        private Seller seller;
        private String highPrice;

        /**
         * Constructor para ofertas de porcentaje
         * @param price
         * @param priceValidUntil
         * @param availability
         */
        public Offers(String price, String priceValidUntil, String availability) {
            this.type = TYPE_OFFER;
            this.price = price;
            this.priceValidUntil = priceValidUntil;
            this.availability = availability;
            this.seller = new Seller();
            this.highPrice = price;
        }

        /**
         * Constructor para Aggegator Offer
         * @param lowPrice
         * @param price
         * @param priceValidUntil
         * @param availability
         */
        public Offers(String lowPrice, String price, String priceValidUntil, String availability) {
            this.type = TYPE_AGREGATE_OFFER;
            this.price = price;
            this.priceValidUntil = priceValidUntil;
            this.availability = availability;
            this.seller = new Seller();
            this.lowPrice = lowPrice;
            this.highPrice = price;
        }

        public String getType() {
            return type;
        }

        public String getPriceCurrency() {
            return priceCurrency;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getPriceValidUntil() {
            return priceValidUntil;
        }

        public void setPriceValidUntil(String priceValidUntil) {
            this.priceValidUntil = priceValidUntil;
        }

        public String getItemCondition() {
            return itemCondition;
        }

        public String getAvailability() {
            return availability;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }

        public Seller getSeller() {
            return seller;
        }

        public String getLowPrice() {
            return lowPrice;
        }

        public void setLowPrice(String lowPrice) {
            this.lowPrice = lowPrice;
        }

        public String getHighPrice() { return highPrice; }

        public void setHighPrice(String highPrice) { this.highPrice = highPrice; }

        static class Seller {
            @JsonProperty("@type")
            private String type = "Organization";
            private String name ="Farmatodo";

            public String getType() {
                return type;
            }

            public String getName() {
                return name;
            }

        }
    }

    static class Brand {
        @JsonProperty("@type")
        private String type = "Thing";
        private String name ="Farmatodo";

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    static class AggregateRating{
        @JsonProperty("@type")
        private String type = "AggregateRating";
        private Double ratingValue = 4.4d;
        private int ratingCount= 89;

        public Double getRatingValue() {
            return ratingValue;
        }

        public void setRatingValue(Double ratingValue) {
            this.ratingValue = ratingValue;
        }

        public int getRatingCount() {
            return ratingCount;
        }

        public void setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
        }
    }

}
