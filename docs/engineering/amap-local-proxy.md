# 高德本地代理与真实 SDK 接入

> Version: 0.2.1
> Status: Draft
> Implementation: Partial（本地接入，不是生产部署批准）
> Project: v0.4 / Explore 0.2
> Baseline: e7c90e3
> Updated: 2026-11-15
> Source: 高德 JS API 2.0 官方文档与本机联调

## 启动

在仓库根目录执行 `powershell.exe -NoProfile -File frontend/tools/start-map-proxy.ps1`。启动器优先使用当前进程变量，缺失时只读取用户保存的 AMAP_WEB_KEY、AMAP_SECURITY_JS_CODE，注入子进程，不修改持久环境。代理只监听 127.0.0.1:5175，与 Java/Gateway 无关。前端运行 `npm --prefix frontend run dev -- --host 127.0.0.1 --port 5174 --strictPort`。

Vite 将 /map-local 和 /_AMapService 转给此代理；现有 /api 代理不变。点击 Explore 的“打开交互地图”才加载 SDK。没有代理时目录仍可使用。生产构建不包含密钥，也不会自带本地代理；生产需单独部署同源服务和访问控制，不能直接把此开发代理公开到互联网。

浏览器 Web Key 本身是 SDK 的公开参数；安全密钥只由代理添加到固定的高德 HTTPS 上游请求。代理不打印请求、查询参数或上游错误，不转发重定向、上游响应头及含安全密钥的响应。只接受 GET、精确服务路径白名单、本机 Host 与允许 Origin。此实现不是任意 URL 代理，但仍仅适合本机开发，不是公网配额防滥用方案。

## 坐标与交互

Explore 0.2.1 使用已核验并固定到 `frontend/src/data/cityCoordinates.json` 的八城市行政区中心，日常打开页面不请求在线坐标。核验工具 `frontend/tools/verify-map-coordinates.mjs` 经真实高德 AMap.DistrictSearch、subdistrict=0、extensions=base 获取，校验返回 adcode 与数字坐标，只有八城市全部成功才写文件。编码：北京110000、上海310000、广州440100、苏州320500、厦门350200、丽江530700、黄山341000、哈尔滨230100。坐标为高德服务坐标（GCJ-02），不再转换；它们代表行政区中心，不代表某住宅或个人地址。2026-11-15 实测取得，JSON 自带来源、版本、日期与坐标系；不下载行政区边界。重新核验需启动代理与 Vite、具备 Playwright 环境后运行上述工具，审核差异再提交；不是自动刷新任务。

来源：[高德行政区查询](https://lbs.amap.com/api/javascript-api-v2/guide/services/district-search)，接口核验日期 2026-11-15。上线前仍需确认实际用途许可及域名设置；变量存在不等于商业授权已验证。

地图适配器复用 mapSession 生命周期、共享 selectCity；默认全国视角，列表/搜索更新后定位选中城市，地图按钮更新详情。使用原生可聚焦按钮提供等效键盘选择，原目录保持可用。地图标记 title 提供简短悬停提示，不是独立富内容预览卡。画布保留平台版权与审图信息，关闭滚轮缩放减少页面滚动冲突。

## 已发现并修正

- Codex 进程未继承变量，但当前用户环境存在；启动器安全读取。
- Web Key 首尾空白导致无效请求；代理在内存 trim，不改用户变量。
- serviceHost 缺协议导致相对路径错误；改为完整同源 URL。
- 全国视角上海/苏州文字遮挡；调整标签锚点。

[官方安全代理说明](https://lbs.amap.com/api/javascript-api-v2/guide/abc/jscode) · [验收](../design/explore-0.2-validation.md) · [导航](../README.md)

## 0.2.1 代理复核与边界

只允许 /v3/log/init（SDK 初始化）、/v4/map/styles（官方样式）、/v3/config/district（维护时核验坐标）；主机分别固定 restapi.amap.com 与样式专用 webapi.amap.com。未知路径、非 GET、非本机 Host、不允许的 Origin、绝对/双斜线目标和不合法 JSONP callback 被拒绝；用户提交的 key/jscode 被服务端覆盖。不接受任意上游 URL，不转发 Cookie、Authorization 或重定向。不打印上游异常消息。

旧 502 的确经过代理的统一错误响应，但旧版本将网络、超时及重定向错误合并，不能追溯证明具体传输根因。现在异常只返回 upstream_timeout 或 upstream_network_or_redirect 枚举，反射安全密钥的响应继续拒绝；不把成功率、Key 有效性或静态坐标改动当成网络根因证据。固定城市已消除在线查询这个运行时故障环节，SDK/底图仍依赖网络。

`node frontend/tests/map-proxy.mjs` 使用假的单元测试凭据与替身上游检查白名单、Host/Origin/方法、callback、覆盖凭据、禁止重定向选项、超时/网络分类、反射密钥拒绝；它不是平台授权或公网安全认证。127.0.0.1 监听约束保持，生产上线仍需独立部署与安全审查。
