# Explore 0.3 城市故事验收

> Version: 0.1
> Status: Draft（待内容与视觉评审）
> Implementation: Partial（阅读功能已验证；上海、苏州摄影待补）
> Project: v0.4 / Explore 0.3
> Baseline: 8292957d1157e65b3097065ad35766c11d769beb
> Branch: feat/explore-city-stories-v0.3
> Updated: 2026-11-16
> Source: 本地 Git、真实 Edge 回归、官方机构页面与 Commons 授权记录

## 分支与实现

初始实际为 feat/explore-map-v0.2，工作区干净。按用户授权从本地 feat/v0.4-microservices 创建独立故事分支，基线 8292957（Explore 0.1 合并）；没有继承或复制未合并的地图代码，没有本轮远程同步声明。

独立 cityStories 数据、可复用 CityStory 组件及懒加载 CityStoryPage 路由 /explore/stories/:cityId。原详情只增加三城市入口；原八城 JSON 完全保留，其他五城功能不变。上一篇/下一篇有边界、不循环，路由切换聚焦标题，返回城市目录；未知 ID 给出明确提示。认证守卫原样保留。

三城每篇包含引言、建筑/历史文化背景、编辑观察与租住问题、分段依据和来源列表。北京实景为已核验 CC0 摄影；上海、苏州为明确摄影待补状态。图片失败仍显示完整文字和出处。没有虚构房价、租金、年代或将代表案例泛化到全部居民。原目录地理气候资料不变；新故事不把未经证实的建筑气候关联写成事实。

## 验证结果

- npm --prefix frontend run build：vue-tsc 与生产构建通过；既有主包约 949 kB 警告保留，故事独立懒加载块约 9.8 kB。
- city-stories.mjs：三城 × 1440/1024/390/320px，正文、HTTPS 来源链接、北京照片成功加载、未授权图片占位、图片失败、未知 ID、上一/下一篇焦点、目录入口及八城市保留通过，无横向溢出或运行时错误。
- explore.mjs、ui-home.mjs、ui-home-polish.mjs 通过。首页认证用例采用模拟 HTTP，未声称新增真实后端联调。
- 外部事实与摄影许可通过浏览器资料核验；自动化仅验证来源链接的 href，不宣称每次外部网站网络请求均成功。
- 本轮未添加依赖、地图 SDK、安全代理或配置；没有 Java、SQL、JWT、Gateway、Nacos 变更。

## 截图与剩余事项

真实 Edge 截图在外层 04_版本交付/Explore-0.3/验收/2026-11-16/，story-{beijing,shanghai,suzhou}-{1440,1024,390,320}.png，每城均包含完整详情。截图不作为代码素材加入 Git。

待补：上海与苏州的授权摄影；更完整的建筑与地域气候关联研究；三城编辑/视觉评审。北京原图约 2.44 MiB，后续可另做有来源记录的图片优化。本轮保留原图，不冒充全部摄影交付完成。

合并地图分支时保留独立故事组件/数据/路由；仅 Explore 详情入口与文档导航可能需要人工合并。地图分支版权及生产问题不由本轮解决。

[内容与图片来源](../product/explore/city-sources.md) · [文档导航](../README.md)
