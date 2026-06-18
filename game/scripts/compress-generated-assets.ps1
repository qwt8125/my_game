param(
    [string]$AssetRoot = "src\main\resources\static\assets\generated",
    [int]$MaxBytes = 51200
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$root = Resolve-Path -LiteralPath $AssetRoot
$sizes = @(192, 160, 128, 112, 96, 80, 64, 48)
$files = Get-ChildItem -LiteralPath $root -Recurse -File -Filter *.png |
    Where-Object { $_.Name -notlike "*.tmp.png" }

foreach ($file in $files) {
    if ($file.Length -le $MaxBytes) {
        Write-Host "[skip] $($file.FullName) $($file.Length) bytes"
        continue
    }

    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    $stream = New-Object System.IO.MemoryStream @(,$bytes)
    $source = [System.Drawing.Image]::FromStream($stream)
    try {
        $bestTemp = $null
        $bestSize = [Int64]::MaxValue
        foreach ($maxSide in $sizes) {
            $scale = [Math]::Min($maxSide / [double]$source.Width, $maxSide / [double]$source.Height)
            if ($scale -gt 1) { $scale = 1 }
            $width = [Math]::Max(1, [int][Math]::Round($source.Width * $scale))
            $height = [Math]::Max(1, [int][Math]::Round($source.Height * $scale))

            $bitmap = New-Object System.Drawing.Bitmap $width, $height
            try {
                $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
                try {
                    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
                    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
                    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighSpeed
                    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
                    $graphics.DrawImage($source, 0, 0, $width, $height)
                } finally {
                    $graphics.Dispose()
                }

                $temp = "$($file.FullName).$maxSide.tmp.png"
                $bitmap.Save($temp, [System.Drawing.Imaging.ImageFormat]::Png)
                $tempInfo = Get-Item -LiteralPath $temp
                if ($tempInfo.Length -lt $bestSize) {
                    if ($bestTemp -and (Test-Path -LiteralPath $bestTemp)) {
                        Remove-Item -LiteralPath $bestTemp -Force
                    }
                    $bestTemp = $temp
                    $bestSize = $tempInfo.Length
                } else {
                    Remove-Item -LiteralPath $temp -Force
                }
                if ($bestSize -le $MaxBytes) { break }
            } finally {
                $bitmap.Dispose()
            }
        }

        if ($bestTemp) {
            Remove-Item -LiteralPath $file.FullName -Force
            Move-Item -LiteralPath $bestTemp -Destination $file.FullName
            $final = Get-Item -LiteralPath $file.FullName
            $status = if ($final.Length -le $MaxBytes) { "ok" } else { "large" }
            Write-Host "[$status] $($file.FullName) $($final.Length) bytes"
        }
    } finally {
        $source.Dispose()
        $stream.Dispose()
    }
}
