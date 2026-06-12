#!/usr/bin/env ruby
# frozen_string_literal: true

# ╔══════════════════════════════════════════════════════════════════════════════╗
# ║                  RubikSync — Play Store Screenshot Automator               ║
# ║                                                                            ║
# ║  Bağlı Android cihazdan 8 adet Play Store uyumlu ekran görüntüsü alır.    ║
# ║  ADB üzerinden uygulamayı açar, ekranlar arasında gezinir ve               ║
# ║  cihazın orijinal çözünürlüğünde (1080x2400) screenshot kaydeder.         ║
# ║                                                                            ║
# ║  Kullanım: ruby scripts/take_screenshots.rb                               ║
# ║                                                                            ║
# ║  Gereksinimler:                                                            ║
# ║    • ADB kurulu ve PATH'te (brew install android-platform-tools)           ║
# ║    • USB/WiFi ile bağlı Android cihaz                                      ║
# ║    • RubikSync uygulaması cihazda yüklü                                    ║
# ╚══════════════════════════════════════════════════════════════════════════════╝

require "fileutils"
require "open3"
require "time"

# ─── Yapılandırma ─────────────────────────────────────────────────────────────
PACKAGE        = "com.vahitkeskin.rubiksync"
MAIN_ACTIVITY  = "#{PACKAGE}.MainActivity"
OUTPUT_DIR     = File.expand_path("~/Desktop")
DEVICE_TMP     = "/sdcard/rubiksync_screenshot.png"
TOTAL_SHOTS    = 8

# Compose animasyon geçiş süresi (ms). Nav geçişleri 700ms, splash ~2s.
ANIM_WAIT_MS   = 1200
SPLASH_WAIT_MS = 3500

# ─── Renk Yardımcıları ────────────────────────────────────────────────────────
module C
  RESET  = "\e[0m"
  BOLD   = "\e[1m"
  GREEN  = "\e[32m"
  CYAN   = "\e[36m"
  YELLOW = "\e[33m"
  RED    = "\e[31m"
  DIM    = "\e[2m"
  ORANGE = "\e[38;5;208m"
end

def log_info(msg)    = puts("#{C::CYAN}#{C::BOLD}  ℹ #{C::RESET} #{msg}")
def log_success(msg) = puts("#{C::GREEN}#{C::BOLD}  ✓ #{C::RESET} #{msg}")
def log_warn(msg)    = puts("#{C::YELLOW}#{C::BOLD}  ⚠ #{C::RESET} #{msg}")
def log_error(msg)   = puts("#{C::RED}#{C::BOLD}  ✗ #{C::RESET} #{msg}")
def log_step(n, msg) = puts("#{C::ORANGE}#{C::BOLD}  [#{n}/#{TOTAL_SHOTS}]#{C::RESET} #{msg}")

# ─── ADB Wrapper ──────────────────────────────────────────────────────────────
def adb(cmd)
  full_cmd = "adb #{cmd}"
  stdout, stderr, status = Open3.capture3(full_cmd)
  unless status.success?
    log_error("ADB komutu başarısız: #{full_cmd}")
    log_error("Stderr: #{stderr.strip}") unless stderr.strip.empty?
    return nil
  end
  stdout.strip
end

def adb!(cmd)
  result = adb(cmd)
  abort("#{C::RED}ADB komutu kritik hata: adb #{cmd}#{C::RESET}") if result.nil?
  result
end

# ─── Yardımcı Metodlar ────────────────────────────────────────────────────────
def wait(ms)
  sleep(ms / 1000.0)
end

def tap(x, y)
  adb("shell input tap #{x} #{y}")
  wait(200) # Dokunma sonrası küçük bekleme
end

def swipe(x1, y1, x2, y2, duration_ms = 300)
  adb("shell input swipe #{x1} #{y1} #{x2} #{y2} #{duration_ms}")
  wait(200)
end

def back
  adb("shell input keyevent KEYCODE_BACK")
  wait(ANIM_WAIT_MS) # Nav animasyonu bekle
end

