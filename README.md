<p align="center">
  <img src="shared/src/commonMain/composeResources/drawable/app_icon.png" width="128" height="128" alt="RubikSync Logo"/>
</p>

# 🧩 RubikSync - Kotlin Multiplatform Rubik Küpü Çözücü & 3D Simülatör

**RubikSync**, Android, iOS ve Masaüstü (JVM) platformlarında çalışan; fiziksel Rubik Küpünüzü kamera veya galeri aracılığıyla tarayarak saniyeler içinde **3D simülasyon ortamında çözüm adımlarını sunan** yenilikçi bir Kotlin Multiplatform mobil ve masaüstü uygulamasıdır.

Uygulama; gelişmiş renk analizi filtreleri, platforma özgü yerel kamera akışları, interaktif kılavuz çizgileri, zenginleştirilmiş 3D görselleştirme motoru ve en gelişmiş zeka küpü çözme algoritmalarından biri olan **Kociemba Algoritması**'nı bünyesinde barındırır.

---

## ✨ Öne Çıkan Özellikler

### 📷 Görüntü İşleme & Kamera Yönetimi
- **Özel Kamera Arayüzü (Android & iOS):** Sistem kamerasından bağımsız olarak Android tarafında **CameraX**, iOS tarafında **AVFoundation** mimarileriyle yerel kamera önizlemeleri ve çekim kontrolleri sunulur.
- **Kare Hizalama Izgarası:** Ekranın en dar kenarını baz alan dinamik dashed kare çerçeve ve 9 adet çıkartmanın (sticker) tam merkezinde yer alan yeşil örnekleme dots.
- **İnteraktif Kalibrasyon Sihirbazı:** Fotoğraf çekildikten veya galeriden yüklendikten sonra **Izgara Boyutu**, **Yatay Konum** ve **Dikey Konum** sürgüleriyle yeşil ızgaranın piksel koordinatları ince ayarlanabilir.
- **Seçici Piksel Filtrelemesi (Selective Averaging):** Plastik siyah ızgara çizgileri (`RGB < 45`) ile parlama ve yansımalar (`MaxRGB > 250`) otomatik olarak filtrelenerek temiz piksel renk ortalamaları toplanır.
- **Gelişmiş Renk Sınıflandırma:** İnsan gözünün renkleri algılama biçimine en yakın olan **CIE L*a*b*** renk uzayı ve merkez renkler referans alınarak hesaplanan **CIEDE2000 (Delta E 2000)** formülüyle gölge ve yansımalardan arındırılmış kararlı renk eşlemeleri yapılır.
- **Otomatik Hata Düzeltme & Doğrulama:** Tarama tamamlandığında 54-renk haritası [RubikCubeState](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/ui/state/RubikAppState.kt) durumuna aktarılır. Küp çözülebilirse pencereler otomatik kapanır, hatalı ise [EditorDialog.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/ui/dialogs/EditorDialog.kt) hata şeridiyle açık kalır ve elle renk düzeltmesine imkan tanır.

### 🎨 3D Grafik & Simülasyon Motoru
- **İnteraktif 3D Küp:** Compose Multiplatform Canvas üzerinde çalışan; fare veya dokunmatik hareketlerle serbestçe döndürülebilen (**Orbit**), yakınlaştırılabilen (**Zoom**) ve kaydırılabilen (**Pan**) 3D modelleme.
- **Çözüm Oynatıcı:** Çözüm adımlarının 3D model üzerinde animasyonlu olarak oynatılması, hamle hızının kaydırıcı yardımıyla canlı olarak ayarlanabilmesi.
- **Dinamik Rehber Küp (Guide Cube):** [CubeRotationGuide.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/CubeRotationGuide.kt) ile tarama adımlarında kameranın taranan yüze en kısa açısal yoldan ($\Delta\theta \le \pi$ normalizasyonu ile) dönmesi ve taranmış tüm yüzlerin renklerinin kılavuz üzerinde kalıcı olarak gösterilmesi.
- **Katman Dönüş Kilidi & İki Parmakla Genel Döndürme:** Tek parmakla küp üzerindeki bir çıkartma sürüklendiğinde tüm küpün dönmesi engellenir (Katman Dönüş Kilidi). Kullanıcı istediği an iki parmağını ekranda sürükleyerek küpü serbestçe kendi etrafında döndürebilir ve arkasını/altını kolayca inceleyebilir.

