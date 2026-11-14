-- ----------------------------------------------------------------------------
-- 线路图册 holidays_tour.images
-- 多张线路图片，存储为逗号分隔的 OSS id 列表（与 cover_image/map_image 同样的
-- 单字段存储方式，复用 plus-ui 的 ImageUpload 组件本身支持的多选上传能力）。
-- 只需执行一次。
-- ----------------------------------------------------------------------------

ALTER TABLE `holidays_tour`
    ADD COLUMN `images` varchar(2000) NULL COMMENT '线路图册(多图，OSS id 用逗号分隔)' AFTER `map_image`;
