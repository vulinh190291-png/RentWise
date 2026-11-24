export interface CityStory {
 id:string; city:string; title:string; deck:string; updated:string
 image?:{src:string;alt:string;author:string;work:string;license:string;licenseUrl:string;width:number;height:number;caption:string;changes:string}
 sections:{title:string;text:string;source:number}[]
 observations:string[]
 sources:{title:string;url:string}[]
}
export const cityStories:CityStory[]=[
 {id:'beijing',city:'北京',title:'从一扇门，读懂一座院',deck:'胡同与院落之间，是从街巷进入居所的空间转换。以四合院为线索，观察传统住宅如何围绕一片露天庭院展开。',updated:'2026-11-16',
 image:{width:2906,height:3875,caption:'沿门内通道看向院落：这是一处空间记录，具体宅院身份未核实，不对应正文的保护案例。摄影日期 2025-03-18。',changes:'原图，未裁切或调色。',src:'/images/stories/beijing-courtyard.jpg',alt:'北京一处院落的建筑与庭院，具体宅院身份未核实',author:'Dudva',work:'https://commons.wikimedia.org/wiki/File:Courtyard_in_Beijing.jpg',license:'CC0 1.0',licenseUrl:'https://creativecommons.org/publicdomain/zero/1.0/'},
 sections:[{title:'围绕庭院展开的住宅',text:'北京市政府的四合院介绍将正房、倒座房与东西厢房围合庭院的布局作为这一传统住宅形式的核心。阅读院落，可以先看房屋与空地之间的关系，而不只是门头上的装饰。这里讨论的是历史住宅样本，不是所有北京居民的生活方式。',source:0},
 {title:'从胡同走进不同的院落',text:'北京市发布的修缮保护资料将街巷、胡同与四合院放在旧城保护语境中讨论。院落保存状况与空间格局并不相同，不能用一张整洁庭院照片推断任何具体住宅的现状。历史空间的阅读，也需要区分原有建筑、后来的改建和今天的使用。',source:1},
 {title:'日照、朝向与家庭秩序',text:'北京大学刊载的韩茂莉讲座记录，将传统四合院正房朝南与日照需求联系起来，也说明内外院分隔、长幼分居不同房屋的空间秩序。朝向既回应光照，也与历史家庭生活组织相遇。这是讲座讨论的传统格局，不是今日所有院落的使用规则，更不等于保温、通风性能已经得到验证。',source:2},
 {title:'气候不是一张建筑标签',text:'一处院落的实际日照、挡风和通风条件，应结合朝向、周边遮挡与使用方式现场观察。本文不把所有围合院落都描述为冬暖夏凉，也不把传统形式直接等同于现代居住舒适度。以下问题是编辑建议，不是气候性能评定。',source:-1}],
 observations:['分别在庭院和室内观察采光，确认窗前是否有遮挡。','询问公共院落、通道和房间的使用边界。','核对保温、采暖和潮湿痕迹，不凭建筑风格判断设施状况。'],sources:[{title:'北京市政府：北京传统民居——老北京四合院',url:'https://www.beijing.gov.cn/tsbj/sxym/202007/t20200713_1946380.html'},{title:'北京市住建委：旧城房修分类与保护',url:'https://zjw.beijing.gov.cn/bjjs/xxgk/zcjd2024/sx83/436477602/index.shtml'},{title:'北京大学：韩茂莉讲授四合院与四合院文化（2026-04-24）',url:'https://news.pku.edu.cn/info/7921/2758261.htm'}]},
 {id:'shanghai',city:'上海',title:'门里门外，里弄里的城市生活',deck:'把视线从城市天际线移到一条里弄。石库门住宅的门头、合院格局与相连的住宅，提供了另一种观察上海历史居住空间的尺度。',updated:'2026-11-16',
 image:{src:'/images/stories/shanghai-zhangyuan.jpg',width:1280,height:851,alt:'上海张园一处石库门山墙，砖墙、窗与装饰细部',author:'Livelikerw',work:'https://commons.wikimedia.org/wiki/File:A_Type_of_Shikumen_Gable_in_ZhangYuan_Shanghai.JPG',license:'CC BY-SA 3.0',licenseUrl:'https://creativecommons.org/licenses/by-sa/3.0/',caption:'张园石库门山墙的局部，摄于 2014-01-21；记录当时外观，不代表修缮后的现状或整个里弄布局。',changes:'采用 Wikimedia 1280px 等比缩略版；未另行裁切或调色。'},
 sections:[{title:'一座城市中的多种住宅形式',text:'上海黄浦区的官方资料将石库门营造技艺与江南民居的中轴线格局联系起来。石库门是理解上海历史住宅的一条线索，但不能代表全城住宅，也不能据此推断今天每户家庭的生活条件。',source:0},
 {title:'从建筑看到营造技艺',text:'国家级非物质文化遗产名录将石库门里弄建筑营造技艺列为传统技艺项目。这个视角提醒我们：历史住宅不仅是可观看的立面，也包含建造与维护的知识。阅读门、墙和住宅之间的联系，比给街区贴上统一的怀旧标签更有意义。',source:1},
 {title:'从砖墙青瓦走进天井',text:'国家非遗项目资料描述了清水砖外墙与青瓦屋顶，并按中轴顺序介绍门、天井、客堂、厢房和厨房。材料构成街巷可见的表面，空间序列则把入口、起居与炊事组织在紧凑的住宅内。天井提示我们观察室内外联系，但资料没有提供热工测量，不能据此断言所有石库门都适应湿热或天然凉爽。',source:1},
 {title:'把空间感受变成具体问题',text:'看见相连的建筑与共同使用的通道，可以进一步观察门窗是否相对、日常出入是否相互影响。采光、通风与雨后排水需要针对具体房屋核对，不能从历史名称推导性能。这里不提供关于整座城市的噪声或潮湿统计。',source:-1}],
 observations:['沿实际出入路线走一遍，确认公共通道和消防通行情况。','观察相邻门窗，询问噪声、隐私及共用空间的规则。','核对厨卫设施、排水和维修责任，不把历史风貌等同于设施完好。'],sources:[{title:'上海市政府：黄浦区石库门营造技艺介绍',url:'https://www.shanghai.gov.cn/huangpu/index.html'},{title:'中国非物质文化遗产网：石库门里弄建筑营造技艺',url:'https://www.ihchina.cn/project_details/14731'}]},
 {id:'suzhou',city:'苏州',title:'在庭院里，容纳一片山水',deck:'先沿河街观察临水建筑，再进入私家园林的山水世界。区分城市日常空间与特定宅园样本，阅读江南居住文化的不同尺度。',updated:'2026-11-16',
 image:{src:'/images/stories/suzhou-pingjiang.jpg',width:1280,height:853,alt:'苏州平江历史街区的河道、桥与临水建筑，黑白摄影',author:'Nicolas Vollmer',work:'https://commons.wikimedia.org/wiki/File:Canal_dans_le_centre_ancien_de_Suzhou_(16999191119).jpg',license:'CC BY 2.0',licenseUrl:'https://creativecommons.org/licenses/by/2.0/',caption:'平江历史街区河道与沿岸建筑，摄于 2015-02-01。这是河街空间，不是古典园林照片；不能据照片确认每栋建筑的年代与用途。',changes:'采用 Wikimedia 1280px 等比缩略版；黑白为来源版本，未另行裁切或调色。'},
 sections:[{title:'河街空间与园林，是两个尺度',text:'苏州地方志资料借《平江图》说明河道与街道并行、建筑临水的城市格局，并描述粉墙黛瓦的风貌。河街关系提供交通与建筑布局的地理线索；私家园林则是在特定宅园内经营山水。两者同处苏州，却不能互相替代，更不能把园林当成普通家庭住宅的标准。',source:1},
 {title:'住宅中的自然缩影',text:'UNESCO 对苏州古典园林的介绍强调：这些私人园林在住宅范围内，以水、石、植物和建筑组织自然世界的缩影。它们是特定历史文化样本，不代表江南所有普通住宅，更不意味着苏州家庭都拥有园林。',source:0},
 {title:'水与庭院的文化意义',text:'这里的水体既是园林空间的一部分，也参与山水意境的表达。园林中的自然经过有意识的组织；观赏性的水景不等于城市水系，更不能直接作为住宅排水性能的证据。本文不从园林案例推导整座水乡的建筑规律。',source:0},
 {title:'从赏景回到日常使用',text:'参观园林时，可以把观景与行走分开观察：哪里适合停留，哪里承担通行，室内外怎样连接。租住场景则需要另行核对地面、墙角和门窗的实际状况；美学体验与居住维护是不同的问题。',source:-1}],
 observations:['留意雨后通道与门槛是否湿滑，询问排水维护安排。','检查靠近庭院的房间采光、通风及墙角状况。','分清可进入的公共景观、私人空间与实际租住范围。'],sources:[{title:'UNESCO：苏州古典园林',url:'https://whc.unesco.org/en/list/813/'},{title:'苏州市地方志办：苏州古建筑（2025-09-24）',url:'https://dfzb.suzhou.gov.cn/dfzb/szdq/202509/594654cf1bd54131abe74ddbbd1eb684.shtml'}]}
]