### 🎧 Akustik Sentez & Kullanıcı Deneyimi
- **Gerçekçi Mekanik Ses Efektleri:** Küpün her dönüşünde (manuel çevirmeler, karıştırma (scramble) ve çözüm oynatma dahil) düşük gecikmeli **SoundPool** entegrasyonu ile sentezlenmiş keskin plastik zeka küpü dönüş sesi çalınır.
- **Güvenli Düzenleme Kilidi (Editable Toggle):** Üst paneldeki kilit butonu (🔓/🔒) ile küpün döndürme özellikleri kilitlenebilir. Bu sayede, 3D model üzerinde inceleme (orbit/zoom/pan) yaparken yanlışlıkla dönüş hamlelerinin tetiklenmesi engellenir.

### ⚙️ Arayüz, Navigasyon & Çoklu Dil
- **Jetpack Compose Navigation:** Splash Screen -> Dashboard -> Settings ekranları arası geçişler standart `NavHost` ve `rememberNavController` mimarisine geçirilerek tamamen rota tabanlı hale getirilmiştir. Ekran geçişleri sağdan sola kayma (`slideInHorizontally`) ve fade geçiş animasyonları ile zenginleştirilmiştir.
- **Çoklu Dil Desteği:** Ayarlar ekranında yer alan modern dil seçici dropdown menü aracılığıyla uygulama dili (TR, EN, JA, DE, RU, FR, ES, vb.) canlı olarak değiştirilebilir. Menünün açılış animasyonu ile dış kartın genişleme hareketi senkronize edilerek sıfır gecikmeli, premium bir geçiş sağlanmıştır.

---

## 📊 Matematiksel Temeller ve Algoritmalar

### 1. 3D Uzay Dönüşleri, Kuaterniyonlar ve Rodrigues Formülü
3D simülasyondaki her bir küp parçacığının (cubie) konumu ve yönelimi dönüşler sırasında güncellenir. Bir parçacığın konum vektörünü ($\mathbf{v}$), dönme ekseni ($\mathbf{u}$) etrafında $\theta = \pi/2$ radyan (90 derece) kadar döndürmek için **Rodrigues Rotasyon Formülü** kullanılır:
$$\mathbf{v}' = \mathbf{v} \cos\theta + (\mathbf{u} \times \mathbf{v}) \sin\theta + \mathbf{u} (\mathbf{u} \cdot \mathbf{v}) (1 - \cos\theta)$$
Burada $\mathbf{v}'$ dönme sonrası yeni konum vektörünü, $\mathbf{u}$ normalize edilmiş dönme eksenini, $\times$ çapraz çarpımı ve $\cdot$ ise nokta çarpımı temsil eder.

#### 🎥 Kamera Projeksiyon Modeli
Kameranın 3D uzaydaki koordinatları ekran alanına yansıtılırken [CubeScreenProjector.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/CubeScreenProjector.kt) sınıfı tarafından aşağıdaki matematiksel dönüşüm matrisleri uygulanır:
$$\mathbf{v}_{\text{camera}} = R_x(\phi) R_y(\theta) \mathbf{v}_{\text{world}}$$
$$R_y(\theta) = \begin{bmatrix} \cos\theta & 0 & \sin\theta \\ 0 & 1 & 0 \\ -\sin\theta & 0 & \cos\theta \end{bmatrix}, \quad R_x(\phi) = \begin{bmatrix} 1 & 0 & 0 \\ 0 & \cos\phi & -\sin\phi \\ 0 & \sin\phi & \cos\phi \end{bmatrix}$$
$$\text{depth} = d + z_c, \quad \text{scale} = \frac{f}{\text{depth}}$$
$$x_{\text{screen}} = \frac{W}{2} + \text{pan}_x + x_c \cdot \text{scale}, \quad y_{\text{screen}} = \frac{H}{2} - \text{pan}_y - y_c \cdot \text{scale}$$

