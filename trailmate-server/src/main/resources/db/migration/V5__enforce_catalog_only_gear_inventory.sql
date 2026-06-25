update user_gear_inventory
set deleted_at = coalesce(deleted_at, now()),
    updated_at = now()
where deleted_at is null
  and (
      custom = true
      or catalog_item_id is null
  );

alter table user_gear_inventory
    add constraint chk_user_gear_inventory_catalog_only
    check (
        deleted_at is not null
        or (
            custom = false
            and catalog_item_id is not null
        )
    );
