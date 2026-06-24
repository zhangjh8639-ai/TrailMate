# TrailMate 装备目录数据库

## 表

| 表名 | 说明 |
| --- | --- |
| `gear_catalog_item` | 服务端维护的品牌装备目录，供手机端按路线需求只读匹配 |

## `gear_catalog_item`

关键字段：

| 字段 | 说明 |
| --- | --- |
| `catalog_item_id` | 稳定目录 ID，例如 `cat_headlamp_bd_spot_400` |
| `category` | 路线装备建议类别，例如 `头灯` |
| `brand` | 品牌 |
| `model` | 型号 |
| `display_name` | App 展示名 |
| `weight_grams` | 克重，可为空 |
| `tags_csv` | 适用标签，当前用逗号分隔 |
| `image_url` | 装备图片地址 |
| `image_attribution` | 图片来源或授权备注 |
| `source` | `seed`、`admin`、`import` 等来源 |
| `active` | 是否可被用户选择 |

图片文件不存入 PostgreSQL。建议放在 OSS、七牛云、Cloudflare R2、S3 或 CDN，数据库只保存 URL。

## 已废弃的个人库存表

早期迁移曾创建过 `user_gear_inventory`，但当前产品方向不再维护用户库存关系。

生产主路径只从 `gear_catalog_item` 获取品牌、型号、重量、标签和缩略图，手机端只展示本次路线的只读匹配结果。
旧库升级时由 `V9__drop_personal_gear_inventory.sql` 删除历史库存表；不要新增库存 API、保存库存动作、可用状态开关或自定义装备表单。

## 维护目录示例

```sql
insert into gear_catalog_item (
  catalog_item_id, category, brand, model, display_name,
  weight_grams, tags_csv, image_url, image_attribution, source
) values (
  'cat_headlamp_nitecore_nu25',
  '头灯',
  'Nitecore',
  'NU25 UL',
  'Nitecore NU25 UL',
  45,
  '夜间,轻量,备用电池',
  'https://cdn.example.com/trailmate/gear/nitecore-nu25-ul.png',
  'brand/product image source',
  'admin'
);
```
