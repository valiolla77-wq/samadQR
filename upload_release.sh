#!/usr/bin/env bash
# اسکریپت انتشار خودکار APK روی GitHub Release
# استفاده:
# GITHUB_TOKEN="ghp_xxx" ./upload_release.sh v1.2.0

set -e

TAG="${1:-v1.2.0}"
REPO="${2:-valiolla77-wq/samadQR}"
APK_PATH="release-apk/SamadFoodQR-v1.2.0.apk"

if [ ! -f "$APK_PATH" ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ -z "$GITHUB_TOKEN" ]; then
    echo "⚠️ اخطار: متغیر محیطی GITHUB_TOKEN تنظیم نشده است."
    echo "برای آپلود خودکار با curl، توکن گیت‌هاب را به شکل زیر وارد کنید:"
    echo "GITHUB_TOKEN=\"token_shoma\" ./upload_release.sh $TAG $REPO"
    exit 1
fi

echo "🚀 در حال ایجاد Release برای $REPO با تگ $TAG..."

# ایجاد ریلیز
RELEASE_RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/$REPO/releases \
  -d "{
    \"tag_name\": \"$TAG\",
    \"name\": \"نسخه جدید سامانه سماد ($TAG)\",
    \"body\": \"انتشار رسمی سامانه سماد به همراه بروزرسانی خودکار و انتخاب سلف دانشجو\",
    \"draft\": false,
    \"prerelease\": false
  }")

UPLOAD_URL=$(echo "$RELEASE_RESPONSE" | grep -o 'https://uploads.github.com/[^"]*' | head -n 1 | sed 's/{?name,label}//')

if [ -z "$UPLOAD_URL" ]; then
    echo "❌ خطا در دریافت لینک آپلود ریلیز:"
    echo "$RELEASE_RESPONSE"
    exit 1
fi

echo "📤 در حال آپلود فایل APK ($APK_PATH)..."

curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @"$APK_PATH" \
  "${UPLOAD_URL}?name=SamadFoodQR-${TAG}.apk"

echo "✅ انتشار با موفقیت در گیت‌هاب تکمیل شد!"
