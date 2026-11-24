# RentWise Homepage v0.1｜现行首页设计规格

> Version: 0.1
> Status: Approved（仅产品方向、五区结构与四业务布局；具体实施提案及视觉细节仍为Draft）
> Implementation: Partial（首轮五区首页已实现，Living/Compare仍为规划预览；待视觉评审）
> Project: RentWise v0.4 / Homepage v0.1
> Baseline: feat/explore-integrated-v0.4 / 9e55cda
> Owner: TBD
> Updated: 2026-11-22
> Source: 用户本轮已确认方向、当前Vue源码及既有产品/验收资料

## 唯一现行规格与产品边界

**Editorial Lifestyle × Interactive Exploration：用内容激发兴趣，再引导用户使用工具。** 此文件为后续首页改版唯一现行规格；[UI 0.1原规格](RentWise_UI_0.1_首页与设计系统_Codex开发规格_v0.1.md)仅保留历史实施与设计系统依据，发生首页结构冲突时以本文为准。不改写[Explore原始构想](../product/explore/overview.md)，不新建产品愿景。

采用杂志式内容首页、左右分栏首屏，以Explore、Living、Compare、Learn形成四业务布局。居住探索与租房风险教育是互补场景；不宣称提供房源挂牌、交易或法律意见。Living专题是内容方向，不能把历史Living Picks热点/商品跳转构想当成已开发模块。Compare具体比较对象、输入、算法和数据来源尚未定义，不默认等同房源比较或租金预测。

## 当前真实状态（源码检查，非本轮运行验收）

| 模块 | 已实现/集成状态 | 首页可承诺范围 |
| --- | --- | --- |
| 现有落地页 | UI 0.1/0.1.1已实现；Login.vue含真实认证表单、住宅展示、科普内容 | 新五区公共首页已独立实现，旧登录落地页仍保留认证用途 |
| Explore | 八城目录与详情已实现；地图0.2与三城故事0.3在本地集成分支整合、待评审，未据此宣称远程已发布 | 目录与北京/上海/苏州故事可作为入口；地图按需加载、失败回目录；窄屏版权与生产部署仍待确认 |
| Living | 规划中；仅有Living Picks构想，无独立页面/路由 | 展示策划中说明，专题正文与素材未批准前不提供阅读假链接 |
| Learn | 登录、诊断、画像、训练与阶段测评已实现，有历史验收 | 入口复用/dashboard及其认证守卫；不能将规划情境模拟器列为已实现 |
| Compare | 规划中；本轮路由未发现Compare，缺功能规格 | 仅“规划中”，不提供可操作比较按钮，不填虚构价格/结果 |

证据：[集成验收](explore-integrated-validation.md)、[UI 0.1验收](ui-0.1-validation.md)、[UI 0.1.1验收](ui-0.1.1-validation.md)、[实际路由](../../frontend/src/router/index.ts)。本地状态不等同GitHub上线状态。

## 五大区域与入口契约

以下为已确认结构的实施级草案；示例文案可编辑，不代表正式上线文案已批准。

| 区域 | 内容与层次 | 按钮及真实目标 | 手机布局与未完成处理 |
| --- | --- | --- | --- |
| 1 Hero左右分栏 | 顶部品牌/轻导航；左侧一个H1、品牌短述“读懂居住，也读懂选择”、简短引言与主行动；右侧一张精选住宅摄影及必要署名。具体左右内容分配为本规格建议，不等同已批准视觉稿 | 主“探索城市”→/explore；次“开始风险学习”→/dashboard。账号导航“登录”→/login、“注册”→/register | 单列按标题→引言→主行动→摄影顺序；登录为可见导航，不用大图挤走行动；无授权则显示明确摄影占位，不借用参考图 |
| 2 Explore精选城市摄影 | H2、简短引言、北京/上海/苏州三城编辑式图文条目；避免重复装饰卡片；照片只代表其具体场景 | 城市标题/“阅读故事”→/explore/stories/beijing、/shanghai、/suzhou（后两项均在/explore/stories/下）；“全部城市”→/explore | 纵向列表，图片、标题、说明相邻；其余五城不产生故事链接，可由目录查看基础档案；不在首页加载地图SDK |
| 3 Living居住灵感专题 | H2、一个主专题位及短导语；专题选题、正文和摄影仍待审定，非商品网格 | 当前无目标路由，不提供“查看专题”活跃按钮；导航Living可滚动到#living，区内明确“专题策划中” | 图文上下排列；没有可信内容时仅短说明与标注占位，不伪装为可读文章，不建立/living假路由 |
| 4 Compare与Learn工具入口 | H2、两个不同用途的工具介绍；内容阅读后自然过渡到行动，Learn为当前可用工具 | Learn“开始风险学习”→/dashboard；Compare只显示“规划中”，没有链接或伪禁用链接。导航Compare可滚动到#tools | 单列、Learn可用行动清楚；不让Compare规划入口与可用功能混淆。未登录由既有守卫去/login；登录成功进/dashboard |
| 5 Footer | 品牌简介、教育/文化内容边界、真实导航、摄影来源说明 | /explore、/dashboard、/login、/register及本页可用锚点；来源可链接已记录作者作品页 | 自然换行；没有隐私政策/条款页面时不放空链接或虚构法律文档，不填未经核实备案信息 |

