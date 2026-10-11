#!/usr/bin/env bash
# اسکریپت انتشار خودکار تمامی APKها روی GitHub Release
# پشتیبانی از چند فایل: arm64-v8a, armeabi-v7a, universal

set -e

TAG="${1:-v1.4.1}"
REPO="${2:-valiolla77-wq/samadQR}"

if [ -z "$GITHUB_TOKEN" ]; then
    echo "⚠️ اخطار: متغیر محیطی GITHUB_TOKEN تنظیم نشده است."
    exit 1
fi

echo "🚀 در حال بررسی Release موجود یا ایجاد Release جدید برای $REPO با تگ $TAG..."

# بررسی اینکه آیا ریلیز از قبل وجود دارد یا خیر
EXISTING_RELEASE=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/$REPO/releases/tags/$TAG)

UPLOAD_URL=$(echo "$EXISTING_RELEASE" | grep -o 'https://uploads.github.com/[^"]*' | head -n 1 | sed 's/{?name,label}//')

if [ -z "$UPLOAD_URL" ]; then
    echo "در حال ایجاد Release جدید $TAG..."
    RELEASE_RESPONSE=$(curl -s -X POST \
      -H "Authorization: token $GITHUB_TOKEN" \
      -H "Accept: application/vnd.github.v3+json" \
      https://api.github.com/repos/$REPO/releases \
      -d "{
        \"tag_name\": \"$TAG\",
        \"name\": \"انتشار نسخه جدید سامانه سماد ($TAG)\",
        \"body\": \"## 📱 انتشار نسخه جدید سامانه سماد ($TAG)\n\n### ✨ تغییرات این نسخه:\n- ⚡ **تولید دقیق کیوآر کد منطبق بر استاندارد سلف سماد:** حالت بایت (Byte Mode)، سطح تصحیح خطای بالا (ECC H)، نسخه ۲ و پس‌زمینه سفید خالص جهت اسکن فوق‌سریع در گیت.\n- 🔍 **بزرگ‌نمایی تمام‌صفحه بارکد و QR:** امکان باز کردن کارت بارکد در سایز بزرگ با روشنایی متمرکز برای سلف دانشگاه.\n- 📐 **بهینه‌سازی ابعاد کارت‌ها:** سایزبندی ارگونومیک و فشرده متناسب با انواع صفحه‌نمایش‌های گوشی بدون اسکرول مزاحم.\n- 🔙 **اصلاح کامل ناوبری پیام‌رسان:** دکمه بازگشت مستقیم به میزکار و پشتیبانی از دکمه فیزیکی گوشی (BackHandler).\n- ⚙️ **خروجی چندمعماری (Multi-Architecture):** نسخه‌های مجزا و سبک برای پردازنده‌های arm64-v8a و armeabi-v7a و نسخه جامع universal.\n\n### 📥 راهنمای دانلود:\n- **arm64-v8a:** بهینه برای گوشی‌های مدرن (حجم کمتر)\n- **armeabi-v7a:** مناسب برای گوشی‌های قدیمی ۳۲ بیتی\n- **universal:** نسخه یکپارچه سازگار با تمام دستگاه‌ها\",
        \"draft\": false,
        \"prerelease\": false
      }")
    UPLOAD_URL=$(echo "$RELEASE_RESPONSE" | grep -o 'https://uploads.github.com/[^"]*' | head -n 1 | sed 's/{?name,label}//')
fi

if [ -z "$UPLOAD_URL" ]; then
    echo "❌ خطا در دریافت یا ایجاد لینک آپلود ریلیز."
    echo "$EXISTING_RELEASE"
    exit 1
fi

echo "Upload URL: $UPLOAD_URL"

# جستجوی فایل‌های APK
APKS=$(find app/build/outputs/apk -name "*.apk")

if [ -z "$APKS" ]; then
    echo "❌ هیچ فایل APK در مسیر app/build/outputs/apk یافت نشد!"
    exit 1
fi

echo "📤 در حال آپلود فایل‌های APK تولید شده..."

for apk in $APKS; do
    fname=$(basename "$apk")
    if [[ "$fname" == *"arm64-v8a"* ]]; then
        asset_name="SamadFoodQR-${TAG}-arm64-v8a.apk"
    elif [[ "$fname" == *"armeabi-v7a"* ]]; then
        asset_name="SamadFoodQR-${TAG}-armeabi-v7a.apk"
    elif [[ "$fname" == *"universal"* ]]; then
        asset_name="SamadFoodQR-${TAG}-universal.apk"
    else
        asset_name="SamadFoodQR-${TAG}-${fname}"
    fi

    echo "Uploading $apk as $asset_name..."
    curl -s -X POST \
      -H "Authorization: token $GITHUB_TOKEN" \
      -H "Content-Type: application/vnd.android.package-archive" \
      --data-binary @"$apk" \
      "${UPLOAD_URL}?name=${asset_name}" > /dev/null
    echo "✅ $asset_name آپلود شد."
done

echo "🎉 تمام نسخه‌های APK با موفقیت روی GitHub Release منتشر شدند!"