#### 🔄 Kamera Açıları En Kısa Yol Normalizasyonu (Shortest-Path Interpolation)
Kamera açısı geçişleri sırasında oluşan sarsıntıları engellemek için, hedef açı ile mevcut açı arasındaki fark en kısa rotasyon yönüne normalize edilir:
$$\Delta\theta = \operatorname{atan2}(\sin(\theta_{\text{target}} - \theta_{\text{current}}), \cos(\theta_{\text{target}} - \theta_{\text{current}}))$$
Bu sayede yaw geçişleri hiçbir zaman $\pi$ radyandan (180 derece) daha fazla dönmez, ters yönden en kısa yolu tercih eder.

### 2. Grup Teorisi ve Rubik Küpü Grubu ($\mathcal{G}$)
Rubik Küpünün tüm geçerli durumları, matematiksel olarak simetrik permütasyon grubunun bir alt grubunu oluşturur. Küpün 8 köşe ve 12 kenar parçası bulunmaktadır:
- Köşelerin permütasyonu $\sigma \in \mathcal{S}_8$, yönelimleri $x \in \mathbb{Z}_3^8$.
- Kenarların permütasyonu $\tau \in \mathcal{S}_{12}$, yönelimleri $y \in \mathbb{Z}_2^{12}$.

Rubik Küpü Grubu $\mathcal{G}$, aşağıdaki 3 fiziksel kısıtı sağlayan tüm $(\sigma, x, \tau, y)$ bileşenlerinden oluşur:
1. **Köşe Yönelim Kısıtı:** $\sum_{i=1}^8 x_i \equiv 0 \pmod 3$
2. **Kenar Yönelim Kısıtı:** $\sum_{i=1}^{12} y_i \equiv 0 \pmod 2$
3. **Permütasyon Parite Kısıtı:** $\operatorname{sgn}(\sigma) = \operatorname{sgn}(\tau)$

Bu kısıtlar toplam grubu $12$ kat daraltır. Böylece grubun mertebesi (eleman sayısı):
$$|\mathcal{G}| = \frac{8! \cdot 3^8 \cdot 12! \cdot 2^{12}}{12} = 8! \cdot 3^7 \cdot 12! \cdot 2^{10} = 43,252,003,274,489,856,000 \approx 4.33 \times 10^{19}$$

Tomas Rokicki vd. (2010) tarafından yapılan bilgisayar destekli ispatlar, Rubik Küpü grubundaki herhangi bir durumun **Half-Turn Metric (HTM)** standardında en fazla **20 hamlede** çözülebileceğini matematiksel olarak kanıtlamıştır (Tanrı Sayısı).

### 3. Kociemba İki Fazlı (Two-Phase) Arama Algoritması
[RubikSolver.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/solver/RubikSolver.kt) dosyasında implement edilen Kociemba algoritması, devasa arama uzayını çözmek için aramayı iki bağımsız faza böler:
- **Phase 1 (G -> G1):** Küpün köşelerinin yönelimlerini ($3^7 = 2187$ durum), kenarlarının yönelimlerini ($2^{11} = 2048$ durum) ve orta katmandaki (UD-slice) 4 kenarın yerleşimini ($\binom{12}{4} = 495$ durum) düzeltir. Durum uzayı $N_1 \approx 2.21 \times 10^9$'dur. Bu faz tamamlandığında küp, yalnızca şu üreteçlerin alt kümesiyle çözülebilecek $G_1$ alt grubuna ulaşmış olur:
  $$G_1 = \langle U, D, R^2, L^2, F^2, B^2 \rangle$$
