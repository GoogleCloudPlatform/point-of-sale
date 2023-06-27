CREATE TABLE items (
  item_id STRING(36) NOT NULL,
  name STRING(1024),
  type STRING(1024),
  price NUMERIC,
  imageUrl STRING(1024),
  quantity INT64,
  labels ARRAY<STRING(1024)>,
  version INT64,
) PRIMARY KEY(item_id);

CREATE TABLE payments (
  payment_id STRING(36) NOT NULL,
  unitList STRING(1024),
  type STRING(1024),
  paidAmount NUMERIC,
  version INT64,
) PRIMARY KEY(payment_id);

CREATE TABLE payment_units (
  payment_id STRING(36) NOT NULL,
  payment_unit_id STRING(36) NOT NULL,
  item_id STRING(36) NOT NULL,
  name STRING(1024),
  quantity NUMERIC,
  totalcost NUMERIC,
  version INT64,
) PRIMARY KEY(payment_id, payment_unit_id),
  INTERLEAVE IN PARENT payments ON DELETE NO ACTION;
