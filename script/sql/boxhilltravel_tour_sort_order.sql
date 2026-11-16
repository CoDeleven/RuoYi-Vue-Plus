-- ----------------------------------------------------------------------------
-- 线路展示排序 holidays_tour.sort_order
-- 数值越小越靠前；前台 Tour 列表默认（Relevance）按此字段排序。
-- 只需执行一次。
-- ----------------------------------------------------------------------------

ALTER TABLE `holidays_tour`
    ADD COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT '展示排序，数值越小越靠前' AFTER `status`,
    ADD KEY `idx_status_sort_order` (`status`, `sort_order`);

-- 按当前前台顺序（id 倒序）初始化为 1, 2, 3 ...，上线后网站显示顺序保持不变
UPDATE `holidays_tour` t
    JOIN (
        SELECT `id`, ROW_NUMBER() OVER (ORDER BY `id` DESC) AS `rn`
        FROM `holidays_tour`
    ) r ON r.`id` = t.`id`
SET t.`sort_order` = r.`rn`;
