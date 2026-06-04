<p align="center">
  <img src="shared/src/commonMain/composeResources/drawable/app_icon.png" width="128" height="128" alt="RubikSync Logo"/>
</p>

# 🧩 RubikSync - Kotlin Multiplatform Rubik Küpü Çözücü & 3D Simülatör

**RubikSync**, Android, iOS ve Masaüstü (JVM) platformlarında çalışan; fiziksel Rubik Küpünüzü kamera aracılığıyla tarayarak saniyeler içinde **3D simülasyon ortamında çözüm adımlarını sunan** yenilikçi bir Kotlin Multiplatform mobil ve masaüstü uygulamasıdır.

Uygulama; gelişmiş renk analizi filtreleri, platforma özgü yerel kamera akışları, interaktif kılavuz çizgileri, zenginleştirilmiş 3D görselleştirme motoru ve en gelişmiş zeka küpü çözme algoritmalarından biri olan **Kociemba Algoritması**'nı bünyesinde barındırır.

---

## 🚀 Öne Çıkan Özellikler

### 1. 📷 Özel Kamera Arayüzü & Kare Hizalama Kılavuzu (Android & iOS)
- **Sistem Kamerasından Bağımsız:** Android tarafında **CameraX**, iOS tarafında **AVFoundation** mimarileri kullanılarak tamamen uygulama içi çalışan yerel kamera önizlemeleri geliştirilmiştir.
- **Kare Kılavuz Çerçevesi (Dashed Yellow):** Küpün tam hizalanması için ekranın en dar kenarını baz alan dinamik kare çerçeve.
- **3x3 Hizalama Izgarası (Green Grid):** Küpün 9 adet çıkartmasını (sticker) tam hizalamayı kolaylaştıran kılavuz çizgileri ve her çıkartmanın tam merkezinde yer alan yeşil örnekleme dots.
- **Flaş ve İptal Kontrolleri:** Işığın yetersiz olduğu durumlarda flaşı tek tuşla açıp kapatabilme özelliği.

### 2. 🎛️ İnteraktif 3x3 Kalibrasyon & İnce Ayar Sihirbazı
- **Slider Kontrolleri:** Çekilen fotoğrafın kılavuzla tam örtüşmemesi durumunda **Izgara Boyutu**, **Yatay Konum** ve **Dikey Konum** ayar sürgüleri ile yeşil ızgarayı fotoğrafın üzerinde canlı olarak kaydırabilme.
- **Seçici Piksel Filtrelemesi (Selective Averaging):** 
  - Küpün plastik siyah ızgara çizgilerinden gelen koyu pikseller (`RGB < 45`) filtrelenir.
  - Specular glare (ışık parlamaları) ve yansımalar (`MaxRGB > 250` ve renk farkı `< 15`) otomatik olarak ayıklanır.
- **Manuel Renk Düzeltme:** Algoritmanın algıladığı renklerde hata olması durumunda, 3x3 önizleme alanında hatalı hücreye dokunarak elle renk değiştirebilme imkanı.

### 3. 🎨 Gelişmiş CIE L*a*b* ve CIEDE2000 Renk Sınıflandırma
- Standart RGB uzaklığı yerine insan gözünün renkleri algılama biçimine en yakın olan **CIE L*a*b* renk uzayı** kullanılmıştır.
- Küpün 6 merkez rengi (Turuncu, Kırmızı, Sarı, Beyaz, Yeşil, Mavi) kalibrasyon referansı olarak kilitlenir. Diğer köşe ve kenar hücreleri, bu merkez renklere olan **CIEDE2000 (Delta E 2000)** formülüyle en doğru şekilde sınıflandırılır.

### 4. 🕹️ İnteraktif 3D Küp Simülatörü
- Sahne üzerinde fare veya dokunmatik hareketlerle **Orbit (Döndürme)**, **Zoom (Yakınlaştırma)** ve **Pan (Ölçekleme)** desteği.
- Küpün katmanlarını arayüzdeki butonlar veya 3D hareketlerle interaktif olarak döndürebilme.
- Çözüm adımlarının 3D model üzerinde adım adım animasyonlu olarak oynatılması.

