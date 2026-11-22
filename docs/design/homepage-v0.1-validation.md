# Homepage v0.1 首轮实施与验收

> Version: 0.1
> Status: Draft（待首轮视觉评审）
> Implementation: Partial（公共首页已实现；Living/Compare仅预览；测试范围见下）
> Project: RentWise v0.4 / Homepage v0.1
> Baseline: 72841ee（设计文档保存提交）；继承集成9e55cda
> Branch: feat/homepage-v0.1
> Owner: TBD
> Updated: 2026-11-22
> Source: 本地源码、Edge浏览器、生产构建与既有素材授权记录

## 基线与范围

原集成分支工作区只有4份设计文档改动。先提交72841ee保存它们，再从该完整集成基线创建首页分支；不回退到缺地图/故事的开发基线。未推送或创建PR。

新增独立Home.vue与可复用EditorialPhoto.vue；根路由改为公共懒加载首页。登录页、注册页、认证守卫、Pinia/API及登录后Dashboard跳转均未修改。没有引入UI框架或新依赖、地图SDK、后端/数据库/微服务变更。

五区：Hero左文右图；三城错落编辑式摄影/独立故事入口；Living授权室内参考与策划标识；Compare规划说明与Learn真实Dashboard入口；品牌Footer。手机单列。Living/Compare不提供假路由，导航指向本页介绍。保留图片署名、许可、失败反馈与无障碍跳转。

## 素材与性能

Hero使用Erwan Hesry乡间住宅照片，Living使用Christian室内照片，来源与许可沿用[素材记录](assets-attribution.md)。三城照片、作者、许可和改动信息直接读取[cityStories.ts](../../frontend/src/data/cityStories.ts)，详见[城市来源](../product/explore/city-sources.md)。没有下载新摄影、生成实景或采用概念稿参考图；等比展示，不另行裁切/调色。

复用系统字体、暖白石墨色作为首轮可评审方案，不代表最终视觉参数已批准。静态数据与阅读页面组件分离：首页加载故事摘要数据，不加载CityStoryPage或地图SDK；下方图片lazy。生产Home JS约7.85kB，故事共享数据约8.48kB；既有主包约950kB警告仍在。北京原图约2.44MiB，未来可单独优化，不隐瞒媒体开销。

## 本轮验证

- npm --prefix frontend run build：TypeScript与生产构建通过。首次普通find结果的模板类型推断报错，改为computed后通过，未跳过类型检查。
- homepage.mjs：320/390/1024/1440宽度，无水平溢出，五区、授权摄影加载、规划入口、三城故事/返回、Explore八城、Learn跳登录、Dashboard保护、带测试token的公共首页、跳至正文焦点、图片失败、减少运动设置通过。首页请求中未出现amap/CityMap/amapAdapter/CityStoryPage。
- ui-home与ui-home-polish：既有登录页/住宅轮播/表单校验/重复提交/认证守卫及响应式回归通过。使用模拟HTTP验证业务契约，不把它当成真实MySQL后端联调。
- Explore八城、三城故事四档、8项画像契约、地图准备状态机及代理安全回归均通过。地图/HTTP替身测试不等同真实SDK或后端联调。本轮未重新调试地图版权。

截图：外层 `04_版本交付/Homepage-v0.1/2026-11-22/`，home-{1440,1024,390,320}-full.png及对应-hero.png。截图由真实Edge渲染，非设计效果图。

## 遗留与评审范围

Living专题、Compare算法/API/CMS均未实现；精确字体、色值和正式摄影选片待评审。新首页公开展示不代表地图公网授权/代理安全或窄屏版权问题已解决。首页不加载地图，不采用forceWebGL或版权绕过。认证真实后端链路沿用历史结果，本轮未新建账户或写学习数据。

[现行设计规格](homepage-v0.1.md) · [ADR](homepage-adr-001.md)

文档链接与git diff --check通过。本轮实现变更8文件：Home.vue、EditorialPhoto.vue、router/index.ts、homepage.mjs、现行规格、ADR、此验收报告及docs/README.md。未修改登录/注册/Pinia/学习页与地图适配器。
