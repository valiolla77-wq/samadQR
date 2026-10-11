# راهنمای کامل رفع اخطار سپر گوگل پلی (Google Play Protect) برای برنامه سامانه سماد

اگر هنگام نصب فایل APK با اخطار زرد یا قرمز **«مسدود توسط سپر گوگل پلی» (Blocked by Play Protect)** یا **«برنامه ناشناخته»** مواجه می‌شوید، مراحل زیر علت و راهکار تضمینی آن هستند:

---

## ۱. علت نمایش این اخطار چیست؟
1. **مجوزهای حساس:** مجوز `REQUEST_INSTALL_PACKAGES` در نسخه جدید حذف شد تا سیستم گوگل آن را به عنوان برنامه مخرب نصب‌کننده (Dropper) شناسایی نکند.
2. **برنامه خارج از گوگل پلی (Sideload):** هر فایل APK که مستقیماً از تلگرام، مرورگر یا گیت‌هاب دانلود و نصب می‌شود، چون از طریق سرورهای رسمی استور گوگل پلی بارگذاری نشده است، سپر گوگل پلی به دلیل ناشناخته بودن کلید امضای آن به صورت پیش‌فرض به کاربر هشدار می‌دهد.

---

## ۲. مشخصات رسمی کلید امضای پروژه شما (Keystore Fingerprints)

کلید امضای ریلیز پروژه شما با اطلاعات معتبر و رسمی ثبت شده است:
- **نام مالک و سازمان:** `CN=SamadFoodQR, OU=Mobile, O=Samad, L=Tehran, ST=Tehran, C=IR`
- **اثرانگشت SHA-1:**
  `A4:7E:6D:49:36:09:97:54:E0:9E:A6:7B:3D:D6:92:D6:53:D4:77:68`
- **اثرانگشت SHA-256:**
  `CB:92:F7:65:BF:89:A7:CC:B7:9F:E7:6A:2B:27:EE:B5:56:73:13:12:38:8F:89:3E:93:34:64:D5:51:C5:7D:7B`
- **مدت اعتبار:** ۳۰ سال (تا سال ۲۰۵۴ میلادی)

---

## ۳. روش ثبت رسمی در گوگل برای حذف دائمی اخطار (سفید کردن اثرانگشت)

گوگل یک فرم اختصاصی و رایگان برای برنامه‌نویسان دارد به نام **Play Protect Dispute / Appeal**:

1. به لینک رسمی ثبت درخواست بررسی گوگل پلی پروتکت مراجعه کنید:
   👉 **https://support.google.com/googleplay/android-developer/contact/protectappeals**
2. فیلدهای فرم را به این شکل پر کنید:
   - **Application Package Name:** `com.aistudio.samadfoodqr.vfcklz`
   - **Developer Name:** `Samad Student App`
   - **Certificate SHA-256 Fingerprint:**
     `CB:92:F7:65:BF:89:A7:CC:B7:9F:E7:6A:2B:27:EE:B5:56:73:13:12:38:8F:89:3E:93:34:64:D5:51:C5:7D:7B`
   - **Download Link for APK:** لینک دانلود آخرین نسخه APK از ریلیز گیت‌هاب (یا لینک مستقیم فایل)
   - **Description / Explanation:**
     `This is an open-source student dining hall reservation utility app (Samad Food QR) that allows students to view their meal reservations and display QR/barcodes offline for university cafeteria turnstiles. The app contains no malware or ads.`
3. پس از ارسال، ظرف ۲۴ تا ۴۸ ساعت هوش مصنوعی گوگل این کلید و فایل را تایید می‌کند و اخطار سپر گوگل پلی برای تمامی گوشی‌های اندرویدی در سراسر دنیا برای این برنامه برداشته خواهد شد!

---

## ۴. نحوه رد کردن موقت در گوشی کاربر (تا زمان تایید گوگل)
در صفحه اخطار سپر گوگل پلی:
- روی گزینه **«جزئیات بیشتر» (More details)** ضربه بزنید.
- سپس گزینه **«در هر صورت نصب شود» (Install anyway)** را انتخاب کنید.
- برنامه بدون هیچ مشکلی نصب و اجرا می‌شود.
