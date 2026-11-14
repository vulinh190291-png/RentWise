# 新用户画像 502 与学习闭环回归报告

> Document version: v0.2
> Status: Draft
> Implementation: Verified（本报告限定的缺陷与业务路径）
> Baseline: v0.4 / 52212db；独立分支 fix/profile-not-initialized；原 UI 验收基于 b024108
> Owner: TBD
> Updated: 2026-11-14
> Source: 实际 HTTP 响应、浏览器操作、当前源码与回归测试；数据库计数由用户在 MySQL Workbench 只读查询后提供

## 结论

无画像本身是现有业务状态；502 的原因是 **Learning 将 Profile 的 404 错误映射为 502**，不是仅凭“尚未诊断”或数据库计数推断服务故障。修复后，未诊断测试账户经 Gateway 和直连 Learning 均返回 404、明确说明及 data:null；真实诊断后均返回 200 和一致的五主题画像。Dashboard 与画像页区分此状态和真正的服务故障，并引导诊断。

## Git 与范围

开始时 feat/ui-design-system-home 工作区干净，提交为 b024108110d0aca9cc46ef4f3a10d9d9446b03ff。完成原 UI 浏览器验收后，从 origin/feat/v0.4-microservices（52212db）建立 fix/profile-not-initialized。UI 分支指针及其 17 个文件保持不变。

本轮只改画像错误映射、相关页面状态、测试和文档；不改数据库结构、依赖、JWT 逻辑或服务配置，不删除任何账户/记录。用户重启了 Learning Service；Gateway、Profile 与 Nacos 未重启。独立本地 Commit 经用户授权，SHA 以最终 Git 输出为准；没有推送、创建 PR 或合并。

## 复现与调用链证据

1. 专用账户注册并登录后、诊断前，GET /api/profile/me。
2. 修复前 userId=7：Gateway 8080 与直接 Learning 8082 均为 502，响应均为：
   `{"success":false,"data":null,"message":"Profile Service request failed: NotFound"}`。
