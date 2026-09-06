# 🍎 QuranOxu - iOS Versiyası (Kotlin & Compose Multiplatform) Yol Xəritəsi

Bu sənəd **QuranOxu** tətbiqinin **Kotlin Multiplatform (KMP)** və **Compose Multiplatform (CMP)** texnologiyalarından istifadə edərək **iOS** versiyasının hazırlanması, Apple tələbləri, xərclər və App Store-a çıxarılması üzrə tam detallı fəaliyyət planıdır.

---

## 📌 1. Texnoloji Yanaşma: Niyə Compose Multiplatform (CMP)?

Mövcud Android tətbiqimiz 100% **Jetpack Compose** və müasir **MVI / Clean Architecture** ilə yazıldığı üçün ən optimal yol **Compose Multiplatform**-dur:
- **Kod Təkrarının 85-90% Azalması:** UI ekranları (`SurahListScreen`, `SurahDetailScreen`, `SettingsScreen`, `VerseCard`, `AudioPlayerBar`), Domain UseCase-ləri və ViewModels həm Android, həm də iOS üçün **tək bir ortaq kod bazasında (`commonMain`)** saxlanılacaq.
- **İki fərqli dildə (Swift və Kotlin) sıfırdan yazmağa ehtiyac qalmır.**
- **Görünüş və Davranış Eyniliyi:** Android-dəki animasiyalar, zərif dizayn, ərəb xəttatlığı və mövzular iOS-da eynilə və piksel dəqiqliyi ilə işləyir.

---

## 💰 2. Apple Developer Program Tələbləri və Xərclər

### A. Üzvlük və Ödənişlər
- **İllik Ödəniş:** **$99 USD / il** (Apple Developer Program rüsumu).
  - Bu ödəniş tətbiqin həm TestFlight-da ictimai paylanması, həm də rəsmi **Apple App Store**-a yerləşdirilməsi üçün məcburidir.
  - İlk 3 ay üçün xüsusi pulsuz sınaq müddəti tərtibatçılar üçün mövcud deyil; Apple hesabı illik birdəfəlik $99 abunəliklə aktivləşdirilir.

### B. Hesab Tipləri (Seçiminizə Uyğun)
1. **Fərdi Hesab (Individual Developer Account):**
   - Yalnız şəxsi Apple ID və pasport / şəxsiyyət vəsiqəsi tələb olunur.
   - App Store-da tərtibatçı adı olaraq sizin şəxsi adınız (məsələn: *Sanan Haji*) görünür.
   - Açılması ən asan və ən sürətli yoldur (adətən 24-48 saat ərzində təsdiqlənir).
2. **Təşkilat Hesabı (Organization / Company Account):**
   - Şirkət/Təşkilat adına çıxarmaq üçün beynəlxalq **D-U-N-S nömrəsi** (Dun & Bradstreet) və şirkətin qeydiyyat sənədi tələb olunur.
   - App Store-da şirkətinizin və ya təşkilatın rəsmi adı görünür.

### C. $99 Ödəmədən Əvvəl Pulsuz Sınaq Mümkündürmü?
- **BƏLİ (Şəxsi Cihazda Sınaq - 0 AZN):**
  - Apple ID-si olan hər kəs Mac kompüterində Xcode vasitəsilə tətbiqi birbaşa **öz şəxsi iPhone-na** yazıb test edə bilər (Personal Team Provisioning).
  - Qeyd: Şəxsi pulsuz sertifikat hər 7 gündən bir yenilənməlidir və yalnız kabel / yerli şəbəkə ilə yazılır.
- **TestFlight (Geniş Test üçün $99 Tələb Olunur):**
  - Apple Developer Program aktiv olduqdan sonra Apple-ın **TestFlight** platformasından istifadə edə bilirik:
    - **100 daxili testçi:** Dərhal (yoxlanışsız) yükləyə bilər.
    - **10,000 xarici testçi:** İctimai link vasitəsilə heç bir kabel olmadan sadəcə linkə klikləyərək tətbiqi iPhone-larına quraşdıra bilirlər (hər build 90 gün aktiv qalır).

---

