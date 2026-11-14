# =====================================================================
# TuFactuScan - Prueba de seguridad (Fase 2, bloque 2)
#
# Uso: con el backend arrancado, desde la carpeta backend ejecutar:
#   powershell -ExecutionPolicy Bypass -File pruebas\prueba-seguridad.ps1
#
# Comprueba login, token, roles y aislamiento por empresa. Cada prueba
# muestra OK (verde) o FALLO (rojo) y al final un resumen.
# =====================================================================

$Base = "http://localhost:8080/api"
$script:ok = 0
$script:fallos = 0

# Lanza una petición y devuelve @{ Status = código; Body = respuesta }.
# Invoke-RestMethod lanza excepción con códigos 4xx/5xx; aquí se captura.
function Llamar($Metodo, $Ruta, $Token = $null, $Cuerpo = $null) {
    $cabeceras = @{}
    if ($Token) { $cabeceras["Authorization"] = "Bearer $Token" }
    $parametros = @{ Method = $Metodo; Uri = "$Base$Ruta"; Headers = $cabeceras; ContentType = "application/json; charset=utf-8" }
    if ($Cuerpo) { $parametros["Body"] = [System.Text.Encoding]::UTF8.GetBytes(($Cuerpo | ConvertTo-Json)) }
    try {
        $r = Invoke-RestMethod @parametros
        return @{ Status = 200; Body = $r }
    } catch {
        $codigo = 0
        if ($_.Exception.Response) { $codigo = [int]$_.Exception.Response.StatusCode }
        return @{ Status = $codigo; Body = $null }
    }
}

# Muestra el resultado de una comprobación y lo cuenta.
function Comprobar($Descripcion, $Condicion) {
    if ($Condicion) {
        Write-Host "  OK     $Descripcion" -ForegroundColor Green
        $script:ok++
    } else {
        Write-Host "  FALLO  $Descripcion" -ForegroundColor Red
        $script:fallos++
    }
}

# Inicia sesión y devuelve el token (o $null si falla).
function Login($Email, $Password) {
    $r = Llamar "POST" "/auth/login" $null @{ email = $Email; password = $Password }
    if ($r.Status -eq 200) { return $r.Body.token }
    return $null
}

Write-Host ""
Write-Host "Prueba de seguridad de TuFactuScan" -ForegroundColor Cyan
Write-Host "----------------------------------"

# 1. Rutas públicas y sin token
$r = Llamar "GET" "/estado"
Comprobar "GET /estado es público (200)" ($r.Status -eq 200)

$r = Llamar "GET" "/auth/me"
Comprobar "GET /auth/me sin token se rechaza (401)" ($r.Status -eq 401)

# 2. Login
$r = Llamar "POST" "/auth/login" $null @{ email = "admin@tallergotera.es"; password = "incorrecta" }
Comprobar "Login con contraseña incorrecta se rechaza (401)" ($r.Status -eq 401)

$r = Llamar "POST" "/auth/login" $null @{ email = "no-es-un-email"; password = "x" }
Comprobar "Login con email mal formado se rechaza (400)" ($r.Status -eq 400)

$tokenAdmin = Login "admin@tallergotera.es" "Admin1234!"
Comprobar "Login del administrador devuelve un token" ($null -ne $tokenAdmin)

# 3. Token válido
$r = Llamar "GET" "/auth/me" $tokenAdmin
Comprobar "GET /auth/me con token devuelve al admin (200)" ($r.Status -eq 200 -and $r.Body.email -eq "admin@tallergotera.es")

# 4. Roles
$r = Llamar "GET" "/usuarios" $tokenAdmin
Comprobar "El ADMIN puede listar usuarios (200)" ($r.Status -eq 200)

$tokenEmpleado = Login "empleado@tallergotera.es" "Empleado1234!"
$r = Llamar "GET" "/usuarios" $tokenEmpleado
Comprobar "El EMPLEADO no puede listar usuarios (403)" ($r.Status -eq 403)

# 5. Aislamiento por empresa
# Se compara por empresaId (número) y no por nombre: Windows PowerShell 5.1
# decodifica mal las tildes de las respuestas JSON ("Mecánico" -> "MecÃ¡nico").
$miEmpresa = (Llamar "GET" "/auth/me" $tokenAdmin).Body.empresaId
$r = Llamar "GET" "/usuarios" $tokenAdmin
$lista = @($r.Body)
$deOtraEmpresa = @($lista | Where-Object { $_.empresaId -ne $miEmpresa }).Count
Write-Host "         (usuarios recibidos: $($lista.Count), de otra empresa: $deOtraEmpresa)" -ForegroundColor DarkGray
Comprobar "El admin del taller solo ve usuarios de su empresa (2)" ($lista.Count -eq 2 -and $deOtraEmpresa -eq 0)

$tokenHuerta = Login "lahuerta@email.es" "Huerta1234!"
$r = Llamar "GET" "/usuarios" $tokenHuerta
Comprobar "El admin de la frutería solo ve su usuario (1)" ($r.Status -eq 200 -and @($r.Body).Count -eq 1)

# 6. Token manipulado: se cambia un carácter en mitad de la firma
#    (no el último, porque sus bits finales son de relleno y no cambiarían la firma)
if ($tokenAdmin) {
    $pos = $tokenAdmin.Length - 10
    $actual = $tokenAdmin.Substring($pos, 1)
    $cambio = if ($actual -eq "A") { "B" } else { "A" }
    $tokenFalso = $tokenAdmin.Substring(0, $pos) + $cambio + $tokenAdmin.Substring($pos + 1)
    $r = Llamar "GET" "/auth/me" $tokenFalso
    Comprobar "Un token manipulado se rechaza (401)" ($r.Status -eq 401)
}

Write-Host "----------------------------------"
$color = if ($script:fallos -eq 0) { "Green" } else { "Red" }
Write-Host "Resultado: $($script:ok) OK, $($script:fallos) FALLOS" -ForegroundColor $color
Write-Host ""