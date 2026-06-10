# Builds the Chubb - Project Radical take-home assessment deck via PowerPoint COM.
$ErrorActionPreference = 'Stop'

function RGB([int]$r,[int]$g,[int]$b) { return ($r + ($g -shl 8) + ($b -shl 16)) }

$PURPLE   = RGB 94 45 145
$BLUE     = RGB 56 110 235
$DARK     = RGB 33 33 33
$ACCENT   = RGB 102 45 145
$GREY     = RGB 90 90 90
$WHITE    = RGB 255 255 255
$LAVENDER = RGB 233 228 245

# COM enum constants
$msoTextHoriz = 1
$msoShapeRect = 1
$msoTrue = -1
$msoFalse = 0
$ppLayoutBlank = 12
$msoGradientHorizontal = 1
$ppAlignLeft = 1
$ppAlignRight = 3

$W = 960.0   # 16:9 widescreen (13.333in)
$H = 540.0   # 7.5in

$pp = New-Object -ComObject PowerPoint.Application
$deck = $pp.Presentations.Add($msoFalse)
$deck.PageSetup.SlideWidth = $W
$deck.PageSetup.SlideHeight = $H

function Add-Slide { $deck.Slides.Add($deck.Slides.Count + 1, $ppLayoutBlank) }

function Set-GradientBg($slide) {
    $bg = $slide.Shapes.AddShape($msoShapeRect, 0, 0, $W, $H)
    $bg.Line.Visible = $msoFalse
    $bg.Fill.ForeColor.RGB = $PURPLE
    $bg.Fill.BackColor.RGB = $BLUE
    $bg.Fill.TwoColorGradient($msoGradientHorizontal, 1)
    $bg.Shadow.Visible = $msoFalse
    return $bg
}

function Add-Text($slide, $left, $top, $width, $height, $text, $size, $bold, $color, $align) {
    $tb = $slide.Shapes.AddTextbox($msoTextHoriz, $left, $top, $width, $height)
    $tf = $tb.TextFrame
    $tf.WordWrap = $msoTrue
    $tr = $tf.TextRange
    $tr.Text = $text
    $tr.Font.Size = $size
    $tr.Font.Name = 'Segoe UI'
    if ($bold) { $tr.Font.Bold = $msoTrue } else { $tr.Font.Bold = $msoFalse }
    $tr.Font.Color.RGB = $color
    $tr.ParagraphFormat.Alignment = $align
    return $tb
}

function Add-HclLogo($slide, $top, $color) {
    Add-Text $slide 56 $top 360 40 "HCLTech" 22 $true $color $ppAlignLeft | Out-Null
}

function Add-Bullets($slide, $left, $top, $width, $height, $lines, $size) {
    $tb = $slide.Shapes.AddTextbox($msoTextHoriz, $left, $top, $width, $height)
    $tf = $tb.TextFrame
    $tf.WordWrap = $msoTrue
    $joined = ($lines -join "`r")
    $tr = $tf.TextRange
    $tr.Text = $joined
    $tr.Font.Size = $size
    $tr.Font.Name = 'Segoe UI'
    $tr.Font.Color.RGB = $DARK
    $tr.ParagraphFormat.Alignment = $ppAlignLeft
    $tr.ParagraphFormat.SpaceAfter = 10
    $tr.ParagraphFormat.SpaceBefore = 0
    for ($i = 1; $i -le $tr.Paragraphs().Count; $i++) {
        $p = $tr.Paragraphs($i)
        $p.ParagraphFormat.Bullet.Visible = $msoTrue
        $p.ParagraphFormat.Bullet.Character = 8226
        $p.IndentLevel = 1
    }
    return $tb
}

function Add-SectionSlide($title, $bullets) {
    $s = Add-Slide
    Add-Text $s 56 40 850 60 $title 30 $true $DARK $ppAlignLeft | Out-Null
    # accent rule under the title
    $rule = $s.Shapes.AddShape($msoShapeRect, 58, 104, 120, 4)
    $rule.Line.Visible = $msoFalse
    $rule.Fill.ForeColor.RGB = $ACCENT
    Add-Bullets $s 58 130 850 360 $bullets 16 | Out-Null
    Add-Text $s 760 500 150 28 "HCLTech" 12 $true $BLUE $ppAlignRight | Out-Null
    return $s
}

# ---------------------------------------------------------------- Slide 1: Title
$s1 = Add-Slide
Set-GradientBg $s1 | Out-Null
Add-Text $s1 56 40 500 40 "HCLTech  |  Supercharging Progress" 18 $true $WHITE $ppAlignLeft | Out-Null
Add-Text $s1 56 200 700 60 "Chubb - Project Radical" 40 $true $WHITE $ppAlignLeft | Out-Null
Add-Text $s1 56 270 700 36 "Take-home Assessment Submission" 20 $true $WHITE $ppAlignLeft | Out-Null
Add-Text $s1 56 330 700 36 "chubb-insurance" 18 $false $LAVENDER $ppAlignLeft | Out-Null
Add-Text $s1 56 370 700 36 "By nagarani" 18 $false $LAVENDER $ppAlignLeft | Out-Null
Add-Text $s1 700 495 210 28 "Jun 10, 2026" 14 $false $WHITE $ppAlignRight | Out-Null

