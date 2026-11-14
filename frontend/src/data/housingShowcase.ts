export interface HousingStory {
 id: string; title: string; subtitle: string; introduction: string;
 advantages: string[]; considerations: string[]; checklist: string[];
 image: string; alt: string; imagePosition: string; imageWidth: number; imageHeight: number; photographer: string; source: string;
}
// Editorial references, not listings or legal classifications. Licenses: docs/design/assets-attribution.md.
export const housingStories: HousingStory[] = [
 { id:'apartment', title:'普通公寓', subtitle:'在熟悉的城市里，留一处自己的日常。', introduction:'公寓的体验不只由面积决定。朝向、通风、公共区域与周边环境，都值得在看房时亲自感受。这里的名称只是日常空间描述，不是法律分类。', advantages:['生活空间相对集中，日常动线容易观察。','可结合通勤与周边设施选择适合自己的生活半径。'], considerations:['邻里噪声、采光和物业维护因楼栋而异。','照片无法替代不同时段的实地观察。'], checklist:['试开窗户，观察通风与遮挡。','核对水电计费、维修责任和附属设备清单。','了解公共区域维护与访客管理方式。'], image:'/images/housing-apartment.webp', alt:'开放式起居空间，沙发、餐桌与木色厨房相连', imagePosition:'50% 45%', imageWidth:1400, imageHeight:788, photographer:'Christian', source:'https://unsplash.com/photos/B5EPQVhBD70' },
 { id:'duplex', title:'复式空间', subtitle:'让生活分层，也让选择更清楚。', introduction:'上下分层的空间能为不同活动留出边界。看房时，不妨实际走过楼梯、站到上层，体验层高、温度与动线。LOFT 与复式是常见描述，具体用途与条件需逐项核实。', advantages:['上下层可形成不同的活动区域。','空间组织有更多可能性。'], considerations:['楼梯会占用空间，也可能增加搬运难度。','上下层的采光、通风与温度可能不同。'], checklist:['核对房屋用途、计费方式与合同描述。','检查楼梯、扶手及夜间照明。','实测层高，确认家具搬入路径。'], image:'/images/housing-duplex.webp', alt:'木色楼梯连接上下层，金属扶手与上层休息空间', imagePosition:'50% 38%', imageWidth:1400, imageHeight:2100, photographer:'Caroline Badran', source:'https://unsplash.com/photos/vQc0ZDy7Wv8' },
 { id:'courtyard', title:'院落住宅', subtitle:'把一小片户外，放进生活的节奏。', introduction:'院落连接室内与户外，也带来更多需要照料的细节。比起想象中的安静，更值得关注实际的排水、防潮、日照与公共边界。', advantages:['可能拥有更直接的户外活动空间。','室内外之间可形成丰富的生活动线。'], considerations:['植物、排水与户外设施需要维护。','私密性、虫蚊和季节性潮湿需现场观察。'], checklist:['确认院落使用边界与维护责任。','观察排水、防潮和门窗状况。','了解周边通行、噪声与夜间照明。'], image:'/images/housing-courtyard.webp', alt:'法国乡间住宅前的砂石院落、树荫与石墙', imagePosition:'50% 48%', imageWidth:1400, imageHeight:933, photographer:'Erwan Hesry', source:'https://unsplash.com/photos/uj3VsJLhWE4' },
]
