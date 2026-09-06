# 🚀 QuranOxu - Production Release Checklist & Yol Xəritəsi

Bu sənəd **QuranOxu** tətbiqinin Google Play Store-a uğurla buraxılması üçün tələb olunan işlərin bölgüsünü və statusunu əks etdirir.

---

## 🟢 1. Agent Tərəfindən Tamamlanan Texniki İşlər (DONE)

- [x] **Release Keystore yaradılması:**
  - `quranoxu-release.jks` yaradıldı (2048-bit RSA, 10000 gün etibarlılıq).
- [x] **`.gitignore` konfiqurasiyası:**
  - `*.jks`, `*.keystore` və `keystore.properties` faylları `.gitignore`-a əlavə edildi, açarların Git-ə düşməsi əngəlləndi.
- [x] **Avtomatlaşdırılmış imzalama (`keystore.properties`):**
  - Kök qovluqda `keystore.properties` yaradıldı və `build.gradle.kts`-ə bağlandı.
- [x] **`app/build.gradle.kts` sazlanması:**
  - `versionCode = 1`, `versionName = "1.0.0"` təyin edildi.
  - `signingConfigs` vasitəsilə avtomatik imzalama qoşuldu.
  - `isMinifyEnabled = true` və `isShrinkResources = true` aktivləşdirildi.
- [x] **ProGuard / R8 qaydaları (`app/proguard-rules.pro`):**
  - Room, SQLite, Kotlinx, Coroutines, Media3/ExoPlayer və Compose üçün qoruma qaydaları yazıldı.
- [x] **Production Bundle (`.aab`) və Release APK çıxarışı:**
  - `./gradlew assembleRelease bundleRelease` uğurla icra olundu (0 xəta).
  - İstehsalat faylı hazırdır: `app/build/outputs/bundle/release/app-release.aab` (5.7 MB).
  - İmzalanmış test APK-sı: `app/build/outputs/apk/release/app-release.apk` (3.5 MB).
- [x] **Real Cihazda (Xiaomi POCO F5) Sınaq:**
  - İmzalanmış release APK birbaşa telefona quraşdırıldı, Room bazasının, ərəbcə mətnlərin və audio sisteminin qüsursuz işlədiyi təsdiqləndi.
- [x] **Marketinq Vizual Materialları:**
  - **Tətbiq İkonu:** 512x512 PNG hazırlandı (`store_assets/icon_512.png`).
  - **Qapaq Şəkli (Feature Graphic):** 1024x500 PNG hazırlandı (`store_assets/feature_graphic.png`).
- [x] **Məxfilik Siyasəti (Privacy Policy):**
  - `PRIVACY_POLICY.md` və veb üçün hazır `privacy-policy.html` yaradıldı.
- [x] **Mağaza Mətnləri (Metadata):**
  - Tətbiqin adı, qısa və geniş təsvirləri `store_assets/STORE_LISTING.md` faylında hazırlandı.

---

## 🟡 2. Sizin (İstifadəçi) Tərəfindən Edilməli Olanlar (Action Items)

Aşağıdakı addımlar birbaşa **Google Play Console** hesabınıza və şəxsi məlumatlarınıza bağlı olduğu üçün sizin tərəfinizdən edilməlidir:

### Addım 1: Google Play Console Hesabı
- [ ] [Google Play Console](https://play.google.com/console)-a daxil olun (Əgər developer hesabınız yoxdursa, birdəfəlik $25 qeydiyyat rüsumu ilə açın).
- [ ] **"Create App" (Tətbiq Yarat)** düyməsinə klikləyin:
  - App Name: `QuranOxu - Quran və Tərcüməsi`
  - Default language: `Azerbaijani (az)`
  - App or Game: `App`
  - Free or Paid: `Free`

### Addım 2: Privacy Policy Linkinin Yerləşdirilməsi
- [ ] Layihədə hazırladığımız `privacy-policy.html` faylını ictimai bir yerə yerləşdirin:
  - *Tövsiyə:* GitHub Pages aktivləşdirin və ya Notion / Telegra.ph səhifəsinə mətni kopyalayın.
- [ ] Play Console-da **Policy -> Privacy Policy** bölməsinə həmin linki daxil edin.

### Addım 3: İcazələr və Bəyannamələr (App Content)
- [ ] **Data Safety (Məlumat Təhlükəsizliyi):**
  - "Does your app collect or share user data?" sualına **"NO"** seçin.
- [ ] **Foreground Service Permissions:**
  - `FOREGROUND_SERVICE_MEDIA_PLAYBACK` üçün: Tətbiqin Quran audiosunu arxa planda oxuduğunu bildirin və telefonunuzda Quran dinləyərkən bildiriş panelini çəkdiyiniz 15-30 saniyəlik qısa ekran videosunu (Google Drive və ya YouTube linki) əlavə edin.
- [ ] **Content Rating (IARC Sorğusu):**
  - Kateqoriya olaraq "Reference / Education" seçin. Bütün zorakılıq, qumar və s. suallara "No" cavabı verərək **3+ (Everyone)** reytinqi alın.
- [ ] **Target Audience:**
  - 13 yaşdan yuxarı və ya 18+ seçin.

### Addım 4: Qrafik Materialların Yüklənməsi (Store Presence)
- [ ] **Store Listing** bölməsinə daxil olun:
  - **App Icon:** `store_assets/icon_512.png` faylını yükləyin.
  - **Feature Graphic:** `store_assets/feature_graphic.png` faylını yükləyin.
  - **Screenshots:** Telefonunuzdan və ya kompüterdən 4-8 ədəd skrinşot əlavə edin.
  - **Mətnlər:** `store_assets/STORE_LISTING.md` faylındakı başlıq, qısa və geniş təsviri kopyalayıb yapışdırın.

### Addım 5: Production Bundle (.aab) Faylının Yüklənməsi
- [ ] **Release -> Production** (və ya əvvəlcə **Closed Testing**) bölməsinə keçin:
  - Kompüterinizdən `app/build/outputs/bundle/release/app-release.aab` faylını yükləyin.
  - Release notes bölməsinə yazın: `İlk rəsmi buraxılış. Quran oxu və dinləmə funksionallıqları.`

### Addım 6: 20 Tester Tələbi (Yalnız Yeni Fərdi Hesablar üçün)
- [ ] Əgər hesabınız 2023-cü ilin noyabrından sonra açılmış fərdi hesabdırsa, Google tətbiqi birbaşa istehsalata buraxmağa icazə vermir.
- [ ] **Closed Testing** bölməsində 20 nəfər dost/tanışın Gmail ünvanını əlavə edin və onların 14 gün ərzində tətbiqi yükləyib saxlamasını təmin edin.
- [ ] 14 gündən sonra "Apply for Production" düyməsi aktivləşəcək və tətbiqi 100% ictimaiyyətə buraxa biləcəksiniz!
