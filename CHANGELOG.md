# Değişiklik Kaydı

Bu dosya Inplay Radar Android uygulamasında ve APK derleme sürecinde yapılan önemli, doğrulanmış değişiklikleri listeler. Genel depo/başka projelere ait eski otomasyon notları bu kayda dahil edilmez.

## 1.0.0

### Android uygulaması
- Inplay Radar web arayüzünü `com.inplayradar.mobile` paket adıyla Android WebView içinde açan uygulama.
- Web oturum çerezleri ve sayfa içi JavaScript desteği.
- Aşağı çekerek yenileme.
- Sol kenardan kaydırmalı gezinme menüsü ve menü dışına dokunarak kapatma.

### WebView düzeltmeleri
- `.ir-skip-link` öğesi `display: none !important;` CSS kuralıyla gizlenecek şekilde düzenlendi.
- CSS, AndroidX WebKit `WebViewCompat.addDocumentStartJavaScript` kullanılarak belge başlangıcında uygulanacak şekilde taşındı.
- `WebViewFeature.DOCUMENT_START_SCRIPT` desteği kontrol edilerek uyumluluk gözetildi.
- Geç çalışan sayfa-sonu JavaScript müdahalesi ve gereksiz WebView odak geçersiz kılmaları kaldırıldı.

### Derleme ve yayınlama
- Android APK derlemesi GitHub Actions üzerinden otomatikleştirildi.
- Derleme ortamı Ubuntu 24.04 ve JDK 17 olarak düzenlendi.
- Gradle 8.10.2 ve Android API 35 / Build Tools 35.0.0 kullanıldı.
- APK, Actions Artifact olarak yüklenir.
- İmzalama anahtarı ve parolalar GitHub Actions Secrets üzerinden alınır.
- Ana dala yapılan push işlemleri `v1.0.0` Release APK'sını günceller.
- Sürüm etiketi derlemelerinde otomatik release notları etkinleştirildi; ana dal güncellemelerinde aynı release notlarının tekrar tekrar eklenmesi önlendi.

## Kayıt ilkeleri

- Yalnızca Inplay Radar'a ait, doğrulanmış değişiklikler eklenir.
- Her yeni sürüm ayrı başlık altında kaydedilir.
- Commit geçmişi denetim ve geri dönüş amacıyla korunur.