- **Phase 2 (G1 -> Çözüm):** Yalnızca $G_1$ üreteçlerini (yan yüzlerin 180° yarım dönüşleri ve üst/alt yüzlerin dönüşleri) kullanarak köşelerin permütasyonunu ($8! = 40,320$), UD-slice kenarlarının permütasyonunu ($4! = 24$) ve geri kalan 8 kenarın permütasyonunu ($8! = 40,320$) çözer. Durum uzayı $N_2 \approx 3.90 \times 10^{10}$'dur.

Arama performansını optimize etmek amacıyla her iki faz için bellek üzerinde mesafe/budama tabloları (pruning tables) tutulur ve Iterative Deepening $A^*$ (IDA*) derinlik aramasıyla birleştirilerek milisaniyeler içinde optimal çözümü üretir.

### 4. CIE L*a*b* Renk Dönüşüm Formülü ve CIEDE2000 (Delta E 2000)
Ortam ışığı, gölgeler ve parlama gürültülerini filtrelemek amacıyla [RubikImageProcessor.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/RubikImageProcessor.kt) sınıfı pikselleri sRGB uzayından CIE L*a*b* uzayına dönüştürür.

1. **RGB -> XYZ Dönüşümü:**
   $$V_{\text{linear}} = \begin{cases} \frac{V_{\text{srgb}}}{12.92} & \text{if } V_{\text{srgb}} \le 0.04045 \\ \left(\frac{V_{\text{srgb}} + 0.055}{1.055}\right)^{2.4} & \text{otherwise} \end{cases} \quad (V \in \{R, G, B\})$$
   D65 referans aydınlatıcısı ($X_n=95.047, Y_n=100.000, Z_n=108.883$) altında dönüşüm matrisi uygulanır:
   $$\begin{bmatrix} X \\ Y \\ Z \end{bmatrix} = \begin{bmatrix} 0.4124 & 0.3576 & 0.1805 \\ 0.2126 & 0.7152 & 0.0722 \\ 0.0193 & 0.1192 & 0.9505 \end{bmatrix} \begin{bmatrix} R_{\text{linear}} \times 100 \\ G_{\text{linear}} \times 100 \\ B_{\text{linear}} \times 100 \end{bmatrix}$$

2. **XYZ -> CIE L*a*b* Dönüşümü:**
   $$f(t) = \begin{cases} t^{1/3} & \text{if } t > 0.008856 \\ 7.787 t + \frac{16}{116} & \text{otherwise} \end{cases}$$
   $$L^* = 116 f(Y/Y_n) - 16, \quad a^* = 500 [f(X/X_n) - f(Y/Y_n)], \quad b^* = 200 [f(Y/Y_n) - f(Z/Z_n)]$$

