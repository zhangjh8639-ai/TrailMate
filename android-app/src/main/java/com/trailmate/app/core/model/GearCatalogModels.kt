package com.trailmate.app.core.model

data class GearCatalogItem(
    val catalogItemId: String,
    val category: String,
    val brand: String,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val tags: List<String>,
    val imageUrl: String? = null,
    val imageAttribution: String? = null,
    val source: String
)

object TrailMateGearCatalogPreviewData {
    val items = listOf(
        GearCatalogItem(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = listOf("防水", "硬壳", "暴露路段"),
            imageUrl = "/gear-thumbnails/arcteryx-beta-lt.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_headlamp_bd_spot_400",
            category = "头灯",
            brand = "Black Diamond",
            model = "Spot 400",
            displayName = "Black Diamond Spot 400",
            weightGrams = 78,
            tags = listOf("夜间", "备用电池", "安全"),
            imageUrl = "/gear-thumbnails/black-diamond-spot-400.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_shoes_salomon_x_ultra_4_gtx",
            category = "徒步鞋",
            brand = "Salomon",
            model = "X Ultra 4 GTX",
            displayName = "Salomon X Ultra 4 GTX",
            weightGrams = 760,
            tags = listOf("防滑", "防水", "长距离"),
            imageUrl = "/gear-thumbnails/salomon-x-ultra-4-gtx.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_poles_leki_legacy_lite",
            category = "登山杖",
            brand = "Leki",
            model = "Legacy Lite AS",
            displayName = "Leki Legacy Lite AS",
            weightGrams = 510,
            tags = listOf("长距离", "下坡", "稳定"),
            imageUrl = "/gear-thumbnails/leki-legacy-lite-as.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_insulation_montbell_plasma",
            category = "保温层（抓绒/羽绒）",
            brand = "Montbell",
            model = "Plasma 1000 Down Jacket",
            displayName = "Montbell Plasma 1000 Down Jacket",
            weightGrams = 130,
            tags = listOf("保暖", "轻量", "高海拔"),
            imageUrl = "/gear-thumbnails/montbell-plasma-1000.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_water_source_hydrapak_2l",
            category = "备用水",
            brand = "Hydrapak",
            model = "Seeker 2L",
            displayName = "Hydrapak Seeker 2L",
            weightGrams = 76,
            tags = listOf("补水", "长距离", "可压缩"),
            imageUrl = "/gear-thumbnails/hydrapak-seeker-2l.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_first_aid_adventure_medical_ultralight",
            category = "急救包",
            brand = "Adventure Medical Kits",
            model = "Ultralight/Watertight .7",
            displayName = "Adventure Medical Kits Ultralight/Watertight .7",
            weightGrams = 227,
            tags = listOf("急救", "防水", "多人"),
            imageUrl = "/gear-thumbnails/adventure-medical-ultralight-7.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_power_nitecore_nb10000",
            category = "移动电源",
            brand = "Nitecore",
            model = "NB10000 Gen 2",
            displayName = "Nitecore NB10000 Gen 2",
            weightGrams = 150,
            tags = listOf("充电", "轻量", "手机导航"),
            imageUrl = "/gear-thumbnails/nitecore-nb10000-gen2.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_navigation_garmin_etrex_se",
            category = "导航设备",
            brand = "Garmin",
            model = "eTrex SE",
            displayName = "Garmin eTrex SE",
            weightGrams = 156,
            tags = listOf("备用导航", "长续航", "离线"),
            imageUrl = "/gear-thumbnails/garmin-etrex-se.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        ),
        GearCatalogItem(
            catalogItemId = "cat_pack_osprey_talon_22",
            category = "背包",
            brand = "Osprey",
            model = "Talon 22",
            displayName = "Osprey Talon 22",
            weightGrams = 900,
            tags = listOf("日行", "背负", "水袋兼容"),
            imageUrl = "/gear-thumbnails/osprey-talon-22.png",
            imageAttribution = "TrailMate hosted catalog thumbnail",
            source = "seed"
        )
    )
}
