update gear_catalog_item
set image_url = case catalog_item_id
    when 'cat_rain_arcteryx_beta_lt' then '/gear-thumbnails/arcteryx-beta-lt.png'
    when 'cat_headlamp_bd_spot_400' then '/gear-thumbnails/black-diamond-spot-400.png'
    when 'cat_shoes_salomon_x_ultra_4_gtx' then '/gear-thumbnails/salomon-x-ultra-4-gtx.png'
    when 'cat_poles_leki_legacy_lite' then '/gear-thumbnails/leki-legacy-lite-as.png'
    when 'cat_insulation_montbell_plasma' then '/gear-thumbnails/montbell-plasma-1000.png'
    when 'cat_water_source_hydrapak_2l' then '/gear-thumbnails/hydrapak-seeker-2l.png'
    when 'cat_first_aid_adventure_medical_ultralight' then '/gear-thumbnails/adventure-medical-ultralight-7.png'
    when 'cat_power_nitecore_nb10000' then '/gear-thumbnails/nitecore-nb10000-gen2.png'
    when 'cat_navigation_garmin_etrex_se' then '/gear-thumbnails/garmin-etrex-se.png'
    when 'cat_pack_osprey_talon_22' then '/gear-thumbnails/osprey-talon-22.png'
    else image_url
end,
    image_attribution = 'TrailMate hosted catalog thumbnail',
    updated_at = now()
where catalog_item_id in (
    'cat_rain_arcteryx_beta_lt',
    'cat_headlamp_bd_spot_400',
    'cat_shoes_salomon_x_ultra_4_gtx',
    'cat_poles_leki_legacy_lite',
    'cat_insulation_montbell_plasma',
    'cat_water_source_hydrapak_2l',
    'cat_first_aid_adventure_medical_ultralight',
    'cat_power_nitecore_nb10000',
    'cat_navigation_garmin_etrex_se',
    'cat_pack_osprey_talon_22'
);
