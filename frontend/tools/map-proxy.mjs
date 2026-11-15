import http from 'node:http'
const key = process.env.AMAP_WEB_KEY?.trim()
const secret = process.env.AMAP_SECURITY_JS_CODE?.trim()
if (!key || !secret) throw new Error('Map environment is not configured')
const send = (res, status, body) => { res.writeHead(status, {'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store'}); res.end(JSON.stringify(body)) }
const server = http.createServer(async (req,res) => {
 if (req.method !== 'GET') return send(res,405,{message:'Method not allowed'})
 if (!['127.0.0.1:5175','localhost:5175'].includes(req.headers.host)) return send(res,403,{message:'Local access only'})
 const origin = req.headers.origin
 if (origin && !['http://127.0.0.1:5174','http://localhost:5174','http://localhost:5173','http://127.0.0.1:5173'].includes(origin)) return send(res,403,{message:'Origin not allowed'})
 const url = new URL(req.url,'http://127.0.0.1:5175')
 if (url.pathname === '/map-local/config') return send(res,200,{key,serviceHost:'/_AMapService'})
 if (!url.pathname.startsWith('/_AMapService/')) return send(res,404,{message:'Not found'})
 const path = url.pathname.slice('/_AMapService'.length)
 if (!/^\/v[34]\//.test(path)) return send(res,404,{message:'Unsupported service'})
 const upstream = new URL(path, path === '/v4/map/styles' ? 'https://webapi.amap.com' : 'https://restapi.amap.com')
 upstream.search = url.search
 upstream.searchParams.set('key',key)
 upstream.searchParams.set('jscode',secret)
 try {
  const response = await fetch(upstream,{signal:AbortSignal.timeout(10000),redirect:'error'})
  const body = Buffer.from(await response.arrayBuffer())
  // Never forward headers, redirects, or a response reflecting the security code.
  if (body.includes(Buffer.from(secret)) || body.includes(Buffer.from(encodeURIComponent(secret)))) return send(res,502,{message:'Map response rejected'})
  res.writeHead(response.status,{'Content-Type':response.headers.get('content-type') || 'application/octet-stream','Cache-Control':'no-store'})
  res.end(body)
 } catch { send(res,502,{message:'Map upstream unavailable'}) }
})
server.listen(5175,'127.0.0.1',()=>process.stdout.write('Local map proxy ready on 127.0.0.1:5175\n'))
