
# RentWise Frontend

RentWise v0.3 前端项目，使用 Vue 3 + TypeScript + Vite 构建。

## 技术栈

Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios、Element Plus。

## 本地运行

```powershell
npm.cmd install
npm.cmd run dev
```

默认访问地址：

http://localhost:5173

请先确保后端已启动在 8080 端口。

## 生产构建

```powershell
npm.cmd run build
```

## 页面路由

| 路径 | 页面 |
| --- | --- |
| /login | 登录 |
| /register | 注册 |
| /dashboard | 学习中心 |
| /diagnosis | 初始诊断 |
| /results | 学习画像 |
| /training | 自适应训练 |
| /assessment | 阶段测评 |

## API 连接

开发环境通过 Vite 代理，将 `/api` 请求转发至：

http://localhost:8080

用户登录后，JWT 保存在 sessionStorage 中。

业务页面需要登录认证。