### 5. 🎵 Gerçekçi Mekanik Ses Efektleri (Android & iOS)
- Küpün her dönüşünde (manuel çevirmeler, karıştırma (scramble) ve çözüm oynatma dahil) düşük gecikmeli **SoundPool** entegrasyonu ile sentezlenmiş **gerçekçi plastik zeka küpü dönüş sesi** çalınır.
- Ses durumu (`🔊` / `🔇`) kilit butonunun hemen yanındaki ses seviyesi butonuyla kontrol edilebilir ve **DataStore Preferences** üzerinden kalıcı olarak kaydedilir.

### 6. 🔐 Güvenli Düzenleme Kilidi (Editable Toggle)
- Üst paneldeki kilit butonu (🔓/🔒) ile küpün döndürme özellikleri kilitlenebilir. Bu sayede, 3D model üzerinde inceleme (orbit/zoom/pan) yaparken yanlışlıkla dönüş hamlelerinin tetiklenmesi engellenir.
- Küp kilitlendiğinde, ses butonu da otomatik olarak inaktif hale gelir ve opaklığı azaltılarak görsel geri bildirim sağlanır.

### 7. 🛣️ Jetpack Compose Navigation Entegrasyonu
- Rotalar arası geçişler (Splash Screen -> Dashboard -> Settings) standart `NavHost`, `composable` ve `rememberNavController` mimarisine geçirilerek tamamen rota tabanlı hale getirilmiştir.
- Ekran geçişleri (özellikle Ayarlar ekranı) sağdan sola kayma (`slideInHorizontally`) ve fade geçiş animasyonları ile zenginleştirilmiştir.

### 8. 🌐 Çoklu Dil Seçeneği ve Senkronize Arayüz
- Ayarlar ekranında yer alan modern dil seçici dropdown menü aracılığıyla uygulama dili (TR, EN, JA, DE, RU, FR, ES, vb.) canlı olarak değiştirilebilir.
- Dropdown menünün dikey genişleme/kapanma animasyonu ile dış kartın genişleme hareketi senkronize edilerek sıfır gecikmeli, premium bir arayüz geçişi sağlanmıştır.

### 9. 🔄 3D Rehber Küpü (Adaptive Shortest-Path & Persistent Colors)
- [CubeRotationGuide.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/CubeRotationGuide.kt) ile tarama adımlarında kameranın taranan yüze en kısa açısal yoldan ($\Delta\theta \le \pi$ normalizasyonu ile) dönmesi sağlanır.
- Taranmış olan tüm yüzlerin renkleri (ve mevcut taranmakta olan yüz) 3D kılavuz küpü üzerinde kalıcı olarak gösterilerek kullanıcıya kusursuz bir görsel geribildirim sunulur.

### 10. 🎯 Otomatik Tarama Uygulama Lojikleri (Scanner-to-Model Auto-Apply)
- Tarama tamamlandığında geçerli küp kombinasyonu otomatik olarak [RubikCubeState](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/ui/state/RubikAppState.kt) durumuna aktarılır.
- Eğer dizilim geçerli (çözülebilir) ise tarama sihirbazı ve editör pencereleri anında kapatılarak kullanıcı paneline geçilir. Geçersiz kombinasyon durumunda ise, kullanıcıyı bilgilendiren hata şeritleri ile [EditorDialog.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/ui/dialogs/EditorDialog.kt) arayüzü açık tutularak elle renk düzeltmesi yapılmasına imkan tanınır.

### 11. 🎧 Akustik Sentez ve Düşük Gecikmeli Ses Motoru (High-Fidelity Click SoundEngine)
- Küp katmanlarının her dönüş hareketinde, sürtünme ve yankı gürültülerinden tamamen arındırılmış, 80 ms süreli keskin plastik tık tık ses efekti çalınır. Android tarafında yerel `SoundPool` API entegrasyonu ile sıfır gecikme hedeflenmiştir.

---

## 📐 Algoritmalar ve Yüksek Matematik Detayları

