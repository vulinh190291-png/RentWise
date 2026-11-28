# Compare v0.1 · 城市居住生活图鉴

> Version: 0.1
> Status: Draft
> Implementation: Partial（核心真实链路通过；统计资料与生产验收待补）
> Project: RentWise v0.4
> Updated: 2026-11-28
> Source: 本轮用户需求、现有源码、Explore已核验来源记录；不是新一轮外部统计调查

## 产品和实现

公开/compare：双城市选择、六维并列阅读、吸顶导航、CSS/IntersectionObserver渐显与Vue切换；尊重reduced-motion。两城不可相同；切换清空旧结果，避免标题与数据错配。加载/失败/重试/缺失值可见。住房摄影和文化故事复用已有授权资源，原Explore/Living数据不迁移。没有总分、排名、预测、商品或推荐算法。

六维呈现：地理文字背景、住房摄影、成本数字区域、交通侧栏信息、就业Bento双栏、文化故事链接。只有NUMBER且COMPARABLE并为非负数时显示meter图表和短暂展开动画；不显示缺失数值，不把定性材料换算成数字。初始数据没有已核验数值，因此真实初始页面不显示统计图。动画图表分支尚需独立数值夹具和真实统计资料验证。

新增content-service，Java17/Spring Boot3.5.16，默认8084，Nacos注册名content-service，JdbcTemplate只读查询。Gateway增加更高优先级/api/content/** → lb://content-service；学习/api/**与认证代码不变。前端路由懒加载，无新动画库。

## 数据及比较契约

[初始化SQL](../../sql/compare-v0.1.sql)仅作用于rentwise_content_v04：city（slug唯一）、metric（code唯一、dimension索引）、source、city_metric_value（city/metric唯一，外键与source索引）。本版每城每指标一个当前观察值，不支持多时期历史选择。INSERT IGNORE用于保留已有行，更新资料须单独审查，不能据重复导入宣称更新成功。

复用八城slug。北京/上海/苏州地理、住房、文化共9条定性摘要，来源来自[既有城市核验记录](../product/explore/city-sources.md)，保留2026-11-14核验日，不宣称本轮重新核验网页。统计时期未明确时写UNSPECIFIED，不将核验日期假作统计时期。住房范围限定具体历史样本，不概括全城。成本/通勤/就业及其余城市缺失值不落伪数据。

服务按metric匹配并聚合。状态优先级：任一侧无值→MISSING；单位、范围、方法为空或不同→INCOMPARABLE；时期未知→INCOMPARABLE；有效时期不同→DIFFERENT_PERIOD；全部相同→COMPARABLE。对比结论不代表来源精度或城市优劣；指标录入者仍需保证相同口径字符串代表相同定义。

## 公共API

- GET /api/content/cities：返回八城slug/name数组。
- GET /api/content/cities/{slug}：城市基本信息与六指标value（含缺失null）。未知slug404。
- GET /api/content/compare?left=beijing&right=shanghai：leftCity/rightCity及metrics数组；每项含code/name/dimension/kind、left/right值、status。每个非空值含textValue/numericValue、period、unit、scope、method、sourceTitle/sourceUrl、checkedAt。
- 缺少参数400，相同城市400，未知城市404。未设计鉴权写接口；这些接口不经过学习JWT过滤链。数据库不可用不能包装成成功空数据。

## 本地启动

1. 使用有建库权限的本机MySQL账户执行sql/compare-v0.1.sql，先确认目标为独立内容库，不修改现有业务库。本轮初始化后查询8城/6指标/9来源/9值。
2. 在独立终端设置CONTENT_DB_PASSWORD（不写入Git），可选CONTENT_DB_USERNAME、CONTENT_DB_URL；默认连接本机rentwise_content_v04。设置NACOS_ENABLED=true，再执行mvn -pl content-service spring-boot:run。
3. 启动Nacos后，在另一个终端以NACOS_ENABLED=true启动Gateway：mvn -pl gateway-service spring-boot:run。当前运行的旧Gateway需要重启才读取新增路由；不要擅自停止其他用户进程。
4. 验收可临时设置SERVER_PORT=8085启动备用Gateway；前端终端设置RENTWISE_API_PROXY=http://127.0.0.1:8085，再npm --prefix frontend run dev。默认仍代理localhost:8080；变量只在Vite进程使用，不含凭据。
5. curl实际公共API，核实Nacos中content-service健康实例与Gateway转发。代理通了不等于生产授权、安全或性能验收完成。

## 本轮验收与限制

- TypeScript与生产构建通过；原有主包警告约951.74kB保留。
- mvn -pl content-service -am test通过：同口径、异时期、异单位、缺失侧、未知时期规则。未伪造真实接口通过。
- MySQL独立库初始化成功，四表和种子计数实测；未执行学习业务库写入。
- Codex启动content-service和备用Gateway均因Java本机loopback错误退出：Unable to establish loopback connection / Invalid argument: connect。已请求本机终端启动。真实8084/8085接口、Nacos注册、Gateway完整链路仍未验证；当前不是完整交付验收通过。
- 前端夹具测试使用现有城市来源构造响应，不是真实后端。覆盖四档尺寸、六维、切换、缺失、错误提示、首页入口及故事链接；截图名称明确标注fixture。物理设备、人工视觉、真实数字图表仍待验证。
- 外层截图目录：04_版本交付/Compare-v0.1/2026-11-28，compare-fixture-{1440,1024,390,320}.png。不纳入Git，不含密钥。

## 未实现

数值统计原稿核验、真实数值可比图表验收、多时期选择、生产部署与负载测试。无Redis、爬虫、新认证逻辑或地图变更。


## 本机服务就绪后补充验收

2026-11-28用户从本机终端启动服务后，真实验证通过：8084及Gateway8085的cities均返回8城；cities/suzhou返回6指标；compare返回相同六维结果与3个MISSING；同城/缺参数400，未知城市404。Nacos实际查询返回content-service实例127.0.0.1:8084、healthy=true。没有重启8080的既有学习Gateway；正式使用默认端口前仍需按授权重启加载路由。

compare-real.mjs直接访问两端并在真实浏览器验证双城选择、故事跳转/返回、1440/390布局，未mock。证据在外层目录real-api.json、compare-real-1440.png、compare-real-390.png；与fixture截图明确区分。修复加载状态作为Transition key导致重新挂载的问题后通过；重新构建通过。

首页回归首次因重复goto当前首页后返回导航到about:blank失败；测试改为在已处于首页时直接点击故事，并等待目标路由，不改业务；四档首页、Living/Compare导航、故事返回与学习守卫回归通过。两份测试都保留失败历史于本轮执行记录，不将mock认证当作真实学习验收。

仍未完成：真实数字统计资料、数字图表独立分支测试、真机/人工评审、生产部署与负载。该版本可评审，不宣称所有比较场景和生产就绪。
