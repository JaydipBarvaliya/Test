# mount-nas.ps1
# Maps NAS share to a drive letter using provided credentials (secure prompt)

$DriveLetter = "N"
$SharePath   = "\\NSAPDVCS01.D2-TDBFG.COM\SRCHD_0027D"

Write-Host "Target share: $SharePath"
Write-Host "Mapping to drive: $DriveLetter`:"

# If drive already mapped, remove it first
$existing = Get-PSDrive -Name $DriveLetter -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Drive $DriveLetter`: is already mapped. Removing it..."
    net use "$DriveLetter`:" /delete /y | Out-Null
}

# Prompt for credentials securely
$cred = Get-Credential -Message "Enter your NAS credentials (e.g., DOMAIN\ENPID or ENPID@domain)"

# Map drive (persistent makes it survive reboot)
New-PSDrive -Name $DriveLetter -PSProvider FileSystem -Root $SharePath -Credential $cred -Persist

# Test
if (Test-Path "$DriveLetter`:") {
    Write-Host "✅ Mapped successfully. Opening in File Explorer..."
    Start-Process "explorer.exe" "$DriveLetter`:\"
} else {
    Write-Host "❌ Mapping failed. Try a different username format or verify access."
}





cd $env:USERPROFILE\Downloads
powershell -ExecutionPolicy Bypass -File .\mount-nas.ps1



net use N: \\NSAPDVCS01.D2-TDBFG.COM\SRCHD_0027D /user:D2-TDBFG\ENPID12345 *



net use N: /delete /y

