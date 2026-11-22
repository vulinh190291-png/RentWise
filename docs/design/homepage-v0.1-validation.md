# Homepage v0.1 首轮实施与验收

> Version: 0.1
> Status: Approved（桌面与390px手机视觉方向经用户评审通过；不代表真实业务联调验收）
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

复用系统字体、暖白石墨色；首轮桌面与390px手机整体视觉方向已由用户确认通过，本轮保留结构与配色。静态数据与阅读页面组件分离：首页加载故事摘要数据，不加载CityStoryPage或地图SDK；下方图片lazy。生产Home JS约7.85kB，故事共享数据约8.48kB；既有主包约950kB警告仍在。北京原图约2.44MiB，未来可单独优化，不隐瞒媒体开销。

## 本轮验证

- npm --prefix frontend run build：TypeScript与生产构建通过。首次普通find结果的模板类型推断报错，改为computed后通过，未跳过类型检查。
- homepage.mjs：320/390/1024/1440宽度，无水平溢出，五区、授权摄影加载、规划入口、三城故事/返回、Explore八城、Learn跳登录、Dashboard保护、带测试token的公共首页、跳至正文焦点、图片失败、减少运动设置通过。首页请求中未出现amap/CityMap/amapAdapter/CityStoryPage。
- ui-home与ui-home-polish：既有登录页/住宅轮播/表单校验/重复提交/认证守卫及响应式回归通过。使用模拟HTTP验证业务契约，不把它当成真实MySQL后端联调。
- Explore八城、三城故事四档、8项画像契约、地图准备状态机及代理安全回归均通过。地图/HTTP替身测试不等同真实SDK或后端联调。本轮未重新调试地图版权。

截图：外层 `04_版本交付/Homepage-v0.1/2026-11-22/`，home-{1440,1024,390,320}-full.png及对应-hero.png。截图由真实Edge渲染，非设计效果图。

## 遗留与评审范围

Living专题、Compare算法/API/CMS均未实现；当前桌面和手机视觉方向已通过；后续新增摄影仍须核验授权。新首页公开展示不代表地图公网授权/代理安全或窄屏版权问题已解决。首页不加载地图，不采用forceWebGL或版权绕过。认证真实后端链路沿用历史结果，本轮未新建账户或写学习数据。

[现行设计规格](homepage-v0.1.md) · [ADR](homepage-adr-001.md)

文档链接与git diff --check通过。本轮实现变更8文件：Home.vue、EditorialPhoto.vue、router/index.ts、homepage.mjs、现行规格、ADR、此验收报告及docs/README.md。未修改登录/注册/Pinia/学习页与地图适配器。


## 手机端小范围精修（2026-11-22）

来源：用户本轮视觉评审结论、基线476d282、本机Edge生产预览和homepage.mjs回归。保留Hero标题/按钮/纵向结构、Living和工具区布局；未修改路由、认证、后端、地图或照片原件。

- 仅手机端三城摄影使用4:3统一展示框，以object-fit:contain完整保留原图；北京竖图两侧保留背景空间，避免裁掉建筑。390px下框高约262.5px，320px下约210px。桌面保持原比例与错落排版。
- 手机说明与署名字号由10px增至12px，允许自然换行；作者、作品来源和许可全部保留。授权链接、主导航/账号/页脚导航触摸高度至少44px。
- TypeScript（vue-tsc -b）及生产构建通过；原有约950kB主包警告仍存在。
- 对生产预览运行homepage.mjs通过：320/390/1024/1440px、无横向溢出、三城图片比例/不裁切、署名与许可链接、触摸高度、三城故事和返回、城市目录、Learn到登录、Dashboard守卫、图片失败、键盘跳至正文。带token测试仅为状态fixture。
- 已目视复核320/390px完整截图的文字换行、摄影和署名。截图存于本地资料库`04_版本交付/Homepage-v0.1/2026-11-22-polish/`，保留四档full与hero截图，不覆盖首轮证据。
- 自动化浏览器点击与导航检查已通过；用户人工实际点击验收、真实数据库登录及学习闭环联调仍需独立验证，不能由视觉通过或fixture测试替代。