### 1. 3D Uzay Dönüşleri, Kuaterniyonlar ve Rodrigues Formülü
3D simülasyondaki her bir küp parçacığının (cubie) konumu ve yönelimi dönüşler sırasında güncellenir. Bir parçacığın konum vektörünü ($\mathbf{v}$), dönme ekseni ($\mathbf{u}$) etrafında $\theta = \pi/2$ radyan (90 derece) kadar döndürmek için **Rodrigues Rotasyon Formülü** kullanılır:
$$\mathbf{v}' = \mathbf{v} \cos\theta + (\mathbf{u} \times \mathbf{v}) \sin\theta + \mathbf{u} (\mathbf{u} \cdot \mathbf{v}) (1 - \cos\theta)$$
Burada:
- $\mathbf{v}'$ dönme sonrası yeni konum vektörüdür.
- $\mathbf{u}$ normalize edilmiş dönme eksenidir.
- $\times$ çapraz çarpımı (cross product), $\cdot$ ise nokta çarpımı (dot product) temsil eder.

3D simülasyonda her küp parçacığının kendi lokal yönelim vektörleri (`rightBasis`, `upBasis`, `forwardBasis`) de bu formülle döndürülerek 3D uzaydaki oryantasyon matrisi güncellenir. Kayan nokta sapmalarını (floating-point drift) engellemek için her dönüş bitiminde koordinatlar en yakın tamsayı veya yarım sayıya yuvarlanır.

#### 🎥 Kamera Projeksiyon Modeli
Kameranın 3D uzaydaki koordinatları ekran alanına yansıtılırken [CubeScreenProjector.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/CubeScreenProjector.kt) sınıfı tarafından aşağıdaki matematiksel dönüşüm matrisleri uygulanır:
1. **Dünya Koordinatlarından Kamera Koordinatlarına Geçiş:**
   Dünya uzayındaki bir $\mathbf{v}_{\text{world}} = (x, y, z)^T$ vektörü, `yaw` ($\theta$) ve `pitch` ($\phi$) açıları kullanılarak sırasıyla Y ve X eksenleri etrafında döndürülür:
   $$\mathbf{v}_{\text{camera}} = R_x(\phi) R_y(\theta) \mathbf{v}_{\text{world}}$$
   Açık biçimde:
   $$R_y(\theta) = \begin{bmatrix} \cos\theta & 0 & \sin\theta \\ 0 & 1 & 0 \\ -\sin\theta & 0 & \cos\theta \end{bmatrix}, \quad R_x(\phi) = \begin{bmatrix} 1 & 0 & 0 \\ 0 & \cos\phi & -\sin\phi \\ 0 & \sin\phi & \cos\phi \end{bmatrix}$$
   $$\mathbf{v}' = R_y(\theta) \mathbf{v}_{\text{world}} \implies \begin{aligned} x' &= x \cos\theta + z \sin\theta \\ y' &= y \\ z' &= -x \sin\theta + z \cos\theta \end{aligned}$$
   $$\mathbf{v}_{\text{camera}} = R_x(\phi) \mathbf{v}' \implies \begin{aligned} x_c &= x' \\ y_c &= y' \cos\phi - z' \sin\phi \\ z_c &= y' \sin\phi + z' \cos\phi \end{aligned}$$

2. **Perspektif Projeksiyon ve Ekran Alanına Eşleme:**
   Kamera mesafesi $d$ ve odak uzaklığı $f$ (focal length) kullanılarak perspektif derinlik katsayısı $\text{scale}$ hesaplanır:
   $$\text{depth} = d + z_c$$
   $$\text{scale} = \frac{f}{\text{depth}}$$
   Ardından ekran merkezi $(W/2, H/2)$ ve kaydırma değerleri ($\text{pan}_x, \text{pan}_y$) eklenerek piksel koordinatları bulunur:
   $$x_{\text{screen}} = \frac{W}{2} + \text{pan}_x + x_c \cdot \text{scale}$$
   $$y_{\text{screen}} = \frac{H}{2} - \text{pan}_y - y_c \cdot \text{scale}$$

