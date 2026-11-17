import http from 'node:http'
import { fileURLToPath } from 'node:url'
import { resolve } from 'node:path'
const send = (res, status, body) => { res.writeHead(status, {'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store'}); res.end(JSON.stringify(body)) }
export function createMapProxy({key,secret,fetchImpl=fetch}) {
 if (!key || !secret) throw new Error('Map environment is not configured')
 return http.createServer(async (req,res) => {
 if (req.method !== 'GET') return send(res,405,{message:'Method not allowed'})
 if (!['127.0.0.1:5175','localhost:5175'].includes(req.headers.host)) return send(res,403,{message:'Local access only'})
 const origin = req.headers.origin
 if (origin && !['http://127.0.0.1:5174','http://localhost:5174','http://localhost:5173','http://127.0.0.1:5173'].includes(origin)) return send(res,403,{message:'Origin not allowed'})
 if (!req.url?.startsWith('/') || req.url.startsWith('//') || req.url.length>8192) return send(res,400,{message:'Invalid request'})
 const url = new URL(req.url,'http://127.0.0.1:5175')
 if (url.pathname === '/map-local/config') return send(res,200,{key,serviceHost:'/_AMapService'})
 if (!url.pathname.startsWith('/_AMapService/')) return send(res,404,{message:'Not found'})
 const path = url.pathname.slice('/_AMapService'.length)
 if (!['/v3/log/init','/v3/config/district','/v4/map/styles'].includes(path)) return send(res,404,{message:'Unsupported service'})
 const callback=url.searchParams.get('callback')
 if(callback && !/^[A-Za-z_$][\w.$]*$/.test(callback)) return send(res,400,{message:'Invalid callback'})
 const upstream = new URL(path, path === '/v4/map/styles' ? 'https://webapi.amap.com' : 'https://restapi.amap.com')
 upstream.search = url.search
 upstream.searchParams.set('key',key)
 upstream.searchParams.set('jscode',secret)
 try {
  const response = await fetchImpl(upstream,{signal:AbortSignal.timeout(10000),redirect:'error'})
  const body = Buffer.from(await response.arrayBuffer())
  // Never forward headers, redirects, or a response reflecting the security code.
  if (body.includes(Buffer.from(secret)) || body.includes(Buffer.from(encodeURIComponent(secret)))) return send(res,502,{message:'Map response rejected'})
  res.writeHead(response.status,{'Content-Type':response.headers.get('content-type') || 'application/octet-stream','Cache-Control':'no-store'})
  res.end(body)
 } catch(error) { send(res,502,{message:'Map upstream unavailable',code:error?.name==='TimeoutError' ? 'upstream_timeout' : 'upstream_network_or_redirect'}) }
})
}
if(process.argv[1] && fileURLToPath(import.meta.url)===resolve(process.argv[1])) {
 const server=createMapProxy({key:process.env.AMAP_WEB_KEY?.trim(),secret:process.env.AMAP_SECURITY_JS_CODE?.trim()})
 server.listen(5175,'127.0.0.1',()=>process.stdout.write('Local map proxy ready on 127.0.0.1:5175\n'))
}