本轮变更：Home.vue、EditorialPhoto.vue、homepage.mjs、本报告与docs/README.md。无新业务、依赖、配色或动效系统。


## 最终功能验收首次尝试：环境阻塞（历史记录，后续复验见下）

### 基线与运行环境

- 验收基线：`feat/homepage-v0.1` / `1f132948ae216a8071287964b2c7dd527114701f`，开始时工作区干净。未调整视觉、认证、Java、地图或配置文件。
- 真实浏览器：Microsoft Edge 158.0.4370.33，Windows，Playwright自动化（非用户人工点击）。本轮Vue地址为`http://127.0.0.1:5179`。
- MySQL已运行，实际只读连接成功；两个v0.4库以及users、diagnosis_session、answer_record、user_mastery、profile_sync_checkpoint表存在。只检查结构名称与专用测试账号计数，不读取密码哈希或其他用户记录。
- Nacos 3.0.3、Profile、Learning、Gateway尝试启动失败。首次受执行环境文件/网络权限限制；经授权的提升执行重试后，三种Java服务仍报`Unable to establish loopback connection`，根层异常`java.net.SocketException: Invalid argument: connect`。进程级`-Djava.net.preferIPv4Stack=true`重试无效。这是环境阻塞，具体系统网络原因尚未确认，不能归因于首页业务。
- 使用现有环境凭据，仅核对存在状态；临时设置`SPRING_JPA_HIBERNATE_DDL_AUTO=validate`防止启动自动改表，没有修改持久环境变量、Nacos配置或数据库结构。MySQL和Vue可用不等于Java服务可用。已请求用户从本机终端启动四项服务，无需提供明文凭据。

### 已通过

1. TypeScript与生产构建通过（原有约950kB主包警告保留）。
2. `homepage.mjs`：四档尺寸、公共首页、图片/授权、规划锚点、三城故事与返回、Explore、Learn跳登录、认证守卫、焦点和图片失败回归通过；其中带token部分为fixture，不能算真实登录通过。
3. `explore.mjs`：八城搜索/空结果/选择、键盘、四档视口、错误降级及学习入口回归通过；错误测试包含替身。
4. 另做无mock的公共导航与网络观察：八城逐个搜索选中、三城故事摄影与署名、返回城市目录、浏览器后退/前进、未登录Dashboard跳转均通过。Living/Compare只跳本页锚点，没有虚假业务路由。
5. 上述无mock公共流程：pageerror/console error为0，失败网络请求为0，HTTP错误为0；首页没有AMap/CityMap/amapAdapter请求。没有打开真实地图，不将此结论扩大为地图SDK验收。

### 未通过与未测试

| 项目 | 本轮结果 | 证据/边界 |
| --- | --- | --- |
| Nacos及三个Java服务启动 | 未通过，环境阻塞 | loopback异常；Gateway直接请求ECONNREFUSED |
| 专用账号注册 | 未通过，服务不可用 | UI调用POST /api/auth/register实际返回502，未返回userId |
| 真实登录、JWT、认证恢复、退出 | 未测试 | 注册阻塞；不使用模拟token冒充 |
| 诊断10题及结果、训练提交/反馈、阶段测评 | 未测试 | 未进入学习流程 |
| 画像读取/更新及数据库对照 | 未测试 | 没有业务写入，不能用历史计数替代 |
| 用户人工点击验收 | 待用户执行 | 自动化浏览器不是人工验收 |
| 真实地图与生产部署合规 | 未测试 | 保留既有窄屏版权及部署边界 |

专用测试用户名`home_accept_1795324922561`，只读查询users中计数为0，没有新增该用户或其学习记录。首次脚本尝试因测试用户名超过32位被前端校验阻止；已缩短测试用户名，第二次得到上述真实502。该脚本问题只修复测试，不改业务表单。

### 可复验脚本与证据

新增[真实首页学习验收脚本](../../frontend/tests/homepage-real.mjs)：明确opt-in；每次创建一个独立随机测试账号，通过真实UI注册、登录、10题诊断、训练答题、阶段测评、退出，再检查公共Explore故事导航。真实JWT仅驻留内存/sessionStorage，不打印或写证据。脚本当前仅运行到注册502，后续流程尚未验证，不宣称整份脚本通过。不要反复运行产生无意义测试记录，不清理现有记录。