## 🛠️ 3. Texniki Arxitektura və Kodun Daşınması (Migration Map)

Mövcud layihəmiz KMP strukturu üçün çox əlverişlidir. Komponentlərin bölgüsü:

| Komponent | Mövcud Android Həlli | KMP / iOS Qarşılığı | Status / Çətinlik |
| :--- | :--- | :--- | :--- |
| **Domain Layer** | `DomainEntities`, `DomainUseCases`, Repositories | `commonMain` qovluğunda 100% eynilə qalır | 🟢 Dəyişiklik yoxdur |
| **UI Ekranları & Komponentlər** | Jetpack Compose (`SurahCard`, `VerseCard`, Dialogs) | Compose Multiplatform (CMP) | 🟢 90% birbaşa işləyir |
| **State & MVI** | `StateFlow`, `SharedFlow`, `ViewModel` | `androidx.lifecycle:lifecycle-viewmodel` (KMP dəstəkli) | 🟢 Tam dəstəklənir |
| **Database (SQLite)** | `QuranDatabaseHelper` (Android SQLite) | **Room KMP** (androidx.room 2.7+) və ya **SQLDelight** | 🟡 KMP konfiqurasiyası |
| **Parametrlər & Keş** | Android `SharedPreferences` / DataStore | **`multiplatform-settings`** (iOS `NSUserDefaults` / Android `SharedPreferences`) | 🟢 Çox sadə keçid |
| **Audio Pleyer** | Android `Media3 / ExoPlayer` + `ForegroundService` | `expect/actual` interfeysi: Android-də ExoPlayer, iOS-da `AVPlayer` + `MPNowPlayingInfoCenter` | 🟡 Platforma-spesifik kod |
| **Xatırlatmalar** | `AlarmManager` + `BroadcastReceiver` | Android: `AlarmManager`, iOS: `UNUserNotificationCenter` | 🟡 Platforma-spesifik kod |
| **Analitika & Xətalar** | Firebase Crashlytics & Analytics | `GitLive Firebase Kotlin SDK` və ya rəsmi KMP modulları | 🟢 Kitabxana dəstəyi var |

---

## 📁 4. Layihənin Yeni Qovluq Strukturu (KMP)

```text
Quranoxu/
├── composeApp/                     # Əsas çoxplatformalı modul
│   ├── src/
│   │   ├── commonMain/             # Bütün ORTAQ kodlar (UI, ViewModel, UseCase, Data)
│   │   │   ├── kotlin/az/sananhaji/quranoxu/
│   │   │   │   ├── domain/         # Vahid Entity və UseCase-lər
│   │   │   │   ├── presentation/   # Vahid Compose UI ekranları və komponentlər
│   │   │   │   ├── data/           # Repository, DB və Audio interfeysləri
│   │   │   │   └── util/           # Köməkçi alətlər
│   │   │   └── composeResources/   # Şəkillər, şriftlər və ikonlar
│   │   │
│   │   ├── androidMain/            # Yalnız Android-ə məxsus kodlar
│   │   │   └── kotlin/az/sananhaji/quranoxu/
│   │   │       ├── service/        # Android ExoPlayer xidməti
│   │   │       └── MainActivity.kt
│   │   │
│   │   └── iosMain/                # Yalnız iOS-a məxsus kodlar
│   │       └── kotlin/az/sananhaji/quranoxu/
│   │           ├── audio/          # iOS AVPlayer və Kilit Ekranı (NowPlaying) inteqrasiyası
│   │           └── MainViewController.kt # iOS üçün Compose UI pəncərəsi
│   └── build.gradle.kts
│
├── iosApp/                         # Doğma Xcode Layihəsi (Swift wrapper)
│   ├── iosApp/
│   │   ├── iOSApp.swift            # iOS tətbiqin başlanğıc nöqtəsi
│   │   ├── Info.plist              # İcazələr (Background Audio, Bildirişlər)
│   │   └── Assets.xcassets         # iOS ikonları və App Store qrafikləri
│   └── iosApp.xcodeproj
│
├── gradle/
└── settings.gradle.kts
```

---