3. 另建从未诊断的对照账户 userId=8（profile_probe_1794637005805），Gateway 同样返回上述 502。
4. 使用既有内部凭据直连 Profile 8083：GET /internal/profiles/8 返回 404（status:404、error:Not Found、path:/internal/profiles/8）；同一服务读取已初始化 userId=7 为 200。凭据仅在内存使用，未写文件或输出，也未修改环境变量。
5. Gateway 的 /api/** 路由转发到 lb://training-service。Gateway 与 Learning 响应一致，且 Profile 实际可达，不支持“Gateway 未路由成功”或“Profile 整体离线”的解释。

源码链路：

- Gateway → [ProfileController](../../learning-service/src/main/java/com/rentwise/profile/controller/ProfileController.java)：从认证主体取得 userId。
- [RemoteProfileFacade.getProfile](../../learning-service/src/main/java/com/rentwise/profile/service/RemoteProfileFacade.java)：RestClient GET /internal/profiles/{userId}，使用配置的 Profile URL 和 X-Internal-Key。
- [ProfileInternalController](../../profile-service/src/main/java/com/rentwise/profilesvc/ProfileInternalController.java)：校验内部凭据，调用 service.getProfile。
- [ProfileSnapshotService.getProfile](../../profile-service/src/main/java/com/rentwise/profilesvc/ProfileSnapshotService.java)：Repository 查询 user_mastery；行数不等于五主题数时抛 ResponseStatusException(404, "Profile not initialized")。行数为五但缺主题时另抛 409。
- 修复前 RestClient.retrieve 将 404 抛为 HttpClientErrorException.NotFound；Learning 的通用 catch(RestClientException) 转成 DomainException(BAD_GATEWAY)。
- [GlobalExceptionHandler](../../learning-service/src/main/java/com/rentwise/common/exception/GlobalExceptionHandler.java) 按 DomainException.status 输出 HTTP 状态与 ApiResponse 错误体，所以外部看到 502。
- [本地 ProfileService](../../learning-service/src/main/java/com/rentwise/profile/service/ProfileService.java) 对未初始化状态本来就使用 NOT_FOUND。修复使远程路径恢复这一语义。

## 最小修复

- 只在 RemoteProfileFacade 的读取 GET 上处理上游 404，抛 DomainException(NOT_FOUND, "Profile not initialized for user ...")；不制造任何画像或修改成功响应结构。
- GET 的 401/403/409/500/503、连接故障、无效成功体仍为 502；同步 POST 的 404 仍为 502。
- frontend/src/api/profile.ts 只将该画像读取接口的 404 转为 ProfileNotInitializedError。Dashboard、Results 显示“尚无学习画像 / 请先完成初始诊断，再查看学习画像。”并保留诊断入口；其他异常显示“学习画像加载失败”和重试入口。
- 没有修改登录首页、设计系统、注册、认证或训练/测评提交逻辑。

本修复沿用现有 GET 404 契约。它不区分零行与部分画像缺失；若未来需要区分数据损坏、错误路由或其他资源 404，应增加机器可读的领域错误码，而非将所有 404 无条件扩展为同一业务状态。

## 真实业务验收

### 修复前的学习闭环（原 UI 分支）

账户 ui01_1794636831873，userId=7：

- 真实十题诊断完成，五主题初始分数均为 33。
- 前端训练推荐与提交成功：answerId=60、caseId=1、DEPOSIT_RETURN/EASY/correct=true，syncStatus=SYNCED。
- 返回真实解释与建议追问，押金主题升至 50；下一题为提前退租 EASY。
- 前端逐题完成五题阶段测评并提交：sessionId=8，HTTP 200。
- 测评后押金 60，其余四主题 50；下一题提前退租 MEDIUM。
- 独立 GET profile/me 与测评响应一致，刷新 Dashboard 显示后续画像。

### 修复后真实回归（2026-11-14 14:32–14:33，北京时间）

用户确认以当前修复源码重启 Learning 后，脚本通过真实接口创建账户 profile_fix_1794637969189，userId=9；密码随机生成，仅在测试进程内存使用，JWT 来自真实登录。没有拦截或伪造任何 API 成功响应。

| 步骤 | 实测结果 |
| --- | --- |
| 注册、登录、users/me | 成功，认证身份为测试 userId=9 |
| 诊断前 Gateway /api/profile/me | 404，success:false、data:null、message:"Profile not initialized for user 9" |
| 诊断前 Learning /api/profile/me | 与 Gateway 相同 |
| Dashboard 与 Results | 真实 404 显示“尚无学习画像”，按钮进入诊断；无“学习画像加载失败”提示 |
| 真实十题诊断 | sessionId=9，答案及完成请求均 200 |
| 诊断后两个入口的画像 | 均 200、success:true；五主题内容一致 |
| 刷新 Dashboard | 认证恢复、显示五主题，未初始化提示消失 |
| 真实前端训练提交 | answerId=76、syncStatus=SYNCED，显示解释和追问反馈 |
| 真实五题阶段测评 | sessionId=10，完成提交 200 |
| 测评后独立画像读取及 Dashboard | 与测评返回值相同；押金 60、其余四主题 50 |

这里的训练与测评验证包含真实提交，绝非仅页面跳转。反馈文案根据案例产生，不假设存在单独 feedback 表。测试账户 9 的物理表计数未通过 Workbench 再查，不能将响应断言写成数据库直接查询结果。

## 数据库只读证据

来源：用户于 2026-11-14 在 MySQL Workbench 执行只读查询后返回；不是代理直接连接 MySQL 的查询结果。

| userId | 查询项 | 用户返回的真实计数 |
| --- | --- | --- |
| 8 | rentwise_profile_v04.user_mastery | 0 |
| 8 | rentwise_training_v04.answer_record | 0 |
| 7 | answer_record / DIAGNOSIS | 10 |
| 7 | answer_record / TRAINING | 1 |
| 7 | answer_record / ASSESSMENT | 5 |

这些计数确认对照账户的业务前置条件，并佐证第一轮写入；502 根因还由 HTTP 和异常转换源码共同确认。userId=7 的 user_mastery 物理行数、会话/checkpoint 详情和 userId=9 的表计数仍未由 Workbench 返回，均不宣称已查。可选复核见[只读 SQL](profile-readonly-check.sql)，不读取 password_hash、JWT 或密钥；库名默认值若被本机配置覆盖，应先确认。

本轮保留三名专用测试账户：7、8、9。账户 7 和 9 各完成 10 诊断 + 1 训练 + 5 测评；8 保持未诊断。未删除测试资料、未改其他用户数据。

## 自动化回归与证据位置

- RemoteProfileFacadeTest：原始回归先观察到“预期 404、实际 502”的失败；修复后补齐为 8 项，含真实 Controller → Facade → MockRestServiceServer → GlobalExceptionHandler 的 HTTP 契约断言。它不访问真实 MySQL，也不等同于真实端口验收。
- Learning 全量测试：48 tests，0 failures，0 errors，0 skipped；BUILD SUCCESS。命令：`mvn -o -Dmaven.repo.local=<现有缓存目录> -pl learning-service test`。
- 前端：`npm run build`（含 vue-tsc -b）通过。v0.4 基线主包约 1108 kB，保留既有 500 kB 警告；UI 分支的拆包优化未被混入本修复。
- [浏览器契约回归](../../frontend/tests/profile-state.mjs)：Dashboard/Results × 未初始化、已初始化、502、网络失败，共 8 项通过。**显式使用模拟响应**，不作为真实后端联调证据。新增页面状态断言先失败，再随修复通过。
- [真实端口回归](../../frontend/tests/profile-real-integration.mjs)：本次运行通过，覆盖上述七组真实检查；再次执行会新建专用账户和测试记录，必须显式设置 RENTWISE_REAL_TEST=1。
- 两个浏览器脚本使用现有 Playwright 和系统 Edge，通过 PLAYWRIGHT_MODULE 指定安装目录；无新增项目依赖。真实回归还需要 EVIDENCE_DIR，浏览器地址可用 FRONTEND_URL 覆盖；默认 Gateway 8080、Learning 8082、前端 5173。
- `git diff --check` 与本次 Markdown 文件相对链接检查通过。最终提交只包含下表所列文件；无 Java/Vue 以外的配置、数据库或依赖变更。

持久化证据留在项目总目录的 `04_版本交付/UI-0.1/验收/2026-11-14/learning-cycle/`，不自动纳入 Git：

- real-results.json 与原 UI 的训练、测评、Dashboard 截图：修复前的真实闭环。
- contract-dashboard-missing.png、contract-results-missing.png：明确标记 contract 的模拟契约截图。
- profile-fix/real-regression.json 与该子目录的五张 real-*.png：修复后的真实 HTTP 状态、提交标识和浏览器效果。两张未初始化页面截图已经人工视觉核对。

## 本轮文件清单

| 文件 | 改动 |
| --- | --- |
| learning-service/src/main/java/com/rentwise/profile/service/RemoteProfileFacade.java | GET 404 状态保留 |
| learning-service/src/test/java/com/rentwise/profile/RemoteProfileFacadeTest.java | 8 项远程边界与 HTTP 契约回归 |
| frontend/src/api/profile.ts | 画像 404 转为明确的领域错误 |
| frontend/src/views/dashboard/Dashboard.vue | 未初始化与故障分开展示 |
| frontend/src/views/results/Results.vue | 未初始化与故障分开展示 |
| frontend/tests/profile-state.mjs | 8 项浏览器契约测试 |
| frontend/tests/profile-real-integration.mjs | 需主动启用的真实业务回归 |
| docs/README.md | 索引本报告与 SQL |
| docs/engineering/api-v0.4.md | 补充画像 404 契约及本次验证边界 |
| docs/engineering/profile-502-investigation.md | 本报告 |
| docs/engineering/profile-readonly-check.sql | 准确的只读复核查询 |

## 剩余边界与 PR 建议

- 部分画像缺失和路由误配仍与业务 404 共用状态；本轮不修改 Profile 契约或制造默认画像。
- 服务关闭/权限失败由边界测试覆盖，没有停用户服务模拟真实故障；并发、同步重试与其他用户历史数据未在本轮额外验证。
- 自动独立审查代理因服务使用限额未运行成功；已对实际差异、测试与证据进行主执行者复核，不宣称独立审查通过。
- UI PR：feat/ui-design-system-home → feat/v0.4-microservices，保持 b024108 的 UI 工作。
- 缺陷 PR：fix/profile-not-initialized → feat/v0.4-microservices，仅本轮 11 个文件，包含为呈现正确业务状态所必需的三处前端修改。两个分支独立；未来合并时注意 docs/README.md 导航双方新增内容，不覆盖任一方。
- 推送与 PR 操作仍需用户后续授权，本轮不执行。

[文档导航](../README.md)