服务就绪后设置`RENTWISE_REAL_TEST=1`、`PLAYWRIGHT_MODULE`（已安装Playwright包路径）、`FRONTEND_URL`与持久`EVIDENCE_DIR`，执行`node frontend/tests/homepage-real.mjs`。默认Gateway/学习服务端口8080/8082。必须再按实际返回userId只读核对两个数据库，预期诊断10条、训练1条、阶段测评5条，以及五主题画像与接口值；预期值不是本轮实测值。

本地证据目录：`04_版本交付/Homepage-v0.1/2026-11-22-functional/`：

- `public-network.json`：无mock公共导航、浏览器版本及控制台/网络统计。
- `public-network.mjs`：公共导航观察复现脚本。
- `retry/real-regression.json`：实际注册502与未继续的真实业务流程。
- `database-readonly.txt`：该专用账号行数0；执行在READ ONLY事务中。
- 根目录`real-regression.json`为首次脚本用户名长度问题的失败记录，不能代替第二次结果。

### 需要用户亲自执行的点击清单

服务就绪后，使用独立测试账号；不操作真实用户数据：

1. 新隐私窗口访问首页，点击Living/Compare，确认只滚动到规划说明；点击Learn，确认未登录进入登录页。
2. 从首页进入Explore，搜索并切换城市；打开北京/上海/苏州故事，检查摄影署名、来源、返回目录和浏览器前进后退。
3. 注册/登录测试账号，确认Dashboard显示本人；刷新后仍登录，新用户显示诊断引导而非服务故障。
4. 实际答完十题诊断并查看结果；从Dashboard进入训练，提交一题并读反馈；完成五题阶段测评。
5. 回Dashboard与完整画像查看变化；使用脱敏只读SQL对照该userId的会话状态、答题计数和五主题画像。
6. 退出后再打开Dashboard，确认回登录；在320/390px手机实际点击导航和按钮。本轮截图/自动化不替代这些人工步骤。

### PR依赖与安全顺序

本轮成功fetch远程后，`origin/feat/v0.4-microservices`仍为`8292957d1157e65b3097065ad35766c11d769beb`，是当前HEAD祖先；相差13提交。`feat/explore-integrated-v0.4`当前为`72841ee`，也是HEAD祖先，已包括地图`7e4e163`、故事`29d7cba`、集成`9e55cda`与首页设计文档。首页相对集成仅新增`476d282`和`1f13294`，8个文件；Explore、Learn依赖未丢失。

推荐先审查Explore集成分支→`feat/v0.4-microservices`，明确其地图发布阻塞；再审查首页→已包含集成历史的`feat/v0.4-microservices`。若需提前分别审查，可将首页PR暂以`feat/explore-integrated-v0.4`为Base，集成保留历史合并后再改Base并重新核对差异。当前直接以开发基线开首页PR会混入11个集成/准备提交，不能称为纯首页PR。若集成采用squash，必须另行检查继承关系，不能直接假定差异消失；本轮不改历史。

本轮只新增验收脚本并更新本报告/导航；没有推送、PR或合并。最终真实业务验收仍待服务就绪，不能标为完整通过。


## 服务就绪后的真实功能复验（2026-11-22）

**当前结论：本轮真实浏览器学习闭环及只读数据库对照通过；用户人工点击验收、真实地图与生产部署合规仍未覆盖。** 上节环境阻塞是首次尝试的历史记录，后续服务已可访问；未查明原Java loopback错误的系统根因，不宣称由首页代码修复。

### 真实账户与接口证据

使用`homepage-real.mjs`，Edge 158.0.4370.33，未mock业务响应、未注入伪造JWT。通过真实表单创建专用账号`home_accept_1795325713983`（userId=10），密码随机生成且仅驻留测试进程内存。测试未修改其他用户数据，不删除本次记录。