def take_screenshot(index)
  filename = "image#{index}.png"
  local_path = File.join(OUTPUT_DIR, filename)

  # Cihazda ekran görüntüsü al
  adb!("shell screencap -p #{DEVICE_TMP}")

  # Bilgisayara çek
  adb!("pull #{DEVICE_TMP} #{local_path}")

  # Cihazdan geçici dosyayı sil
  adb("shell rm -f #{DEVICE_TMP}")

  file_size = File.size(local_path)
  size_str = file_size > 1_000_000 ? "#{(file_size / 1_000_000.0).round(2)} MB" : "#{(file_size / 1000.0).round(1)} KB"

  log_success("#{filename} kaydedildi (#{size_str}) → #{local_path}")
  local_path
end

def get_screen_size
  output = adb!("shell wm size")
  if output =~ /(\d+)x(\d+)/
    [$1.to_i, $2.to_i]
  else
    [1080, 2400] # Samsung A52 varsayılan
  end
end

def force_stop
  adb("shell am force-stop #{PACKAGE}")
  wait(500)
end

def launch_app
  adb!("shell am start -n #{PACKAGE}/#{MAIN_ACTIVITY} -a android.intent.action.MAIN -c android.intent.category.LAUNCHER")
end

def is_app_running?
  result = adb("shell pidof #{PACKAGE}")
  result && !result.empty?
end

# ─── Ön Kontroller ────────────────────────────────────────────────────────────
def preflight_checks
  puts
  puts "#{C::BOLD}#{C::ORANGE}╔══════════════════════════════════════════════════════════════╗#{C::RESET}"
  puts "#{C::BOLD}#{C::ORANGE}║      🧩 RubikSync — Play Store Screenshot Automator        ║#{C::RESET}"
  puts "#{C::BOLD}#{C::ORANGE}╚══════════════════════════════════════════════════════════════╝#{C::RESET}"
  puts

  # ADB kontrolü
  adb_path = `which adb 2>/dev/null`.strip
  if adb_path.empty?
    log_error("ADB bulunamadı! Lütfen kurun: brew install android-platform-tools")
    abort
  end
  log_info("ADB: #{adb_path}")

  # Cihaz kontrolü
  devices_output = adb!("devices -l")
  device_lines = devices_output.lines.select { |l| l.include?("device") && !l.start_with?("List") }
  if device_lines.empty?
    log_error("Bağlı Android cihaz bulunamadı!")
    log_warn("USB kablo veya WiFi ADB bağlantınızı kontrol edin.")
    abort
  end

  device_info = device_lines.first.strip
  model = device_info[/model:(\S+)/, 1] || "Bilinmeyen"
  device = device_info[/device:(\S+)/, 1] || ""
  log_success("Cihaz bulundu: #{model} (#{device})")

  # Ekran boyutu
  w, h = get_screen_size
  log_info("Ekran çözünürlüğü: #{w}x#{h}")
  if w >= 1080 && h >= 1920
    log_success("Play Store uyumlu çözünürlük ✓")
  else
    log_warn("Çözünürlük Play Store minimum gereksinimini karşılamayabilir (önerilen: ≥1080x1920)")
  end

  # Uygulama yüklü mü?
  # pm path doğrudan paketi kontrol eder ve multi-user sorunlarından kaçınır
  pkg_check = adb("shell pm path #{PACKAGE}")
  unless pkg_check && pkg_check.include?("package:")
    log_error("#{PACKAGE} uygulaması cihazda yüklü değil!")
    log_warn("Önce uygulamayı cihaza yükleyin: ./gradlew installDebug")
    abort
  end
  log_success("#{PACKAGE} yüklü ✓")

  # Çıktı dizini
  FileUtils.mkdir_p(OUTPUT_DIR)
  log_info("Çıktı dizini: #{OUTPUT_DIR}")

  puts
  [w, h]
end

