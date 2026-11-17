$ErrorActionPreference = 'Stop'
foreach ($name in @('AMAP_WEB_KEY', 'AMAP_SECURITY_JS_CODE')) {
    if (-not [Environment]::GetEnvironmentVariable($name, 'Process')) {
        [Environment]::SetEnvironmentVariable($name, [Environment]::GetEnvironmentVariable($name, 'User'), 'Process')
    }
    if (-not [Environment]::GetEnvironmentVariable($name, 'Process')) { throw "$name is not configured" }
}
node (Join-Path $PSScriptRoot 'map-proxy.mjs')
