# UI 0.1 素材与内容来源

> Document version: v0.2
> Status: Draft
> Implementation: Partial
> Baseline: UI 0.1.1 / v0.4 (b6c6107)
> Updated: 2026-11-14
> Source: UI 0.1 规格、当前源码、Unsplash 作品与许可原文及本轮视觉检查

## UI 0.1 历史占位
`frontend/public/images/housing-placeholder.svg` 为本轮直接编写的几何 SVG 中性占位，无第三方图片、字体、商标或房产营销素材。它不是摄影，页面与 alt 均明确标注。UI 0.1 时三种介绍共用该占位；UI 0.1.1 已使用下述摄影，原 SVG 保留用于历史追溯。

科普文字为本轮编辑的一般性空间观察与核对建议，不是房源、法律分类、价格数据或地方性法规判断。具体住房条件需现场核实。未使用已否定的绿色插画或旧 hero.png。

## 替换条件
以后替换摄影仍需记录作者、原始来源链接、许可条款、下载日期、是否允许裁剪及商业展示。未经核实不导入。更新 housingShowcase.ts 的 image/alt，移除对应占位提示前必须确认图片确为相符摄影；重跑图片失败与移动裁剪验收。

[UI 验收](ui-0.1-validation.md) · [导航](../README.md)

## UI 0.1.1 摄影（2026-11-14 核验并导入）

三个作品页面均明确标记免费 Unsplash License，未采用 Unsplash+ 付费内容。核对[Unsplash 许可原文](https://unsplash.com/license)：允许下载、复制、修改、分发及商业/非商业使用；不得未经显著修改单独出售图片，也不得建立竞品图片库。本仓库只将三张摄影作为页面配图，详情中链接作者作品来源，不将素材称为房源或业主背书。

| 本地文件 | 作者 / 来源页面 | 画面与用途 | 导入处理 |
| --- | --- | --- | --- |
| [housing-apartment.webp](../../frontend/public/images/housing-apartment.webp) | Christian / [B5EPQVhBD70](https://unsplash.com/photos/B5EPQVhBD70) | 起居、餐厨相连的公寓室内；源页注明 SONY ILCE-7M3 | 1400×788，WebP，127582 bytes |
| [housing-duplex.webp](../../frontend/public/images/housing-duplex.webp) | Caroline Badran / [vQc0ZDy7Wv8](https://unsplash.com/photos/vQc0ZDy7Wv8) | 木楼梯与上层空间；源页注明 FUJIFILM X-T30 II | 1400×2100，WebP，384838 bytes |
| [housing-courtyard.webp](../../frontend/public/images/housing-courtyard.webp) | Erwan Hesry / [uj3VsJLhWE4](https://unsplash.com/photos/uj3VsJLhWE4) | 法国乡间住宅及前院；源页注明 SONY ILCE-7C | 1400×933，WebP，419362 bytes |

由作品页指向的 images.unsplash.com 提供缩放/转码：参数 `auto=format&fit=max&w=1400&q=80&fm=webp`。原始资源标识分别为 `photo-1680416124536-22b16c411402`、`photo-1768578927229-93ec4150fe9a`、`photo-1778158257064-e8603dc1105a`。首页通过 object-fit:cover 和逐图焦点裁剪，详情用 contain 展示完整照片；没有生成式修改。合计 931782 bytes，首图仅约 125 KiB，其余随轮播选择加载。

三张图片已查看：没有明显人物、商标或水印；不推断照片中的房屋用途、可租性或具体法律分类。照片只服务于空间观察与通用科普，不能替代现场核查。公开平台许可是本次使用依据，不声称另获房产权利人或所有入镜作品的独立背书。来源页与授权应随以后素材替换继续维护。

### 文件指纹

- `housing-apartment.webp` SHA-256：`9eeb06d310584fa33a553a1fe39a725ee7079255edda15f81ab4491757435eb6`
- `housing-duplex.webp` SHA-256：`727799de6642396765382647af6904a690e8bd8e155432a892609ba577d9b7fc`
- `housing-courtyard.webp` SHA-256：`005438e2a33afc911eeee4fac30285ff9e0fa7ae5f43db2db42908c7d1b03641`
