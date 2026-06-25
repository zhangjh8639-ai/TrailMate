insert into gear_catalog_item (
    catalog_item_id, category, brand, model, display_name,
    weight_grams, tags_csv, image_url, image_attribution, source
) values
(
    'cat_first_aid_adventure_medical_ultralight',
    '急救包',
    'Adventure Medical Kits',
    'Ultralight/Watertight .7',
    'Adventure Medical Kits Ultralight/Watertight .7',
    227,
    '急救,防水,多人',
    'https://cdn.trailmate.local/gear/adventure-medical-ultralight-7.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_power_nitecore_nb10000',
    '移动电源',
    'Nitecore',
    'NB10000 Gen 2',
    'Nitecore NB10000 Gen 2',
    150,
    '充电,轻量,手机导航',
    'https://cdn.trailmate.local/gear/nitecore-nb10000-gen2.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_navigation_garmin_etrex_se',
    '导航设备',
    'Garmin',
    'eTrex SE',
    'Garmin eTrex SE',
    156,
    '备用导航,长续航,离线',
    'https://cdn.trailmate.local/gear/garmin-etrex-se.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_pack_osprey_talon_22',
    '背包',
    'Osprey',
    'Talon 22',
    'Osprey Talon 22',
    900,
    '日行,背负,水袋兼容',
    'https://cdn.trailmate.local/gear/osprey-talon-22.png',
    'TrailMate catalog seed image',
    'seed'
)
on conflict (catalog_item_id) do update set
    category = excluded.category,
    brand = excluded.brand,
    model = excluded.model,
    display_name = excluded.display_name,
    weight_grams = excluded.weight_grams,
    tags_csv = excluded.tags_csv,
    image_url = excluded.image_url,
    image_attribution = excluded.image_attribution,
    source = excluded.source,
    active = true,
    updated_at = now();
