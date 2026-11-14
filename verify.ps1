$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$processes = @()
# Resolve the actual JVM so cleanup also works with Windows' java.exe launcher shim.
$javaHomeLine = cmd.exe /d /c 'java -XshowSettings:properties -version 2>&1' |
    ForEach-Object { $_.ToString() } | Select-String '^\s*java\.home\s*=\s*(.+)$' | Select-Object -First 1
if (!$javaHomeLine) { throw 'Could not find java.home. Install Java 17 or newer.' }
$java = Join-Path $javaHomeLine.Matches[0].Groups[1].Value.Trim() 'bin/java.exe'
function Wait-Http($url, $ready) {
    $until = (Get-Date).AddSeconds(75)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 3
            if (& $ready $response) { return $response }
        } catch { }
        Start-Sleep -Milliseconds 700
    } while ((Get-Date) -lt $until)
    throw "Timed out at $url. Check the application logs."
}
try {
    foreach ($port in 8761,8088,8089,8888,18088) {
        if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
            throw "Port $port is already in use. Stop the existing application before running this check."
        }
    }
    foreach ($name in 'eureka-server','client','gateway','service-voiture') {
        $jar = Join-Path $PSScriptRoot "$name/target/$name-1.0-SNAPSHOT.jar"
        if (!(Test-Path -LiteralPath $jar)) { throw "Build the lab first: missing $jar" }
    }
    $processes += Start-Process $java -ArgumentList '-jar','eureka-server/target/eureka-server-1.0-SNAPSHOT.jar' -RedirectStandardOutput eureka.log -RedirectStandardError eureka-error.log -PassThru -WindowStyle Hidden
    $null = Wait-Http 'http://localhost:8761/' { param($r) $r.StatusCode -eq 200 }
    # Leave the old static port empty: this checks discovery, not just a matching response.
    $processes += Start-Process $java -ArgumentList '-jar','client/target/client-1.0-SNAPSHOT.jar','--server.port=18088' -RedirectStandardOutput client.log -RedirectStandardError client-error.log -PassThru -WindowStyle Hidden
    $direct = Wait-Http 'http://localhost:18088/clients' { param($r) ($r.Content | ConvertFrom-Json).Count -eq 3 }
    $single = Invoke-WebRequest -UseBasicParsing 'http://localhost:18088/client/1'
    if (($single.Content | ConvertFrom-Json).nom -ne 'Rabab SELIMANI') { throw 'Unexpected client 1' }
    $entry = Wait-Http 'http://localhost:8761/eureka/apps/SERVICE-CLIENT' { param($r) $r.Content -match '<status>UP</status>' }
    $processes += Start-Process $java -ArgumentList '-jar','service-voiture/target/service-voiture-1.0-SNAPSHOT.jar' -RedirectStandardOutput voiture.log -RedirectStandardError voiture-error.log -PassThru -WindowStyle Hidden
    $null = Wait-Http 'http://localhost:8761/eureka/apps/SERVICE-VOITURE' { param($r) $r.Content -match '<status>UP</status>' }
    $processes += Start-Process $java -ArgumentList '-jar','gateway/target/gateway-1.0-SNAPSHOT.jar' -RedirectStandardOutput gateway.log -RedirectStandardError gateway-error.log -PassThru -WindowStyle Hidden
    $list = Wait-Http 'http://localhost:8888/clients' { param($r) $r.StatusCode -eq 200 }
    $null = Wait-Http 'http://localhost:8761/eureka/apps/GATEWAY' { param($r) $r.Content -match '<status>UP</status>' }
    $detail = Invoke-WebRequest -UseBasicParsing 'http://localhost:8888/client/1'
    if ($list.Content -cne $direct.Content -or $detail.Content -cne $single.Content) { throw 'Gateway changed the client response' }
    foreach ($port in 18088,8888) {
        try {
            $null = Invoke-WebRequest -UseBasicParsing "http://localhost:$port/client/999999"
            throw "Expected HTTP 404 on port $port"
        } catch {
            if ([int]$_.Exception.Response.StatusCode -ne 404) { throw }
        }
    }
    $null = Wait-Http 'http://localhost:8888/voitures' { param($r) ($r.Content | ConvertFrom-Json).Count -eq 3 }
    $car = Invoke-RestMethod 'http://localhost:8089/voitures/1'
    if ($car.client.nom -ne 'Rabab SELIMANI') { throw 'Wrong owner for voiture 1' }
    $owned = Invoke-RestMethod 'http://localhost:8888/voitures/client/1'
    if ($owned.Count -ne 2 -or ($owned | Where-Object { $_.clientId -ne 1 })) { throw 'Wrong cars for client 1' }
    $created = Invoke-RestMethod 'http://localhost:8888/voitures/2' -Method Post -ContentType 'application/json' -Body '{"marque":"Dacia","matricule":"LAB-19","model":"Logan","clientId":1}'
    if ($created.clientId -ne 2 -or $created.client.nom -ne 'Amal RAMI') { throw 'Creation did not use the URL owner' }
    $null = Invoke-RestMethod "http://localhost:8888/voitures/$($created.id)" -Method Put -ContentType 'application/json' -Body '{"model":"Sandero"}'
    $persisted = Invoke-RestMethod "http://localhost:8089/voitures/$($created.id)"
    if ($persisted.model -ne 'Sandero' -or $persisted.marque -ne 'Dacia' -or $persisted.clientId -ne 2) { throw 'Partial update did not persist correctly' }
    try {
        $null = Invoke-WebRequest -UseBasicParsing 'http://localhost:8888/voitures/999999'
        throw 'Expected missing voiture to return 404'
    } catch {
        if ([int]$_.Exception.Response.StatusCode -ne 404) { throw }
    }
    'PASS: Eureka registration, both service routes, Voiture detail/filter/create/update, and 404.'
} finally {
    foreach ($process in $processes) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
}
