export interface CityStory {
 id:string; city:string; title:string; deck:string; updated:string
 image?:{src:string;alt:string;author:string;work:string;license:string;licenseUrl:string}
 sections:{title:string;text:string;source:number}[]
 observations:string[]
 sources:{title:string;url:string}[]
}
export const cityStories:CityStory[]=[
 {id:'beijing',city:'北京',title:'从一扇门，读懂一座院',deck:'胡同与院落之间，是从街巷进入居所的空间转换。以四合院为线索，观察传统住宅如何围绕一片露天庭院展开。',updated:'2026-11-16',
 image:{src:'/images/stories/beijing-courtyard.jpg',alt:'北京一处院落的建筑与庭院，具体宅院身份未核实',author:'Dudva',work:'https://commons.wikimedia.org/wiki/File:Courtyard_in_Beijing.jpg',license:'CC0 1.0',licenseUrl:'https://creativecommons.org/publicdomain/zero/1.0/'},
 sections:[{title:'围绕庭院展开的住宅',text:'北京市政府的四合院介绍将正房、倒座房与东西厢房围合庭院的布局作为这一传统住宅形式的核心。阅读院落，可以先看房屋与空地之间的关系，而不只是门头上的装饰。这里讨论的是历史住宅样本，不是所有北京居民的生活方式。',source:0},
 {title:'从胡同走进不同的院落',text:'北京市发布的修缮保护资料将街巷、胡同与四合院放在旧城保护语境中讨论。院落保存状况与空间格局并不相同，不能用一张整洁庭院照片推断任何具体住宅的现状。历史空间的阅读，也需要区分原有建筑、后来的改建和今天的使用。',source:1},
 {title:'气候不是一张建筑标签',text:'一处院落的实际日照、挡风和通风条件，应结合朝向、周边遮挡与使用方式现场观察。本文不把所有围合院落都描述为冬暖夏凉，也不把传统形式直接等同于现代居住舒适度。以下问题是编辑建议，不是气候性能评定。',source:-1}],
 observations:['分别在庭院和室内观察采光，确认窗前是否有遮挡。','询问公共院落、通道和房间的使用边界。','核对保温、采暖和潮湿痕迹，不凭建筑风格判断设施状况。'],sources:[{title:'北京市政府：北京传统民居——老北京四合院',url:'https://www.beijing.gov.cn/tsbj/sxym/202007/t20200713_1946380.html'},{title:'北京市住建委：旧城房修分类与保护',url:'https://zjw.beijing.gov.cn/bjjs/xxgk/zcjd2024/sx83/436477602/index.shtml'}]},
 {id:'shanghai',city:'上海',title:'门里门外，里弄里的城市生活',deck:'把视线从城市天际线移到一条里弄。石库门住宅的门头、合院格局与相连的住宅，提供了另一种观察上海历史居住空间的尺度。',updated:'2026-11-16',
 sections:[{title:'一座城市中的多种住宅形式',text:'上海黄浦区的官方资料将石库门营造技艺与江南民居的中轴线格局联系起来。石库门是理解上海历史住宅的一条线索，但不能代表全城住宅，也不能据此推断今天每户家庭的生活条件。',source:0},
 {title:'从建筑看到营造技艺',text:'国家级非物质文化遗产名录将石库门里弄建筑营造技艺列为传统技艺项目。这个视角提醒我们：历史住宅不仅是可观看的立面，也包含建造与维护的知识。阅读门、墙和住宅之间的联系，比给街区贴上统一的怀旧标签更有意义。',source:1},
 {title:'把空间感受变成具体问题',text:'看见相连的建筑与共同使用的通道，可以进一步观察门窗是否相对、日常出入是否相互影响。采光、通风与雨后排水需要针对具体房屋核对，不能从历史名称推导性能。这里不提供关于整座城市的噪声或潮湿统计。',source:-1}],
 observations:['沿实际出入路线走一遍，确认公共通道和消防通行情况。','观察相邻门窗，询问噪声、隐私及共用空间的规则。','核对厨卫设施、排水和维修责任，不把历史风貌等同于设施完好。'],sources:[{title:'上海市政府：黄浦区石库门营造技艺介绍',url:'https://www.shanghai.gov.cn/huangpu/index.html'},{title:'中国非物质文化遗产网：石库门里弄建筑营造技艺',url:'https://www.ihchina.cn/project_details/14731'}]},
 {id:'suzhou',city:'苏州',title:'在庭院里，容纳一片山水',deck:'水、石、植物与建筑，在有限空间中组织出丰富的观看关系。以苏州古典园林为样本，阅读居住空间与文化表达之间的联系。',updated:'2026-11-16',
 sections:[{title:'住宅中的自然缩影',text:'UNESCO 对苏州古典园林的介绍强调：这些私人园林在住宅范围内，以水、石、植物和建筑组织自然世界的缩影。它们是特定历史文化样本，不代表江南所有普通住宅，更不意味着苏州家庭都拥有园林。',source:0},
 {title:'水与庭院的文化意义',text:'这里的水体既是园林空间的一部分，也参与山水意境的表达。园林中的自然经过有意识的组织；观赏性的水景不等于城市水系，更不能直接作为住宅排水性能的证据。本文不从园林案例推导整座水乡的建筑规律。',source:0},
 {title:'从赏景回到日常使用',text:'参观园林时，可以把观景与行走分开观察：哪里适合停留，哪里承担通行，室内外怎样连接。租住场景则需要另行核对地面、墙角和门窗的实际状况；美学体验与居住维护是不同的问题。',source:-1}],
 observations:['留意雨后通道与门槛是否湿滑，询问排水维护安排。','检查靠近庭院的房间采光、通风及墙角状况。','分清可进入的公共景观、私人空间与实际租住范围。'],sources:[{title:'UNESCO：苏州古典园林',url:'https://whc.unesco.org/en/list/813/'}]}
]