# ─── Ana Ekran Görüntüsü Akışı ───────────────────────────────────────────────
def run_screenshot_flow(screen_w, screen_h)
  # Koordinat hesaplamaları (1080x2400 baz, oransal)
  cx = screen_w / 2       # Ekran ortası X
  cy = screen_h / 2       # Ekran ortası Y

  # Header butonları — sağ üstte (lock, shake, sound, settings sırasıyla)
  # Header padding: start=16dp, end=16dp. Butonlar: 34dp, spacing: 8dp
  # Settings butonu en sağda, sırayla sola doğru: settings, sound, shake, lock
  header_y = (screen_h * 0.04).to_i   # ~95px (status bar + padding)

  # Settings gear icon — sağ üst köşe
  settings_x = screen_w - (screen_w * 0.04).to_i
  settings_y = header_y + 17

  # ControlPanel tab selector oranları — ekranın altında
  # ControlPanel paneli yaklaşık ekranın %60'ından başlıyor
  panel_top_y = (screen_h * 0.68).to_i
  tab_y = panel_top_y + (screen_h * 0.02).to_i

  # 3 tab: Moves | Actions | AI — eşit genişlikte
  tab_w = screen_w / 3
  moves_tab_x = tab_w / 2
  actions_tab_x = tab_w + tab_w / 2
  ai_tab_x = 2 * tab_w + tab_w / 2

  # Editor Screen — "Scan with Camera" card (üstte)
  scan_card_y = (screen_h * 0.18).to_i

  start_time = Time.now
  saved_files = []

  puts "#{C::BOLD}#{C::CYAN}  ─── Ekran Görüntüsü Süreci Başladı ───#{C::RESET}"
  puts

  # ═══════════════════════════════════════════════════════════════════════════
  # 1. SPLASH SCREEN
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(1, "Splash Screen — Uygulama başlatılıyor...")
  force_stop
  wait(500)
  launch_app
  wait(1500) # Splash animasyonunun ortasını yakala (tam ortasında loading bar görünür)
  saved_files << take_screenshot(1)

  # Splash tamamlanmasını bekle → Dashboard'a geçiş
  wait(SPLASH_WAIT_MS)

  # ═══════════════════════════════════════════════════════════════════════════
  # 2. DASHBOARD — Moves Tab (Dark Theme)
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(2, "Dashboard — Moves Tab (Ana Ekran)")
  wait(ANIM_WAIT_MS)

  # Showcase aktifse dismiss et (ekrana dokun)
  3.times do
    tap(cx, cy)
    wait(400)
  end
  wait(500)

  # Scramble yap — küp karışık görünsün (Actions tab'a geç, Scramble'a bas, Moves'a dön)
  tap(actions_tab_x, tab_y)
  wait(ANIM_WAIT_MS)
  # Actions tab'daki ilk buton Scramble — sol tarafta
  scramble_x = screen_w / 6
  scramble_y = tab_y + (screen_h * 0.04).to_i
  tap(scramble_x, scramble_y)
  wait(2000) # Scramble animasyonu

  # Moves tab'a geri dön
  tap(moves_tab_x, tab_y)
  wait(ANIM_WAIT_MS)
  saved_files << take_screenshot(2)

  # ═══════════════════════════════════════════════════════════════════════════
  # 3. DASHBOARD — Actions Tab
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(3, "Dashboard — Actions Tab (Scramble/Undo/Reset)")
  tap(actions_tab_x, tab_y)
  wait(ANIM_WAIT_MS)
  saved_files << take_screenshot(3)

  # ═══════════════════════════════════════════════════════════════════════════
  # 4. DASHBOARD — AI Tab (Design + Solve)
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(4, "Dashboard — AI Tab (Design + Solve)")
  tap(ai_tab_x, tab_y)
  wait(ANIM_WAIT_MS)
  saved_files << take_screenshot(4)

  # ═══════════════════════════════════════════════════════════════════════════
  # 5. SETTINGS SCREEN
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(5, "Settings — Tema, Dil, Hakkında")
  tap(settings_x, settings_y)
  wait(ANIM_WAIT_MS + 300) # Slide-in animasyonu (700ms) + biraz extra
  saved_files << take_screenshot(5)

  # ═══════════════════════════════════════════════════════════════════════════
  # 6. README / ABOUT SCREEN (Settings içinden)
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(6, "Readme — Uygulama Dokümantasyonu")
  # Readme butonu Settings ekranının altında
  # About kartının altındaki Readme linkine bas
  readme_y = (screen_h * 0.82).to_i
  tap(cx, readme_y)
  wait(ANIM_WAIT_MS + 300)

  # Eğer Readme açılmadıysa sayfayı scroll edip tekrar dene
  scroll_readme_y = (screen_h * 0.85).to_i
  tap(cx, scroll_readme_y)
  wait(ANIM_WAIT_MS)

  saved_files << take_screenshot(6)
  back # Readme'den geri → Settings

  # ═══════════════════════════════════════════════════════════════════════════
  # 7. EDITOR SCREEN (Dashboard'dan)
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(7, "Editor — Küp Tasarımcısı")
  back # Settings'den geri → Dashboard
  wait(ANIM_WAIT_MS)

  # AI tab'a geç → Design butonuna bas
  tap(ai_tab_x, tab_y)
  wait(ANIM_WAIT_MS)

  # Design butonu AI tab'ın sol yarısında
  design_x = screen_w / 4
  design_y = tab_y + (screen_h * 0.04).to_i
  tap(design_x, design_y)
  wait(ANIM_WAIT_MS + 300)
  saved_files << take_screenshot(7)

  # ═══════════════════════════════════════════════════════════════════════════
  # 8. SCANNER SCREEN (Editor içinden — Scan with Camera)
  # ═══════════════════════════════════════════════════════════════════════════
  log_step(8, "Scanner — Kamera Tarama Sihirbazı")
  # Editor'da "Scan with Camera" kartına bas (üst kısımda)
  tap(cx, scan_card_y)
  wait(ANIM_WAIT_MS + 300)
  saved_files << take_screenshot(8)

  # ═══════════════════════════════════════════════════════════════════════════
  # SONUÇ
  # ═══════════════════════════════════════════════════════════════════════════
  elapsed = (Time.now - start_time).round(1)

  # Cihazdan temp dosya temizliği
  adb("shell rm -f #{DEVICE_TMP}")

  puts
  puts "#{C::BOLD}#{C::GREEN}  ─── Tamamlandı! ───#{C::RESET}"
  puts
  puts "  #{C::BOLD}📸 #{saved_files.size} ekran görüntüsü kaydedildi#{C::RESET}"
  puts "  #{C::BOLD}⏱  Toplam süre: #{elapsed} saniye#{C::RESET}"
  puts "  #{C::BOLD}📁 Konum: #{OUTPUT_DIR}/#{C::RESET}"
  puts

  puts "  #{C::DIM}─────────────────────────────────────────────#{C::RESET}"
  saved_files.each_with_index do |path, i|
    size = File.size(path)
    size_str = size > 1_000_000 ? "#{(size / 1_000_000.0).round(2)} MB" : "#{(size / 1000.0).round(1)} KB"
    puts "  #{C::GREEN}✓#{C::RESET} image#{i + 1}.png #{C::DIM}(#{size_str})#{C::RESET}"
  end
  puts "  #{C::DIM}─────────────────────────────────────────────#{C::RESET}"
  puts

  # Play Store uyumluluk kontrolü
  puts "  #{C::BOLD}#{C::CYAN}📱 Play Store Uyumluluk:#{C::RESET}"
  puts "  #{C::GREEN}✓#{C::RESET} Çözünürlük: #{screen_w}x#{screen_h}"
  puts "  #{C::GREEN}✓#{C::RESET} Format: PNG (kayıpsız)"
  puts "  #{C::GREEN}✓#{C::RESET} Kaynak: Gerçek cihaz ekran görüntüsü"
  puts "  #{C::GREEN}✓#{C::RESET} Toplam: #{saved_files.size} adet screenshot"
  puts
end

# ─── Çalıştır ─────────────────────────────────────────────────────────────────
begin
  screen_w, screen_h = preflight_checks
  run_screenshot_flow(screen_w, screen_h)
rescue Interrupt
  puts
  log_warn("İşlem kullanıcı tarafından iptal edildi.")
  adb("shell rm -f #{DEVICE_TMP}")
  exit(1)
rescue => e
  log_error("Beklenmeyen hata: #{e.message}")
  log_error(e.backtrace&.first.to_s)
  adb("shell rm -f #{DEVICE_TMP}")
  exit(1)
end