#### 🔄 Kamera Açıları En Kısa Yol Normalizasyonu (Shortest-Path Interpolation)
3D Rehber Küpünün [CubeRotationGuide.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/CubeRotationGuide.kt) kamera açısı geçişleri sırasında oluşan sarsıntıları engellemek için, hedef açı ile mevcut açı arasındaki fark en kısa rotasyon yönüne normalize edilir:
$$\Delta\theta = \operatorname{atan2}(\sin(\theta_{\text{target}} - \theta_{\text{current}}), \cos(\theta_{\text{target}} - \theta_{\text{current}}))$$
Bu sayede yaw geçişleri hiçbir zaman $\pi$ radyandan (180 derece) daha fazla dönmez, ters yönden en kısa yolu tercih ederek pürüzsüzce akar.

---

### 2. Grup Teorisi ve Rubik Küpü Grubu ($\mathcal{G}$)
Rubik Küpünün tüm geçerli durumları, matematiksel olarak simetrik permütasyon grubunun bir alt grubunu oluşturur. Küpün 8 köşe ve 12 kenar parçası bulunmaktadır.
- Köşelerin permütasyonu $\sigma \in \mathcal{S}_8$, yönelimleri $x \in \mathbb{Z}_3^8$.
- Kenarların permütasyonu $\tau \in \mathcal{S}_{12}$, yönelimleri $y \in \mathbb{Z}_2^{12}$.

Rubik Küpü Grubu $\mathcal{G}$, aşağıdaki 3 fiziksel kısıtı (yasa) sağlayan tüm $(\sigma, x, \tau, y)$ bileşenlerinden oluşur:
1. **Köşe Yönelim Kısıtı (Corner Orientation Constraint):** Köşe yönelimlerinin toplamı 3'ün katı olmalıdır:
   $$\sum_{i=1}^8 x_i \equiv 0 \pmod 3$$
2. **Kenar Yönelim Kısıtı (Edge Orientation Constraint):** Kenar yönelimlerinin toplamı çift olmalıdır:
   $$\sum_{i=1}^{12} y_i \equiv 0 \pmod 2$$
3. **Permütasyon Parite Kısıtı (Permutation Parity Constraint):** Köşe permütasyonunun işareti (paritesi), kenar permütasyonunun işaretine eşit olmalıdır:
   $$\operatorname{sgn}(\sigma) = \operatorname{sgn}(\tau)$$

Bu üç kısıt, serbest permütasyon uzayının boyutunu sırasıyla $3$, $2$ ve $2$ oranında küçülterek toplam grubu $12$ kat daraltır. Böylece grubun mertebesi (eleman sayısı):
$$|\mathcal{G}| = \frac{8! \cdot 3^8 \cdot 12! \cdot 2^{12}}{12} = 8! \cdot 3^7 \cdot 12! \cdot 2^{10} = 43,252,003,274,489,856,000 \approx 4.33 \times 10^{19}$$

#### 🏁 Tanrı Sayısı (God's Number)
Grup teorisi çerçevesinde yapılan bilgisayar destekli ispatlar ( Tomas Rokicki, Herbert Kociemba, Morley Davidson ve John Dethridge - 2010), Rubik Küpü grubundaki herhangi bir durumun **Half-Turn Metric (HTM)** standardında en fazla **20 hamlede**, **Quarter-Turn Metric (QTM)** standardında ise en fazla **26 hamlede** çözülebileceğini matematiksel olarak kanıtlamıştır.

---

