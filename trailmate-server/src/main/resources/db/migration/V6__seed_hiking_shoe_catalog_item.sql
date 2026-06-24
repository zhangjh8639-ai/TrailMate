insert into gear_catalog_item (
    catalog_item_id, category, brand, model, display_name,
    weight_grams, tags_csv, image_url, image_attribution, source
) values (
    'cat_shoes_salomon_x_ultra_4_gtx',
    '徒步鞋',
    'Salomon',
    'X Ultra 4 GTX',
    'Salomon X Ultra 4 GTX',
    760,
    '防滑,防水,长距离',
    'https://cdn.trailmate.local/gear/salomon-x-ultra-4-gtx.png',
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
