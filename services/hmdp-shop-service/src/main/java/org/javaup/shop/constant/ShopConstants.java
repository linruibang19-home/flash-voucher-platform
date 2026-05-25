package org.javaup.shop.constant;

public final class ShopConstants {

    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 10;
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final String CACHE_SHOP_NULL_KEY = "cache:shop:null:";
    public static final long CACHE_SHOP_TTL_MINUTES = 30L;
    public static final long CACHE_NULL_TTL_MINUTES = 2L;
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String SHOP_BLOOM_FILTER = "shop";

    private ShopConstants() {
    }
}
