<div align="center">

# 📝 گزارش آپدیت بزرگ v1.3.0 — «کالیبراسیون و صداقت علمی»
## v1.3.0 Major Update Report — "Calibration & Scientific Honesty"

**SubsurfaceExplorer (Sal-Subsurf)** · تاریخ انتشار / Release date: **2026-11-07** · `versionCode 4`

**[🇮🇷 فارسی](#report-fa) · [🇬🇧 English](#report-en)** · مطابق / conforms to: `subsurf.deep.report/1.3.0`

</div>

---

<a id="report-fa"></a>
<div dir="rtl">

## ۱) خلاصهٔ اجرایی

نسخهٔ **1.3.0** مهم‌ترین به‌روزرسانی علمی پروژه تا امروز است. این نسخه در پاسخ به یک **بازبینی علمی مستقل از v1.2.0** تهیه شد و چهار ستون دارد:

1. **رفع یک باگ حیاتی** در فراخوانی Process API که تحلیل زندهٔ (live) را عملاً بی‌اعتبار می‌کرد؛
2. **جایگزینی نمرهٔ دستی وزن‌دار با آمار کالیبرهٔ CPF-lite** (p-value کانفورمال روی null سوروگایت) به‌عنوان معیار آشکارسازی؛
3. **کاشی‌کاری رزولوشن بومی ۱۰ متر** به‌جای کوچک‌سازی کل ناحیه به ۵۱۲ پیکسل؛
4. **سخت‌سازی امنیت و صداقت خروجی** (مقایسهٔ زمان-ثابت کلید، CORS پیکربندی‌پذیر، پرچم `synthetic` همه‌جا، خروجی غنی‌تر).

نتیجهٔ خالص: نرخ مثبت کاذب زیر رانش فصلی قوی از **~۶۹٪ به ~۳٪** کاهش یافت (≈۲۳×)، در حالی که recall روی بی‌هنجاری کاشته‌شده **1.00** باقی ماند. مجموعهٔ تست‌ها از ۵۰ به **۷۲ تست** گسترش یافت و «قرارداد کالیبراسیون» اکنون در هر push در CI آزموده می‌شود.

## ۲) انگیزه: بازبینی علمی مستقل

بازبینی خارجی v1.2.0 چهار ایراد اصلی مطرح کرد که همه درست بودند:

- **باگ واحدها:** اسکریپت evalscript باندهای بازتابی را بدون `units:"DN"` درخواست می‌کرد → API اعداد اعشاری ۰..۱ (رفلکتانس) برمی‌گرداند و با `sampleType: UINT16` همه‌چیز به ۰ یا ۱ گرد می‌شود؛ هارمونیزه‌سازی `(DN−1000)/10⁴` سپس همه‌چیز را صفر می‌کند. یعنی خروجی live از نظر فیزیکی بی‌معنا بود.
- **عدم کالیبراسیون:** نمرهٔ ۰–۱۰۰ با وزن‌های دستی هیچ تضمین آماری نداشت؛ زیر رانش فصلی (فنولوژی، رطوبت فصلی خاک) ناحیهٔ بزرگی به‌اشتباه «بی‌هنجاری» علامت می‌خورد.
- **از دست رفتن رزولوشن:** تحلیل به گرید ≤۵۱۲px resample می‌شد؛ در AOIهای بزرگ هر پیکسل چند ده متر می‌شد و الگوهای ریز (مثل ردیف چاه‌های قنات) محو می‌شدند.
- **خطر سوءبرداشت selftest:** آشکارسازی‌های دادهٔ ترکیبی به‌قدر کافی صریح علامت‌گذاری نشده بودند.

## ۳) باگ حیاتی evalscript و رفع آن

**قبل (v1.2.0):**

</div>

```js
// بدون units → پاسخ REFLECTANCE (0..1) → در UINT16 به 0/1 گرد می‌شود
sample: { bands: ["B02","B03","B04","B08","B11","B12"], ... }
```

<div dir="rtl">

**بعد (v1.3.0):**

</div>

```js
sample: {
  "0": { bands: ["B02","B03","B04","B08","B11","B12"], units: "DN" },  // ← حیاتی
  "1": { bands: ["SCL", "dataMask"] }                                   // بدون units
},
output: { bands: 8, sampleType: "UINT16" }
```

<div dir="rtl">

- DN خام بازتاب می‌شود و در بک‌اند با **هارمونیزه‌سازی baseline 04.00** به رفلکتانس فیزیکی تبدیل می‌شود: `reflectance = (DN − DN_OFFSET) / 10⁴` که `DN_OFFSET` با متغیر محیطی `S2_DN_OFFSET` (پیش‌فرض `1000`) قابل تغییر است.
- `SCL` و `dataMask` باندهای طبقه‌بندی/ماسک هستند و عمداً بدون units درخواست می‌شوند.
- **تست رگرسیون** در `tests/test_cdse_parsing.py` اکنون موجودی evalscript را قفل می‌کند: اگر کسی دوباره `units:"DN"` را حذف کند، CI قرمز می‌شود.

## ۴) موتور آماری CPF-lite (حالت پیش‌فرض `fusion_mode="cpf"`)

CPF-lite به‌جای «چند ویژگی را با هم جمع کنیم و با آستانهٔ دلخواه ببریم»، برای هر منطقه یک **null آماری اختصاصی** می‌سازد و فقط انحراف‌هایی را معنادار می‌داند که در برابر آن null دوام بیاورند. شش کانال شواهد علامت‌دار: `z_ndvi, z_ndwi, z_mndwi, z_ndmi, z_nbr, z_bsi` (z-score مقاوم median/MADِ میانهٔ پنجرهٔ اخیر).

**مراحل (دقیقاً همان چیزی که در `deep/cpf.py` پیاده شده):**

| گام | عمل | جزئیات |
| --- | --- | --- |
| ۱ | **روندزدایی زمینه** | چندجمله‌ای درجه-۳ (مجموع-درجه) با Tukey-biweight IRLS (۶ تکرار) روی هر کانال؛ روندهای هموار فنولوژی/خاک/روشنایی حذف می‌شوند؛ NaN-safe |
| ۲ | **سفیدسازی** | استانداردسازی median/MAD → کوواریانس شینکریج‌شدهٔ تریم‌شده (مرکز ۸۰٪ نمونه‌ها، λ=0.1) → d² ماهالانوبیس؛ شواهد همبسته دوبار حساب نمی‌شوند |
| ۳ | **مقیاس null تجربی** | مقیاس‌بندی سبک Efron: `d² × median(d²)/χ²₆(0.5)` تا بدنهٔ منطقه از توزیع χ²₆ پیروی کند |
| ۴ | **سوروگایت** | فاز-تصادفی‌سازی FFT با **فاز مشترک بین کانال‌ها** (طیف هر کانال و ساختار هم‌بستگی بین‌کانالی حفظ می‌شود)؛ پیش‌فرض ۱۹۹ سوروگایت |
| ۵ | **آمار خوشه** | بذر خوشه: p پیکسلی χ² < `p_pix=1e-3`؛ حداقل `min_px=9` پیکسل؛ آماره = cluster-mass |
| ۶ | **p کانفورمال add-1** | `p = (1 + #{تودهٔ سوروگایت ≥ مشاهده‌شده}) / (n_surr + 1)`؛ کنترل خطای خانوادگی زیر رانش هموار |
| ۷ | **تصمیم** | معنادار فقط اگر `p ≤ alpha=0.01`؛ اطمینان: **high** (p≤0.001) / **medium** (p≤0.01) / **low** |

**نمرهٔ اهمیت پیکسلی `sig_score`:** چون `sf()` در دنبالهٔ دور اشباع می‌شود، نمرهٔ ۰–۱۰۰ با مقیاس لگاریتمی محاسبه می‌شود: `100·clip(−log10(sf)/6, 0, 1)` — مثلاً p=1e-6 → 100.

**چرا این مهم است؟** زیر رانش فصلی، «همه‌چیز کمی غیرعادی است»؛ روش‌های پیکسل‌به‌پیکسل بدون null زمینه، میلیون‌ها پیکسل را مثبت می‌کنند. CPF-lite با ساختن null از خود منطقه (سوروگایت‌های فاز-تصادفی)، فقط الگوهایی را نگه می‌دارد که از نظر فضایی-آماری از تصادفیِِ هم‌بسته بزرگ‌ترند. **تضمین صادقانه:** CPF علتِ تغییر را طبقه‌بندی نمی‌کند؛ فقط می‌گوید «این الگو به‌سختی محصول شانسِ این منطقه است».

## ۵) نتایج اندازه‌گیری‌شدهٔ کالیبراسیون

مجموعهٔ `tests/test_null_injection.py` (۵ تست، با ۹۹ سوروگایت و alpha=0.01 در CI) چهار صحنه را می‌آزماید: null خالص (B)، رانش فصلی بدون بی‌هنجاری (D)، بی‌هنجاری کاشته‌شده + رانش (E)، بی‌هنجاری ضعیف کاشته‌شده (F). اعداد اندازه‌گیری‌شده در برابر قرارداد CI:

| صحنه | legacy (v1.2.0) | CPF-lite (v1.3.0) | قرارداد CI برای CPF |
| --- | --- | --- | --- |
| null خالص | FP < ۱۰٪ | FP < ۲٪ | FP < ۲٪ |
| رانش قوی، بدون بی‌هنجاری | FP ≈ **۶۹٪** ناحیه | FP ≈ **۳٪** | FP ≤ ۸٪ و حتماً < legacy |
| کاشت + رانش | FP ≈ ۷۲٪ | **recall = 1.00**، FP ≈ **۷٫۵٪** | recall ≥ ۰٫۹۰، FP ≤ ۱۰٪ |
| کاشت ضعیف | — | recall ≥ ۰٫۸۰ | recall ≥ ۰٫۸۰، FP ≤ ۶٪ |

یعنی با رفع کاملِ recall، نرخ مثبت کاذب حدوداً **۲۳ برابر** کاهش یافته است. این اعداد روی دادهٔ ترکیبی قطعی (seed ثابت) اندازه‌گیری شده‌اند و در هر push بازتولید می‌شوند.

## ۶) کاشی‌کاری رزولوشن بومی ۱۰ متر

با `resolution_m` (۱۰ تا ۱۰۰ متر) تحلیل روی **گرید بومی سنتینل-۲** انجام می‌شود؛ `resolution_m=None` رفتار قدیمی (گرید ≤۵۱۲px، resample با nearest) را نگه می‌دارد.

| پارامتر | مقدار | توضیح |
| --- | --- | --- |
| اندازهٔ تایل | ≤ ۵۱۲px | هر تایل کل زنجیرهٔ تحلیل را مستقل اجرا می‌کند |
| هالهٔ overlap | ۲۴px | مرزهای مصنوعی بین تایل‌ها اثر نمی‌گذارند؛ اجزا پس از دوخت سراسری لیبل می‌گیرند |
| بودجهٔ کل | ≤ ۴٫۲M پیکسل | ≈ ۲۰٫۵ × ۲۰٫۵ کیلومتر در ۱۰ متر؛ فراتر: پیام خطای شفاف |
| سقف ضلع | ۳۰۷۲px | — |
| resampling درخواستی | `bilinear` (بومی) / `nearest` (گرید) | در `processing.resampling` به CDSE اعلام می‌شود |

دو نکتهٔ طراحی: (۱) بیضی کاشته‌شدهٔ selftest در مختصات **گرید کامل** تعریف می‌شود تا هر تایل به‌تنهایی مثل یک null-control رفتار کند و آشکارسازی‌های تکراریِ مرز تایل رخ ندهد؛ (۲) حافظه: پشتهٔ هر تایل پس از پردازش آزاد می‌شود تا سقف مصرف روی میزبان‌های کوچک (مثل پلن رایگان رانفلر) حفظ شود. report.json هم `tiles`, `tiles_ok`, `tiles_failed`, `native_resolution`, `resolution_m` را گزارش می‌کند.

## ۷) امنیت

- **مقایسهٔ زمان-ثابت کلید:** `require_api_key` اکنون از `hmac.compare_digest` استفاده می‌کند؛ بازیابی بایت‌به‌بایت کلید از روی زمان پاسخ ممکن نیست.
- **CORS پیکربندی‌پذیر:** `CORS_ORIGINS` (جداشده با کاما؛ پیش‌فرض `*` برای سازگاری اپ/کنسول)؛ متدها فقط `GET, POST, OPTIONS` و هدرها فقط `Content-Type, X-API-Key`.

## ۸) گزارش و خروجی‌ها

`report.json` با schema `subsurf.deep.report/1.3.0`:

- بلوک جدید `cpf`: `alpha`, `n_surrogates`, `p_pix`, `clusters_seeded`, `clusters_significant`, `min_cluster_p`, `null_calibration` (توضیح متنی روش برای مخاطب گزارش).
- فیلدهای جدید: `fusion_mode`, `native_resolution`, `resolution_m`, `tiles`, `tiles_ok`, `tiles_failed`.
- هر feature از `detections.geojson` اکنون شامل: `bbox_wgs84`، `elongation_ratio` (نسبت قطر اصلی به فرعی — آشکارسازی‌های خطی مثل ردیف چاه‌ها دیگر زیر «لکهٔ محدب» گم نمی‌شوند)، `p_value` و `method` (در حالت CPF)، و `synthetic` (در selftest).
- **پرچم synthetic همه‌جا:** در report، در هر feature، در متادیتای FeatureCollection و در پاسخ API — خروجی selftest به‌هیچ‌وجه با شواهد زمین واقعی قابل اشتباه نیست.
- `CAVEATS` به‌روزرسانی شد: «معناداری CPF نسبت به null سوروگایتِ همین منطقه کالیبره شده؛ نرخ مثبت کاذب را زیر رانش هموار کنترل می‌کند، اما علت تغییر را طبقه‌بندی نمی‌کند».

## ۹) تغییرات API

| فیلد/تغییر | جزئیات |
| --- | --- |
| `fusion_mode` | `cpf` (پیش‌فرض) یا `legacy` — هر دو مسیر تست شده‌اند |
| `n_surrogates` | ۰–۹۹۹ (پیش‌فرض ۱۹۹)؛ اگر با alpha انتخابی هیچ معناداری ممکن نباشد، خودکار به کف add-1 یعنی `1/alpha − 1` بالا برده می‌شود |
| `alpha` | ۰٫۰۰۱–۰٫۱ (پیش‌فرض ۰٫۰۱) |
| `resolution_m` | ۱۰–۱۰۰ (None = گرید ۵۱۲px قدیمی) |
| `POST /selftest` | پارامترهای query جدید: `resolution_m`, `fusion_mode`, `n_surrogates` |
| `GET /capabilities` | اکنون `fusion_modes` و `native_resolution` (سقف ۴٫۲M px، شرح تایل‌بندی) را تبلیغ می‌کند |

## ۱۰) کنسول وب (`/deep-console`)

- انتخاب حالت fusion (CPF/legacy) و رزولوشن (بومی ۱۰/۲۰/۵۰ متر یا گرید).
- خط پایانِ هر اجرا اکنون `synthetic` / `native` / تعداد تایل‌ها را نشان می‌دهد.

## ۱۱) پوشش تست — ۵۰ → ۷۲

| ماژول (جدید/به‌روز) | تعداد | نقش |
| --- | --- | --- |
| `test_null_injection.py` **(جدید)** | ۵ | قرارداد FP/recall legacy در برابر CPF (جدول بخش ۵) |
| `test_cpf.py` **(جدید)** | ۹ | تست واحد روندزدایی/سفیدسازی/مقیاس null/کاشت/سکوت |
| `test_tiling.py` **(جدید)** | ۶ | سقف‌ها، پوشش دقیق هستهٔ تایل‌ها، selftest بومی، legacy از مسیر تایل |
| `test_cdse_parsing.py` (به‌روز) | ۹ | تجزیهٔ CDSE + رگرسیون `units:"DN"` |
| بقیه (deep/api/validation/geo/terrain_units) | ۴۳ | بدون رگرسیون |
| **جمع** | **۷۲** | همه سبز (اجرای تأییدشده) |

نتیجهٔ selftest توکار نسخهٔ 1.3.0 (اجرای واقعی): `clusters_significant=1`، `min_cluster_p=0.005` — دقیقاً کف add-1 با ۱۹۹ سوروگایت؛ یعنی آشکارسازیِ بیضی کاشته‌شده با بیشینهٔ اطمینانِ ممکن برای این بودجهٔ سوروگایت.

## ۱۲) مهاجرت از v1.2.0

- **بدون تغییر شکست‌دهندهٔ API.** درخواست‌های قدیمی کار می‌کنند؛ فقط معیار آشکارسازی به‌طور پیش‌فرض CPF است. رفتار v1.2.0: `fusion_mode="legacy"`.
- **وابستگی جدید:** `scipy>=1.11` (در `pyproject.toml` و `requirements.txt`).
- مصرف‌کنندگان report.json باید `schema` را چک کنند (`subsurf.deep.report/1.3.0`) و بلوک `cpf` را برای تفسیر p بخوانند.
- نسخه‌ها: اپ `versionCode 4` / `versionName 1.3.0`؛ بک‌اند 1.3.0.
- برای استقرار Runflare فقط کافی است متغیرهای محیطی و `requirements.txt` جدید را داشته باشید (راهنما: `docs/DEPLOY-RUNFLARE-FA.md`).

## ۱۳) محدودیت‌ها (صادقانه)

- CPF معناداری **فضایی-آماری** می‌دهد، نه تفسیر فیزیکی؛ خروجی در کنار دانش زمین‌شناسی/زراعی معنا پیدا می‌کند.
- سوروگایت فاز-تصادفی فرض می‌کند ساختار هم‌بستگی بین‌کانالی تحت H₀ ایستاست؛ رانش‌های غیرهموار (مثلاً تغییر کاربری ناگهانی) باید با فیلترهای قبلی (ماسک SCL، gate اعتبار صحنه) و تفسیر انسانی کنار گذاشته شوند.
- بودجهٔ ۴٫۲M پیکسل = حداکثر ~۴۲۰ کیلومتر مربع در ۱۰ متر؛ برای AOI بزرگ‌تر باید تقسیم دستی کرد.
- همهٔ محدودیت‌های v1.2.0 (نوری = بدون دید زیرسطحی، بدون تخمین عمق) همچنان برقرارند؛ بخش ۱۲ `docs/DEEP-PROCESSING.md`.

## ۱۴) فایل‌های تغییر یافته/ایجادشده

| فایل | نوع تغییر |
| --- | --- |
| `backend/subsurface_api/deep/cpf.py` | **جدید** — موتور CPF-lite |
| `backend/subsurface_api/deep/pipeline.py` | بازنویسی — کاشی‌کاری بومی، گزارش 1.3.0، CAVEATS |
| `backend/subsurface_api/deep/cdse_process.py` | رفع `units:"DN"` + resampling + retry |
| `backend/subsurface_api/deep/labels.py` | props جدید (bbox، elongation، p_value، synthetic) |
| `backend/subsurface_api/deep/router.py` | فیلدهای جدید + hmac + capabilities |
| `backend/subsurface_api/deep/jobs.py` | params کامل |
| `backend/subsurface_api/main.py` | CORS پیکربندی‌پذیر |
| `backend/subsurface_api/static/deep_console.html` | کنترل‌های v1.3.0 |
| `backend/tests/test_null_injection.py`, `test_cpf.py`, `test_tiling.py` | **جدید** |
| `backend/tests/test_cdse_parsing.py`, `test_deep.py` | به‌روز |
| `backend/pyproject.toml`, `requirements.txt` | 1.3.0 + scipy |
| `android/app/build.gradle.kts` | versionCode 4 / versionName 1.3.0 |
| `CHANGELOG.md`, `docs/DEEP-PROCESSING.md`, `docs/ROADMAP.md`, `README.md` | مستندسازی |
| `docs/UPDATE-REPORT-v1.3.0.md` | **جدید** — همین سند |

</div>

---

<a id="report-en"></a>

## 1) Executive summary

Version **1.3.0** is the project's most significant scientific upgrade to date, prepared in response to an **independent scientific review of v1.2.0**. It rests on four pillars:

1. **Fixing a critical bug** in the Process API call that invalidated live-mode analysis entirely;
2. **Replacing the hand-weighted score with calibrated CPF-lite statistics** (conformal p-values against a surrogate null) as the detection criterion;
3. **Native 10 m-resolution tiling** instead of downsampling the whole AOI to 512 px;
4. **Security & honesty hardening** (constant-time key comparison, configurable CORS, `synthetic` flags everywhere, richer output).

Net result: false-positive area under strong seasonal drift dropped from **~69 % to ~3 %** (≈23× lower) while recall on the planted anomaly stayed at **1.00**. The test suite grew from 50 to **72 tests**, and the calibration contract now runs in CI on every push.

## 2) Motivation: the independent review

The external review of v1.2.0 raised four valid issues:

- **Units bug:** the evalscript requested reflectance bands without `units:"DN"` → the API served 0..1 floats and the `sampleType: UINT16` truncated them to 0/1; the `(DN−1000)/10⁴` harmonisation then zeroed everything. Live output was physically meaningless.
- **No calibration:** the 0–100 hand-weighted score carried no statistical guarantee; under seasonal drift (phenology, seasonal soil moisture) large areas were falsely flagged.
- **Lost resolution:** analysis resampled the AOI to ≤512 px; for large regions each pixel spanned tens of metres and fine patterns (e.g. qanat shaft rows) vanished.
- **Selftest misread risk:** synthetic detections were not flagged explicitly enough.

## 3) The critical evalscript bug and its fix

**Before (v1.2.0):**

```js
// no units → REFLECTANCE (0..1) → truncated to 0/1 under UINT16
sample: { bands: ["B02","B03","B04","B08","B11","B12"], ... }
```

**After (v1.3.0):**

```js
sample: {
  "0": { bands: ["B02","B03","B04","B08","B11","B12"], units: "DN" },  // ← critical
  "1": { bands: ["SCL", "dataMask"] }                                   // unit-less
},
output: { bands: 8, sampleType: "UINT16" }
```

- Raw DN is served and harmonised to physical reflectance on the backend with the **baseline-04.00** rule: `reflectance = (DN − DN_OFFSET) / 10⁴`, where `DN_OFFSET` is overridable via `S2_DN_OFFSET` (default `1000`).
- `SCL` and `dataMask` are classification/mask bands and are deliberately requested without units.
- A **regression test** in `tests/test_cdse_parsing.py` now locks the evalscript: removing `units:"DN"` again turns CI red.

## 4) The CPF-lite statistical engine (default `fusion_mode="cpf"`)

Instead of "sum some features and cut at an arbitrary threshold", CPF-lite builds a **region-specific statistical null** and only deviations that survive it are reported. Six signed evidence channels: `z_ndvi, z_ndwi, z_mndwi, z_ndmi, z_nbr, z_bsi` (robust median/MAD z-scores of the recent-window median).

**Steps (exactly as implemented in `deep/cpf.py`):**

| Step | Operation | Details |
| --- | --- | --- |
| 1 | **Context detrending** | Degree-3 total-degree polynomial fitted with Tukey-biweight IRLS (6 iterations) per channel; smooth phenology/soil/illumination trends removed; NaN-safe |
| 2 | **Whitening** | median/MAD standardisation → trimmed shrinkage covariance (central 80 %, λ=0.1) → Mahalanobis d²; correlated evidence is not double-counted |
| 3 | **Empirical null scaling** | Efron-style: `d² × median(d²)/χ²₆(0.5)` so the bulk of the region follows χ²₆ |
| 4 | **Surrogates** | FFT phase randomisation with **phases shared across channels** (per-channel spectra and cross-channel correlation preserved); 199 by default |
| 5 | **Cluster statistic** | Seed clusters where per-pixel χ² p < `p_pix=1e-3`; at least `min_px=9` pixels; statistic = cluster-mass |
| 6 | **Add-1 conformal p** | `p = (1 + #{surrogate mass ≥ observed}) / (n_surr + 1)`; family-wise control under smooth drift |
| 7 | **Decision** | Significant only if `p ≤ alpha=0.01`; confidence: **high** (p≤0.001) / **medium** (p≤0.01) / **low** |

**Per-pixel `sig_score`:** because `sf()` saturates in the far tail, the 0–100 score is log-scaled: `100·clip(−log10(sf)/6, 0, 1)` — e.g. p=1e-6 → 100.

**Why it matters:** under seasonal drift "everything is slightly unusual"; pixel-wise methods without a contextual null flag millions of pixels. CPF-lite builds the null from the region itself (phase-randomised surrogates) and keeps only patterns statistically larger than correlated chance. **Honest guarantee:** CPF does not classify the *cause* of a change; it only states "this pattern is unlikely to be chance for this region".

## 5) Measured calibration results

The `tests/test_null_injection.py` suite (5 tests, 99 surrogates and alpha=0.01 in CI) exercises four scenarios: pure null (B), drift with no anomaly (D), planted anomaly + drift (E), weak planted anomaly (F). Measured numbers vs the CI contract:

| Scenario | Legacy (v1.2.0) | CPF-lite (v1.3.0) | CI contract for CPF |
| --- | --- | --- | --- |
| Pure null | FP < 10 % | FP < 2 % | FP < 2 % |
| Strong drift, no anomaly | FP ≈ **69 %** of area | FP ≈ **3 %** | FP ≤ 8 % and strictly < legacy |
| Planted + drift | FP ≈ 72 % | **recall = 1.00**, FP ≈ **7.5 %** | recall ≥ 0.90, FP ≤ 10 % |
| Weak planted | — | recall ≥ 0.80 | recall ≥ 0.80, FP ≤ 6 % |

With recall fully retained, the false-positive rate dropped by roughly **23×**. These numbers are measured on deterministic synthetic data (fixed seed) and reproduce on every push.

## 6) Native 10 m-resolution tiling

With `resolution_m` (10–100 m) the analysis runs on the **native Sentinel-2 grid**; `resolution_m=None` keeps the legacy behaviour (≤512-px grid, nearest resampling).

| Parameter | Value | Notes |
| --- | --- | --- |
| Tile size | ≤ 512 px | Each tile runs the full chain independently |
| Overlap halo | 24 px | Tile borders do not artefact; components are labelled globally after stitching |
| Total budget | ≤ 4.2 M px | ≈ 20.5 × 20.5 km at 10 m; beyond that: a clear error message |
| Side cap | 3072 px | — |
| Requested resampling | `bilinear` (native) / `nearest` (grid) | Declared to CDSE via `processing.resampling` |

Two design notes: (1) the selftest's planted ellipse lives in **full-grid** coordinates so every tile behaves as its own null-control and no border-duplicated detections occur; (2) memory: each tile's stack is freed after processing so the footprint stays viable on small hosts (e.g. the free Runflare plan). report.json also records `tiles`, `tiles_ok`, `tiles_failed`, `native_resolution`, `resolution_m`.

## 7) Security

- **Constant-time key comparison:** `require_api_key` now uses `hmac.compare_digest`; byte-by-byte key recovery via response timing is no longer possible.
- **Configurable CORS:** `CORS_ORIGINS` (comma-separated; default `*` for app/console compatibility); methods restricted to `GET, POST, OPTIONS` and headers to `Content-Type, X-API-Key`.

## 8) Report and outputs

`report.json` conforms to schema `subsurf.deep.report/1.3.0`:

- New `cpf` block: `alpha`, `n_surrogates`, `p_pix`, `clusters_seeded`, `clusters_significant`, `min_cluster_p`, `null_calibration` (a human-readable method note).
- New fields: `fusion_mode`, `native_resolution`, `resolution_m`, `tiles`, `tiles_ok`, `tiles_failed`.
- Every `detections.geojson` feature now carries: `bbox_wgs84`, `elongation_ratio` (major/minor axis ratio — line-like detections such as shaft rows are no longer hidden by the convex-hull blob), `p_value` and `method` (CPF mode), and `synthetic` (selftest).
- **Synthetic flags everywhere:** in the report, every feature, the FeatureCollection metadata and the API response — selftest output can never be mistaken for real-terrain evidence.
- `CAVEATS` updated: "CPF significance is calibrated against a surrogate null of THIS region: it controls false positives under smooth seasonal drift, it does not classify the cause of a change."

## 9) API changes

| Field / change | Details |
| --- | --- |
| `fusion_mode` | `cpf` (default) or `legacy` — both paths are tested |
| `n_surrogates` | 0–999 (default 199); auto-raised to the add-1 floor `1/alpha − 1` when significance would otherwise be impossible |
| `alpha` | 0.001–0.1 (default 0.01) |
| `resolution_m` | 10–100 (None = legacy 512-px grid) |
| `POST /selftest` | New query parameters: `resolution_m`, `fusion_mode`, `n_surrogates` |
| `GET /capabilities` | Now advertises `fusion_modes` and `native_resolution` (4.2 M px cap, tiling description) |

## 10) Web console (`/deep-console`)

- Fusion-mode (CPF/legacy) and resolution (native 10/20/50 m or grid) controls.
- The completion line now surfaces `synthetic` / `native` / tile counts.

## 11) Test coverage — 50 → 72

| Module (new/updated) | Count | Role |
| --- | --- | --- |
| `test_null_injection.py` **(new)** | 5 | Legacy-vs-CPF FP/recall contract (table in §5) |
| `test_cpf.py` **(new)** | 9 | Unit tests: detrend/whiten/null-scale/plant/quiet |
| `test_tiling.py` **(new)** | 6 | Caps, exact tile-core coverage, native selftest, legacy through tiles |
| `test_cdse_parsing.py` (updated) | 9 | CDSE parsing + `units:"DN"` regression |
| Others (deep/api/validation/geo/terrain_units) | 43 | No regressions |
| **Total** | **72** | All green (verified run) |

Built-in v1.3.0 selftest result (actual run): `clusters_significant=1`, `min_cluster_p=0.005` — exactly the add-1 floor with 199 surrogates; i.e. the planted ellipse is detected at the maximum confidence this surrogate budget allows.

## 12) Migrating from v1.2.0

- **No breaking API change.** Old requests keep working; only the default detection criterion is now CPF. For v1.2.0 behaviour: `fusion_mode="legacy"`.
- **New dependency:** `scipy>=1.11` (in `pyproject.toml` and `requirements.txt`).
- Consumers of report.json should check `schema` (`subsurf.deep.report/1.3.0`) and read the `cpf` block to interpret p-values.
- Versions: app `versionCode 4` / `versionName 1.3.0`; backend 1.3.0.
- For Runflare deployment simply update the environment variables and `requirements.txt` (guide: `docs/DEPLOY-RUNFLARE-FA.md`).

## 13) Limitations (honestly stated)

- CPF provides **spatial-statistical** significance, not physical interpretation; outputs must be read alongside geology/agronomy knowledge.
- Phase-randomised surrogates assume the cross-channel correlation structure is stationary under H₀; non-smooth land changes (e.g. abrupt land use change) must be filtered by the existing guards (SCL masking, scene validity gate) and human interpretation.
- The 4.2 M px budget ≈ at most ~420 km² at 10 m; larger AOIs must be split manually.
- All v1.2.0 limitations still hold (optical = no underground vision, no depth estimates); see §12 of `docs/DEEP-PROCESSING.md`.

## 14) Changed / added files

| File | Change |
| --- | --- |
| `backend/subsurface_api/deep/cpf.py` | **New** — CPF-lite engine |
| `backend/subsurface_api/deep/pipeline.py` | Rewritten — native tiling, 1.3.0 report, CAVEATS |
| `backend/subsurface_api/deep/cdse_process.py` | `units:"DN"` fix + resampling + retry |
| `backend/subsurface_api/deep/labels.py` | New props (bbox, elongation, p_value, synthetic) |
| `backend/subsurface_api/deep/router.py` | New fields + hmac + capabilities |
| `backend/subsurface_api/deep/jobs.py` | Full params |
| `backend/subsurface_api/main.py` | Configurable CORS |
| `backend/subsurface_api/static/deep_console.html` | v1.3.0 controls |
| `backend/tests/test_null_injection.py`, `test_cpf.py`, `test_tiling.py` | **New** |
| `backend/tests/test_cdse_parsing.py`, `test_deep.py` | Updated |
| `backend/pyproject.toml`, `requirements.txt` | 1.3.0 + scipy |
| `android/app/build.gradle.kts` | versionCode 4 / versionName 1.3.0 |
| `CHANGELOG.md`, `docs/DEEP-PROCESSING.md`, `docs/ROADMAP.md`, `README.md` | Documentation |
| `docs/UPDATE-REPORT-v1.3.0.md` | **New** — this document |

---

<div align="center">

**SubsurfaceExplorer v1.3.0** · MIT (code) · CC BY 4.0 (docs) · 2026-11-07

</div>
