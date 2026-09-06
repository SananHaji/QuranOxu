# 📋 QuranOxu iOS - Hərtərəfli QA Test Planı və Test Keyslər (Test Cases)

Bu sənəd **QuranOxu iOS** (Compose Multiplatform) tətbiqinin bütün funksionallıqlarının QA (Quality Assurance) test keyslərini və sınaq nəticələrini ehtiva edir.

---

## 📑 Test Keyslərin Xülasəsi

| ID | Test Sahəsi | Test Keysin Adı | Nəticə |
|---|---|---|---|
| **TC-01** | Baza & Siyahı | 114 Surənin düzgün yüklənməsi və metadata (Məkkə/Mədinə, ayə sayı, ərəbcə adı) | ✅ Keçdi |
| **TC-02** | Axtarış | Surə adı, tərcümə adı, nömrəsi və ərəbcə hərflərlə axtarış | ✅ Keçdi |
| **TC-03** | Çeşidləmə | 5 fərqli çeşidləmə rejimi (Qurandakı sıra, Nüzul sırası, Əlifba, Ayə sayı az/çox) | ✅ Keçdi |
| **TC-04** | Audio Yükləmə | «Yüklə» düyməsi, canlı faiz indikatoru (`0% -> 100%`) və «Oflayn» statusuna keçid | ✅ Keçdi |
| **TC-05** | Oflayn Dinləmə | İnternetsiz rejimdə yerli keşdən MP3 faylların oxunması | ✅ Keçdi |
| **TC-06** | Keş İdarəsi | Oflayn surənin silinməsi, təsdiq dialoqu və statusun yenidən «Yüklə»yə qayıtması | ✅ Keçdi |
| **TC-07** | Fərdi Ayə Dinləmə | Ayə kartında səs düyməsi, audio axını və aktiv ayənin zümrüd yaşılı haşiyə ilə vurğulanması | ✅ Keçdi |
| **TC-08** | Avtomatik Keçid | «Surəni Dinlə» düyməsi: 1-ci ayə bitəndə avtomatik 2-ciyə keçmə və siyahının avtomatik sürüşməsi | ✅ Keçdi |
| **TC-09** | Alt Audio Bar | Pleyer idarəetməsi: Play, Pause, İrəli, Geri və «X» ilə pleyerin bağlanıb səsin kəsilməsi | ✅ Keçdi |
| **TC-10** | Yuxu Taymeri | Taymer dialoqu, dəqiqə seçimi, alt barda canlı geri sayım və vaxt bitəndə səsin dayanması | ✅ Keçdi |
| **TC-11** | Səs Sürəti | 0.75x, 1.0x, 1.25x, 1.5x, 2.0x sürət seçimi və səsin sürətinin anında tənzimlənməsi | ✅ Keçdi |
| **TC-12** | Ayə Detalları | Ərəbcə xətt, Azərbaycan tərcüməsi, oxunuş transkripsiyası və ərəbcə mətni gizlət/göstər | ✅ Keçdi |
| **TC-13** | Əlfəcinlər | Ayəni əlfəcinə əlavə etmə, «Qeydlər» bölməsində əks olunma və bir toxunuşla həmin ayəyə keçid | ✅ Keçdi |
| **TC-14** | Şəxsi Qeydlər | Ayəyə qeyd yazma, redaktə etmə, silmə və «Qeydlər» bölməsində siyahılanması | ✅ Keçdi |
| **TC-15** | Oxunma İzləmə | Ayələr oxunduqca tərəqqinin artması, surə kartında faiz xətti və sıfırlama dialoqu | ✅ Keçdi |
| **TC-16** | Analitika | Oxunan ayələrin ümumi sayı, faizi, tamamlanan surələr və növbəti oxu tövsiyəsi | ✅ Keçdi |
| **TC-17** | Tənzimləmələr | Açıq / Tünd / Sistem mövzuları, baza dili seçimi, audio dili seçimi və keşi təmizləmə | ✅ Keçdi |

---

## 🔍 Ətraflı Test Keyslər və Test Addımları

### TC-01: 114 Surənin Yüklənməsi və Görünüşü
* **İlkin şərt:** Tətbiq açılır.
* **Addımlar:**
  1. Əsas ekranı aşağı sürüşdürərək 1-dən 114-ə qədər surələrə baxmaq.
  2. Surə kartındakı məlumatları yoxlamaq: Nömrə, Azərbaycan adı, Tərcümə adı, Məkkə/Mədinə yeri, Ayə sayı, Ərəbcə adı.
* **Gözlənilən nəticə:** Bütün 114 surə tam və xətasız əks olunur.

### TC-04: Audio Yükləmə (Oflayn İstifadə Üçün)
* **İlkin şərt:** İnternet bağlantısı aktivdir.
* **Addımlar:**
  1. Fatihə surəsində və ya Surə detalında «Yüklə» / «Səsi yüklə» düyməsini sıxmaq.
  2. Tərəqqi faizini izləmək.
* **Gözlənilən nəticə:** Arxa fonda ayələrin MP3 faylları yüklənir, faiz artır (məs: `28%`, `71%`, `100%`), bitdikdən sonra kartda yaşıl «Oflayn» nişanı çıxır.

### TC-07 & TC-08: Audio Dinləmə və Ayələrarası Avtomatik Keçid
* **İlkin şərt:** Fatihə surəsi açılıb.
* **Addımlar:**
  1. «Surəni Dinlə» düyməsinə toxunmaq.
  2. 1-ci ayənin oxunuşunu gözləmək.
  3. 1-ci ayə bitdikdə 2-ci ayəyə keçidi müşahidə etmək.
* **Gözlənilən nəticə:**
  - 1-ci ayə oxunarkən ətrafında 2.dp zümrüd yaşılı haşiyə görünür və «Ayə 1» nişanı canlı yaşıl olur.
  - Ayə bitən kimi AVPlayer avtomatik 2-ci ayəni başlatmalı, haşiyə 2-ci ayəyə keçməli və ekran avtomatik aşağı sürüşməlidir.

### TC-09: Alt Audio Player Bar və «X» Düyməsi
* **İlkin şərt:** Səs oxunur.
* **Addımlar:**
  1. Geri qayıdıb surələr siyahısına çıxmaq.
  2. Altda açılan qeyzer pleyer barında «X» (Bağla) düyməsini sıxmaq.
* **Gözlənilən nəticə:** Səs anında susur və alt pleyer paneli aşağı sürüşərək ekrandan tamamilə yox olur.

### TC-13 & TC-14: Əlfəcinlər və Qeydlər
* **İlkin şərt:** Surə detal ekranı.
* **Addımlar:**
  1. Ayə kartında əlfəcin ikonuna basmaq (dolu əlfəcinə çevrilir).
  2. Qələm ikonuna basıb "Təfəkkür" qeydi yazaraq yadda saxlamaq.
  3. Alt menyudan «Qeydlər» bölməsinə keçmək.
* **Gözlənilən nəticə:** Həm əlfəcinlər, həm də qeydlər tabında həmin ayələr görünür və kliklədikdə birbaşa həmin ayəyə aparır.