| 操作 | 实际结果 |
| --- | --- |
| 注册、登录、users/me | HTTP 200；Dashboard显示本人；真实JWT保存在sessionStorage |
| 未诊断画像（Gateway及Learning直连） | HTTP 404；`Profile not initialized for user 10`、data=null；前端引导诊断 |
| 诊断开始、10次答题、完成 | 全部HTTP 200；sessionId=11；UI显示完成 |
| 诊断后画像 | Gateway和Learning均200、五主题内容相同；初始五项得分33 |
| Dashboard刷新恢复 | 认证及画像正常恢复 |
| 训练推荐、答题提交 | HTTP 200；answerId=92；syncStatus=SYNCED；反馈说明与下一题推荐正常 |
| 五题阶段测评 | HTTP 200；sessionId=12；UI显示完成 |
| 测评后独立画像读取 | HTTP 200；与测评返回画像逐项相同；Dashboard正常显示 |
| 退出与受保护路由 | token清除；再次访问Dashboard回到login |
| 首页Explore及故事返回 | 八城市目录可访问；北京/上海/苏州独立故事、返回目录和浏览器后退通过 |

以上为自动化驱动真实浏览器的实际业务提交，不是用户人工验收。与首次服务不可用的注册502分开记录；本轮正常运行的业务请求未出现502或其他5xx。

### 只读MySQL对照（实测）

在READ ONLY事务中仅查询userId=10，未查询密码哈希；未执行UPDATE/DELETE或修改结构：

- `rentwise_training_v04.users`：确认id=10与专用测试用户名匹配。
- `diagnosis_session`：11/DIAGNOSIS、12/ASSESSMENT均为FINISHED。
- `answer_record`：DIAGNOSIS=10、TRAINING=1、ASSESSMENT=5；训练答题id=92属于userId=10、caseId=1。
- `rentwise_profile_v04.user_mastery`：5行。DEPOSIT_RETURN=60，其余EARLY_TERMINATION、REPAIR_RESPONSIBILITY、COST_BEARING、BREACH_LIABILITY均50；consecutive_wrong均0、last_difficulty均EASY。
- 五主题的score、consecutiveWrong、lastDifficulty由脚本逐项比对，与实际测评后的画像API响应一致。
- `profile_sync_checkpoint.last_answer_id=97`。

### 控制台与网络边界

真实学习流程无pageerror。记录到3条浏览器404资源提示，对应未初始化画像的3次页面请求（真实业务空状态）；没有将其删除或算作服务故障。另有2次`favicon.svg`的ERR_ABORTED，出现在连续页面导航期间，后续直接请求该资源为200/image/svg+xml；未发现图标缺失证据。此前独立公共导航流程控制台与失败请求仍为0。不能将整轮真实学习流程描述成“控制台完全零提示”。

### 更新后的验收状态与证据

- 已通过：真实注册/登录、未初始化引导、诊断、训练提交反馈、测评、画像独立读取与数据库一致、认证刷新/退出、公共Explore与故事导航；前次TypeScript/构建/四档首页回归仍适用，本轮未改业务源码。
- 未通过：本轮有效服务下未发现新的首页或学习业务失败；历史环境启动失败记录保留。
- 未测试：人工点击清单、真实移动物理设备、真实地图SDK与公网部署合规、过期JWT/故障恢复/并发等未包含场景；不扩大本轮通过范围。此前画像“部分缺失与未初始化共用404”边界保留。
- 本地证据：`04_版本交付/Homepage-v0.1/2026-11-22-functional/live/`中的`real-regression.json`、`database-readonly.txt`、`database-comparison.json`及真实诊断完成、训练反馈、阶段测评、Dashboard截图。
- 实施代码不变；本轮新增验收脚本、更新本报告与导航。PR Base及先Explore集成、后首页的建议不变。没有推送、创建PR或合并。


## 验收收尾与PR依赖复核（2026-11-22）

本轮只审查并保存3项验收成果：`docs/README.md`、本报告、`frontend/tests/homepage-real.mjs`。未重跑会新增用户的真实测试，保留userId=10实测证据。测试脚本仅清理行尾空格；语法检查、文档本地链接和Git差异检查通过。