3. **CIEDE2000 Renk Farkı Formülü:**
   $$\Delta E_{00} = \sqrt{\left(\frac{\Delta L'}{k_L S_L}\right)^2 + \left(\frac{\Delta C'}{k_C S_C}\right)^2 + \left(\frac{\Delta H'}{k_H S_H}\right)^2 + R_T \left(\frac{\Delta C'}{k_C S_C}\right) \left(\frac{\Delta H'}{k_H S_H}\right)}$$
   Burada parlaklık değişimlerine (gölge, ışık şiddeti) karşı algoritmanın toleransını artırmak için açıklık ağırlık katsayısı **$k_L = 2.0$** olarak seçilmiştir. Mavi-yeşil renk kaymalarını düzeltmek için kullanılan rotasyon terimi ($R_T$) ise:
   $$R_T = -\sin(2\Delta\theta) R_C, \quad R_C = 2 \sqrt{\frac{\bar{C}'^7}{\bar{C}'^7 + 25^7}}$$

### 5. Kuhn-Munkres (Hungarian) Algoritması ile Renk Dağılım Optimizasyonu
Küpün 6 adet merkez rengi referans olarak belirlendikten sonra, geri kalan 48 hücrenin (köşe ve kenar etiketleri) her birine tam olarak **8 adet** renk atanması gerekmektedir (6 yüzey $\times$ 8 = 48 hücre). Bu kısıt, $48 \times 48$ boyutlarında bir maliyet matrisi ($C$) kurulmasıyla çözülür.

#### 🧮 Matematiksel Model
Kuhn-Munkres algoritması aşağıdaki doğrusal programlama (integer linear programming) problemini $O(N^3)$ zamanda çözer:
$$\min \sum_{i=1}^{48} \sum_{j=1}^{48} C_{ij} x_{ij}$$
Kısıtlar:
$$\sum_{j=1}^{48} x_{ij} = 1 \quad (\forall i), \quad \sum_{i=1}^{48} x_{ij} = 1 \quad (\forall j), \quad x_{ij} \in \{0, 1\}$$
Burada $x_{ij} = 1$ olması, $i$. taranan hücrenin $j$. renge atanması anlamına gelir. Bu süreç sonucunda, fiziksel olarak çözülmesi imkansız olan taranan hatalı durumlar matematiksel olarak en yakın geçerli küp durumuna otomatik olarak optimize edilmiş olur.

---

## 🏗️ Sistem Mimarisi ve Veri Akış Modeli

### 1. Global Uygulama Mimarisi ve Veri Akış Modeli
Aşağıdaki diyagramda, kameradan veya galeriden alınan ham görüntünün işlenip, matematiksel optimizasyonlardan geçerek 3D Kotlin simülatörüne ve Kociemba çözücüsüne aktarılma süreci gösterilmiştir:

```mermaid
graph TD
    %% Katman Tanımlamaları
    subgraph UI ["Görünüm Katmanı (Compose Multiplatform)"]
        Dashboard["Dashboard & 3D Canvas"]
        Scanner["ScannerWizard (Kamera & Hizalama)"]
        Editor["EditorDialog (2D Ağ Haritası & Elle Boyama)"]
    end

    subgraph Platform ["Platform Arayüzleri (expect/actual)"]
        CamAndroid["CameraX (Android)"]
        CamiOS["AVFoundation (iOS)"]
        SoundAndroid["SoundPool Engine (Android)"]
    end

    subgraph Processor ["Görsel İşleme & Optimizasyon Motoru"]
        ImgProc["RubikImageProcessor"]
        LabConv["RGB -> CIE L*a*b* Dönüşümü"]
        DeltaE["CIEDE2000 Renk Farkı Analizi"]
        Hungarian["Hungarian Algorithm (Bipartite Matching)"]
    end

    subgraph Core ["3D Simülatör & Çözücü Mimarisi"]
        CubeState["RubikCubeState (3D Cubies)"]
        Solver["RubikSolver (Kociemba Search)"]
        Persistence["DataStore & Room DB"]
    end

    %% Veri Akış Bağlantıları
    CamAndroid -->|Ham JPG / Galeri Resmi| Scanner
    CamiOS -->|Ham JPG / Galeri Resmi| Scanner
    
    Scanner -->|Hizalanmış Piksel Matrisi| ImgProc
    ImgProc --> LabConv
    LabConv --> DeltaE
    DeltaE -->|Hata/Gölgeleri Filtrele| Hungarian
    
    Hungarian -->|Optimum 54-Renk Durumu| Editor
    Editor -->|Onayla / Elle Düzelt| CubeState
    
    CubeState -->|toSnapshot| Solver
    Solver -->|Çözüm Adımları (Moves)| Dashboard
    CubeState -->|onMoveStarted| SoundAndroid
    
    CubeState -->|Kalıcılık Kaydet| Persistence
    Persistence -.->|Geri Yükle| CubeState

    %% Stil Tanımlamaları
    style UI fill:#0f172a,stroke:#38bdf8,stroke-width:2px,color:#fff
    style Platform fill:#1e1b4b,stroke:#818cf8,stroke-width:2px,color:#fff
    style Processor fill:#14532d,stroke:#4ade80,stroke-width:2px,color:#fff
    style Core fill:#7c2d12,stroke:#fb923c,stroke-width:2px,color:#fff
```

### 2. 3D Render ve Projeksiyon Matematik Akışı
```mermaid
graph TD
    subgraph DataState ["Küp Durum Verisi"]
        RubikCubeState["RubikCubeState"]
        CubieTransforms["27 x CubieTransform<br>(Konum & Yönelim Matrisleri)"]
        RubikCubeState -->|temsil eder| CubieTransforms
    end

    subgraph Geometry3D ["3D Geometri ve Dönüşüm Motoru"]
        CubeStickerGeometry["CubeStickerGeometry<br>(Sticker Köşe Noktaları)"]
        MoveMathHelper["MoveMathHelper<br>(Dönüş Eksenleri)"]
        Rodrigues["Vector3.rotateAround()<br>(Rodrigues Formülü)"]
        
        CubieTransforms -->|Dönüş Tetiklendiğinde| Rodrigues
        MoveMathHelper -->|Dönüş Ekseni & Açı| Rodrigues
        CubeStickerGeometry -->|Lokal Koordinatlar| Rodrigues
    end

    subgraph CamProjection ["Kamera ve Projeksiyon Modeli (CubeScreenProjector)"]
        YawMatrix["R_y(yaw) Rotasyonu"]
        PitchMatrix["R_x(pitch) Rotasyonu"]
        DepthCalc["Depth = distance + Z_camera"]
        ScaleCalc["Scale = focalLength / Depth"]
        ViewportMapping["Ekran Piksel Eşleme<br>(centerX + panX + X * scale)"]
        
        Rodrigues -->|3D Dünya Koordinatı| YawMatrix
        YawMatrix --> PitchMatrix
        PitchMatrix -->|Kamera Uzayı (Xc, Yc, Zc)| DepthCalc
        DepthCalc --> ScaleCalc
        PitchMatrix -->|Xc, Yc| ViewportMapping
        ScaleCalc --> ViewportMapping
    end

    subgraph Rendering ["Çizim ve Derleme Arayüzü"]
        SortFaces["Ressam Algoritması (Painters Algorithm)<br>(Z-Derinliğine Göre Sıralama)"]
        BackfaceCulling["Backface Culling<br>(Yüzey Normali ile Kamera Vektörü Dot Product)"]
        CanvasDraw["Compose Canvas.drawPath()<br>(Renk & Gölgeleme Uygulaması)"]
        
        ViewportMapping -->|2D Ekran Noktaları| SortFaces
        BackfaceCulling -->|Görünmeyen Yüzleri Ele| SortFaces
        SortFaces -->|Sırayla Çiz| CanvasDraw
    end

    %% Stil Tanımlamaları
    style DataState fill:#1e293b,stroke:#38bdf8,stroke-width:2px,color:#fff
    style Geometry3D fill:#311042,stroke:#d946ef,stroke-width:2px,color:#fff
    style CamProjection fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#fff
    style Rendering fill:#7c2d12,stroke:#f97316,stroke-width:2px,color:#fff
```

---

## 📦 Teknolojik Altyapı ve Bağımlılıklar

| Alan | Kullanılan Kütüphane / Teknoloji | KMP Desteği | Açıklama |
| --- | --- | --- | --- |
| **Çekirdek** | Kotlin Multiplatform (KMP) | Evet | Android, iOS ve JVM için ortak kod paylaşımı. |
| **Arayüz (UI)** | Compose Multiplatform | Evet | Bildirimsel (Declarative) ortak UI geliştirme ortamı. |
| **Android Kamera** | Jetpack CameraX (`1.3.4`) | Hayır | `PreviewView`, `ImageCapture` yerel kamera yönetimi. |
| **iOS Kamera** | AVFoundation (`AVKit` / `UIKit`) | Hayır | `AVCaptureSession` ve `AVCapturePhotoOutput` yerel iOS kamera yönetimi. |
| **Navigasyon** | Jetpack Compose Navigation KMP (`2.8.0-alpha10`) | Evet | Rota tabanlı KMP uyumlu ekran yönetimi ve geçiş animasyonları. |
| **Veritabanı** | Android Jetpack Room DB | Evet | SQLite tabanlı küp durumlarının kalıcı olarak saklanması. |
| **Ayarlar Kalıcılığı** | Jetpack DataStore Preferences | Evet | Ses, tema, dil ve kilit durumlarının anahtar-değer (K-V) kalıcılığı. |
| **Ses Motoru** | Android SoundPool API | Hayır | Düşük gecikmeli yerel Android ses efekti motoru. |
| **Görsel İşleme** | Kotlin Native / Skia Graphics | Evet | Platform-specific görsellerin belleğe yüklenmesi ve piksel matrisine dönüştürülmesi. |
| **Algoritma** | Kociemba Algorithm | Evet | İki Fazlı (Two-Phase) optimum Rubik Küp çözücü algoritma. |

---

## 📱 Ekran ve Kullanıcı Deneyimi Tanıtımı

### 1. Ana Ekran & 3D İnteraktif Simülasyon
- Uygulama açıldığında kullanıcıyı karşılayan 3D küp görünümüdür.
- Kullanıcı küpü elle karıştırabilir, katmanları sağa-sola döndürebilir.
- Ekranın üst kısmında küpün çözülmesi için gerekli hamle sayısı yer alır.
- **"Tara ve Çöz"** butonuna basılarak tarama sihirbazı başlatılır.
- Küp başarıyla tarandıktan sonra **"Çözümü Oynat"** butonu aktifleşir ve küpün adım adım çözüm animasyonu başlar. Hamle hızı kaydırıcı yardımıyla canlı olarak ayarlanabilir.

### 2. Özel Kamera Tarama Ekranı
- **"Fotoğraf Çek"** butonuna basıldığında açılan tam ekran arayüzdür.
- Sarı kesikli bir kare çerçeve ve yeşil 3x3 ızgara çizgileri canlı kamera görüntüsünün üzerine bindirilir.
- Kullanıcı küpü tam kare çerçevenin içine hizalayarak ortadaki deklanşör butonuyla çekimi gerçekleştirir. Flaş ikonuyla karanlık ortamlarda aydınlatma sağlanabilir.

### 3. Hizalama ve Renk Kalibrasyon Arayüzü
- Fotoğraf çekildikten veya galeriden seçildikten sonra otomatik olarak açılır.
- Fotoğrafın üzerinde yeşil renkte ızgara çizgileri ve örnekleme kareleri (`patch`) gösterilir.
- Sürgülerle (Sliders) ızgara küpün üzerine tam oturtulur.
- Algılanan 3x3 renk dağılımı görsel olarak hemen altta listelenir. Eğer herhangi bir renk yanlış algılanmışsa kullanıcı o hücreye dokunarak listeden doğru rengi seçebilir.
- **"Onayla"** butonuna basıldığında o yüzün renk dağılımı kaydedilir ve bir sonraki yüze geçilir. 6 yüz de tarandığında Kociemba çözücü algoritması arka planda çalışarak çözümü üretir.

---

## 🛠️ Kurulum ve Çalıştırma

### Gereksinimler
- macOS işletim sistemi (iOS derlemesi yapabilmek için).
- Android Studio / Xcode.
- Java JDK 17+.

### Çalıştırma Komutları

#### 🤖 Android
```bash
# Android uygulamasını derleyin ve cihazda/emülatörde çalıştırın
./gradlew :androidApp:installDebug
```

#### 🍏 iOS
1. Xcode ile `/iosApp/iosApp.xcodeproj` dosyasını açın.
2. Hedef cihazı seçin (Simulator veya Fiziksel Cihaz).
3. **Run (Cmd+R)** butonuna basarak derleyin.
*(KMP modül derlemesi Xcode build phases üzerinden otomatik olarak Gradle aracılığıyla tetiklenir).*

#### 💻 Masaüstü (JVM)
```bash
# Masaüstü uygulamasını çalıştırın
./gradlew :desktopApp:run
```

---

> **[!NOTE]**
> RubikSync projesi, Kotlin Multiplatform ve Compose Multiplatform'un gücünü, gelişmiş matematiksel renk modelleri, 3D grafik projeksiyon motoru ve düşük gecikmeli sentezlenmiş ses birimleriyle birleştiren üst seviye bir mühendislik çalışmasıdır.