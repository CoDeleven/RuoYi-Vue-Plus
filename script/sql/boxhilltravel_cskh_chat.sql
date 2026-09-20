-- 系统参数：CSKH 在线客服 / Live Chat 开关（site.cskh.chat.enabled）
-- 管理端可在「系统管理 -> 参数设置」中编辑该参数：true 开启，false 关闭
insert into sys_config values(1761700000000000004, 'CSKH在线客服开关', 'site.cskh.chat.enabled', 'true', 'Y', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'true:开启在线客服, false:关闭在线客服');