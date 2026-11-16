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

北京：[Courtyard in Beijing.jpg](https://commons.wikimedia.org/wiki/File:Courtyard_in_Beijing.jpg)，作者 Dudva，作者自摄，页面标注摄影日期 2025-03-18，[CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)，2026-11-16 核验。原图 2906×3875，2,561,949 字节，未做图像修改，CSS contain 展示，不裁切。SHA256：09BD5524B7F33B0A5CBE98694C920A8F425C9908DE13E8C96FFD3545CEB311B3。仓库文件：[beijing-courtyard.jpg](../../../frontend/public/images/stories/beijing-courtyard.jpg)。画面为门内通道/院落空间，未确认特定宅院历史身份，不对应正文中的具体保护案例。作者、作品及许可在页面显示。

上海、苏州：尚未完成逐图授权核验，未下载或展示政府、博物馆及 UNESCO 照片。保留明确占位，不将北京或页头的通用摄影冒充本城实景。后续补图必须录入作者、作品原页、许可、核验日及必要范围说明。