敏感信息审查：待提交文件未包含实际密码、JWT、安全密钥、Cookie或连接凭据。脚本中的password/token是运行时变量，不是字面凭据；密码随机生成，认证信息未写入证据对象。报告保留的是专用测试账号、userId和必要业务计数，不含真实用户隐私资料。运行日志、数据库导出、截图、JSON证据均留在外层本地资料库，未纳入本次提交。脚本只在显式opt-in后写专用测试记录，后续新增输出逻辑仍需审查敏感信息，不能据此保证任意未来服务端错误文本均安全。

### 当前引用与冲突判断

成功fetch后远程开发基线仍为`8292957`；Explore集成为`72841ee`，包含地图`7e4e163`和故事`29d7cba`；Homepage实施与精修为`476d282`、`1f13294`，另加本轮验收提交。

- 开发基线是集成分支祖先，集成分支是Homepage祖先；本轮未实际执行merge。
- Explore集成相对开发基线：11个提交（含2个历史合并提交）、37个变更文件。
- Homepage在本轮验收提交后相对集成：3个提交、9个变更文件。代码/组件/路由/测试及首页设计验收文档，没有Java或认证逻辑变更。
- 重叠文件为`frontend/src/router/index.ts`、`docs/README.md`、`docs/design/homepage-v0.1.md`、`docs/design/homepage-adr-001.md`。当前是线性后续修改，不构成三方冲突；若目标分支后续变化，必须重新检查。
- Home.vue及EditorialPhoto.vue在Homepage新增；Explore.vue、CityStoryPage.vue、cityStories.ts、cityCoordinates.json、三城摄影的Git blob均与集成分支相同。首页路由差异仅把根路径redirect改为懒加载公共首页；Explore/故事路由与认证守卫保留。
- 授权摄影不在Homepage PR中重复新增，也不能单独把Homepage提交移到缺少Explore依赖的旧基线上。

### PR建议文案

| 顺序 | 建议标题 | Head → Base | 摘要及待验收 |
| --- | --- | --- | --- |
| 1 | feat: integrate Explore city map and cultural stories | feat/explore-integrated-v0.4 → feat/v0.4-microservices | 整合八城地图、目录与三城独立故事、授权摄影、返回上下文及本地代理。保留历史。明确包含72841ee首页设计文档准备，尚不包含首页Vue实现。窄屏版权、正式授权/生产代理审查及物理手机验收未完成，宜Draft审查，不将本地地图成功等同可公开发布。 |
| 2 | feat: add editorial homepage with verified learning navigation | feat/homepage-v0.1 → feat/v0.4-microservices（先包含集成历史） | 公共五区首页、授权摄影、移动精修、真实学习闭环验收与数据库对照。Living/Compare仅规划；人工点击清单、物理设备和生产发布待确认，保留约950kB主包警告。 |

### 获授权后的安全操作顺序（本轮未执行）

1. 人工确认Explore的版权与部署边界是否允许进入开发基线，以及PR包含首页设计文档准备是否接受。确认GitHub允许保留历史的merge commit策略。
2. 单独推送Explore集成分支并开PR，检查37文件范围、CI和目标引用。经另行合并授权后优先用merge commit保留原分支祖先关系；不要squash/rebase后假定依赖自然消失。
3. 集成合入后fetch，确认`72841ee`仍是远程开发基线祖先；重新检查Homepage相对目标的commits/files，预期只剩3个首页提交、9文件（若目标无其他变更）。检查路由和3份重叠文档最终内容。
4. 三城故事页面/数据/摄影应仍存在；重新执行构建与首页/Explore导航回归，再单独推送Homepage并创建第二个PR。没有必要重复cherry-pick地图或故事提交。
5. 若需要提前审查Homepage，可暂以集成分支为Base；必须等待第一PR保留历史合入后再改Base并复核。若第一PR只能squash，停止照搬此步骤，另做独立安全分支整理，不强推或改写原分支。
6. 两个PR的人工审查、CI、冲突状态及实际合并均需届时确认；本轮仅完成本地验收提交，未创建PR，因此没有可声称通过的GitHub CI或在线可合并状态。
