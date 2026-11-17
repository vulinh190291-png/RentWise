import assert from 'node:assert/strict'
import http from 'node:http'
import {createMapProxy} from '../tools/map-proxy.mjs'
let calls=0,mode='ok'
const secret='unit-test-secret-not-a-platform-credential'
const server=createMapProxy({key:'unit-test-public-key',secret,fetchImpl:async(url,options)=>{
 calls++;assert.equal(url.origin,'https://restapi.amap.com');assert.equal(url.searchParams.get('jscode'),secret);assert.equal(options.redirect,'error')
 if(mode==='timeout')throw new DOMException('unsafe details must not escape','TimeoutError')
 if(mode==='network')throw new Error('unsafe details must not escape')
 return new Response(mode==='echo'?secret:'{"status":"1"}',{status:200})
}})
await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve))
const port=server.address().port
const request=(path,headers={},method='GET')=>new Promise((resolve,reject)=>{
 const req=http.request({host:'127.0.0.1',port,path,method,headers:{Host:'127.0.0.1:5175',...headers}},res=>{let body='';res.on('data',chunk=>body+=chunk);res.on('end',()=>resolve({status:res.statusCode,body}))});req.on('error',reject);req.end()
})
try {
 for(const path of ['/_AMapService/v3/weather/weatherInfo','/_AMapService/v4/anything','/_AMapService/https://example.com/','/_AMapService/v3/../v3/unknown'])assert.equal((await request(path)).status,404)
 assert.equal((await request('//example.com/v3/log/init')).status,400)
 assert.equal((await request('/_AMapService/v3/log/init?callback=alert(1)')).status,400)
 assert.equal((await request('/map-local/config',{Origin:'https://example.com'})).status,403)
 assert.equal((await request('/map-local/config',{Host:'example.com'})).status,403)
 assert.equal((await request('/map-local/config',{},'POST')).status,405)
 assert.equal(calls,0)
 const config=await request('/map-local/config');assert.equal(config.status,200);assert.equal(config.body.includes(secret),false)
 assert.equal((await request('/_AMapService/v3/log/init?key=untrusted&jscode=untrusted')).status,200)
 for(const value of ['timeout','network','echo']) {mode=value;const result=await request('/_AMapService/v3/log/init');assert.equal(result.status,502);assert.equal(result.body.includes(secret),false);assert.equal(result.body.includes('unsafe details'),false);if(value==='timeout')assert.equal(JSON.parse(result.body).code,'upstream_timeout')}
 console.log('PASS proxy: fixed paths/host, method/origin/callback guards, credential override, redirect rejection, safe timeout/network errors and reflected-secret rejection (mock upstream).')
}finally{await new Promise(resolve=>server.close(resolve))}