### 3. Kociemba İki Fazlı (Two-Phase) Arama Algoritması
[RubikSolver.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/solver/RubikSolver.kt) dosyasında implement edilen Kociemba algoritması, $4.33 \times 10^{19}$ boyutundaki arama uzayını doğrudan aramak yerine çözümü iki bağımsız faza böler:
- **Phase 1 (G -> G1):** Küpün köşelerinin yönelimlerini ($3^7 = 2187$ durum), kenarlarının yönelimlerini ($2^{11} = 2048$ durum) ve orta katmandaki (UD-slice) 4 kenarın yerleşimini (permütasyon sırası gözetilmeksizin, $\binom{12}{4} = 495$ durum) düzeltir. Bu fazın durum uzayı büyüklüğü:
  $$N_1 = 2187 \times 2048 \times 495 \approx 2.21 \times 10^9$$
  Phase 1 tamamlandığında küp, yalnızca şu üreteçlerin alt kümesiyle çözülebilecek $G_1$ alt grubuna ulaşmış olur:
  $$G_1 = \langle U, D, R^2, L^2, F^2, B^2 \rangle$$

- **Phase 2 (G1 -> Çözüm):** Yalnızca $G_1$ üreteçlerini (yani yan yüzlerin sadece 180 derecelik yarım dönüşleri ile üst/alt yüzlerin 90 veya 180 derecelik dönüşleri) kullanarak köşelerin permütasyonunu ($8! = 40,320$ durum), UD-slice kenarlarının permütasyonunu ($4! = 24$ durum) ve geri kalan 8 kenarın permütasyonunu ($8! = 40,320$ durum) çözer. Durum uzayı büyüklüğü:
  $$N_2 = 40,320 \times 24 \times 40,320 \approx 3.90 \times 10^{10}$$ (simetriler ile daha da küçültülür).

#### 🗄️ Budama Tabloları (Pruning Tables) ve Astar (A*) Arama
Arama performansını optimize etmek amacıyla her iki faz için bellek üzerinde mesafe/budama tabloları (pruning tables) tutulur. Arama sırasında, o anki koordinatların çözüme olan minimum hamle uzaklığı bu tablolardan $O(1)$ sürede sorgulanır. Heuristic fonksiyonu:
$$h(s) = \max\left(\text{PruningTable}_{\text{twist\_slice}}(s), \text{PruningTable}_{\text{flip\_slice}}(s)\right)$$
Bu heuristic, Iterative Deepening $A^*$ (IDA*) derinlik aramasıyla birleştirilerek milisaniyeler içinde ~20 hamlelik optimal çözümü üretir.

---