## 📋 5. Apple App Store Tələbləri və Qaydaları (Review Guidelines)

Tətbiqin Apple tərəfindən rədd edilməməsi (rejection olmaması) üçün diqqət edilməli qaydalar:

1. **Background Audio Rejimi (`UIBackgroundModes`):**
   - Quran səsinin telefon kilidli olanda və ya digər tətbiqlərə keçəndə kəsilməməsi üçün `Info.plist`-də `audio` fon rejimi qeyd olunmalıdır.
   - Apple Review üçün Quran dinləmə videosu və ya izahat dərhal qəbul olunur.
2. **Kilit Ekranı Audio İdarəetməsi (`MPNowPlayingInfoCenter` & `MPRemoteCommandCenter`):**
   - iOS-da arxa planda səs oxudan tətbiqlər kilit ekranında və İdarəetmə Mərkəzində (Control Center) surənin adı, qari və Play/Pause düymələrini göstərməlidir.
3. **Safe Area və Ekran Kənarları:**
   - iPhone-ların "Dynamic Island" və çərtik (notch) hissələrinə uyğunlaşma Compose Multiplatform-da `WindowInsets.safeDrawing` ilə avtomatik idarə olunur.
4. **App Store Qrafikləri:**
   - **Tətbiq İkonu:** 1024x1024 PNG (alfa kanalsız, şəffaf olmayan fon).
   - **Ekran Görüntüləri (Screenshots):**
     - 6.7 düym (iPhone 15/16 Pro Max - 1290 x 2796 px).
     - 6.5 düym (iPhone 11 Pro Max / XS Max - 1242 x 2688 px).
5. **Privacy Nutrition Labels & Privacy Policy:**
   - Hazırladığımız `https://sananhaji.github.io/QuranOxu/privacy-policy.html` linki App Store Connect-də də Məxfilik Siyasəti kimi qəbul olunacaq.

---

## 🗓️ 6. Mərhələli İcra Planı (Roadmap)

### 🔹 Faza 1: Layihə Strukturunun KMP-yə Transformasiyası (2-3 Gün)
- Mövcud Android layihəsində KMP modulunun (`composeApp` və `iosApp`) konfiqurasiya edilməsi.
- Gradle versiyalarının (Kotlin 2.0+, Compose Multiplatform 1.6+) sazlanması.
- `commonMain` moduluna Domain və Presentation qatının köçürülməsi.

### 🔹 Faza 2: Database və Data Qatının Port Edilməsi (3-4 Gün)
- `quran.db` SQLite bazasının iOS resurslarına inteqrasiyası (Room KMP və ya SQLDelight ilə).
- Parametrlərin (`SettingsPreferences`) `multiplatform-settings` ilə əvəzlənməsi.
- Bütün surə və ayə sorğularının həm Android, həm iOS-da yoxlanılması.

### 🔹 Faza 3: iOS Audio Mühərriki və Kilit Ekranı Nəzarəti (3-5 Gün)
- iOS üçün `AVPlayer` əsaslı audio oxutma servisi.
- Kilit ekranında (Lock Screen) və Apple Watch / Bluetooth qulaqlıqlarda surə adı, ifaçı və Play/Pause düymələrinin sazlanması.
- Ekran görüntülərinin iOS ölçülərində (1290x2796) hazırlanması.

### 🔹 Faza 4: Xcode Build və Şəxsi iPhone-da Sınaq (1-2 Gün)
- Layihənin Mac-də Xcode vasitəsilə derlənməsi (build edilməsi).
- Şəxsi iPhone cihazınızda tətbiqin quraşdırılması və real sınaq (0 AZN xərclə).

### 🔹 Faza 5: Apple Developer Qeydiyyatı, TestFlight və App Store (1 Həftə)
- Apple Developer Program ($99/il) aktivasiyası.
- TestFlight-a ilk versiyanın göndərilməsi və ictimai testçilərin cəlb edilməsi.
- App Store Connect-də metadata, skrinşotlar və məxfilik məlumatlarının daxil edilməsi.
- Apple Review komandasına göndərilməsi və uğurlu nəşr! 🚀
