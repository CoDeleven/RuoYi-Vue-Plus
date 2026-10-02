-- 系统参数：Explorer Pass 页面显示开关（site.explorer.pass.enabled）
-- 管理端可在「系统管理 -> 参数设置」中编辑该参数：true 显示，false 隐藏（导航链接隐藏，页面重定向到首页）
insert into sys_config values(1761700000000000005, 'Explorer Pass页面开关', 'site.explorer.pass.enabled', 'true', 'Y', 1761000000000000103, 1761100000000000001, sysdate(), null, null, 'true:显示Explorer Pass页面, false:隐藏Explorer Pass页面');
