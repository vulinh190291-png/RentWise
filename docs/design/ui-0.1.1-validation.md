# RentWise UI 0.1.1 首页视觉精修验收

> Document version: v0.1
> Status: Draft（待用户视觉评审）
> Implementation: Verified（本次浏览器与构建范围）
> Project version: UI 0.1.1 / v0.4
> Baseline: b6c6107b8e49c3b02feabeebcd2b226f841972ba
> Branch: feat/ui-home-polish
> Owner: TBD
> Updated: 2026-11-14
> Source: 已合并 PR #9/#10、UI 0.1 规格、当前源码、真实浏览器截图/测量及 Unsplash 作品和许可页面

## 基线与范围

开始时 fix/profile-not-initialized 工作区干净；从远程 fetch 确认 PR #9（651d830）与 #10（b6c6107）均已合入 feat/v0.4-microservices。本地开发基线以 ff-only 同步后创建本分支，保留所有原分支和提交。

只修改首页模板/样式、住宅图片与展示内容、前端回归和文档。Login.vue 的 script setup 与基线逐字一致；Router、Pinia、API、注册页、后端、数据库、依赖及微服务配置均无差异。没有添加地图、Living Picks、房源查询或交易功能。

## 视觉调整

- 暖白首页背景、石墨文字、60/40 合体双栏保留；压缩导航、品牌介绍和表单内部留白，删去重复鼓励文案。
- 品牌说明改为“读懂租约，也读懂生活”：居住探索帮助观察空间，风险学习帮助识别签约前的问题。下方内容将两者连接，不暗示完整 Explore 规划功能已实现。
- 桌面住宅区 480px；平板 380px；手机 320px。逐图焦点裁剪、底部渐变承托文字，保留手动轮播和详情。
- 手机保留直达登录的导航锚点；登录区域可接收锚点焦点，Tab 继续进入用户名。没有固定/裁切表单高度，窄屏和错误内容允许自然滚动。
- 导入三张经过来源/许可核验的压缩摄影，作者、授权与哈希见[素材记录](assets-attribution.md)。页面注明摄影参考，详情链接来源；原占位 SVG 未删除。

## 视口实测

下表为 CSS 像素、页面初始 scrollY=0、浏览器缩放 100% 的登录区域底边。完整区域含注册链接；不是仅计算提交按钮。

| 视口 | UI 0.1 底边 | UI 0.1.1 底边 | 结果 |
| --- | --- | --- | --- |
| 1440×900 | 1112.5px | 700.6px | 完整登录区位于首屏 |
| 1024×768 | 1078.6px | 661.6px | 完整登录区位于首屏 |
| 768×1024 | 历史未测此高度 | 1130.2px | 单列、正常纵向滚动 |
| 390×844 | 图片区 380px | 图片区 320px | 无横向溢出；登录锚点可直达 |
| 320×740 | 图片区 380px | 图片区 320px | 无横向溢出；表单保持触摸尺寸 |

五种视口均无水平溢出；1024×768 空字段校验后提交按钮仍在可视区。移动端不承诺图片和完整表单同时放入一个屏幕，保留自然阅读顺序和直接登录入口。

## 验证

- 新增 frontend/tests/ui-home-polish.mjs：先记录桌面登录区超出首屏及手机图片过高的失败，随后通过五视口、键盘跳转、校验可见性与三图加载检查。
- 既有 frontend/tests/ui-home.mjs：四视口、轮播不丢输入、弹窗 Tab/ESC/焦点回退、减少运动、表单验证、重复提交防护、模拟登录/注册、路由守卫、图片失败提示全部通过，无页面未捕获异常。仅将图片失败匹配从旧 SVG 调整到新 WebP。
- TypeScript（vue-tsc -b）与 npm run build 通过。主包 1014.49 kB，既有 500 kB 警告仍存在；本轮不改依赖或打包配置。
- 本轮不运行真实注册/训练写入测试；认证逻辑未改，UI 脚本的模拟响应不当作新一次真实后端验收。PR #9 已验收的画像修复未改动，其部分画像缺失与未初始化共用 404 的边界保留。
- git diff --check、文档相对路径与变更范围核对通过。

本地重跑：使用现有 Playwright 包（PLAYWRIGHT_MODULE 指定目录）与系统 Edge，设置 UI_BASE_URL 为开发服务器，UI_SCREENSHOT_DIR 为持久证据目录；执行两个脚本。UI_CAPTURE_PREFIX 可区分前后截图。依赖与锁文件未改变。

## 截图与来源

持久目录：项目总目录 `04_版本交付/UI-0.1.1/验收/2026-11-14/`。

- before-1440.png / before-1024.png：本轮实测的修改前首屏。
- polish-1440.png / polish-1024.png：修改后桌面首屏。
- polish-390.png / polish-390-full.png：手机首屏与完整页面。
- polish-390-login.png：手机点击“登录”锚点后的完整表单区域。
- polish-story-普通公寓.png / polish-story-复式空间.png / polish-story-院落住宅.png：三种照片裁剪与叠层。
- before-measurements.json / polish-measurements.json：测量值和断言结果。

截图来自本机系统 Edge 的实际页面渲染，不是设计效果图。1440、1024、390 首屏与三种住宅照片已视觉检查；最终视觉接受由用户评审。截图保留本地，不将整套重复 PNG 放入 Git。

## 变更与剩余边界

代码与素材：Login.vue、HousingShowcase.vue、housingShowcase.ts、三张 housing-*.webp；测试：ui-home.mjs、ui-home-polish.mjs；文档：本报告、素材来源、文档导航。原 SVG 保留。

主包警告未解决；未做全套屏幕阅读器、Safari/Firefox、真实设备和所有缩放比例测试。实际视口高度、用户字体与长错误消息不同可能需要纵向滚动，不能用隐藏溢出来强行满足首屏。完整 UI/UX v1.0 原稿仍未导入。

只创建本地提交，不推送、不创建 PR。Commit SHA 以最终交付及 Git 日志为准。

[文档导航](../README.md) · [UI 0.1 验收历史](ui-0.1-validation.md)
