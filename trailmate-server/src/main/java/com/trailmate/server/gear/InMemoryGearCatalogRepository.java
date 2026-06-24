package com.trailmate.server.gear;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryGearCatalogRepository implements GearCatalogRepository {
    private final List<GearCatalogItem> items = List.of(
        new GearCatalogItem(
            "cat_rain_arcteryx_beta_lt",
            "雨衣（防水透气）",
            "Arc'teryx",
            "Beta LT Jacket",
            "Arc'teryx Beta LT Jacket",
            395,
            List.of("防水", "硬壳", "暴露路段"),
            "/gear-thumbnails/arcteryx-beta-lt.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_headlamp_bd_spot_400",
            "头灯",
            "Black Diamond",
            "Spot 400",
            "Black Diamond Spot 400",
            78,
            List.of("夜间", "备用电池", "安全"),
            "/gear-thumbnails/black-diamond-spot-400.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_shoes_salomon_x_ultra_4_gtx",
            "徒步鞋",
            "Salomon",
            "X Ultra 4 GTX",
            "Salomon X Ultra 4 GTX",
            760,
            List.of("防滑", "防水", "长距离"),
            "/gear-thumbnails/salomon-x-ultra-4-gtx.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_poles_leki_legacy_lite",
            "登山杖",
            "Leki",
            "Legacy Lite AS",
            "Leki Legacy Lite AS",
            510,
            List.of("长距离", "下坡", "稳定"),
            "/gear-thumbnails/leki-legacy-lite-as.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_insulation_montbell_plasma",
            "保温层（抓绒/羽绒）",
            "Montbell",
            "Plasma 1000 Down Jacket",
            "Montbell Plasma 1000 Down Jacket",
            130,
            List.of("保暖", "轻量", "高海拔"),
            "/gear-thumbnails/montbell-plasma-1000.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_water_source_hydrapak_2l",
            "备用水",
            "Hydrapak",
            "Seeker 2L",
            "Hydrapak Seeker 2L",
            76,
            List.of("补水", "长距离", "可压缩"),
            "/gear-thumbnails/hydrapak-seeker-2l.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_first_aid_adventure_medical_ultralight",
            "急救包",
            "Adventure Medical Kits",
            "Ultralight/Watertight .7",
            "Adventure Medical Kits Ultralight/Watertight .7",
            227,
            List.of("急救", "防水", "多人"),
            "/gear-thumbnails/adventure-medical-ultralight-7.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_power_nitecore_nb10000",
            "移动电源",
            "Nitecore",
            "NB10000 Gen 2",
            "Nitecore NB10000 Gen 2",
            150,
            List.of("充电", "轻量", "手机导航"),
            "/gear-thumbnails/nitecore-nb10000-gen2.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_navigation_garmin_etrex_se",
            "导航设备",
            "Garmin",
            "eTrex SE",
            "Garmin eTrex SE",
            156,
            List.of("备用导航", "长续航", "离线"),
            "/gear-thumbnails/garmin-etrex-se.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        ),
        new GearCatalogItem(
            "cat_pack_osprey_talon_22",
            "背包",
            "Osprey",
            "Talon 22",
            "Osprey Talon 22",
            900,
            List.of("日行", "背负", "水袋兼容"),
            "/gear-thumbnails/osprey-talon-22.png",
            "TrailMate hosted catalog thumbnail",
            "seed"
        )
    );

    @Override
    public List<String> listCategories() {
        return items.stream()
            .map(GearCatalogItem::category)
            .distinct()
            .sorted()
            .toList();
    }

    @Override
    public List<GearCatalogItem> search(String category, String query) {
        String normalizedCategory = normalize(category);
        String normalizedQuery = normalize(query);
        return items.stream()
            .filter(item -> normalizedCategory.isBlank() || normalize(item.category()).equals(normalizedCategory))
            .filter(item -> normalizedQuery.isBlank() || searchableText(item).contains(normalizedQuery))
            .sorted(Comparator.comparing(GearCatalogItem::displayName))
            .toList();
    }

    @Override
    public Optional<GearCatalogItem> findById(String catalogItemId) {
        return items.stream()
            .filter(item -> item.catalogItemId().equals(catalogItemId))
            .findFirst();
    }

    private String searchableText(GearCatalogItem item) {
        return normalize(String.join(
            " ",
            item.category(),
            item.brand(),
            item.model(),
            item.displayName(),
            String.join(" ", item.tags())
        ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
