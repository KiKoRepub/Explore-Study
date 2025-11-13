# MCP 连接测试脚本
# 用于诊断 Dee 和 Mcp-Server 之间的连接问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MCP 连接诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查端口占用
Write-Host "[1/5] 检查端口 8085 是否被占用..." -ForegroundColor Yellow
$port8085 = netstat -ano | Select-String ":8085"
if ($port8085) {
    Write-Host "✓ 端口 8085 正在使用中" -ForegroundColor Green
    Write-Host $port8085
} else {
    Write-Host "✗ 端口 8085 未被占用 - Mcp-Server 可能未启动" -ForegroundColor Red
}
Write-Host ""

# 2. 测试基本HTTP连接
Write-Host "[2/5] 测试 Mcp-Server 基本连接..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ Mcp-Server 健康检查成功" -ForegroundColor Green
    Write-Host "响应状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
} catch {
    Write-Host "✗ 无法连接到 Mcp-Server" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)"
}
Write-Host ""

# 3. 测试MCP状态端点
Write-Host "[3/5] 测试 MCP 状态端点..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/mcp/status" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ MCP 状态端点可访问" -ForegroundColor Green
    Write-Host "响应内容: $($response.Content)"
} catch {
    Write-Host "✗ MCP 状态端点不可访问" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)"
}
Write-Host ""

# 4. 测试SSE端点
Write-Host "[4/5] 测试 SSE 端点 /mcp/book..." -ForegroundColor Yellow
try {
    # 使用 curl 测试 SSE 连接（如果可用）
    if (Get-Command curl -ErrorAction SilentlyContinue) {
        Write-Host "使用 curl 测试 SSE 连接..."
        $sseTest = curl -N -H "Accept: text/event-stream" http://localhost:8085/mcp/book --max-time 3 2>&1
        if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq 28) {
            Write-Host "✓ SSE 端点响应正常（超时是正常的，因为SSE是持久连接）" -ForegroundColor Green
        } else {
            Write-Host "✗ SSE 端点测试失败" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠ curl 命令不可用，跳过 SSE 测试" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ SSE 端点测试出错: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. 检查 Java 进程
Write-Host "[5/5] 检查 Java 进程..." -ForegroundColor Yellow
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "✓ 找到 $($javaProcesses.Count) 个 Java 进程" -ForegroundColor Green
    foreach ($proc in $javaProcesses) {
        Write-Host "  - PID: $($proc.Id), 内存: $([math]::Round($proc.WorkingSet64/1MB, 2)) MB"
    }
} else {
    Write-Host "✗ 未找到 Java 进程" -ForegroundColor Red
}
Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "建议操作:" -ForegroundColor Yellow
Write-Host "1. 如果端口 8085 未被占用，请先启动 Mcp-Server"
Write-Host "2. 如果健康检查失败，检查 Mcp-Server 的启动日志"
Write-Host "3. 如果 SSE 端点不可访问，检查 application.yml 配置"
Write-Host "4. 确保 Mcp-Server 和 Dee 使用相同的技术栈（webflux 或 webmvc）"
Write-Host ""
Write-Host "详细解决方案请查看: MCP_CONNECTION_TROUBLESHOOTING.md" -ForegroundColor Cyan