### 4. CIE L*a*b* Renk Dönüşüm Formülü ve CIEDE2000 (Delta E 2000)
Ortam ışığı, gölgeler ve parlama gürültülerini filtrelemek amacıyla [RubikImageProcessor.kt](file:///Users/vahitkeskin/AndroidStudioProjects/RubikSync/shared/src/commonMain/kotlin/com/vahitkeskin/rubiksync/cube/RubikImageProcessor.kt) sınıfı pikselleri sRGB uzayından CIE L*a*b* uzayına dönüştürür.

1. **RGB -> XYZ Dönüşümü:**
   Kamera sensöründen gelen doğrusal olmayan sRGB piksel değerleri gama çözme (gamma companding) işlemine tabi tutulur:
   $$V_{\text{linear}} = \begin{cases} \frac{V_{\text{srgb}}}{12.92} & \text{if } V_{\text{srgb}} \le 0.04045 \\ \left(\frac{V_{\text{srgb}} + 0.055}{1.055}\right)^{2.4} & \text{otherwise} \end{cases} \quad (V \in \{R, G, B\})$$
   D65 referans aydınlatıcısı (D65 white point, $X_n=95.047, Y_n=100.000, Z_n=108.883$) altında dönüşüm matrisi uygulanır:
   $$\begin{bmatrix} X \\ Y \\ Z \end{bmatrix} = \begin{bmatrix} 0.4124 & 0.3576 & 0.1805 \\ 0.2126 & 0.7152 & 0.0722 \\ 0.0193 & 0.1192 & 0.9505 \end{bmatrix} \begin{bmatrix} R_{\text{linear}} \times 100 \\ G_{\text{linear}} \times 100 \\ B_{\text{linear}} \times 100 \end{bmatrix}$$

2. **XYZ -> CIE L*a*b* Dönüşümü:**
   Doğrusal olmayan dönüşüm fonksiyonu $f(t)$ uygulanarak renk değerleri elde edilir:
   $$f(t) = \begin{cases} t^{1/3} & \text{if } t > 0.008856 \\ 7.787 t + \frac{16}{116} & \text{otherwise} \end{cases}$$
   $$L^* = 116 f(Y/Y_n) - 16, \quad a^* = 500 [f(X/X_n) - f(Y/Y_n)], \quad b^* = 200 [f(Y/Y_n) - f(Z/Z_n)]$$

3. **CIEDE2000 Renk Farkı Formülü:**
   İki renk arasındaki fark insan gözünün hassasiyet eğrisine göre ağırlıklandırılarak hesaplanır:
   $$\Delta E_{00} = \sqrt{\left(\frac{\Delta L'}{k_L S_L}\right)^2 + \left(\frac{\Delta C'}{k_C S_C}\right)^2 + \left(\frac{\Delta H'}{k_H S_H}\right)^2 + R_T \left(\frac{\Delta C'}{k_C S_C}\right) \left(\frac{\Delta H'}{k_H S_H}\right)}$$
   Burada parlaklık değişimlerine (gölge, ışık şiddeti) karşı algoritmanın toleransını artırmak için açıklık ağırlık katsayısı **$k_L = 2.0$** olarak seçilmiştir. Mavi-yeşil renk kaymalarını düzeltmek için kullanılan rotasyon terimi ($R_T$) ise:
   $$R_T = -\sin(2\Delta\theta) R_C$$
   $$\Delta\theta = 30^\circ \exp\left(-\left(\frac{\bar{h}' - 275^\circ}{25^\circ}\right)^2\right), \quad R_C = 2 \sqrt{\frac{\bar{C}'^7}{\bar{C}'^7 + 25^7}}$$

---

### 5. Kuhn-Munkres (Hungarian) Algoritması ile Renk Dağılım Optimizasyonu
Küpün 6 adet merkez rengi referans olarak belirlendikten sonra, geri kalan 48 hücrenin (köşe ve kenar etiketleri) her birine tam olarak **8 adet** renk atanması gerekmektedir (6 yüzey $\times$ 8 = 48 hücre). 

Bu kısıt, $48 \times 48$ boyutlarında bir maliyet matrisi ($C$) kurulmasıyla çözülür. Matrisin her bir $C_{ij}$ elemanı, taranan $i$. hücre ile $j$. hedef renk yuvası (White, Yellow, Green, Blue, Red, Orange renklerinin her biri için 8'er adet olmak üzere toplam 48 yuva) arasındaki CIEDE2000 renk farkı değerini temsil eder.

#### 🧮 Matematiksel Model
Kuhn-Munkres algoritması aşağıdaki doğrusal programlama (integer linear programming) problemini $O(N^3)$ zamanda çözer:
$$\min \sum_{i=1}^{48} \sum_{j=1}^{48} C_{ij} x_{ij}$$
Kısıtlar:
$$\sum_{j=1}^{48} x_{ij} = 1 \quad (\forall i), \quad \sum_{i=1}^{48} x_{ij} = 1 \quad (\forall j), \quad x_{ij} \in \{0, 1\}$$

Burada $x_{ij} = 1$ olması, $i$. taranan hücrenin $j$. renge atanması anlamına gelir. Algoritmanın adımları:
1. **Satır ve Sütun İndirgeme:** Her satırdan o satırın en küçük elemanı, her sütundan o sütunun en küçük elemanı çıkarılarak matriste sıfırlar oluşturulur.
2. **Köşe Etiketleme (Vertex Labeling):** Dual değişkenler $u_i$ ve $v_j$ tanımlanır ($u_i + v_j \le C_{ij}$). Eşitlik alt grafında ($u_i + v_j = C_{ij}$) maksimum eşleme aranır.
3. **Alternatif Artan Yollar (Alternating Paths):** Eğer tam eşleme (perfect matching) bulunamazsa, etiket değerleri güncellenerek yeni sıfırlar oluşturulur ve eşleme genişletilir.
Bu süreç sonucunda, fiziksel olarak çözülmesi imkansız olan "10 adet yeşil çıkartma" veya "merkez dışında 2 adet sarı" gibi taranan hatalı durumlar matematiksel olarak en yakın geçerli küp durumuna otomatik olarak optimize edilmiş olur.

---

## 📐 3D Mimari ve Veri Akış Modeli

### 1. Global Uygulama Mimarisi ve Veri Akışı
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
Aşağıdaki diyagramda, 3D küp parçacıklarının (cubies) lokal koordinatlarından başlanarak, dönüş matrisleri, kamera uzayı dönüşü, perspektif derinlik katsayıları ve ekran projeksiyonuna kadar uzanan grafik boru hattı (render pipeline) gösterilmiştir:

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

## 🛠️ Kullanılan Teknolojiler ve Bağımlılıklar

| Alan | Kullanılan Kütüphane / Teknoloji | KMP Desteği | Açıklama |
| --- | --- | --- | --- |
| **Çekirdek** | Kotlin Multiplatform (KMP) | Evet | Android, iOS ve JVM için ortak kod paylaşımı. |
| **Arayüz (UI)** | Compose Multiplatform | Evet | Bildirimsel (Declarative) ortak UI geliştirme ortamı. |
| **Android Kamera** | Jetpack CameraX (`1.3.4`) | Hayır (Android) | `PreviewView`, `ImageCapture` yerel kamera yönetimi. |
| **iOS Kamera** | AVFoundation (`AVKit` / `UIKit`) | Hayır (iOS) | `AVCaptureSession` ve `AVCapturePhotoOutput` yerel iOS kamera yönetimi. |
| **Navigasyon** | Jetpack Compose Navigation KMP (`2.8.0-alpha10`) | Evet | Rota tabanlı KMP uyumlu ekran yönetimi ve geçiş animasyonları. |
| **Veritabanı** | Android Jetpack Room DB | Evet | SQLite tabanlı küp durumlarının kalıcı olarak saklanması. |
| **Ayarlar Kalıcılığı** | Jetpack DataStore Preferences | Evet | Ses, tema, dil ve kilit durumlarının anahtar-değer (K-V) kalıcılığı. |
| **Ses Motoru** | Android SoundPool API | Hayır (Android) | Düşük gecikmeli yerel Android ses efekti motoru. |
| **Görsel İşleme** | Kotlin Native / Skia Graphics | Evet | Platform-specific görsellerin belleğe yüklenmesi ve piksel matrisine dönüştürülmesi. |
| **Algoritma** | Kociemba Algorithm | Evet | İki Fazlı (Two-Phase) optimum Rubik Küp çözücü algoritma. |

---

## 📱 Ekranların Detaylı Tanıtımı

### 1. Ana Ekran & 3D İnteraktif Simülasyon
- Uygulama açıldığında kullanıcıyı karşılayan 3D küp görünümüdür.
- Kullanıcı küpü elle karıştırabilir, katmanları sağa-sola döndürebilir.
- Ekranın üst kısmında küpün çözülmesi için gerekli hamle sayısı yer alır.
- **"Tara ve Çöz"** butonuna basılarak tarama sihirbazı başlatılır.
- Küp başarıyla tarandıktan sonra **"Çözümü Oynat"** butonu aktifleşir ve küpün adım adım çözüm animasyonu başlar. Hamle hızı kaydırıcı yardımıyla canlı olarak ayarlanabilir.

### 2. Özel Kamera Tarama Ekranı (Kamera Diyaloğu)
- **"Fotoğraf Çek"** butonuna basıldığında açılan tam ekran arayüzdür.
- Sarı kesikli bir kare çerçeve ve yeşil 3x3 ızgara çizgileri canlı kamera görüntüsünün üzerine bindirilir.
- Kullanıcı küpü tam kare çerçevenin içine hizalayarak ortadaki deklanşör butonuyla çekimi gerçekleştirir. Flaş ikonuyla karanlık ortamlarda aydınlatma sağlanabilir.
- Çekim yapıldıktan sonra fotoğraf otomatik olarak merkezi bir kareye kırpılarak kalibrasyon ekranına aktarılır.

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