顶部四业务导航：Explore→/explore，Living→#living，Compare→#tools，Learn→/dashboard；规划状态必须在落地区域明确。单个H1，分区H2。图片无须强制裁切；竖幅需利用旁注或独立比例，避免固定横幅填充造成大面积空白。

## 桌面与手机线框（结构，不是已批准视觉尺寸）

```text
桌面 1024 / 1440
┌ 品牌 | Explore Living Compare Learn | 登录/注册 ┐
│ Hero：H1 / 引言 / 主次行动 │ 精选住宅摄影 / 署名 │
├ Explore：城市引言 → 北京 / 上海 / 苏州图文条目 ┤
├ Living：主专题视觉 + 导语 / 当前“策划中”       ┤
├ 工具：Compare说明（规划） │ Learn说明与行动    ┤
└ Footer：品牌边界 / 有效导航 / 素材出处          ┘

手机 320 / 390
品牌 / 登录 → 四业务导航（允许换行）
Hero标题 → 引言 → 行动 → 摄影与署名
Explore标题 → 三城图文纵向排列 → 全部城市
Living标题 → 策划说明 / 摄影占位
工具标题 → Compare规划说明 → Learn行动
Footer纵向内容
```

不采用固定首屏高度裁切正文；平板按可读性切单栏。键盘顺序与视觉顺序一致，跳至正文、清晰focus、按钮触摸尺寸至少44px，图片有alt、装饰图空alt；尊重reduced-motion，不用自动轮播作为唯一阅读方式。精确断点、字号、色值与动效需后续视觉评审。

## 现有代码依赖与复用

| 现有资源 | 复用方式及限制 |
| --- | --- |
| [Login.vue](../../frontend/src/views/auth/Login.vue)、Register.vue | 保留真实登录注册、输入错误、重复提交防护；新内容首页不应复制认证代码 |
| [HousingShowcase.vue](../../frontend/src/components/housing/HousingShowcase.vue) | 可复用住宅知识数据/详情交互；不强制沿用旧轮播首屏，不把通用室内图冒充城市实景 |
| [CityStory.vue](../../frontend/src/components/stories/CityStory.vue)、[CityStoryPage.vue](../../frontend/src/views/explore/CityStoryPage.vue) | 复用独立阅读、署名、失败降级及篇章导航；首页只放精选引导，不嵌入全文 |
| [cityStories.ts](../../frontend/src/data/cityStories.ts) | 可作为三城标题/图片/来源唯一数据来源，避免复制后授权信息漂移 |
| [Explore.vue](../../frontend/src/views/explore/Explore.vue)、CityMap.vue | 复用公开目录与地图入口；SDK保持按需加载，Homepage不预加载地图或改变安全代理 |
| [exploreNavigation.ts](../../frontend/src/data/exploreNavigation.ts) | 保留地图到故事返回上下文，不增加认证信息持久化 |
| [global.css](../../frontend/src/styles/global.css) | 复用既有rw-container/rw-button、焦点及间距基础；现有暖白石墨黑只是过渡参考，不当作新色值已确认 |

实施前 `/` 重定向 `/dashboard`；首轮已改为独立公共Home.vue。已有token仍会从/login与/register转Dashboard；/login、/register及学习路由守卫保留。不要直接把Login.vue改造成五区页面从而破坏登录回归，也不要声称已有/home路由。

## 摄影与未决项

候选只来自[已有首页素材记录](assets-attribution.md)与[三城来源授权清单](../product/explore/city-sources.md)，或明确标注的占位资源。北京CC0、上海CC BY-SA 3.0、苏州CC BY 2.0须继续保留各自署名/许可/改动说明；候选授权成立不等于已选为正式首页摄影。更换裁切、衍生制作或使用范围需同步审核。不得从概念稿截取参考摄影；不能生成照片后声称真实城市记录。

尚未确认：精确字体及字体许可、最终配色、具体动效、正式摄影组合；Living选题内容与Compare功能定义；账号入口最终位置（公共根路由已获授权并实现）。无素材时使用明确占位，不制造内容/数据。

## 后续实施验收门槛

公共首页路由已获授权；首轮视觉细节供用户评审。保留认证、学习与地图/故事回归；覆盖320/390/1024/1440、Tab/Enter/焦点、图片失败、减弱运动、所有按钮目标及规划状态。验证首页无地图SDK预加载、无伪入口、摄影授权完整，TypeScript/构建/文档链接通过。首轮实现与实测范围以验收报告为准，规划功能不计为完成。

决策依据见[Homepage ADR](homepage-adr-001.md)。

## 2026-11-22 首轮实施追加

用户已授权公共根路由与五区开发，详见[首轮验收](homepage-v0.1-validation.md)。上文“尚未实施/待确认路由”为设计时点记录；当前根路由已指向独立Home.vue，登录及学习守卫保留，具体视觉仍待评审。


## Living v0.1 后续落地（2026-11-24）

首页Living导航与预览按钮现指向公共`/living`，不再仅跳本页锚点；三类编辑专题与独立阅读已实现，见[Living唯一规格](living-v0.1.md)。上文策划状态为首轮历史边界，Compare仍规划中。
