USE campus2hand_dev;

INSERT INTO products (id, user_id, title, description, price, category, item_condition, campus, status, created_at, updated_at) VALUES
(1, 3, '微积分（上册）', '九成新，只有前两章有少量笔记，其余全新。高等教育出版社。', 2500, 'textbook', 'like_new', '本部校区', 'on_sale', NOW(), NOW()),
(2, 3, '大学英语四级词汇书', '全新未使用，买多了出一本。', 1500, 'textbook', 'brand_new', '本部校区', 'on_sale', NOW(), NOW()),
(3, 4, 'iPad Air 5 64G', '去年买的，一直带壳贴膜使用，无划痕无维修。配件齐全。', 320000, 'digital', 'like_new', '东校区', 'on_sale', NOW(), NOW()),
(4, 4, '台灯 LED 护眼', '宿舍用不到了，可调节亮度和色温。', 3500, 'lifestyle', 'slightly_used', '东校区', 'on_sale', NOW(), NOW()),
(5, 2, '篮球 Spalding', '只用了一个学期，手感很好。', 6000, 'sports', 'slightly_used', '本部校区', 'on_sale', NOW(), NOW());

INSERT INTO product_images (id, product_id, url, sort_order) VALUES
(1, 1, 'https://picsum.photos/seed/p1/400/400', 0),
(2, 2, 'https://picsum.photos/seed/p2/400/400', 0),
(3, 3, 'https://picsum.photos/seed/p3/400/400', 0),
(4, 4, 'https://picsum.photos/seed/p4/400/400', 0),
(5, 5, 'https://picsum.photos/seed/p5/400/400', 0);