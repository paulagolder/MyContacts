-- Made-up contacts for the tests, added to a new database (newdb.sql).

INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'tid', '', 'Smith  Alice', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'name', '', '{ fn:''Alice'' , sn:''Smith'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'tags', '', 'friends;', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'email', '', 'alice@example.com', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'address', '', '{ street:''1 High Street'' , city:''Testtown'' , postcode:''TT1 1AA'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'phone', '', '+441234567890', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'mobile', '', '+447700900123', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'birthday', '', '{ year:''1950'' , month:''3'' , day:''2'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'email', 'old', 'alice@old.example.com', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'memberof', '000001', '000001', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'hasmember', '000003', '000003', '2026-01-01 12:00:00');
-- related links: Alice and Carol are friends both ways; Bob says Alice is his
-- sister, with no link back, so Alice's Edit page shows it under ex-Related
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'related', 'friend', '000003', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'related', 'friend', '000001', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'related', 'sister', '000001', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'tid', '', 'O''Brien  Bob', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'name', '', '{ fn:''Bob'' , sn:"O''Brien" ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'tags', '', 'family;', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'tid', '', 'Jones  Carol', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'name', '', '{ fn:''Carol'' , sn:''Jones'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'tags', '', 'work;', '2026-01-01 12:00:00');
