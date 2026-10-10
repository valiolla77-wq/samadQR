#!/usr/bin/env bash
# اسکریپت پوش مستقیم سورس‌کد به گیت‌هاب بدون خطای مرورگر
# استفاده:
# GITHUB_TOKEN="ghp_xxxx" ./push_to_github.sh

set -e

REPO="valiolla77-wq/samadQR"
BRANCH="main"
COMMIT_MSG="${1:-Update Samad Food QR with Messenger and bug fixes}"

if [ -z "$GITHUB_TOKEN" ]; then
    echo "⚠️ لطفاً توکن گیت‌هاب (Personal Access Token) خود را وارد کنید:"
    echo "مثال: GITHUB_TOKEN=\"ghp_xxxx\" ./push_to_github.sh"
    exit 1
fi

echo "🚀 در حال آماده‌سازی و ارسال سورس‌کد به https://github.com/$REPO..."

# پاکسازی گیت قبلی در صورت وجود
rm -rf .git

# راه‌اندازی گیت
git init
git config user.name "valiolla77-wq"
git config user.email "valiolla18@gmail.com"
git branch -M "$BRANCH"

# اضافه کردن سورس‌کد (بر اساس .gitignore)
git add .
git commit -m "$COMMIT_MSG"

# ارسال مستقیم به گیت‌هاب با توکن امن
git remote add origin "https://${GITHUB_TOKEN}@github.com/${REPO}.git"
git push -u origin "$BRANCH" --force

# پاکسازی امن ریموت دارای توکن
git remote remove origin
rm -rf .git

echo "✅ تمام کدهای جدید با موفقیت به گیت‌هاب Push شدند!"