# ---------------------------------------------------------------- Slide 2: Agenda
$s2 = Add-Slide
Add-Text $s2 56 40 600 60 "Agenda" 32 $true $ACCENT $ppAlignLeft | Out-Null
$agenda = @(
  "Architecture Design & Decisions",
  "GIT details & working platform",
  "API Specification",
  "AI Working Journal",
  "Testing Strategy"
)
$y = 130
for ($i = 0; $i -lt $agenda.Count; $i++) {
    if ($i % 2 -eq 1) {
        $band = $s2.Shapes.AddShape($msoShapeRect, 56, ($y - 6), 850, 46)
        $band.Line.Visible = $msoFalse
        $band.Fill.ForeColor.RGB = $LAVENDER
    }
    Add-Text $s2 70 $y 50 36 ([string]($i + 1)) 18 $true $ACCENT $ppAlignLeft | Out-Null
    Add-Text $s2 130 $y 760 36 $agenda[$i] 18 $true $DARK $ppAlignLeft | Out-Null
    $y += 64
}
Add-Text $s2 760 500 150 28 "HCLTech" 12 $true $BLUE $ppAlignRight | Out-Null

# ---------------------------------------------------------------- Section slides
Add-SectionSlide "Architecture Design & Decisions" @(
  "Layered architecture with a domain core - dependencies point inward: api -> service -> domain, and infrastructure -> domain.",
  "Domain layer is framework-free (pure business models). DTOs never leave api and JPA entities never leave infrastructure; only domain models cross boundaries, via explicit mappers.",
  "Packages: api (controller, dto, mapper, exception) - service - domain.models - infrastructure.persistence (repository, entity, mapper) - config - common.",
  "Tech stack: Java 21, Spring Boot 3.4.1, Spring Data JPA, H2 in-memory (swappable for PostgreSQL), springdoc-openapi, Lombok, JUnit 5.",
  "Key decisions: contract-first OpenAPI as the source of truth; centralized error handling via @RestControllerAdvice; dynamic filtering via JPA Specifications; thin controllers with business logic in the service layer."
) | Out-Null

Add-SectionSlide "GIT details & working platform" @(
  "Build & run: Maven project (sunday-discussions-api) on JDK 21; launched with ./mvnw spring-boot:run on embedded Tomcat.",
  "AI working platform: Claude Code (Claude Opus 4.8) inside the VS Code extension on Windows 11.",
  "Governance-as-code: .claude/rules (java-code-style, logging, testing, architecture) applied on every generation; .claude/context holds requirements, tech-stack and the AI journal.",
  "Repository layout: src/main (api, domain, service, infrastructure), src/test, k6/ load tests, OpenAPI spec under resources/static/openapi, seed data in data.sql.",
  "Git remote / branch: <add repository URL and branch here>."
) | Out-Null

Add-SectionSlide "API Specification" @(
  "Contract-first OpenAPI 3.0.3 (policies-api.yaml), browsable via Swagger UI at /swagger-ui.html.",
  "GET /api/v1/policies - paginated list; filters by status, lineOfBusiness, region and effectiveDate range; free-text search over number / holder / underwriter; sorting.",
  "GET /api/v1/policies/{id} - single policy; returns 404 when not found.",
  "PATCH /api/v1/policies/flag - bulk flag policies for review (array of IDs); returns requested / flagged / missing counts.",
  "GET /api/v1/policies/summary - counts by status, total premium by line of business, expiring-soon count.",
  "Frontend-friendly transforms: status & region display names, Money (amount + currency), isExpiringSoon (<= 30 days). Errors via ErrorResponse (400 / 404 / 503), no stack traces leaked."
) | Out-Null

Add-SectionSlide "AI Working Journal" @(
  "ai-journal.md logs every prompt: when it was asked, what was asked, what was done, and the outcome (accepted / rejected / challenged).",
  "Rebuilt faithfully from the real Claude Code session transcripts (~/.claude/projects/**/*.jsonl) - not reconstructed from the code.",
  "53 entries spanning 2026-06-07 to 2026-06-09.",
  "Trail: rules & tech-stack setup -> contract-first OpenAPI -> domain / service / api layers -> H2 + seed data -> exception handling -> unit & integration tests -> k6 performance -> vulnerability check.",
  "Captures real iteration and course-corrections: duplicate-entity cleanup, Lombok @Builder fix, and the '204 but flag not updating' fix."
) | Out-Null

Add-SectionSlide "Testing Strategy" @(
  "Unit: PolicyServiceTest (business logic with a mocked repository) and PolicyControllerTest (MockMvc web layer).",
  "Integration: PolicyRepositoryIntegrationTest (@DataJpaTest against H2 - specifications and aggregate queries).",
  "Smoke: ChubAssessmentApplicationTests verifies the Spring context loads.",
  "Performance: k6 load test validates AC7 - p95 < 500ms at 50 concurrent VUs with error rate < 1% (CI-gateable thresholds).",
  "Conventions per .claude/rules/testing.md: JUnit 5, given/when/then naming, unit + integration coverage."
) | Out-Null

# ---------------------------------------------------------------- Closing slide
$sc = Add-Slide
Set-GradientBg $sc | Out-Null
Add-Text $sc 0 230 $W 60 "HCLTech  |  Supercharging Progress" 30 $true $WHITE 2 | Out-Null
Add-Text $sc 0 320 $W 30 "hcltech.com" 16 $false $WHITE 2 | Out-Null

# ---------------------------------------------------------------- Save
$out = "C:\Users\prath\chubb-insurance\chubb-insurance\presentation\Chubb-Project-Radical-Assessment.pptx"
if (Test-Path $out) { Remove-Item $out -Force }
$deck.SaveAs($out)
$deck.Close()
$pp.Quit()
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($pp) | Out-Null
"Saved: $out"
