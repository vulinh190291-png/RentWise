import {housingStories} from './housingShowcase'
export const livingCategories = ['居住空间', '生活方式', '居住好物'] as const
export type LivingCategory = typeof livingCategories[number]
export interface LivingArticle {
 slug: string; category: LivingCategory; title: string; deck: string; featured: boolean;
 author: string; updated: string; imageId?: string;
 sections: {title: string; text: string}[]; tips: string[];
 sources: {title: string; url?: string}[];
}
export const livingArticles: LivingArticle[] = [
 {slug:'room-for-everyday',category:'居住空间',title:'给日常，留一条从容的动线。',deck:'从进门到坐下，重新观察一个熟悉的房间。',featured:true,author:'RentWise 编辑',updated:'2026-11-24',imageId:'apartment',sections:[{title:'先走一遍，再决定摆放',text:'试着按自己的习惯走进房间：放下包、换鞋、倒水、坐下。记下绕行或需要挪动物件的地方。这是一份观察练习，不是统一的装修标准。'},{title:'给不同的日常留位置',text:'可以先用已有家具试摆阅读、用餐和休息的位置，再观察彼此是否干扰。照片只提供空间灵感，不代表你的户型或实际使用条件。'}],tips:['先量已有家具和通道，再考虑添置。','保留门窗、设备和通行所需的空间。','租住空间的改动先与房东确认。'],sources:[{title:'RentWise 原创编辑观察；非建筑设计或检测结论'}]},
 {slug:'a-moment-outside',category:'生活方式',title:'把片刻户外，放进一天里。',deck:'不必拥有一座庭院，也可以练习观察窗外与日常的连接。',featured:false,author:'RentWise 编辑',updated:'2026-11-24',imageId:'courtyard',sections:[{title:'从一个短暂的停留开始',text:'选一个方便停留的位置，记录你在不同时段看见的光线、声音与活动。是否喜欢这个位置，由自己的体验决定；这里不承诺健康或情绪改善效果。'},{title:'共享空间，也共享边界',text:'如果使用的是公共庭院或露台，先了解开放时间、使用规则与维护责任。本文摄影为法国乡间住宅参考，不是可租房源，也不代表公共空间。'}],tips:['选择允许停留的位置，不占用他人通道。','留意天气和场地的使用要求。','用记录替代立即添置物品。'],sources:[{title:'RentWise 原创日常观察提案；非效果研究'}]},
 {slug:'objects-that-stay',category:'居住好物',title:'留下那些，真的会用到的物件。',deck:'从已有的杯子、收纳盒与桌边小物开始，而不是从购物清单开始。',featured:false,author:'RentWise 编辑',updated:'2026-11-24',sections:[{title:'先认识已经拥有的东西',text:'选一处经常使用的桌面，记录一周里实际拿起过的物件。不常用不等于没有价值；这份练习只是帮助自己整理使用习惯。'},{title:'比较用途，而非品牌',text:'考虑一个物件时，可以写下它将放在哪里、怎样清洁、搬家时是否容易带走。本专题没有测试或推荐具体商品，不提供价格、品牌排名或购买链接。'}],tips:['先尝试调整现有物件的位置。','保留需要的说明书并遵循产品使用要求。','购买决定留给自己的实际需求与预算。'],sources:[{title:'RentWise 原创物件使用观察；没有商品测评或商业合作'}]}
]
export const livingPhoto = (article: LivingArticle) => housingStories.find(photo=>photo.id===article.imageId)
