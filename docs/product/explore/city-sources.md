# Explore 0.1 城市样本与来源

> Document version: 0.1
> Status: Draft（内容待进一步编辑评审）
> Implementation: Partial（八城市内容样本；无地图）
> Project version: v0.4 / Explore 0.1
> Baseline: ffc2bd0
> Updated: 2026-11-14
> Source: 官方政府、文物、文旅、气象与 UNESCO 页面；逐项见下表及 JSON

## 内容边界

八座城市按居住文化样本选择，不构成城市等级、宜居排名或全国覆盖。不展示租金、房价、人口、实时天气及数值气候统计。历史文化样本不等同于全市当代住宅，更不代表可租房源。黄山案例明确限定黟县西递、宏村；厦门限定鼓浪屿；哈尔滨限定道外历史街区。

所有页面内容为短篇事实整理，不复制整篇文章、照片或地图。页面内每城可展开来源与核验日期，数据保存在 [explore-cities.json](../../../frontend/public/data/explore-cities.json)。核验日期为 2026-11-14，并非原文发布日期。气象城市介绍含旧行政区划、人口与过时旅游叙述，本轮只采纳定性地理气候背景，不采用其旧统计或天气服务数据。后续应以新官方城市概况替换老页面。

## 来源对应

| 城市 | 地理、气候来源 | 文化、住宅样本来源 |
| --- | --- | --- |
| 北京 | [北京市政府概况](https://www.beijing.gov.cn/renwen/zt/) | [北京市文物局四合院资料](https://wwj.beijing.gov.cn/bjww/resource/cms/article/bjww_362762/10837017/2020073115111634980.pdf) |
| 上海 | [市政府主体功能区规划自然概况](https://www.shanghai.gov.cn/nw31011/20200820/0001-31011_34545.html) | [黄浦区石库门营造技艺](https://www.shanghai.gov.cn/huangpu/index.html) |
| 广州 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101280101.shtml) | [恩宁路保护利用](https://ghzyj.gz.gov.cn/sjb/yw/mcbh/content/post_9824299.html)；骑楼定义见 JSON 中广州市建设工程术语标准 |
| 苏州 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101190401.shtml) | [UNESCO 古典园林](https://whc.unesco.org/en/list/813/) |
| 厦门 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101230201.shtml) | [UNESCO 鼓浪屿](https://whc.unesco.org/en/list/1541/) |
| 丽江 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101291401.shtml) | [UNESCO 丽江古城](https://whc.unesco.org/en/list/811/) |
| 黄山 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101221001.shtml) | [UNESCO 西递、宏村](https://whc.unesco.org/en/list/1002/) |
| 哈尔滨 | [中国天气网定性背景](https://www.weather.com.cn/cityintro/101050101.shtml) | [黑龙江省文旅厅中华巴洛克街区](https://wlt.hlj.gov.cn/wlt/c114169/202404/c00_31731149.shtml) |

租住风险段落为 RentWise 编辑提出的现场核对问题，是由空间观察引出的教育提示，不是来源机构的法律意见，不断言具体城市或房屋存在某项缺陷。没有计算或捏造城市风险率。

## 图片与地图

不引入未经核准的城市摄影。页头复用 UI 0.1.1 已入库的 Erwan Hesry / Unsplash 庭院照片，明确标注“空间观察参考 · 非城市实景”，不绑定任何城市。来源、许可与哈希沿用[素材记录](../../design/assets-attribution.md)。未下载 UNESCO、政府页面或气象网站图片。

地图未接入，原因与比较见[技术报告](../../engineering/explore-map-evaluation.md)。没有行政区 GeoJSON 或私采坐标。

## 数据维护

`id` 是稳定的城市标识，搜索匹配城市中文名、无声调拼音、主题与住宅标题。添加城市需补全地理、气候、文化、住宅、编辑风险问题、来源和核验日期，并更新验收用例数量。当前 version 为 0.1；加载器校验字段、HTTPS 来源与 ID 唯一性，错误时可重试，不用虚构数据填补。需要未核实的字段时先完善来源，不能用此表给未知内容背书。

[原始 Explore 构想](overview.md) · [导航](../../README.md)

## Explore 0.3 三城市故事（2026-11-16）

状态 Draft；故事已实现，内容仍待编辑与视觉评审。独立数据见 [cityStories.ts](../../../frontend/src/data/cityStories.ts)，原八城市 JSON 未修改。故事不采用新的城市数值气候统计；建筑与气候的未证实因果关系不作事实断言，现场观察建议明确标为编辑建议。

- 北京：围合布局据[北京市政府四合院介绍](https://www.beijing.gov.cn/tsbj/sxym/202007/t20200713_1946380.html)；胡同、院落与保护语境据[北京市住建委修缮资料](https://zjw.beijing.gov.cn/bjjs/xxgk/zcjd2024/sx83/436477602/index.shtml)。未引用其最古老等泛化措辞，也不补填宅院年代。
- 上海：中轴线与江南民居联系据[市政府黄浦区介绍](https://www.shanghai.gov.cn/huangpu/index.html)；传统技艺身份据[国家非遗名录](https://www.ihchina.cn/project_details/14731)。不把所有里弄视作同一种住宅。
- 苏州：私人园林、住宅范围内的自然缩影、水石植物与文化表达据[UNESCO](https://whc.unesco.org/en/list/813/)。园林不是全城普通住宅样本，未据园林水景推断住宅排水或城市气候性能。

### 0.3 摄影授权记录

北京：[Courtyard in Beijing.jpg](https://commons.wikimedia.org/wiki/File:Courtyard_in_Beijing.jpg)，作者 Dudva，作者自摄，页面标注摄影日期 2025-03-18，[CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)，2026-11-16 核验。原图 2906×3875，2,561,949 字节，未做图像修改，按原比例展示，不裁切。SHA256：09BD5524B7F33B0A5CBE98694C920A8F425C9908DE13E8C96FFD3545CEB311B3。仓库文件：[beijing-courtyard.jpg](../../../frontend/public/images/stories/beijing-courtyard.jpg)。画面为门内通道/院落空间，未确认特定宅院历史身份，不对应正文中的具体保护案例。作者、作品及许可在页面显示。

初版上海、苏州摄影占位已于本轮替换为下列有明确许可的照片；未使用政府、博物馆或 UNESCO 网页照片。

### 0.3 内容与摄影完善（2026-11-16）

Version: 0.2；Status: Draft（新增内容待编辑审阅）；Implementation: Verified（展示与回归）；Project: v0.4 / Explore 0.3。以下为原稿补充，不改变原八城数据。

- 北京新增日照、朝向及传统家庭空间秩序，依据[北京大学韩茂莉讲座记录](https://news.pku.edu.cn/info/7921/2758261.htm)（2026-04-24）。限定为讲座描述的传统格局，不把历史家庭等级当成当代生活规则。
- 上海新增清水砖、青瓦、天井与起居炊事的空间序列，依据[中国非遗网项目说明](https://www.ihchina.cn/project_details/14731)；不采用其未经本轮复核的居民比例，不从布局直接推导气候性能。
- 苏州新增河街并行、临水建筑与粉墙黛瓦，依据[苏州市地方志办](https://dfzb.suzhou.gov.cn/dfzb/szdq/202509/594654cf1bd54131abe74ddbbd1eb684.shtml)（2025-09-24）。古典园林的山水表达仍据 UNESCO，明确与河街民居区分。未将佛寺材料结构挪用到所有住宅。
- 未找到足以支持三城住宅整体热工性能的测量资料，因此保温、潮湿、排水仍是现场观察问题，不写成已证实的城市建筑性能结论。

| 城市 / 文件 | 作者与作品来源 | 许可 / 使用范围 | 版本与边界 |
| --- | --- | --- | --- |
| 上海 / [shanghai-zhangyuan.jpg](../../../frontend/public/images/stories/shanghai-zhangyuan.jpg) | Livelikerw，自摄；[张园石库门山墙原页](https://commons.wikimedia.org/wiki/File:A_Type_of_Shikumen_Gable_in_ZhangYuan_Shanghai.JPG) | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)；允许复制、传播及改编，须署名和链接许可；改编图片须同许可或兼容许可发布，不暗示作者背书 | 2014-01-21 拍摄；采用 Commons 1280×851 缩略版，未另行裁切、调色；仅表现山墙局部，不代表修缮现状 |
| 苏州 / [suzhou-pingjiang.jpg](../../../frontend/public/images/stories/suzhou-pingjiang.jpg) | Nicolas Vollmer；[作品与授权记录](https://commons.wikimedia.org/wiki/File:Canal_dans_le_centre_ancien_de_Suzhou_(16999191119).jpg)，[作者 Flickr 原页](https://www.flickr.com/photos/nicolas_vollmer_photo/16999191119/) | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/)；允许复制、传播及改编，须署名和链接许可、说明改动，不暗示作者背书 | 2015-02-01 拍摄；采用 Commons 1280×853 缩略版，原版本即黑白，未另行裁切调色；平江河街，不是园林照片 |

核验日均为 2026-11-16。两张图片用于 RentWise 文化故事页面及随仓库分发，许可允许商业与非商业使用；第三方摄影各自保留上述许可，不因项目代码许可而改变。页面保留署名、作品页、许可链接及改动说明。苏州原始 Flickr 授权由 Commons 的 FlickreviewR 2 在 2019-01-12 记录核验；本轮核查 Commons 记录，不声称重新完成原平台独立版权审计。未采用同名检索中的平江花店候选照片。

下载版本地址：

- [上海 1280px](https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/A_Type_of_Shikumen_Gable_in_ZhangYuan_Shanghai.JPG/1280px-A_Type_of_Shikumen_Gable_in_ZhangYuan_Shanghai.JPG)
- [苏州 1280px](https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Canal_dans_le_centre_ancien_de_Suzhou_%2816999191119%29.jpg/1280px-Canal_dans_le_centre_ancien_de_Suzhou_%2816999191119%29.jpg)

- shanghai-zhangyuan.jpg：322,394 字节；SHA256 `ad856729938ccb3e6e255e1c86990c0c45ead3624778c5b4c4ecb3a54aa3b1ff`。
- suzhou-pingjiang.jpg：476,384 字节；SHA256 `660ac305ae804229167ff575d59aff3785c6af4785f34fbbb840740a94fd6734`。
